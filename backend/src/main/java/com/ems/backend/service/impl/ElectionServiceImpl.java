package com.ems.backend.service.impl;

import com.ems.backend.dto.ElectionDto;
import com.ems.backend.entity.Election;
import com.ems.backend.repository.ElectionRepository;
import com.ems.backend.service.ElectionService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
public class ElectionServiceImpl implements ElectionService {

    private final ElectionRepository electionRepository;

    public ElectionServiceImpl(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    @Override
    public List<Election> findAll() {
        return electionRepository.findAllByPriorityOrderByYearDesc();
    }

    @Override
    public Election findById(Integer id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Elección no encontrada con id: " + id));
    }

    @Override
    public Election save(ElectionDto dto) {
        validateDto(dto, null);

        Election election = new Election();
        election.setName(dto.getName().trim());
        election.setElectionType("PRESIDENCIAL");
        election.setYear(dto.getYear());
        election.setStartDate(dto.getStartDate());
        election.setEndDate(dto.getEndDate());
        election.setStatus(dto.getStatus());

        return electionRepository.save(election);
    }

    @Override
    public Election update(Integer id, ElectionDto dto) {
        Election existing = findById(id);

        if (!"P".equals(existing.getStatus())) {
            throw new BusinessRuleException("Solo se pueden editar elecciones en estado Pendiente.");
        }

        validateDto(dto, id);

        existing.setName(dto.getName().trim());
        existing.setYear(dto.getYear());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setStatus(dto.getStatus());

        return electionRepository.save(existing);
    }

    private void validateDto(ElectionDto dto, Integer excludeId) {
        if (dto.getYear() < Year.now().getValue()) {
            throw new BusinessRuleException("El año debe ser el actual o un año futuro.");
        }

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new BusinessRuleException("La fecha de cierre debe ser posterior a la fecha de inicio.");
        }

        if ("A".equals(dto.getStatus())) {
            boolean activeExists = excludeId == null
                    ? electionRepository.existsActiveElection()
                    : electionRepository.findActiveElection()
                            .map(e -> !e.getId().equals(excludeId))
                            .orElse(false);

            if (activeExists) {
                throw new BusinessRuleException("Ya existe una elección activa. Solo puede haber una activa a la vez.");
            }
        }
    }
}
