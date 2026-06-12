package com.ems.backend.service.impl;

import com.ems.backend.entity.Account;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    public VoterServiceImpl(VoterRepository voterRepository, AccountRepository accountRepository) {
        this.voterRepository = voterRepository;
        this.accountRepository = accountRepository;
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
    public Voter save(Voter voter, String dni) {
        // Validar formato DNI
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            throw new BusinessRuleException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }

        if (voter.getBirthDate() == null) {
            throw new BusinessRuleException("La fecha de nacimiento es obligatoria.");
        }
        if (voter.getDniExpiryDate() == null) {
            throw new BusinessRuleException("La fecha de vencimiento del DNI es obligatoria.");
        }

        // Buscar account existente o crear una nueva
        Account account = accountRepository.findByDni(dni)
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    newAccount.setDni(dni);
                    newAccount.setRole("user");
                    newAccount.setPasswordHash(null);
                    // isActive y createdAt los maneja el @PrePersist
                    return accountRepository.save(newAccount);
                });

        // Validar que esa account no esté ya asociada a otro votante
        if (voterRepository.existsByAccount_Id(account.getId())) {
            throw new BusinessRuleException("El DNI ya está asociado a un votante registrado.");
        }

        voter.setAccount(account);
        normalizeVoterFields(voter);
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
    public long getElectoresHabilitados() {
        return voterRepository.countByStatus(STATUS_ACTIVE);
    }

    @Override
    public long getVotantesAsistentes() {
        return voterRepository.countByStatusAndHasVotedTrue(STATUS_ACTIVE);
    }

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
