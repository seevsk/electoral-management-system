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

        long electoresHabilitados = voterService.getElectoresHabilitados();
        long asistentes = voterService.getVotantesAsistentes();
        long ausentes = electoresHabilitados - asistentes;

        double porcentajeAsistencia = electoresHabilitados > 0 ? (asistentes * 100.0) / electoresHabilitados : 0.0;
        double porcentajeAusencia = electoresHabilitados > 0 ? (ausentes * 100.0) / electoresHabilitados : 0.0;

        long totalActas = 92766;
        long actasContabilizadas = (long) (totalActas * (porcentajeAsistencia / 100.0));
        double porcentajeActas = porcentajeAsistencia;

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
