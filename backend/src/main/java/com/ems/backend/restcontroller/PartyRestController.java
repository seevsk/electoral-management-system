package com.ems.backend.restcontroller;

import com.ems.backend.dto.PublicPartyDto;
import com.ems.backend.entity.Party;
import com.ems.backend.service.PartyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parties")
public class PartyRestController {

    private final PartyService partyService;

    public PartyRestController(PartyService partyService) {
        this.partyService = partyService;
    }

    @GetMapping
    public List<PublicPartyDto> findAll() {
        return partyService.findAllActive().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicPartyDto> findById(@PathVariable Integer id) {
        return partyService.findAllActive().stream()
                .filter(party -> party.getId().equals(id))
                .findFirst()
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PublicPartyDto toDto(Party party) {
        return new PublicPartyDto(
                party.getId(),
                party.getName(),
                party.getAcronym(),
                party.getRepresentative(),
                party.getLogoUrl(),
                party.getListPosition()
        );
    }
}
