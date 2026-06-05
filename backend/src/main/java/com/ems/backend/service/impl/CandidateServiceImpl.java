package com.ems.backend.service.impl;

import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import com.ems.backend.entity.PartyElectionRepresentative;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.CandidateRepository;
import com.ems.backend.repository.ElectionRepository;
import com.ems.backend.repository.PartyElectionRepresentativeRepository;
import com.ems.backend.repository.PartyRepository;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.service.CandidateService;
import com.ems.backend.service.CloudinaryStorageService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final String PRESIDENTIAL = "PRESIDENCIAL";

    private final CandidateRepository candidateRepository;
    private final VoterRepository voterRepository;
    private final PartyRepository partyRepository;
    private final ElectionRepository electionRepository;
    private final PartyElectionRepresentativeRepository partyElectionRepresentativeRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                VoterRepository voterRepository,
                                PartyRepository partyRepository,
                                ElectionRepository electionRepository,
                                PartyElectionRepresentativeRepository partyElectionRepresentativeRepository,
                                CloudinaryStorageService cloudinaryStorageService) {
        this.candidateRepository = candidateRepository;
        this.voterRepository = voterRepository;
        this.partyRepository = partyRepository;
        this.electionRepository = electionRepository;
        this.partyElectionRepresentativeRepository = partyElectionRepresentativeRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Override
    public List<Candidate> findAll() {
        return candidateRepository.findAllByOrderByElection_YearDescParty_ListPositionAscIdAsc();
    }

    @Override
    public List<Candidate> findAllActive() {
        return candidateRepository.findByIsActiveTrueOrderByElection_YearDescParty_ListPositionAscIdAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Candidate findById(Integer id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("No se encontró el candidato con id: " + id));
    }

    @Override
    @Transactional
    public Candidate save(Candidate candidate, MultipartFile photoFile) {
        normalizeCandidate(candidate);
        validateCreate(candidate);

        if (candidate.getIsActive() == null) {
            candidate.setIsActive(true);
        }

        candidate.setVoter(resolveVoter(candidate.getVoterId()));
        candidate.setParty(resolveParty(candidate.getPartyId()));
        candidate.setElection(resolveElection(candidate.getElectionId()));
        candidate.setPhotoUrl(null);

        Candidate savedCandidate = candidateRepository.save(candidate);
        syncPartyElectionRepresentativeForCreate(savedCandidate);

        if (hasPhotoFile(photoFile)) {
            String uploadedPhotoUrl = cloudinaryStorageService.uploadCandidatePhoto(photoFile, savedCandidate.getId());
            savedCandidate.setPhotoUrl(uploadedPhotoUrl);
            savedCandidate = candidateRepository.save(savedCandidate);
        }

        return savedCandidate;
    }

    @Override
    @Transactional
    public Candidate update(Integer id, Candidate candidate, MultipartFile photoFile) {
        Candidate existing = findById(id);
        if (!Boolean.TRUE.equals(existing.getIsActive())) {
            throw new BusinessRuleException("No se puede actualizar un candidato inactivo. Habilitelo primero en el panel de estado.");
        }

        normalizeCandidate(candidate);
        validateUpdate(id, candidate);

        existing.setVoter(resolveVoter(candidate.getVoterId()));
        existing.setParty(resolveParty(candidate.getPartyId()));
        existing.setElection(resolveElection(candidate.getElectionId()));
        existing.setVoterId(candidate.getVoterId());
        existing.setPartyId(candidate.getPartyId());
        existing.setElectionId(candidate.getElectionId());
        existing.setListNumber(candidate.getListNumber());

        if (hasPhotoFile(photoFile)) {
            String uploadedPhotoUrl = cloudinaryStorageService.uploadCandidatePhoto(photoFile, existing.getId());
            existing.setPhotoUrl(uploadedPhotoUrl);
        }

        Candidate savedCandidate = candidateRepository.save(existing);
        syncPartyElectionRepresentativeForUpdate(savedCandidate);
        return savedCandidate;
    }

    @Override
    @Transactional
    public void disable(Integer id) {
        Candidate existing = findById(id);
        if (isPresidentialElectionInCourse(existing.getElection())) {
            throw new BusinessRuleException("No se puede inhabilitar este candidato porque participa en una eleccion presidencial en curso.");
        }
        existing.setIsActive(false);
        candidateRepository.save(existing);
    }

    @Override
    @Transactional
    public void enable(Integer id) {
        Candidate existing = findById(id);
        existing.setIsActive(true);
        candidateRepository.save(existing);
    }

    @Override
    public List<Voter> searchEligibleVoters(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return voterRepository.findEligibleUserVotersForCandidates();
        }
        return voterRepository.searchEligibleUserVotersForCandidates(normalizedQuery);
    }

    @Override
    public List<Party> findAvailableParties() {
        return partyRepository.findByIsActiveTrueOrderByListPositionAsc();
    }

    @Override
    public List<Election> findAvailableElections() {
        return electionRepository.findAvailablePresidentialElectionsForCandidates();
    }

    private void validateCreate(Candidate candidate) {
        resolveVoter(candidate.getVoterId());
        resolveParty(candidate.getPartyId());
        resolveElection(candidate.getElectionId());

        if (candidate.getListNumber() == null) {
            throw new BusinessRuleException("El número de lista es obligatorio.");
        }

        if (candidateRepository.existsByVoterIdAndElectionId(candidate.getVoterId(), candidate.getElectionId())) {
            throw new BusinessRuleException("Ya existe un candidato para ese votante en esa elección.");
        }

        if (candidateRepository.existsByListNumberAndPartyIdAndElectionId(candidate.getListNumber(), candidate.getPartyId(), candidate.getElectionId())) {
            throw new BusinessRuleException("Ya existe un candidato con ese número de lista para ese partido y elección.");
        }

        ensurePartyElectionRepresentativeIsAvailable(candidate.getPartyId(), candidate.getElectionId());
    }

    private void validateUpdate(Integer id, Candidate candidate) {
        resolveVoter(candidate.getVoterId());
        resolveParty(candidate.getPartyId());
        resolveElection(candidate.getElectionId());

        if (candidate.getListNumber() == null) {
            throw new BusinessRuleException("El número de lista es obligatorio.");
        }

        if (candidateRepository.existsByVoterIdAndElectionIdAndIdNot(candidate.getVoterId(), candidate.getElectionId(), id)) {
            throw new BusinessRuleException("Ya existe un candidato para ese votante en esa elección.");
        }

        if (candidateRepository.existsByListNumberAndPartyIdAndElectionIdAndIdNot(candidate.getListNumber(), candidate.getPartyId(), candidate.getElectionId(), id)) {
            throw new BusinessRuleException("Ya existe un candidato con ese número de lista para ese partido y elección.");
        }

        ensurePartyElectionRepresentativeIsAvailableForUpdate(candidate.getPartyId(), candidate.getElectionId(), id);
    }

    private void normalizeCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new BusinessRuleException("Los datos del candidato son obligatorios.");
        }

        if (candidate.getVoterId() == null) {
            throw new BusinessRuleException("Debe seleccionar un votante.");
        }
        if (candidate.getPartyId() == null) {
            throw new BusinessRuleException("Debe seleccionar un partido.");
        }
        if (candidate.getElectionId() == null) {
            throw new BusinessRuleException("Debe seleccionar una elección.");
        }
        if (candidate.getListNumber() != null && candidate.getListNumber() <= 0) {
            throw new BusinessRuleException("El número de lista debe ser mayor a cero.");
        }
    }

    private Voter resolveVoter(Integer voterId) {
        Voter voter = voterRepository.findById(voterId)
                .orElseThrow(() -> new BusinessRuleException("No existe el votante seleccionado."));

        if (voter.getAccount() == null || voter.getAccount().getRole() == null || !"user".equalsIgnoreCase(voter.getAccount().getRole())) {
            throw new BusinessRuleException("El votante debe pertenecer a una cuenta de usuario.");
        }
        if (!Boolean.TRUE.equals(voter.getAccount().getIsActive())) {
            throw new BusinessRuleException("El votante debe tener la cuenta activa.");
        }
        if (!"A".equalsIgnoreCase(voter.getStatus())) {
            throw new BusinessRuleException("El votante debe estar en estado activo.");
        }
        return voter;
    }

    private Party resolveParty(Integer partyId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessRuleException("No existe el partido seleccionado."));
        if (!Boolean.TRUE.equals(party.getIsActive())) {
            throw new BusinessRuleException("El partido debe estar activo.");
        }
        return party;
    }

    private Election resolveElection(Integer electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new BusinessRuleException("No existe la elección seleccionada."));

        if (!PRESIDENTIAL.equalsIgnoreCase(election.getElectionType())) {
            throw new BusinessRuleException("La elección seleccionada debe ser presidencial.");
        }
        if (!"A".equalsIgnoreCase(election.getStatus()) && !"P".equalsIgnoreCase(election.getStatus())) {
            throw new BusinessRuleException("La elección seleccionada no está disponible para candidatos.");
        }
        return election;
    }

    private void syncPartyElectionRepresentativeForCreate(Candidate candidate) {
        PartyElectionRepresentative representative = new PartyElectionRepresentative();

        representative.setCandidate(candidate);
        representative.setCandidateId(candidate.getId());
        representative.setParty(candidate.getParty());
        representative.setPartyId(candidate.getPartyId());
        representative.setElection(candidate.getElection());
        representative.setElectionId(candidate.getElectionId());

        partyElectionRepresentativeRepository.save(representative);
    }

    private void syncPartyElectionRepresentativeForUpdate(Candidate candidate) {
        PartyElectionRepresentative representative = partyElectionRepresentativeRepository
                .findByCandidateId(candidate.getId())
                .orElseGet(PartyElectionRepresentative::new);

        representative.setCandidate(candidate);
        representative.setCandidateId(candidate.getId());
        representative.setParty(candidate.getParty());
        representative.setPartyId(candidate.getPartyId());
        representative.setElection(candidate.getElection());
        representative.setElectionId(candidate.getElectionId());

        partyElectionRepresentativeRepository.save(representative);
    }

    private void ensurePartyElectionRepresentativeIsAvailable(Integer partyId, Integer electionId) {
        if (partyElectionRepresentativeRepository.findByPartyIdAndElectionId(partyId, electionId).isPresent()) {
            throw new BusinessRuleException("Este partido ya tiene un candidato asignado para esta elección.");
        }
    }

    private void ensurePartyElectionRepresentativeIsAvailableForUpdate(Integer partyId, Integer electionId, Integer candidateId) {
        partyElectionRepresentativeRepository.findByPartyIdAndElectionId(partyId, electionId)
                .filter(existing -> !candidateId.equals(existing.getCandidateId()))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Este partido ya tiene un candidato asignado para esta elección.");
                });
    }

    private boolean isPresidentialElectionInCourse(Election election) {
        if (election == null) {
            return false;
        }

        if (!PRESIDENTIAL.equalsIgnoreCase(election.getElectionType())) {
            return false;
        }

        if (!"A".equalsIgnoreCase(election.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(election.getStartDate()) && !now.isAfter(election.getEndDate());
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String normalized = query.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean hasPhotoFile(MultipartFile photoFile) {
        return photoFile != null && !photoFile.isEmpty();
    }
}
