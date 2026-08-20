package com.ems.backend.repository;

import com.ems.backend.entity.Candidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    @EntityGraph(attributePaths = {"voter", "voter.account", "party", "election"})
    Optional<Candidate> findById(Integer id);

    @EntityGraph(attributePaths = {"voter", "voter.account", "party", "election"})
    List<Candidate> findAllByOrderByElection_YearDescParty_ListPositionAscIdAsc();

    @EntityGraph(attributePaths = {"voter", "voter.account", "party", "election"})
    List<Candidate> findByIsActiveTrueOrderByElection_YearDescParty_ListPositionAscIdAsc();

    boolean existsByVoterIdAndElectionId(Integer voterId, Integer electionId);

    boolean existsByVoterIdAndElectionIdAndIdNot(Integer voterId, Integer electionId, Integer id);

    boolean existsByListNumberAndPartyIdAndElectionId(Integer listNumber, Integer partyId, Integer electionId);

    boolean existsByListNumberAndPartyIdAndElectionIdAndIdNot(Integer listNumber, Integer partyId, Integer electionId, Integer id);
}
