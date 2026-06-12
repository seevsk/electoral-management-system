package com.ems.backend.service;

import com.ems.backend.dto.ElectionDto;
import com.ems.backend.entity.Election;

import java.util.List;

public interface ElectionService {

    List<Election> findAll();

    Election findById(Integer id);

    Election save(ElectionDto dto);

    Election update(Integer id, ElectionDto dto);
}
