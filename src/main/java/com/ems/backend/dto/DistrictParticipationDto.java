package com.ems.backend.dto;

public record DistrictParticipationDto(
        String locationCode,
        String department,
        String province,
        String district,
        long registeredVoters,
        long votesCast,
        long pendingVotes,
        double participationPercentage
) {
}
