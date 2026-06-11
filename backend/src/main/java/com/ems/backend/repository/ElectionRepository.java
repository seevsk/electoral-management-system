package com.ems.backend.repository;

import com.ems.backend.entity.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;



public interface ElectionRepository extends JpaRepository<Election, Integer> {

    @Query("""
            select e
            from Election e
            order by case
                when e.status = 'A' then 1
                when e.status = 'P' then 2
                when e.status = 'C' then 3
                else 4
            end,
            e.year desc,
            e.id desc
            """)
    List<Election> findAllByPriorityOrderByYearDesc();

    @Query("""
            select e
            from Election e
            where e.electionType = 'PRESIDENCIAL'
              and e.status in ('A', 'P')
            order by case
                when e.status = 'A' then 1
                when e.status = 'P' then 2
                else 3
            end,
            e.year desc,
            e.id desc
            """)
    List<Election> findAvailablePresidentialElectionsForCandidates();

    @Query("""
            select e
            from Election e
            where e.status in ('A', 'P')
            order by case
                when e.status = 'A' then 1
                when e.status = 'P' then 2
                else 3
            end,
            e.year desc,
            e.id desc
            """)
    List<Election> findAvailableElectionsForCandidates();

    @Query("""
            select e
            from Election e
            where e.electionType = 'PRESIDENCIAL'
            order by case
                when e.status = 'A' then 1
                when e.status = 'P' then 2
                when e.status = 'C' then 3
                else 4
            end,
            e.year desc,
            e.id desc
            """)
    Optional<Election> findBestPresidentialElection();

    @Query("select e from Election e where e.status = 'A'")
    Optional<Election> findActiveElection();

    @Query("select count(e) > 0 from Election e where e.status = 'A'")
    boolean existsActiveElection();
}
