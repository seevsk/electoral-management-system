package com.ems.backend.service;

import com.ems.backend.entity.Voter;

import java.util.List;

public interface VoterService {

    List<Voter> findAll();

    List<Voter> findAllActive();

    Voter findById(Integer id);

    Voter save(Voter voter);

    Voter update(Integer id, Voter voter);

    // Cambia el estado a Inactivo (I)
    void disable(Integer id);

    // Cambia el estado a Activo (A)
    void enable(Integer id);



}
