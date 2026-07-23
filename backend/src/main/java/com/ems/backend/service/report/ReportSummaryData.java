package com.ems.backend.service.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportSummaryData(
        long totalElectors,
        long totalVotesEmitted,
        long totalVotesPending,
        BigDecimal participationPercentage,
        long totalVotesRegistered,
        long totalBlankVotes,
        LocalDateTime generatedAt
) {
}
