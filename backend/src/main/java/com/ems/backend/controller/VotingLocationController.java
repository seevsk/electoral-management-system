package com.ems.backend.controller;

import com.ems.backend.dto.VoterVotingLocationResponse;
import com.ems.backend.entity.VoterAssignment;
import com.ems.backend.entity.VotingLocation;
import com.ems.backend.entity.VotingTable;
import com.ems.backend.repository.VoterAssignmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VotingLocationController {

    private final VoterAssignmentRepository assignmentRepository;

    public VotingLocationController(VoterAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/voting-location")
    public String votingLocation(@RequestParam(required = false) String dni, Model model) {
        if (dni != null && !dni.isBlank()) {
            String cleanDni = dni.trim();
            model.addAttribute("dni", cleanDni);

            Optional<VoterAssignment> opt = assignmentRepository.findWithDetailsByDni(cleanDni);
            if (opt.isEmpty()) {
                model.addAttribute("lookupError",
                        "No se encontro un local de votacion para el DNI ingresado. " +
                        "Verifica que el DNI sea correcto o contacta a la ONPE.");
            } else {
                VoterAssignment va = opt.get();
                VotingTable table = va.getVotingTable();
                VotingLocation location = table.getVotingLocation();

                String displayName = va.getVoter().getFirstSurname() + " "
                        + va.getVoter().getSecondSurname() + " "
                        + va.getVoter().getFullName();

                model.addAttribute("result", new VoterVotingLocationResponse(
                        displayName,
                        table.getTableNumber(),
                        location.getName(),
                        location.getAddress(),
                        location.getLocation().getDistrict()
                ));
            }
        }
        return "voters/voting-location";
    }
}
