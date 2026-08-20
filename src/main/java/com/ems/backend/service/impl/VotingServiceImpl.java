package com.ems.backend.service.impl;

import com.ems.backend.dto.BallotEntryDto;
import com.ems.backend.entity.*;
import com.ems.backend.repository.*;
import com.ems.backend.service.VotingService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotingServiceImpl implements VotingService {

    private final ElectionRepository electionRepository;
    private final VoterRepository voterRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final PartyElectionRepresentativeRepository perRepository;

    public VotingServiceImpl(ElectionRepository electionRepository,
                             VoterRepository voterRepository,
                             CandidateRepository candidateRepository,
                             VoteRepository voteRepository,
                             PartyElectionRepresentativeRepository perRepository) {
        this.electionRepository = electionRepository;
        this.voterRepository = voterRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
        this.perRepository = perRepository;
    }

    @Override
    public Optional<Election> findActiveElectionInWindow() {
        return electionRepository.findActiveElection().filter(e -> {
            LocalDateTime now = LocalDateTime.now();
            return !now.isBefore(e.getStartDate()) && !now.isAfter(e.getEndDate());
        });
    }

    @Override
    public List<BallotEntryDto> getBallotEntries(Integer electionId) {
        return perRepository.findActiveBallotEntriesByElectionId(electionId)
                .stream()
                .map(per -> {
                    Party p = per.getParty();
                    Candidate c = per.getCandidate();
                    Voter v = c.getVoter();
                    String fullName = v.getFullName() + " "
                            + v.getFirstSurname() + " "
                            + v.getSecondSurname();
                    return new BallotEntryDto(
                            p.getId(), p.getName(), p.getAcronym(), p.getLogoUrl(),
                            c.getId(), fullName.trim(), c.getPhotoUrl(), c.getListNumber()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BallotEntryDto> getBallotEntry(Integer electionId, Integer candidateId) {
        return perRepository.findActiveBallotEntryByElectionAndCandidate(electionId, candidateId)
                .map(per -> {
                    Party p = per.getParty();
                    Candidate c = per.getCandidate();
                    Voter v = c.getVoter();
                    String fullName = (v.getFullName() + " "
                            + v.getFirstSurname() + " "
                            + v.getSecondSurname()).trim();
                    return new BallotEntryDto(
                            p.getId(), p.getName(), p.getAcronym(), p.getLogoUrl(),
                            c.getId(), fullName, c.getPhotoUrl(), c.getListNumber()
                    );
                });
    }

    @Override
    @Transactional
    public void castVote(String voterDni, Integer electionId, Integer candidateId) {
        Voter voter = voterRepository.findByAccountDni(voterDni)
                .orElseThrow(() -> new BusinessRuleException("Votante no encontrado"));

        if (Boolean.TRUE.equals(voter.getHasVoted())) {
            throw new BusinessRuleException("El votante ya ha emitido su voto en este proceso");
        }

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new BusinessRuleException("Elección no encontrada"));

        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setElection(election);
        vote.setVotedAt(LocalDateTime.now());

        if (candidateId != null) {
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new BusinessRuleException("Candidato no encontrado"));
            vote.setCandidate(candidate);
        }

        voteRepository.save(vote);

        voter.setHasVoted(true);
        voter.setVotedAt(LocalDateTime.now());
        voterRepository.save(voter);
    }
}
