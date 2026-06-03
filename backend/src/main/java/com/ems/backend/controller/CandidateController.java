package com.ems.backend.controller;

import com.ems.backend.entity.Candidate;
import com.ems.backend.service.CandidateService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @ModelAttribute("candidate")
    public Candidate modelCandidate() {
        return new Candidate();
    }

    @GetMapping
    public String redirectToList() {
        return "redirect:/admin/candidates/list";
    }

    @GetMapping("/list")
    public String listCandidates(Model model) {
        model.addAttribute("candidates", candidateService.findAllActive());
        return "candidates/listcandidate";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("query", query);
        model.addAttribute("voters", candidateService.searchEligibleVoters(query));
        model.addAttribute("parties", candidateService.findAvailableParties());
        model.addAttribute("elections", candidateService.findAvailableElections());
        return "candidates/registercandidate";
    }

    @PostMapping("/register")
    public String registerCandidate(
            @ModelAttribute Candidate candidate,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            candidateService.save(candidate, photoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Candidato registrado correctamente.");
            return "redirect:/admin/candidates/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("candidate", candidate);
            return "redirect:/admin/candidates/register";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo registrar el candidato. Intente nuevamente.");
            redirectAttributes.addFlashAttribute("candidate", candidate);
            return "redirect:/admin/candidates/register";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("candidate", candidateService.findById(id));
        model.addAttribute("query", query);
        model.addAttribute("voters", candidateService.searchEligibleVoters(query));
        model.addAttribute("parties", candidateService.findAvailableParties());
        model.addAttribute("elections", candidateService.findAvailableElections());
        return "candidates/updatecandidate";
    }

    @PostMapping("/update/{id}")
    public String updateCandidate(
            @PathVariable Integer id,
            @ModelAttribute Candidate candidate,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            candidateService.update(id, candidate, photoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Candidato actualizado correctamente.");
            return "redirect:/admin/candidates/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/candidates/update/" + id;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo actualizar el candidato. Intente nuevamente.");
            return "redirect:/admin/candidates/update/" + id;
        }
    }

    @GetMapping("/status")
    public String showStatusPanel(Model model) {
        model.addAttribute("candidates", candidateService.findAll());
        return "candidates/disablecandidate";
    }

    @GetMapping("/disable/{id}")
    public String disableCandidate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            candidateService.disable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Candidato deshabilitado correctamente.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/candidates/status";
    }

    @GetMapping("/enable/{id}")
    public String enableCandidate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        candidateService.enable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Candidato habilitado correctamente.");
        return "redirect:/admin/candidates/status";
    }
}
