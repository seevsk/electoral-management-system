package com.ems.backend.service;

import com.ems.backend.dto.BallotEntryDto;
import com.ems.backend.entity.Election;

import java.util.List;
import java.util.Optional;

public interface VotingService {

    Optional<Election> findActiveElectionInWindow();

    List<BallotEntryDto> getBallotEntries(Integer electionId);

    Optional<BallotEntryDto> getBallotEntry(Integer electionId, Integer candidateId);

    // candidateId null = blank vote
    void castVote(String voterDni, Integer electionId, Integer candidateId);
}
