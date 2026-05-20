package com.ems.backend.repository;

import com.ems.backend.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Integer> {

    List<Party> findAllByOrderByListPositionAsc();

    List<Party> findByIsActiveTrueOrderByListPositionAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByAcronymIgnoreCase(String acronym);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    boolean existsByAcronymIgnoreCaseAndIdNot(String acronym, Integer id);

    @Query("SELECT COALESCE(MAX(p.listPosition), 0) FROM Party p")
    Integer findMaxListPosition();
}
