package com.ems.backend.service.impl;

import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Party;
import com.ems.backend.repository.PartyRepository;
import com.ems.backend.service.CloudinaryStorageService;
import com.ems.backend.service.PartyService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public Map<Integer, String> findRepresentativesForDisplayElection(List<Party> parties) {
        if (parties == null || parties.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> partyIds = parties.stream()
                .map(Party::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (partyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = partyRepository.findRepresentativeNamesByPartyIdsForPriorityElection(partyIds);
        Map<Integer, String> representativesByPartyId = new HashMap<>();
        for (Object[] row : rows) {
            Integer partyId = (Integer) row[0];
            String representativeName = (String) row[1];
            representativesByPartyId.put(partyId, representativeName);
        }
        return representativesByPartyId;
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
        party.setAcronym(normalizeAcronym(party.getAcronym()));

        if (party.getRepresentative() == null || party.getRepresentative().trim().isEmpty()) {
            party.setRepresentative("SIN REPRESENTANTE");
        }

        if (party.getName() == null || party.getAcronym() == null) {
            throw new BusinessRuleException("Nombre y siglas son obligatorios.");
        }

        validateAcronym(party.getAcronym());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeAcronym(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private void validateAcronym(String acronym) {
        if (acronym.length() != 3 || !acronym.matches("^[A-Z]{3}$")) {
            throw new BusinessRuleException("Las siglas deben tener exactamente 3 letras.");
        }
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

    //Probando la paginacion en formatoide json
    @Override
    public Page<Party> findAll(Pageable pageable) {
        return partyRepository.findAll(pageable);
    }
}
