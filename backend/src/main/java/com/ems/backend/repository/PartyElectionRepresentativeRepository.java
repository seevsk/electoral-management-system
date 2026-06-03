package com.ems.backend.repository;

import com.ems.backend.entity.PartyElectionRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyElectionRepresentativeRepository extends JpaRepository<PartyElectionRepresentative, Integer> {

    Optional<PartyElectionRepresentative> findByPartyIdAndElectionId(Integer partyId, Integer electionId);

    Optional<PartyElectionRepresentative> findByCandidateId(Integer candidateId);

    boolean existsByCandidateId(Integer candidateId);
}
