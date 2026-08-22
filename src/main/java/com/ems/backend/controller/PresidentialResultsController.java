package com.ems.backend.controller;

import com.ems.backend.dto.PresidentialResultsDto;
import com.ems.backend.entity.Election;
import com.ems.backend.service.ElectionDisplayState;
import com.ems.backend.service.ElectionResultsService;
import com.ems.backend.service.VoterService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String presidencial(Model model) {
        model.addAttribute("pageTitle", "Resultados | Lima Metropolitana");

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
            PresidentialResultsDto results = electionResultsService.getResults(election.getId(), null);
            model.addAttribute("results", results);

            List<Map<String, Object>> ubicacionesList = voterService.getAllUbigeos();
            model.addAttribute("ubicacionesList", ubicacionesList);
        }

        return "results/presidential-results";
    }

    @GetMapping("/api/presidencial/results")
    @ResponseBody
    public ResponseEntity<PresidentialResultsDto> apiResults(@RequestParam(required = false) String district) {
        Optional<Election> electionOpt = electionResultsService.findRelevantPresidentialElection();

        if (electionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Election election = electionOpt.get();
        ElectionDisplayState state = electionResultsService.resolveState(election);

        if (state != ElectionDisplayState.CLOSED) {
            return ResponseEntity.notFound().build();
        }

        String districtCode = (district == null || district.isBlank()) ? null : district;
        PresidentialResultsDto results = electionResultsService.getResults(election.getId(), districtCode);
        return ResponseEntity.ok(results);
    }
}
