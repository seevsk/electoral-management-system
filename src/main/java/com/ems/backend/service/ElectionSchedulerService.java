package com.ems.backend.service;

import com.ems.backend.repository.ElectionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ElectionSchedulerService {

    private final ElectionRepository electionRepository;

    public ElectionSchedulerService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void finalizeExpiredElections() {
        electionRepository.findActiveElection().ifPresent(election -> {
            if (LocalDateTime.now().isAfter(election.getEndDate())) {
                election.setStatus("C");
                electionRepository.save(election);
            }
        });
    }
}
