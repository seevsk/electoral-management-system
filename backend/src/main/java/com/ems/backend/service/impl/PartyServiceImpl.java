package com.ems.backend.service.impl;

import com.ems.backend.entity.Party;
import com.ems.backend.repository.PartyRepository;
import com.ems.backend.service.CloudinaryStorageService;
import com.ems.backend.service.PartyService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public PartyServiceImpl(PartyRepository partyRepository, CloudinaryStorageService cloudinaryStorageService) {
        this.partyRepository = partyRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Override
    public List<Party> findAll() {
        return partyRepository.findAllByOrderByListPositionAsc();
    }

    @Override
    public List<Party> findAllActive() {
        return partyRepository.findByIsActiveTrueOrderByListPositionAsc();
    }

    @Override
    public Party findById(Integer id) {
        return partyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + id));
    }

    @Override
    public Party save(Party party, MultipartFile logoFile) {
        normalizePartyFields(party);
        validateUniquenessForCreate(party);

        if (party.getIsActive() == null) {
            party.setIsActive(true);
        }

        party.setListPosition(partyRepository.findMaxListPosition() + 1);
        Party savedParty = partyRepository.save(party);

        String uploadedLogoUrl = cloudinaryStorageService.uploadPartyLogo(logoFile, savedParty.getId());
        if (uploadedLogoUrl != null) {
            savedParty.setLogoUrl(uploadedLogoUrl);
            savedParty = partyRepository.save(savedParty);
        }

        return savedParty;
    }

    @Override
    public Party update(Integer id, Party party, MultipartFile logoFile) {
        Party existing = findById(id);
        if (!existing.getIsActive()) {
            throw new BusinessRuleException("No se puede actualizar un partido inactivo. Habilitelo primero en el panel de estado.");
        }

        normalizePartyFields(party);
        validateUniquenessForUpdate(id, party);

        existing.setName(party.getName());
        existing.setAcronym(party.getAcronym());
        existing.setRepresentative(party.getRepresentative());

        String uploadedLogoUrl = cloudinaryStorageService.uploadPartyLogo(logoFile, existing.getId());
        if (uploadedLogoUrl != null) {
            existing.setLogoUrl(uploadedLogoUrl);
        }

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

    private void normalizePartyFields(Party party) {
        party.setName(normalizeText(party.getName()));
        party.setAcronym(normalizeText(party.getAcronym()));
        party.setRepresentative(normalizeText(party.getRepresentative()));

        if (party.getName() == null || party.getAcronym() == null || party.getRepresentative() == null) {
            throw new BusinessRuleException("Nombre, siglas y representante son obligatorios.");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateUniquenessForCreate(Party party) {
        if (partyRepository.existsByNameIgnoreCase(party.getName())) {
            throw new BusinessRuleException("Ya existe un partido con ese nombre.");
        }
        if (partyRepository.existsByAcronymIgnoreCase(party.getAcronym())) {
            throw new BusinessRuleException("Ya existe un partido con esas siglas.");
        }
    }

    private void validateUniquenessForUpdate(Integer id, Party party) {
        if (partyRepository.existsByNameIgnoreCaseAndIdNot(party.getName(), id)) {
            throw new BusinessRuleException("Ya existe un partido con ese nombre.");
        }
        if (partyRepository.existsByAcronymIgnoreCaseAndIdNot(party.getAcronym(), id)) {
            throw new BusinessRuleException("Ya existe un partido con esas siglas.");
        }
    }
}
