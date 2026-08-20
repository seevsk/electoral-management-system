package com.ems.backend.controller;

import com.ems.backend.dto.ElectionDto;
import com.ems.backend.entity.Election;
import com.ems.backend.service.ElectionService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping
    public String redirectToList() {
        return "redirect:/admin/elections/list";
    }

    @GetMapping("/list")
    public String listElections(Model model) {
        model.addAttribute("elections", electionService.findAll());
        return "elections/listelection";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("electionDto", new ElectionDto());
        model.addAttribute("years", buildYearOptions());
        return "elections/registerelection";
    }

    @PostMapping("/register")
    public String registerElection(@Valid @ModelAttribute("electionDto") ElectionDto dto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.hasFieldErrors()
                    ? bindingResult.getFieldErrors().get(0).getDefaultMessage()
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("electionError", msg);
            return "redirect:/admin/elections/register";
        }
        try {
            electionService.save(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Elección programada correctamente.");
            return "redirect:/admin/elections/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("electionError", ex.getMessage());
            return "redirect:/admin/elections/register";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Election election = electionService.findById(id);
            if (!"P".equals(election.getStatus())) {
                redirectAttributes.addFlashAttribute("electionError",
                        "Solo se pueden editar elecciones en estado Pendiente.");
                return "redirect:/admin/elections/list";
            }
            ElectionDto dto = toDto(election);
            model.addAttribute("electionDto", dto);
            model.addAttribute("electionId", id);
            model.addAttribute("years", buildYearOptions());
            return "elections/updateelection";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("electionError", "Elección no encontrada.");
            return "redirect:/admin/elections/list";
        }
    }

    @PostMapping("/update/{id}")
    public String updateElection(@PathVariable Integer id,
                                 @Valid @ModelAttribute("electionDto") ElectionDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.hasFieldErrors()
                    ? bindingResult.getFieldErrors().get(0).getDefaultMessage()
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("electionError", msg);
            return "redirect:/admin/elections/update/" + id;
        }
        try {
            electionService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Elección actualizada correctamente.");
            return "redirect:/admin/elections/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("electionError", ex.getMessage());
            return "redirect:/admin/elections/update/" + id;
        }
    }

    private List<Integer> buildYearOptions() {
        int currentYear = Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear; y <= currentYear + 10; y++) {
            years.add(y);
        }
        return years;
    }

    private ElectionDto toDto(Election election) {
        ElectionDto dto = new ElectionDto();
        dto.setName(election.getName());
        dto.setElectionType(election.getElectionType());
        dto.setYear(election.getYear());
        dto.setStartDate(election.getStartDate());
        dto.setEndDate(election.getEndDate());
        dto.setStatus(election.getStatus());
        return dto;
    }
}
