package com.ems.backend.repository;

import com.ems.backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Integer> {

    boolean existsByVoterIdAndElectionId(Integer voterId, Integer electionId);
}
