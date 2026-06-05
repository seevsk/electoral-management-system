package com.ems.backend.repository;

import com.ems.backend.entity.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoterRepository  extends JpaRepository<Voter, Integer>{

    // Verifica si una cuenta ya está asociada a un votante
    boolean existsByAccount_Id(Integer accountId);

    // Verifica si una cuenta ya está asociada a otro votante (para edición)
    boolean existsByAccount_IdAndIdNot(Integer accountId, Integer id);


    List<Voter> findAllByOrderByFullNameAsc();
    List<Voter> findByStatusOrderByFullNameAsc(String status);
}
