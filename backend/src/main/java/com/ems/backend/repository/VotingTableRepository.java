package com.ems.backend.repository;

import com.ems.backend.entity.VotingTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingTableRepository extends JpaRepository<VotingTable, Integer> {

    List<VotingTable> findByVotingLocationId(Integer votingLocationId);
}
