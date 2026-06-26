package com.ems.backend.controller;

import com.ems.backend.entity.Election;
import com.ems.backend.service.ElectionDisplayState;
import com.ems.backend.service.ElectionResultsService;
import com.ems.backend.service.VoterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class PresidentialResultsController {

    private final ElectionResultsService electionResultsService;
    private final VoterService voterService;

    public PresidentialResultsController(ElectionResultsService electionResultsService, VoterService voterService) {
        this.electionResultsService = electionResultsService;
        this.voterService = voterService;
    }

    @GetMapping("/presidencial")
    public String presidencial(@RequestParam(required = false) String district, Model model) {
        Optional<Election> electionOpt = electionResultsService.findRelevantPresidentialElection();

        if (electionOpt.isEmpty()) {
            model.addAttribute("state", ElectionDisplayState.NONE.name());
            return "results/presidential-results";
        }

        Election election = electionOpt.get();
        ElectionDisplayState state = electionResultsService.resolveState(election);

        model.addAttribute("state", state.name());
        model.addAttribute("election", election);

        if (state == ElectionDisplayState.CLOSED) {
            String districtCode = (district == null || district.isBlank()) ? null : district;
            model.addAttribute("results", electionResultsService.getResults(election.getId(), districtCode));

            // Mismo cascade de ubigeos (departamento/provincia/distrito) que usa /participation,
            // para mantener el filtro consistente y escalable entre ambas pantallas.
            List<Map<String, Object>> ubicacionesList = voterService.getAllUbigeos();
            String locationsJson = "[]";
            try {
                locationsJson = new ObjectMapper().writeValueAsString(ubicacionesList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            model.addAttribute("locationsJson", locationsJson);
        }

        return "results/presidential-results";
    }
}
