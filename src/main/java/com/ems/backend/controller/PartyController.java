package com.ems.backend.controller;

import com.ems.backend.entity.Party;
import com.ems.backend.service.PartyService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequestMapping("/admin/parties")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    @ModelAttribute("party")
    public Party modelParty() {
        return new Party();
    }

    @GetMapping
    public String redirectToList() {
        return "redirect:/admin/parties/list";
    }

    @GetMapping("/list")
    public String listParties(Model model) {
        var parties = partyService.findAllActive();
        model.addAttribute("parties", parties);
        model.addAttribute("representativesByPartyId", partyService.findRepresentativesForDisplayElection(parties));
        return "parties/listparty";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "parties/registerparty";
    }

    @PostMapping("/register")
    public String registerParty(
            @ModelAttribute Party party,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            partyService.save(party, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Partido registrado correctamente.");
            return "redirect:/admin/parties/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("party", party);
            return "redirect:/admin/parties/register";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model) {
        Party party = partyService.findById(id);
        model.addAttribute("party", party);

        String representativeName = partyService.findRepresentativesForDisplayElection(Collections.singletonList(party))
                .getOrDefault(id, "SIN REPRESENTANTE");
        model.addAttribute("representativeName", representativeName);

        return "parties/updateparty";
    }

    @PostMapping("/update/{id}")
    public String updateParty(
            @PathVariable Integer id,
            @ModelAttribute Party party,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            partyService.update(id, party, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Partido actualizado correctamente.");
            return "redirect:/admin/parties/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/parties/update/" + id;
        }
    }

    @GetMapping("/status")
    public String showStatusPanel(Model model) {
        model.addAttribute("parties", partyService.findAll());
        return "parties/disableparty";
    }

    @GetMapping("/disable/{id}")
    public String disableParty(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        partyService.disable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Partido deshabilitado correctamente.");
        return "redirect:/admin/parties/status";
    }

    @GetMapping("/enable/{id}")
    public String enableParty(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        partyService.enable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Partido habilitado correctamente.");
        return "redirect:/admin/parties/status";
    }
}
