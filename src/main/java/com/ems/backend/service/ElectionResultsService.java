package com.ems.backend.service;

import com.ems.backend.dto.PresidentialResultsDto;
import com.ems.backend.entity.Election;

import java.util.Optional;

public interface ElectionResultsService {

    Optional<Election> findRelevantPresidentialElection();

    ElectionDisplayState resolveState(Election election);

    PresidentialResultsDto getResults(Integer electionId, String districtCode);
}
