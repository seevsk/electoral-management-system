package com.ems.backend.service;

import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;

public interface PartyService {

    List<Party> findAll();

    List<Party> findAllActive();

    Map<Integer, String> findRepresentativesForDisplayElection(List<Party> parties);

    Party findById(Integer id);

    Party save(Party party, MultipartFile logoFile);

    Party update(Integer id, Party party, MultipartFile logoFile);

    void disable(Integer id);

    void enable(Integer id);

    //probando la paginacion para ver los formatos json
    Page<Party> findAll(Pageable pageable);
}
