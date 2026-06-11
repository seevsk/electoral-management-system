package com.ems.backend.controller;

import com.ems.backend.entity.Election;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.service.VotingService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
@RequestMapping("/voter/vote")
public class VotingController {

    private final VotingService votingService;
    private final VoterRepository voterRepository;

    public VotingController(VotingService votingService, VoterRepository voterRepository) {
        this.votingService = votingService;
        this.voterRepository = voterRepository;
    }

    // ──────────────────────────────────────────────────────────
    // STEP 1: Conditions
    // ──────────────────────────────────────────────────────────

    @GetMapping("/conditions")
    public String conditions(Authentication auth, HttpSession session, Model model) {
        Optional<Election> electionOpt = votingService.findActiveElectionInWindow();
        if (electionOpt.isEmpty()) return "redirect:/voter/portal";

        boolean hasVoted = voterRepository.findByAccountDni(auth.getName())
                .map(v -> Boolean.TRUE.equals(v.getHasVoted()))
                .orElse(false);
        if (hasVoted) return "redirect:/voter/portal";

        // Reset timer on every visit to conditions
        session.setAttribute("votingStartTime", Instant.now());

        model.addAttribute("election", electionOpt.get());
        return "voting/conditions";
    }

    @PostMapping("/start")
    public String start() {
        Optional<Election> electionOpt = votingService.findActiveElectionInWindow();
        if (electionOpt.isEmpty()) return "redirect:/voter/portal";
        return "redirect:/voter/vote/ballot";
    }

    // ──────────────────────────────────────────────────────────
    // STEP 2: Ballot
    // ──────────────────────────────────────────────────────────

    @GetMapping("/ballot")
    public String ballot(HttpSession session, Model model) {
        Optional<Election> electionOpt = votingService.findActiveElectionInWindow();
        if (electionOpt.isEmpty()) return "redirect:/voter/portal";

        if (session.getAttribute("votingStartTime") == null)
            return "redirect:/voter/vote/conditions";

        Election election = electionOpt.get();
        model.addAttribute("election", election);
        model.addAttribute("ballotEntries", votingService.getBallotEntries(election.getId()));
        return "voting/ballot";
    }

    @PostMapping("/select")
    public String select(@RequestParam Integer candidateId) {
        return "redirect:/voter/vote/summary?candidateId=" + candidateId;
    }

    // ──────────────────────────────────────────────────────────
    // STEP 3: Summary
    // ──────────────────────────────────────────────────────────

    @GetMapping("/summary")
    public String summary(@RequestParam(required = false) Integer candidateId,
                          HttpSession session, Model model) {
        if (candidateId == null) return "redirect:/voter/vote/ballot";

        Optional<Election> electionOpt = votingService.findActiveElectionInWindow();
        if (electionOpt.isEmpty()) return "redirect:/voter/portal";

        if (session.getAttribute("votingStartTime") == null)
            return "redirect:/voter/vote/conditions";

        Election election = electionOpt.get();
        model.addAttribute("election", election);
        model.addAttribute("selectedCandidateId", candidateId);

        if (candidateId == 0) {
            model.addAttribute("isBlankVote", true);
        } else {
            votingService.getBallotEntry(election.getId(), candidateId)
                    .ifPresent(e -> model.addAttribute("selectedEntry", e));
        }

        return "voting/summary";
    }

    // ──────────────────────────────────────────────────────────
    // STEP 4: Confirm → Confirmation
    // ──────────────────────────────────────────────────────────

    @PostMapping("/confirm")
    public String confirm(@RequestParam Integer candidateId,
                          @RequestParam Integer electionId,
                          Authentication auth,
                          HttpSession session,
                          RedirectAttributes redirectAttrs) {
        Instant startTime = (Instant) session.getAttribute("votingStartTime");
        if (startTime == null || Instant.now().isAfter(startTime.plus(5, ChronoUnit.MINUTES))) {
            session.removeAttribute("votingStartTime");
            redirectAttrs.addFlashAttribute("votingExpired", true);
            return "redirect:/voter/vote/conditions";
        }

        try {
            Integer actualCandidateId = (candidateId == 0) ? null : candidateId;
            votingService.castVote(auth.getName(), electionId, actualCandidateId);
        } catch (BusinessRuleException e) {
            session.removeAttribute("votingStartTime");
            redirectAttrs.addFlashAttribute("voteError", e.getMessage());
            return "redirect:/voter/vote/conditions";
        }

        session.removeAttribute("votingStartTime");
        return "redirect:/voter/vote/done";
    }

    @GetMapping("/done")
    public String done() {
        return "voting/confirmation";
    }
}
