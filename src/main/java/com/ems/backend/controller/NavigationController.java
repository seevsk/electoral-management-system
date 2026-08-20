package com.ems.backend.controller;

import com.ems.backend.service.VoterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
public class NavigationController
{
    private final VoterService voterService;

    public NavigationController(VoterService voterService) {
        this.voterService = voterService;
    }

    @GetMapping("/")
    public String root()
    {
        return "redirect:/participation";
    }

    @GetMapping("/participation")
    public String participation(Authentication authentication, Model model)
    {
        // Authenticated voters go to their dedicated portal
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/voter/portal";
        }

        // Totales nacionales calculados desde todos los departamentos
        List<Map<String, Object>> scopeList = voterService.getParticipationByScope();
        long totalPadron = 0, votosEmitidos = 0, votosPendientes = 0;
        for (Map<String, Object> scope : scopeList) {
            totalPadron     += ((Number) scope.get("total")).longValue();
            votosEmitidos   += ((Number) scope.get("attended")).longValue();
            votosPendientes += ((Number) scope.get("pending")).longValue();
        }

        double porcentajeVotos      = totalPadron > 0 ? (votosEmitidos   * 100.0) / totalPadron : 0.0;
        double porcentajePendientes = totalPadron > 0 ? (votosPendientes * 100.0) / totalPadron : 0.0;

        model.addAttribute("totalPadron",        totalPadron);
        model.addAttribute("votosEmitidos",      votosEmitidos);
        model.addAttribute("votosPendientes",    votosPendientes);
        model.addAttribute("porcentajeVotos",    porcentajeVotos);
        model.addAttribute("porcentajePendientes", porcentajePendientes);

        List<Map<String, Object>> districtsList = voterService.getParticipationByDistrict();
        String districtsJson = "[]";
        try {
            districtsJson = new ObjectMapper().writeValueAsString(districtsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("districtsJson", districtsJson);

        // Ubigeos para los filtros en cascada
        List<Map<String, Object>> ubicacionesList = voterService.getAllUbigeos();
        String locationsJson = "[]";
        try {
            locationsJson = new ObjectMapper().writeValueAsString(ubicacionesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("locationsJson", locationsJson);

        return "index";
    }

    @GetMapping(value = "/data/lima-distritos.geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> limaDistritosGeoJson() throws Exception
    {
        var resource = new org.springframework.core.io.ClassPathResource("data/lima_callao_distritos_simple.geojson");
        String body = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/login/admin")
    public String adminLogin()
    {
        return "auth/login-admin";
    }

    @GetMapping("/login/voter")
    public String voterLogin()
    {
        return "auth/login-voter";
    }

    @GetMapping("/login/operator")
    public String operatorLogin()
    {
        return "auth/login-operator";
    }

    @GetMapping("/admin/main")
    public String adminMain()
    {
        return "admin/main";
    }
}
