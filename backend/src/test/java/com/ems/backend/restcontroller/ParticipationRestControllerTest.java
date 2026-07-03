package com.ems.backend.restcontroller;

import com.ems.backend.dto.DistrictParticipationDto;
import com.ems.backend.dto.ParticipationSummaryDto;
import com.ems.backend.service.VoterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationRestControllerTest {

    @Mock
    private VoterService voterService;

    @InjectMocks
    private ParticipationRestController controller;

    @Test
    void aggregatesNationalParticipationWithoutIndividualVoterData() {
        when(voterService.getParticipationByScope()).thenReturn(List.of(
                Map.of("total", 100L, "attended", 60L),
                Map.of("total", 50L, "attended", 30L)
        ));

        ParticipationSummaryDto response = controller.getSummary();

        assertThat(response.registeredVoters()).isEqualTo(150);
        assertThat(response.votesCast()).isEqualTo(90);
        assertThat(response.pendingVotes()).isEqualTo(60);
        assertThat(response.participationPercentage()).isEqualTo(60.0);
    }

    @Test
    void returnsParticipationByDistrict() {
        when(voterService.getAllUbigeos()).thenReturn(List.of(Map.of(
                "locationCode", "150101",
                "department", "LIMA",
                "province", "LIMA",
                "district", "LIMA",
                "total", 80L,
                "attended", 20L
        )));

        DistrictParticipationDto response = controller.getDistricts().getFirst();

        assertThat(response.locationCode()).isEqualTo("150101");
        assertThat(response.registeredVoters()).isEqualTo(80);
        assertThat(response.votesCast()).isEqualTo(20);
        assertThat(response.pendingVotes()).isEqualTo(60);
        assertThat(response.participationPercentage()).isEqualTo(25.0);
    }

    @Test
    void handlesEmptyParticipationData() {
        when(voterService.getParticipationByScope()).thenReturn(List.of());

        assertThat(controller.getSummary()).isEqualTo(
                new ParticipationSummaryDto(0, 0, 0, 0.0)
        );
    }
}
