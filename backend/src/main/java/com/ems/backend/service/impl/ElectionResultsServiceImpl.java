package com.ems.backend.service.impl;

import com.ems.backend.dto.CandidateResultDto;
import com.ems.backend.dto.PresidentialResultsDto;
import com.ems.backend.entity.Election;
import com.ems.backend.entity.PartyElectionRepresentative;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.ElectionRepository;
import com.ems.backend.repository.PartyElectionRepresentativeRepository;
import com.ems.backend.repository.VoteRepository;
import com.ems.backend.service.ElectionDisplayState;
import com.ems.backend.service.ElectionResultsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElectionResultsServiceImpl implements ElectionResultsService {

    private final ElectionRepository electionRepository;
    private final PartyElectionRepresentativeRepository partyElectionRepresentativeRepository;
    private final VoteRepository voteRepository;

    public ElectionResultsServiceImpl(ElectionRepository electionRepository,
                                      PartyElectionRepresentativeRepository partyElectionRepresentativeRepository,
                                      VoteRepository voteRepository) {
        this.electionRepository = electionRepository;
        this.partyElectionRepresentativeRepository = partyElectionRepresentativeRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public Optional<Election> findRelevantPresidentialElection() {
        return electionRepository.findBestPresidentialElection();
    }

    @Override
    public ElectionDisplayState resolveState(Election election) {
        LocalDateTime now = LocalDateTime.now();
        String status = election.getStatus();

        if (now.isAfter(election.getEndDate())) {
            return ElectionDisplayState.CLOSED;
        }
        if ("C".equals(status)) {
            return ElectionDisplayState.CLOSED;
        }
        if (now.isBefore(election.getStartDate())) {
            return ElectionDisplayState.SCHEDULED;
        }
        if ("P".equals(status)) {
            return ElectionDisplayState.SCHEDULED;
        }
        return ElectionDisplayState.IN_PROGRESS;
    }

    @Override
    @Transactional(readOnly = true)
    public PresidentialResultsDto getResults(Integer electionId, String districtCode) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Elección no encontrada: " + electionId));

        List<PartyElectionRepresentative> ballotEntries =
                partyElectionRepresentativeRepository.findActiveBallotEntriesByElectionId(electionId);

        Map<Integer, Long> votesByCandidate = voteRepository.countVotesByCandidate(electionId, districtCode)
                .stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));

        long totalVotes = voteRepository.countTotalVotes(electionId, districtCode);
        long blankVotes = voteRepository.countBlankVotes(electionId, districtCode);

        List<CandidateResultDto> candidateResults = ballotEntries.stream()
                .map(per -> {
                    Voter voter = per.getCandidate().getVoter();
                    String fullName = (voter.getFullName() + " "
                            + voter.getFirstSurname() + " "
                            + voter.getSecondSurname()).trim();
                    long voteCount = votesByCandidate.getOrDefault(per.getCandidate().getId(), 0L);
                    double percentage = totalVotes > 0 ? (voteCount * 100.0) / totalVotes : 0.0;
                    return new CandidateResultDto(
                            per.getCandidate().getId(),
                            fullName,
                            per.getCandidate().getPhotoUrl(),
                            per.getCandidate().getListNumber(),
                            per.getParty().getName(),
                            per.getParty().getAcronym(),
                            per.getParty().getLogoUrl(),
                            voteCount,
                            percentage
                    );
                })
                .sorted(Comparator.comparingLong(CandidateResultDto::getVoteCount).reversed())
                .collect(Collectors.toList());

        double blankPercentage = totalVotes > 0 ? (blankVotes * 100.0) / totalVotes : 0.0;

        long chartMaxVotes = candidateResults.stream()
                .mapToLong(CandidateResultDto::getVoteCount)
                .max()
                .orElse(1L);

        return new PresidentialResultsDto(
                election.getId(),
                election.getName(),
                election.getYear(),
                districtCode == null ? "" : districtCode,
                candidateResults,
                blankVotes,
                blankPercentage,
                chartMaxVotes
        );
    }
}
