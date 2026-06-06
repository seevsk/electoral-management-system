package com.ems.backend.controller;

import com.ems.backend.entity.Location;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.AccountRepository;
import com.ems.backend.repository.LocationRepository;
import com.ems.backend.service.VoterService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/voters")
public class VoterController {

    private final VoterService voterService;
    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;

    public VoterController(VoterService voterService,
                           AccountRepository accountRepository,
                           LocationRepository locationRepository) {
        this.voterService = voterService;
        this.accountRepository = accountRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public String redirectToList() {return "redirect:/admin/voters/list";}

    @GetMapping("/list")
    public String listVoters(Model model){
        List<Voter> voters = voterService.findAllActive();

        List<Map<String, Object>> enrichedVoters = voters.stream()
                .map(voter -> {
                    Map<String, Object> enriched = new HashMap<>();
                    enriched.put("id", voter.getId());
                    enriched.put("fullName", voter.getFullName());
                    enriched.put("firstSurname", voter.getFirstSurname());
                    enriched.put("status", voter.getStatus());
                    enriched.put("account", voter.getAccount());

                    Location location = locationRepository.findById(voter.getLocationCode()).orElse(null);
                    enriched.put("department", location != null ? location.getDepartment() : "N/A");
                    enriched.put("province", location != null ? location.getProvince() : "N/A");
                    enriched.put("district", location != null ? location.getDistrict() : "N/A");

                    return enriched;
                })
                .collect(Collectors.toList());

        model.addAttribute("voters", enrichedVoters);
        return "voters/listvoter";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("voter", new Voter());
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("locationsJson", buildLocationsJson());
        return "voters/registervoter";
    }

    @PostMapping("/register")
    public String registerVoter(
            @ModelAttribute Voter voter,
            RedirectAttributes redirectAttributes
    ){
        try {
            voterService.save(voter);
            redirectAttributes.addFlashAttribute("successMessage", "Votante registrado correctamente.");
            return "redirect:/admin/voters/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("voter", voter);
            return "redirect:/admin/voters/register";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model) {
        Voter voter = voterService.findById(id);
        Location currentLocation = locationRepository.findById(voter.getLocationCode()).orElse(null);

        model.addAttribute("currentDepartment", currentLocation != null ? currentLocation.getDepartment() : "");
        model.addAttribute("currentProvince", currentLocation != null ? currentLocation.getProvince() : "");
        model.addAttribute("currentLocationCode", voter.getLocationCode());
        model.addAttribute("voter", voter);
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("locationsJson", buildLocationsJson());

        return "voters/updatevoter";
    }

    @PostMapping("/update/{id}")
    public String updateVoter(
            @PathVariable Integer id,
            @ModelAttribute Voter voter,
            RedirectAttributes redirectAttributes
    ) {
        try {
            voterService.update(id, voter);
            redirectAttributes.addFlashAttribute("successMessage", "Votante actualizado correctamente.");
            return "redirect:/admin/voters/list";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/voters/update/" + id;
        }
    }

    @GetMapping("/status")
    public String showStatusPanel(Model model) {
        model.addAttribute("voters", voterService.findAll());
        return "voters/disablevoter";
    }

    @GetMapping("/disable/{id}")
    public String disableVoter(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        voterService.disable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Votante deshabilitado correctamente.");
        return "redirect:/admin/voters/status";
    }

    @GetMapping("/enable/{id}")
    public String enableVoter(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        voterService.enable(id);
        redirectAttributes.addFlashAttribute("successMessage", "Votante habilitado correctamente.");
        return "redirect:/admin/voters/status";
    }

    /** Construye la lista de ubicaciones en formato JSON para los selectores anidados del formulario */
    private List<Map<String, String>> buildLocationsJson() {
        return locationRepository.findAll().stream()
                .map(l -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("locationCode", l.getLocationCode());
                    map.put("department", l.getDepartment());
                    map.put("province", l.getProvince());
                    map.put("district", l.getDistrict());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
