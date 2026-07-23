package com.ems.backend.service.report;

public record VoterAssignmentRow(
        String dni,
        String voterName,
        String locationName,
        String tableNumber,
        String ubigeo,
        String district
) {
}
