package com.ems.backend.repository;

import com.ems.backend.entity.VoterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoterAssignmentRepository extends JpaRepository<VoterAssignment, Integer> {

    Optional<VoterAssignment> findByVoter_Id(Integer voterId);

    @Query("SELECT va FROM VoterAssignment va " +
           "JOIN FETCH va.voter v " +
           "JOIN FETCH v.account a " +
           "JOIN FETCH va.votingTable vt " +
           "JOIN FETCH vt.votingLocation vl " +
           "JOIN FETCH vl.location l " +
           "WHERE a.dni = :dni")
    Optional<VoterAssignment> findWithDetailsByDni(@Param("dni") String dni);
}
