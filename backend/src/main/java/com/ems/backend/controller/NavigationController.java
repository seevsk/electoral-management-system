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
        // Obtener datos de participación por distrito (Lima Metropolitana)
        List<Map<String, Object>> distritosList = voterService.getParticipationByDistrict();

        // Calcular totales consolidados de Lima a partir de los distritos
        long electoresHabilitados = 0;
        long asistentes = 0;
        for (Map<String, Object> dist : distritosList) {
            electoresHabilitados += (Long) dist.get("total");
            asistentes += (Long) dist.get("attended");
        }
        long ausentes = electoresHabilitados - asistentes;

        double porcentajeAsistencia = electoresHabilitados > 0 ? (asistentes * 100.0) / electoresHabilitados : 0.0;
        double porcentajeAusencia = electoresHabilitados > 0 ? (ausentes * 100.0) / electoresHabilitados : 0.0;

        // Barra de progreso de actas — valor independiente de la participación
        long totalActas = 92766;
        long actasContabilizadas = 0;
        double porcentajeActas = 0.0;

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
