package com.ems.backend.service;

import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import com.ems.backend.entity.Voter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateService {

    List<Candidate> findAll();

    List<Candidate> findAllActive();

    Candidate findById(Integer id);

    Candidate save(Candidate candidate, MultipartFile photoFile);

    Candidate update(Integer id, Candidate candidate, MultipartFile photoFile);

    void disable(Integer id);

    void enable(Integer id);

    List<Voter> searchEligibleVoters(String query);

    List<Party> findAvailableParties();

    List<Election> findAvailableElections();

    //probando la paginacion para ver los formatos json
    Page<Candidate> findAll(Pageable pageable);
}
