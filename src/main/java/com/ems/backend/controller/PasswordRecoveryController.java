package com.ems.backend.controller;

import com.ems.backend.dto.VoterActivationDto;
import com.ems.backend.service.VoterPasswordRecoveryService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordRecoveryController {

    private final VoterPasswordRecoveryService voterPasswordRecoveryService;

    public PasswordRecoveryController(VoterPasswordRecoveryService voterPasswordRecoveryService) {
        this.voterPasswordRecoveryService = voterPasswordRecoveryService;
    }

    @GetMapping("/recovery")
    public String showRecoveryForm() {
        return "auth/recovery";
    }

    @PostMapping("/auth/voter/recovery/lookup")
    public String lookupVoter(@RequestParam(required = false) String dni,
                              RedirectAttributes redirectAttrs) {
        if (dni == null || dni.isBlank() || !dni.matches("\\d{8}")) {
            redirectAttrs.addFlashAttribute("recoveryError", "Ingresa un DNI valido de 8 digitos");
            return "redirect:/recovery";
        }
        try {
            voterPasswordRecoveryService.verifyEligibility(dni);
            redirectAttrs.addFlashAttribute("recoveryDni", dni);
            return "redirect:/recovery/verify";
        } catch (BusinessRuleException e) {
            redirectAttrs.addFlashAttribute("recoveryError", e.getMessage());
            return "redirect:/recovery";
        }
    }

    @GetMapping("/recovery/verify")
    public String showVerifyForm(Model model) {
        String recoveryDni = (String) model.asMap().get("recoveryDni");
        if (recoveryDni == null || recoveryDni.isBlank()) {
            return "redirect:/recovery";
        }
        return "auth/recovery-verify";
    }

    @PostMapping("/auth/voter/recovery/reset")
    public String resetPassword(@Valid @ModelAttribute VoterActivationDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.hasFieldErrors()
                    ? bindingResult.getFieldErrors().get(0).getDefaultMessage()
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttrs.addFlashAttribute("recoveryError", msg);
            redirectAttrs.addFlashAttribute("recoveryDni", dto.getDni());
            return "redirect:/recovery/verify";
        }
        try {
            voterPasswordRecoveryService.resetPassword(dto);
            redirectAttrs.addFlashAttribute("loginSuccess",
                    "Contrasena actualizada correctamente. Ingresa con tu nueva contrasena.");
            return "redirect:/login/voter";
        } catch (BusinessRuleException e) {
            redirectAttrs.addFlashAttribute("recoveryError", e.getMessage());
            redirectAttrs.addFlashAttribute("recoveryDni", dto.getDni());
            return "redirect:/recovery/verify";
        }
    }
}
