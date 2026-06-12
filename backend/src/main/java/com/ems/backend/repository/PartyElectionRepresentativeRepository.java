package com.ems.backend.repository;

import com.ems.backend.entity.PartyElectionRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyElectionRepresentativeRepository extends JpaRepository<PartyElectionRepresentative, Integer> {

    Optional<PartyElectionRepresentative> findByPartyIdAndElectionId(Integer partyId, Integer electionId);

    Optional<PartyElectionRepresentative> findByCandidateId(Integer candidateId);

    boolean existsByCandidateId(Integer candidateId);

    @Query("""
            select per from PartyElectionRepresentative per
            join fetch per.party p
            join fetch per.candidate c
            join fetch c.voter v
            where per.election.id = :electionId
              and c.isActive = true
              and p.isActive = true
            order by p.listPosition asc
            """)
    List<PartyElectionRepresentative> findActiveBallotEntriesByElectionId(@Param("electionId") Integer electionId);

    @Query("""
            select per from PartyElectionRepresentative per
            join fetch per.party p
            join fetch per.candidate c
            join fetch c.voter v
            where per.election.id = :electionId
              and c.id = :candidateId
              and c.isActive = true
              and p.isActive = true
            """)
    Optional<PartyElectionRepresentative> findActiveBallotEntryByElectionAndCandidate(
            @Param("electionId") Integer electionId,
            @Param("candidateId") Integer candidateId);
}
