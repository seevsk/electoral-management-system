package com.ems.backend.service.report;

public record VotingLocationRow(
        String locationName,
        String address,
        String ubigeo,
        String district,
        long tableCount,
        long assignedVotersCount
) {
}
