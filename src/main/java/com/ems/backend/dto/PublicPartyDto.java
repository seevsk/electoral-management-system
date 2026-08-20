package com.ems.backend.dto;

public record PublicPartyDto(
        Integer id,
        String name,
        String acronym,
        String representative,
        String logoUrl,
        Integer listPosition
) {
}
