package com.ems.backend.service;

import com.ems.backend.entity.Party;

import java.util.List;

public interface PartyService {

    List<Party> findAll();

    Party findById(Integer id);

    Party save(Party party);

    Party update(Integer id, Party party);

    void disable(Integer id);

    void enable(Integer id);
}
