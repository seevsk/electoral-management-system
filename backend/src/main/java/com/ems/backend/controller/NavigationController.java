package com.ems.backend.controller;

import com.ems.backend.service.VoterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        List<Map<String, Object>> ambitosList = voterService.getParticipationByScope();
        long totalPadron = 0, votosEmitidos = 0, cuentasActivasSinVoto = 0, cuentasInactivas = 0;
        for (Map<String, Object> scope : ambitosList) {
            totalPadron           += (Long) scope.get("total");
            votosEmitidos         += (Long) scope.get("attended");
            cuentasActivasSinVoto += (Long) scope.get("pending");
            cuentasInactivas      += (Long) scope.get("inactive");
        }

        double porcentajeVotos          = totalPadron > 0 ? (votosEmitidos         * 100.0) / totalPadron : 0.0;
        double porcentajeActivosSinVoto = totalPadron > 0 ? (cuentasActivasSinVoto * 100.0) / totalPadron : 0.0;
        double porcentajeInactivas      = totalPadron > 0 ? (cuentasInactivas      * 100.0) / totalPadron : 0.0;

        model.addAttribute("totalPadron",              totalPadron);
        model.addAttribute("votosEmitidos",            votosEmitidos);
        model.addAttribute("cuentasActivasSinVoto",    cuentasActivasSinVoto);
        model.addAttribute("cuentasInactivas",         cuentasInactivas);
        model.addAttribute("porcentajeVotos",          porcentajeVotos);
        model.addAttribute("porcentajeActivosSinVoto", porcentajeActivosSinVoto);
        model.addAttribute("porcentajeInactivas",      porcentajeInactivas);

        String ambitosJson = "[]";
        try {
            ambitosJson = new ObjectMapper().writeValueAsString(ambitosList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("ambitosJson", ambitosJson);

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

    @GetMapping("/admin/main")
    public String adminMain()
    {
        return "admin/main";
    }
}
