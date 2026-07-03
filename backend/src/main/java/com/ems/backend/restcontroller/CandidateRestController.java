package com.ems.backend.restcontroller;

import com.ems.backend.dto.PublicCandidateDto;
import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import com.ems.backend.entity.Voter;
import com.ems.backend.service.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateRestController {

    private final CandidateService candidateService;

    public CandidateRestController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public List<PublicCandidateDto> findAll() {
        return candidateService.findAllActive().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicCandidateDto> findById(@PathVariable Integer id) {
        return candidateService.findAllActive().stream()
                .filter(candidate -> candidate.getId().equals(id))
                .findFirst()
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PublicCandidateDto toDto(Candidate candidate) {
        Voter voter = candidate.getVoter();
        Party party = candidate.getParty();
        Election election = candidate.getElection();

        String fullName = String.join(" ",
                voter.getFullName(),
                voter.getFirstSurname(),
                voter.getSecondSurname()
        ).trim();

        return new PublicCandidateDto(
                candidate.getId(),
                fullName,
                candidate.getPhotoUrl(),
                candidate.getListNumber(),
                new PublicCandidateDto.PartySummary(
                        party.getId(),
                        party.getName(),
                        party.getAcronym(),
                        party.getLogoUrl(),
                        party.getListPosition()
                ),
                new PublicCandidateDto.ElectionSummary(
                        election.getId(),
                        election.getName(),
                        election.getYear(),
                        election.getStatus()
                )
        );
    }
}
