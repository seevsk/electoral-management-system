package com.ems.backend.service.report;

import java.math.BigDecimal;

public record DistrictParticipationRow(
        String ubigeo,
        String district,
        long totalElectors,
        long votesEmitted,
        long votesPending,
        BigDecimal participationPercentage
) {
}
