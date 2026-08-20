package com.ems.backend.repository;

import com.ems.backend.entity.VotingTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotingTableRepository extends JpaRepository<VotingTable, Integer> {

    List<VotingTable> findByVotingLocationId(Integer votingLocationId);

    @Query("SELECT vt FROM VotingTable vt JOIN FETCH vt.votingLocation WHERE vt.id = :id")
    Optional<VotingTable> findByIdWithLocation(@Param("id") Integer id);
}
