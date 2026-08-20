package com.ems.backend.repository;

import com.ems.backend.entity.VotingLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingLocationRepository extends JpaRepository<VotingLocation, Integer> {

    List<VotingLocation> findByLocationCodeAndIsActiveTrue(String locationCode);
}
