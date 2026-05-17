package com.ems.backend.controller;

import com.ems.backend.entity.Party;
import com.ems.backend.service.PartyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("parties", partyService.findAllActive());
        return "parties/listparty";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "parties/registerparty";
    }

    @PostMapping("/register")
    public String registerParty(@ModelAttribute Party party, RedirectAttributes redirectAttributes) {
        partyService.save(party);
        redirectAttributes.addFlashAttribute("successMessage", "Partido registrado correctamente.");
        return "redirect:/admin/parties/list";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model) {
        model.addAttribute("party", partyService.findById(id));
        return "parties/updateparty";
    }

    @PostMapping("/update/{id}")
    public String updateParty(
            @PathVariable Integer id,
            @ModelAttribute Party party,
            RedirectAttributes redirectAttributes
    ) {
        partyService.update(id, party);
        redirectAttributes.addFlashAttribute("successMessage", "Partido actualizado correctamente.");
        return "redirect:/admin/parties/list";
    }

    @GetMapping("/disable")
    public String showDisablePanel(Model model) {
        model.addAttribute("parties", partyService.findAll());
        return "parties/disableparty";
    }

    @GetMapping("/disable/{id}")
    public String disableParty(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        partyService.disable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Partido deshabilitado correctamente.");
        return "redirect:/admin/parties/disable";
    }

    @GetMapping("/enable/{id}")
    public String enableParty(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        partyService.enable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Partido habilitado correctamente.");
        return "redirect:/admin/parties/disable";
    }
}
