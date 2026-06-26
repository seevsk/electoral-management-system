package com.ems.backend.repository;

import com.ems.backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Integer> {

    boolean existsByVoterIdAndElectionId(Integer voterId, Integer electionId);

    @Query("""
            select c.id, count(v)
            from Vote v
            join v.candidate c
            where v.election.id = :electionId
              and (:districtCode is null or v.voter.locationCode = :districtCode)
            group by c.id
            """)
    List<Object[]> countVotesByCandidate(@Param("electionId") Integer electionId,
                                          @Param("districtCode") String districtCode);

    @Query("""
            select count(v)
            from Vote v
            where v.election.id = :electionId
              and v.candidate is null
              and (:districtCode is null or v.voter.locationCode = :districtCode)
            """)
    long countBlankVotes(@Param("electionId") Integer electionId,
                          @Param("districtCode") String districtCode);

    @Query("""
            select count(v)
            from Vote v
            where v.election.id = :electionId
              and (:districtCode is null or v.voter.locationCode = :districtCode)
            """)
    long countTotalVotes(@Param("electionId") Integer electionId,
                          @Param("districtCode") String districtCode);
}
