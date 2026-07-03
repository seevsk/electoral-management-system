package com.ems.backend.service.impl;

import com.ems.backend.entity.*;
import com.ems.backend.repository.AccountRepository;
import com.ems.backend.repository.VoterAssignmentRepository;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.repository.VotingTableRepository;
import com.ems.backend.service.VoterService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final VoterAssignmentRepository voterAssignmentRepository;
    private final VotingTableRepository votingTableRepository;

    public VoterServiceImpl(VoterRepository voterRepository,
                            AccountRepository accountRepository,
                            VoterAssignmentRepository voterAssignmentRepository,
                            VotingTableRepository votingTableRepository) {
        this.voterRepository = voterRepository;
        this.accountRepository = accountRepository;
        this.voterAssignmentRepository = voterAssignmentRepository;
        this.votingTableRepository = votingTableRepository;
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
    @Transactional
    public Voter save(Voter voter, String dni, Integer votingTableId) {
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            throw new BusinessRuleException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        if (voter.getBirthDate() == null) {
            throw new BusinessRuleException("La fecha de nacimiento es obligatoria.");
        }
        if (voter.getDniExpiryDate() == null) {
            throw new BusinessRuleException("La fecha de vencimiento del DNI es obligatoria.");
        }
        if (votingTableId == null) {
            throw new BusinessRuleException("La asignacion de mesa de votacion es obligatoria.");
        }

        Account account = accountRepository.findByDni(dni)
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    newAccount.setDni(dni);
                    newAccount.setRole("user");
                    newAccount.setPasswordHash(null);
                    return accountRepository.save(newAccount);
                });

        if (voterRepository.existsByAccount_Id(account.getId())) {
            throw new BusinessRuleException("El DNI ya está asociado a un votante registrado.");
        }

        voter.setAccount(account);
        normalizeVoterFields(voter);
        Voter saved = voterRepository.save(voter);

        VotingTable table = resolveTableForDistrict(votingTableId, saved.getLocationCode());
        VoterAssignment assignment = new VoterAssignment();
        assignment.setVoter(saved);
        assignment.setVotingTable(table);
        assignment.setAssignedAt(LocalDate.now());
        assignment.setCreatedAt(LocalDateTime.now());
        voterAssignmentRepository.save(assignment);

        return saved;
    }

    @Override
    @Transactional
    public Voter update(Integer id, Voter voter, Integer votingTableId) {
        Voter existing = findById(id);

        if (STATUS_INACTIVE.equals(existing.getStatus())) {
            throw new BusinessRuleException("No se puede actualizar un votante inactivo. Habilítelo primero.");
        }
        if (votingTableId == null) {
            throw new BusinessRuleException("La asignacion de mesa de votacion es obligatoria.");
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
        Voter saved = voterRepository.save(existing);

        VotingTable table = resolveTableForDistrict(votingTableId, saved.getLocationCode());
        VoterAssignment assignment = voterAssignmentRepository.findByVoter_Id(id)
                .orElse(new VoterAssignment());
        if (assignment.getId() == null) {
            assignment.setAssignedAt(LocalDate.now());
            assignment.setCreatedAt(LocalDateTime.now());
        }
        assignment.setVoter(saved);
        assignment.setVotingTable(table);
        voterAssignmentRepository.save(assignment);

        return saved;
    }

    @Override
    public Page<Voter> findPaginated(int page, int size, String search, String statusFilter) {
        String resolvedSearch = (search == null || search.isBlank()) ? null : search.trim();
        String resolvedStatus = (statusFilter == null || statusFilter.isBlank()) ? null : statusFilter.trim();
        return voterRepository.findPaginatedForStatusPage(
            resolvedSearch,
            resolvedStatus,
            PageRequest.of(Math.max(0, page), size)
        );
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

    //Probando la paginacion en formatoide json
    @Override
    public Page<Voter> findAll(Pageable pageable) {
        return voterRepository.findAll(pageable);
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE MÉTODOS DE PARTICIPACIÓN CIUDADANA
    // =========================================================================

    @Override
    public List<Map<String, Object>> getParticipationByScope() {
        List<Object[]> queryResult = voterRepository.getParticipationByScope();
        List<Map<String, Object>> participationList = new ArrayList<>();

        for (Object[] row : queryResult) {
            String name     = (String) row[0];
            long total      = (Long)   row[1];
            long attended   = (Long)   row[2];
            long pending    = (Long)   row[3];
            long inactive   = (Long)   row[4];
            double pct = total > 0 ? (attended * 100.0) / total : 0.0;

            Map<String, Object> scopeData = new HashMap<>();
            scopeData.put("name",     name);
            scopeData.put("total",    total);
            scopeData.put("attended", attended);
            scopeData.put("pending",  pending);
            scopeData.put("inactive", inactive);
            scopeData.put("pct",      pct);

            participationList.add(scopeData);
        }
        return participationList;
    }

    @Override
    public List<Map<String, Object>> getParticipationByDistrict() {
        List<Object[]> queryResult = voterRepository.getParticipationByDistrict();
        List<Map<String, Object>> participationList = new ArrayList<>();

        for (Object[] row : queryResult) {
            String code   = (String) row[0];
            String name   = (String) row[1];
            long total    = (Long)   row[2];
            long attended = (Long)   row[3];
            long pending  = (Long)   row[4];
            long inactive = (Long)   row[5];
            double pct = total > 0 ? (attended * 100.0) / total : 0.0;

            Map<String, Object> districtData = new HashMap<>();
            districtData.put("code",     code);
            districtData.put("name",     name);
            districtData.put("total",    total);
            districtData.put("attended", attended);
            districtData.put("pending",  pending);
            districtData.put("inactive", inactive);
            districtData.put("pct",      pct);

            participationList.add(districtData);
        }
        return participationList;
    }

    @Override
    public List<Map<String, Object>> getAllUbigeos() {
        List<Object[]> queryResult = voterRepository.findAllUbigeos();
        List<Map<String, Object>> ubigeosList = new ArrayList<>();

        for (Object[] row : queryResult) {
            Map<String, Object> ubigeo = new HashMap<>();
            ubigeo.put("department",  row[0]);
            ubigeo.put("province",    row[1]);
            ubigeo.put("district",    row[2]);
            ubigeo.put("locationCode",row[3]);
            ubigeo.put("total",       row[4]);
            ubigeo.put("attended",    row[5]);
            ubigeo.put("pending",     row[6]);
            ubigeo.put("inactive",    row[7]);
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

    private VotingTable resolveTableForDistrict(Integer votingTableId, String locationCode) {
        VotingTable table = votingTableRepository.findByIdWithLocation(votingTableId)
                .orElseThrow(() -> new BusinessRuleException("Mesa de votacion no encontrada."));
        if (!table.getVotingLocation().getLocationCode().equals(locationCode)) {
            throw new BusinessRuleException(
                    "La mesa seleccionada no pertenece al distrito del votante.");
        }
        return table;
    }
}
