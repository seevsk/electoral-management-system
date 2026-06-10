package com.ems.backend.controller;

import com.ems.backend.service.VoterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class NavigationController
{
    private final VoterService voterService;

    // Inyección del servicio de votantes para acceder a estadísticas reales
    public NavigationController(VoterService voterService) {
        this.voterService = voterService;
    }

    @GetMapping("/")
    public String root()
    {
        return "redirect:/participation";
    }

    @GetMapping("/participation")
    public String participation(Model model)
    {
        // Obtención de datos dinámicos directos de la base de datos
        long electoresHabilitados = voterService.getElectoresHabilitados();
        long asistentes = voterService.getVotantesAsistentes();
        long ausentes = electoresHabilitados - asistentes;

        double porcentajeAsistencia = electoresHabilitados > 0 ? (asistentes * 100.0) / electoresHabilitados : 0.0;
        double porcentajeAusencia = electoresHabilitados > 0 ? (ausentes * 100.0) / electoresHabilitados : 0.0;

        // Simulación de actas en proporción al avance de participación
        long totalActas = 92766; // Total estático del padrón nacional real
        long actasContabilizadas = (long) (totalActas * (porcentajeAsistencia / 100.0));
        double porcentajeActas = porcentajeAsistencia;

        // Añadir atributos de forma directa al modelo de Thymeleaf
        model.addAttribute("electoresHabilitados", electoresHabilitados);
        model.addAttribute("asistentes", asistentes);
        model.addAttribute("ausentes", ausentes);
        model.addAttribute("porcentajeAsistencia", porcentajeAsistencia);
        model.addAttribute("porcentajeAusencia", porcentajeAusencia);

        model.addAttribute("totalActas", totalActas);
        model.addAttribute("actasContabilizadas", actasContabilizadas);
        model.addAttribute("porcentajeActas", porcentajeActas);

        // Obtener ámbitos (departamentos) y serializarlos a JSON para Alpine.js
        List<Map<String, Object>> ambitosList = voterService.getParticipationByScope();
        String ambitosJson = "[]";
        try {
            ambitosJson = new ObjectMapper().writeValueAsString(ambitosList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("ambitosJson", ambitosJson);

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
