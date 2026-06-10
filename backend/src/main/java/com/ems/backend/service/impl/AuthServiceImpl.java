package com.ems.backend.service.impl;

import com.ems.backend.entity.Account;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.AccountRepository;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.service.AuthService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AccountRepository accountRepository,
                           VoterRepository voterRepository,
                           PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.voterRepository = voterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Account authenticateAdmin(String dni, String password) {
        Account account = accountRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessRuleException("Credenciales incorrectas"));

        if (!"admin".equals(account.getRole()))
            throw new BusinessRuleException("Credenciales incorrectas");

        if (!account.getIsActive())
            throw new BusinessRuleException("La cuenta de administrador está desactivada");

        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(password, account.getPasswordHash()))
            throw new BusinessRuleException("Credenciales incorrectas");

        return account;
    }

    @Override
    public Account authenticateVoter(String dni, String password) {
        Account account = accountRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessRuleException("Credenciales incorrectas"));

        if (!"user".equals(account.getRole()))
            throw new BusinessRuleException("Credenciales incorrectas");

        if (account.getPasswordHash() == null
                || !account.getIsActive()
                || !passwordEncoder.matches(password, account.getPasswordHash()))
            throw new BusinessRuleException("Credenciales incorrectas");

        Voter voter = voterRepository.findByAccount_Id(account.getId())
                .orElseThrow(() -> new BusinessRuleException("Credenciales incorrectas"));

        if (!"A".equals(voter.getStatus()))
            throw new BusinessRuleException("Credenciales incorrectas");

        return account;
    }
}
