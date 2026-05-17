package com.ems.backend.service.impl;

import com.ems.backend.entity.Party;
import com.ems.backend.repository.PartyRepository;
import com.ems.backend.service.PartyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;

    public PartyServiceImpl(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    public List<Party> findAll() {
        return partyRepository.findAllByOrderByListPositionAsc();
    }

    @Override
    public Party findById(Integer id) {
        return partyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + id));
    }

    @Override
    public Party save(Party party) {
        if (party.getIsActive() == null) {
            party.setIsActive(true);
        }
        return partyRepository.save(party);
    }

    @Override
    public Party update(Integer id, Party party) {
        Party existing = findById(id);
        existing.setName(party.getName());
        existing.setAcronym(party.getAcronym());
        existing.setRepresentative(party.getRepresentative());
        existing.setLogoUrl(party.getLogoUrl());
        existing.setListPosition(party.getListPosition());
        return partyRepository.save(existing);
    }

    @Override
    public void disable(Integer id) {
        Party existing = findById(id);
        existing.setIsActive(false);
        partyRepository.save(existing);
    }

    @Override
    public void enable(Integer id) {
        Party existing = findById(id);
        existing.setIsActive(true);
        partyRepository.save(existing);
    }
}
