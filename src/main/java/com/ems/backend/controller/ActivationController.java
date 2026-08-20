package com.ems.backend.controller;

import com.ems.backend.config.JwtService;
import com.ems.backend.dto.VoterActivationDto;
import com.ems.backend.entity.Account;
import com.ems.backend.service.VoterActivationService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ActivationController {

    private final VoterActivationService voterActivationService;
    private final JwtService jwtService;

    public ActivationController(VoterActivationService voterActivationService,
                                JwtService jwtService) {
        this.voterActivationService = voterActivationService;
        this.jwtService = jwtService;
    }

    @GetMapping("/activation")
    public String showActivationForm() {
        return "auth/activation";
    }

    @PostMapping("/auth/voter/activation")
    public String activateVoter(@Valid @ModelAttribute VoterActivationDto dto,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.hasFieldErrors()
                    ? bindingResult.getFieldErrors().get(0).getDefaultMessage()
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttrs.addFlashAttribute("activationError", msg);
            return "redirect:/activation";
        }
        try {
            Account account = voterActivationService.activate(dto);
            String token = jwtService.generateToken(account);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtService.createAuthCookie(token).toString());
            return "redirect:/participation";
        } catch (BusinessRuleException e) {
            redirectAttrs.addFlashAttribute("activationError", e.getMessage());
            return "redirect:/activation";
        }
    }
}
