package com.ems.backend.service;

import com.ems.backend.dto.ElectionDto;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ElectionService {

    List<Election> findAll();

    Election findById(Integer id);

    Election save(ElectionDto dto);

    Election update(Integer id, ElectionDto dto);

    //probando la paginacion para ver los formatos json
    Page<Election> findAll(Pageable pageable);
}
