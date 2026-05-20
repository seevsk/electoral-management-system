package com.ems.backend.service;

import com.ems.backend.entity.Party;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PartyService {

    List<Party> findAll();

    List<Party> findAllActive();

    Party findById(Integer id);

    Party save(Party party, MultipartFile logoFile);

    Party update(Integer id, Party party, MultipartFile logoFile);

    void disable(Integer id);

    void enable(Integer id);
}
