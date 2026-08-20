package com.ems.backend.dto;

public record ParticipationSummaryDto(
        long registeredVoters,
        long votesCast,
        long pendingVotes,
        double participationPercentage
) {
}
