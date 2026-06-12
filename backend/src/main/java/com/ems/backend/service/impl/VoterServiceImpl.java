package com.ems.backend.service.impl;

import com.ems.backend.entity.Voter;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.service.VoterService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


@Service
public class VoterServiceImpl implements VoterService {

    private static final String STATUS_ACTIVE = "A";
    private static final String STATUS_INACTIVE = "I";

    private final VoterRepository voterRepository;

    public VoterServiceImpl(VoterRepository voterRepository) {
        this.voterRepository = voterRepository;
    }

    @Override
    public List<Voter> findAll()
    {
        return voterRepository.findAllByOrderByFullNameAsc();
    }

    @Override
    public List<Voter> findAllActive() {
        return voterRepository.findByStatusOrderByFullNameAsc(STATUS_ACTIVE);
    }

    @Override
    public Voter findById(Integer id) {
        return  voterRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Voter not found with id: " + id));
    }

    @Override
    public Voter save(Voter voter) {
        normalizeVoterFields(voter);

        if (voterRepository.existsByAccount_Id(voter.getAccount().getId())) {
            throw new BusinessRuleException("La cuenta ya está asociada a otro votante.");
        }

        // No se setea status aquí — el @PrePersist del entity lo maneja (default "I")
        return voterRepository.save(voter);
    }

    @Override
    public Voter update(Integer id, Voter voter) {
        Voter existing = findById(id);

        if (STATUS_INACTIVE.equals(existing.getStatus())) {
            throw new BusinessRuleException("No se puede actualizar un votante inactivo. Habilítelo primero.");
        }

        normalizeVoterFields(voter);

        if (voterRepository.existsByAccount_IdAndIdNot(voter.getAccount().getId(), id)) {
            throw new BusinessRuleException("La cuenta ya está asociada a otro votante.");
        }

        existing.setAccount(voter.getAccount());
        existing.setFirstSurname(voter.getFirstSurname());
        existing.setSecondSurname(voter.getSecondSurname());
        existing.setFullName(voter.getFullName());
        existing.setGender(voter.getGender());
        existing.setMaritalStatus(voter.getMaritalStatus());
        existing.setBirthDate(voter.getBirthDate());
        existing.setDniExpiryDate(voter.getDniExpiryDate());
        existing.setLocationCode(voter.getLocationCode());

        return voterRepository.save(existing);
    }

    @Override
    public void disable(Integer id) {
        Voter voter = findById(id);
        voter.setStatus(STATUS_INACTIVE);
        voterRepository.save(voter);
    }

    @Override
    public void enable(Integer id) {
        Voter voter = findById(id);
        voter.setStatus(STATUS_ACTIVE);
        voterRepository.save(voter);
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE MÉTODOS DE PARTICIPACIÓN CIUDADANA
    // =========================================================================

    @Override
    public List<Map<String, Object>> getParticipationByScope() {
        List<Object[]> queryResult = voterRepository.getParticipationByScope();
        List<Map<String, Object>> participationList = new ArrayList<>();

        for (Object[] row : queryResult) {
            String name = (String) row[0];
            long total = (Long) row[1];
            long attended = (Long) row[2];
            double pct = total > 0 ? (attended * 100.0) / total : 0.0;

            Map<String, Object> scopeData = new HashMap<>();
            scopeData.put("name", name);
            scopeData.put("total", total);
            scopeData.put("attended", attended);
            scopeData.put("pct", pct);

            participationList.add(scopeData);
        }
        return participationList;
    }

    @Override
    public List<Map<String, Object>> getParticipationByDistrict() {
        // Comentario descriptivo: Obtener los resultados agrupados por distrito desde el repositorio
        List<Object[]> queryResult = voterRepository.getParticipationByDistrict();
        List<Map<String, Object>> participationList = new ArrayList<>();

        for (Object[] row : queryResult) {
            String code = (String) row[0];
            String name = (String) row[1];
            long total = (Long) row[2];
            long attended = (Long) row[3];
            double pct = total > 0 ? (attended * 100.0) / total : 0.0;

            Map<String, Object> districtData = new HashMap<>();
            districtData.put("code", code);
            districtData.put("name", name);
            districtData.put("total", total);
            districtData.put("attended", attended);
            districtData.put("pct", pct);

            participationList.add(districtData);
        }
        return participationList;
    }

    // =========================================================================
    // NUEVO MÉTODO: OBTENER TODOS LOS UBIGEOS PARA FILTROS EN CASCADA
    // =========================================================================

    @Override
    public List<Map<String, Object>> getAllUbigeos() {
        List<Object[]> queryResult = voterRepository.findAllUbigeos();
        List<Map<String, Object>> ubigeosList = new ArrayList<>();

        for (Object[] row : queryResult) {
            Map<String, Object> ubigeo = new HashMap<>();
            ubigeo.put("department", row[0]);      // department
            ubigeo.put("province", row[1]);        // province
            ubigeo.put("district", row[2]);        // district
            ubigeo.put("locationCode", row[3]);    // locationCode
            ubigeo.put("total", row[4]);           // total - NUEVO
            ubigeo.put("attended", row[5]);        // attended - NUEVO
            ubigeosList.add(ubigeo);
        }
        return ubigeosList;
    }

    // Metodos privados

    private void normalizeVoterFields(Voter voter) {
        voter.setFullName(normalizeText(voter.getFullName()));
        voter.setFirstSurname(normalizeText(voter.getFirstSurname()));
        voter.setSecondSurname(normalizeText(voter.getSecondSurname()));

        if (voter.getFullName() == null || voter.getFirstSurname() == null) {
            throw new BusinessRuleException("Nombre completo y  apellidos son obligatorios.");
        }
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
