package com.ems.backend.dto;

public record PublicCandidateDto(
        Integer id,
        String fullName,
        String photoUrl,
        Integer listNumber,
        PartySummary party,
        ElectionSummary election
) {
    public record PartySummary(
            Integer id,
            String name,
            String acronym,
            String logoUrl,
            Integer listPosition
    ) {
    }

    public record ElectionSummary(
            Integer id,
            String name,
            Integer year,
            String status
    ) {
    }
}
