package com.ems.backend.controller;

import com.ems.backend.repository.VotingLocationRepository;
import com.ems.backend.repository.VotingTableRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class VotingAssignmentAdminController {

    private final VotingLocationRepository votingLocationRepository;
    private final VotingTableRepository votingTableRepository;

    public VotingAssignmentAdminController(VotingLocationRepository votingLocationRepository,
                                           VotingTableRepository votingTableRepository) {
        this.votingLocationRepository = votingLocationRepository;
        this.votingTableRepository = votingTableRepository;
    }

    @GetMapping("/voting-locations")
    public List<Map<String, Object>> getVotingLocations(@RequestParam String locationCode) {
        return votingLocationRepository.findByLocationCodeAndIsActiveTrue(locationCode)
                .stream()
                .map(vl -> Map.<String, Object>of("id", vl.getId(), "name", vl.getName()))
                .toList();
    }

    @GetMapping("/voting-tables")
    public List<Map<String, Object>> getVotingTables(@RequestParam Integer locationId) {
        return votingTableRepository.findByVotingLocationId(locationId)
                .stream()
                .map(vt -> Map.<String, Object>of("id", vt.getId(), "tableNumber", vt.getTableNumber()))
                .toList();
    }
}
