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
<<<<<<< HEAD
        // Obtener datos de participación por distrito (Lima Metropolitana)
        List<Map<String, Object>> distritosList = voterService.getParticipationByDistrict();

        // Calcular totales consolidados de Lima a partir de los distritos
        long electoresHabilitados = 0;
        long asistentes = 0;
        for (Map<String, Object> dist : distritosList) {
            electoresHabilitados += (Long) dist.get("total");
            asistentes += (Long) dist.get("attended");
        }
=======
        // Authenticated voters go to their dedicated portal
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/voter/portal";
        }

        long electoresHabilitados = voterService.getElectoresHabilitados();
        long asistentes = voterService.getVotantesAsistentes();
>>>>>>> 33c660d017241e9a8075c40455dd1bd91b2e6897
        long ausentes = electoresHabilitados - asistentes;

        double porcentajeAsistencia = electoresHabilitados > 0 ? (asistentes * 100.0) / electoresHabilitados : 0.0;
        double porcentajeAusencia = electoresHabilitados > 0 ? (ausentes * 100.0) / electoresHabilitados : 0.0;

<<<<<<< HEAD
        // Barra de progreso de actas — valor independiente de la participación
        long totalActas = 92766;
        long actasContabilizadas = 0;
        double porcentajeActas = 0.0;
=======
        long totalActas = 92766;
        long actasContabilizadas = (long) (totalActas * (porcentajeAsistencia / 100.0));
        double porcentajeActas = porcentajeAsistencia;
>>>>>>> 33c660d017241e9a8075c40455dd1bd91b2e6897

        model.addAttribute("electoresHabilitados", electoresHabilitados);
        model.addAttribute("asistentes", asistentes);
        model.addAttribute("ausentes", ausentes);
        model.addAttribute("porcentajeAsistencia", porcentajeAsistencia);
        model.addAttribute("porcentajeAusencia", porcentajeAusencia);

        model.addAttribute("totalActas", totalActas);
        model.addAttribute("actasContabilizadas", actasContabilizadas);
        model.addAttribute("porcentajeActas", porcentajeActas);

        List<Map<String, Object>> ambitosList = voterService.getParticipationByScope();
        String ambitosJson = "[]";
        try {
            ambitosJson = new ObjectMapper().writeValueAsString(ambitosList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("ambitosJson", ambitosJson);

        // Serializar distritos a JSON para Alpine.js
        String distritosJson = "[]";
        try {
            distritosJson = new ObjectMapper().writeValueAsString(distritosList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("distritosJson", distritosJson);

        // ========== NUEVO: DATOS DE UBIGEO PARA LOS FILTROS ==========
        // Obtener todos los ubigeos (departamentos, provincias, distritos)
        List<Map<String, Object>> ubicacionesList = voterService.getAllUbigeos(); // Necesitas crear este método
        String locationsJson = "[]";
        try {
            locationsJson = new ObjectMapper().writeValueAsString(ubicacionesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("locationsJson", locationsJson);
        // ============================================================

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
