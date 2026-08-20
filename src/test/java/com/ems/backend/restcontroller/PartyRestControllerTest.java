package com.ems.backend.restcontroller;

import com.ems.backend.dto.PublicPartyDto;
import com.ems.backend.entity.Party;
import com.ems.backend.service.PartyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyRestControllerTest {

    @Mock
    private PartyService partyService;

    @InjectMocks
    private PartyRestController controller;

    @Test
    void returnsOnlyPublicFieldsForActiveParties() {
        Party party = party(1, "Partido Demo");
        when(partyService.findAllActive()).thenReturn(List.of(party));

        List<PublicPartyDto> response = controller.findAll();

        assertThat(response).containsExactly(new PublicPartyDto(
                1, "Partido Demo", "PD", "Representante", "/logo.png", 1
        ));
    }

    @Test
    void returnsNotFoundWhenPartyIsNotActiveOrDoesNotExist() {
        when(partyService.findAllActive()).thenReturn(List.of());

        assertThat(controller.findById(99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Party party(Integer id, String name) {
        Party party = new Party();
        party.setId(id);
        party.setName(name);
        party.setAcronym("PD");
        party.setRepresentative("Representante");
        party.setLogoUrl("/logo.png");
        party.setListPosition(1);
        party.setIsActive(true);
        return party;
    }
}
