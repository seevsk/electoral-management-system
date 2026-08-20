package com.ems.backend.service.report;

import java.math.BigDecimal;

public record PresidentialResultRow(
        String electionName,
        String candidateName,
        String partyName,
        long votes,
        BigDecimal percentage,
        boolean blankVote
) {
}
