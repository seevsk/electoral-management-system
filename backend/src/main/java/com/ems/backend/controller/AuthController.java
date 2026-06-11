package com.ems.backend.controller;

import com.ems.backend.config.JwtService;
import com.ems.backend.entity.Account;
import com.ems.backend.service.AuthService;
import com.ems.backend.service.exception.BusinessRuleException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/admin/login")
    public String loginAdmin(@RequestParam String dni,
                             @RequestParam String password,
                             HttpServletResponse response,
                             RedirectAttributes redirectAttrs) {
        try {
            Account account = authService.authenticateAdmin(dni, password);
            String token = jwtService.generateToken(account);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtService.createAuthCookie(token).toString());
            return "redirect:/admin/main";
        } catch (BusinessRuleException e) {
            redirectAttrs.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login/admin";
        }
    }

    @PostMapping("/voter/login")
    public String loginVoter(@RequestParam String dni,
                             @RequestParam String password,
                             HttpServletResponse response,
                             RedirectAttributes redirectAttrs) {
        try {
            Account account = authService.authenticateVoter(dni, password);
            String token = jwtService.generateToken(account);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtService.createAuthCookie(token).toString());
            return "redirect:/voter/portal";
        } catch (BusinessRuleException e) {
            redirectAttrs.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login/voter";
        }
    }
}
