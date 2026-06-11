package com.ems.backend.controller;

import com.ems.backend.entity.Election;
import com.ems.backend.repository.ElectionRepository;
import com.ems.backend.repository.VoterRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/voter")
public class VoterPortalController {

    private final ElectionRepository electionRepository;
    private final VoterRepository voterRepository;

    public VoterPortalController(ElectionRepository electionRepository,
                                 VoterRepository voterRepository) {
        this.electionRepository = electionRepository;
        this.voterRepository = voterRepository;
    }

    @GetMapping("/portal")
    public String portal(Authentication authentication, Model model) {
        String dni = authentication.getName();
        LocalDateTime now = LocalDateTime.now();

        Optional<Election> nextOpt = electionRepository.findNextUpcomingPresidentialElection();

        if (nextOpt.isPresent()) {
            Election next = nextOpt.get();

            if ("A".equals(next.getStatus())) {
                boolean inWindow = !now.isBefore(next.getStartDate())
                        && !now.isAfter(next.getEndDate());

                if (inWindow) {
                    boolean hasVoted = voterRepository.findByAccountDni(dni)
                            .map(v -> Boolean.TRUE.equals(v.getHasVoted()))
                            .orElse(false);

                    if (!hasVoted) {
                        return "redirect:/voter/vote/conditions";
                    }

                    model.addAttribute("scenario", "VOTED");
                    model.addAttribute("election", next);
                    return "voter/portal";
                }
            }

            // Pending election or active but before/after window
            model.addAttribute("scenario", "PENDING");
            model.addAttribute("nextElection", next);
            return "voter/portal";
        }

        // No upcoming election
        model.addAttribute("scenario", "COMPLETED");
        return "voter/portal";
    }
}
