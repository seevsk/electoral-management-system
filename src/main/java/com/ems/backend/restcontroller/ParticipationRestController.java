package com.ems.backend.restcontroller;

import com.ems.backend.dto.DistrictParticipationDto;
import com.ems.backend.dto.ParticipationSummaryDto;
import com.ems.backend.service.VoterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/participation")
public class ParticipationRestController {

    private final VoterService voterService;

    public ParticipationRestController(VoterService voterService) {
        this.voterService = voterService;
    }

    @GetMapping
    public ParticipationSummaryDto getSummary() {
        List<Map<String, Object>> scopes = voterService.getParticipationByScope();
        long registeredVoters = scopes.stream().mapToLong(scope -> number(scope.get("total"))).sum();
        long votesCast = scopes.stream().mapToLong(scope -> number(scope.get("attended"))).sum();

        return summary(registeredVoters, votesCast);
    }

    @GetMapping("/districts")
    public List<DistrictParticipationDto> getDistricts() {
        return voterService.getAllUbigeos().stream()
                .map(this::toDistrictDto)
                .toList();
    }

    private DistrictParticipationDto toDistrictDto(Map<String, Object> district) {
        long registeredVoters = number(district.get("total"));
        long votesCast = number(district.get("attended"));
        ParticipationSummaryDto summary = summary(registeredVoters, votesCast);

        return new DistrictParticipationDto(
                text(district.get("locationCode")),
                text(district.get("department")),
                text(district.get("province")),
                text(district.get("district")),
                summary.registeredVoters(),
                summary.votesCast(),
                summary.pendingVotes(),
                summary.participationPercentage()
        );
    }

    private ParticipationSummaryDto summary(long registeredVoters, long votesCast) {
        long pendingVotes = Math.max(registeredVoters - votesCast, 0);
        double percentage = registeredVoters == 0
                ? 0.0
                : votesCast * 100.0 / registeredVoters;

        return new ParticipationSummaryDto(
                registeredVoters,
                votesCast,
                pendingVotes,
                percentage
        );
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private String text(Object value) {
        return value == null ? null : value.toString().trim();
    }
}
