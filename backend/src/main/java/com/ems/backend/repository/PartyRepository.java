package com.ems.backend.repository;

import com.ems.backend.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Integer> {

    List<Party> findAllByOrderByListPositionAsc();
}
