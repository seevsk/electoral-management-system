package com.ems.backend.restcontroller;

import com.ems.backend.dto.PublicCandidateDto;
import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import com.ems.backend.entity.Voter;
import com.ems.backend.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateRestControllerTest {

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private CandidateRestController controller;

    @Test
    void mapsCandidateWithoutExposingVoterData() {
        Candidate candidate = candidate();
        when(candidateService.findAllActive()).thenReturn(List.of(candidate));

        PublicCandidateDto response = controller.findAll().getFirst();

        assertThat(response.fullName()).isEqualTo("Ana Torres Ruiz");
        assertThat(response.party().acronym()).isEqualTo("PD");
        assertThat(response.election().name()).isEqualTo("Elecciones 2026");
        assertThat(response.getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("voter", "account", "dni", "hasVoted", "passwordHash");
    }

    @Test
    void returnsNotFoundWhenCandidateIsNotActiveOrDoesNotExist() {
        when(candidateService.findAllActive()).thenReturn(List.of());

        assertThat(controller.findById(99).getStatusCode().value()).isEqualTo(404);
    }

    private Candidate candidate() {
        Voter voter = new Voter();
        voter.setFullName("Ana");
        voter.setFirstSurname("Torres");
        voter.setSecondSurname("Ruiz");

        Party party = new Party();
        party.setId(2);
        party.setName("Partido Demo");
        party.setAcronym("PD");
        party.setLogoUrl("/logo.png");
        party.setListPosition(3);

        Election election = new Election();
        election.setId(4);
        election.setName("Elecciones 2026");
        election.setYear(2026);
        election.setStatus("A");

        Candidate candidate = new Candidate();
        candidate.setId(5);
        candidate.setVoter(voter);
        candidate.setParty(party);
        candidate.setElection(election);
        candidate.setPhotoUrl("/candidate.png");
        candidate.setListNumber(7);
        candidate.setIsActive(true);
        return candidate;
    }
}
