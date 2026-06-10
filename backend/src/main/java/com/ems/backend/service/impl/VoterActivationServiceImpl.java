package com.ems.backend.service.impl;

import com.ems.backend.dto.VoterActivationDto;
import com.ems.backend.entity.Account;
import com.ems.backend.entity.Voter;
import com.ems.backend.repository.AccountRepository;
import com.ems.backend.repository.VoterRepository;
import com.ems.backend.service.VoterActivationService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoterActivationServiceImpl implements VoterActivationService {

    private final AccountRepository accountRepository;
    private final VoterRepository voterRepository;
    private final PasswordEncoder passwordEncoder;

    public VoterActivationServiceImpl(AccountRepository accountRepository,
                                      VoterRepository voterRepository,
                                      PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.voterRepository = voterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Account activate(VoterActivationDto dto) {
        Account account = accountRepository.findByDni(dto.getDni())
                .orElseThrow(() -> new BusinessRuleException("DNI no registrado en el padrón electoral"));

        if (!"user".equals(account.getRole()))
            throw new BusinessRuleException("Este DNI no corresponde a una cuenta de votante");

        if (account.getPasswordHash() != null)
            throw new BusinessRuleException("Esta cuenta ya fue activada. Ingresa con tu contraseña.");

        Voter voter = voterRepository.findByAccount_Id(account.getId())
                .orElseThrow(() -> new BusinessRuleException("Perfil de votante no encontrado en el padrón"));

        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new BusinessRuleException("Las contrasenas no coinciden");

        validateBiometrics(voter, dto);

        account.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        account.setIsActive(true);
        accountRepository.save(account);

        voter.setStatus("A");
        voterRepository.save(voter);

        return account;
    }

    private void validateBiometrics(Voter voter, VoterActivationDto dto) {
        if (!voter.getBirthDate().equals(dto.getBirthDate()))
            throw new BusinessRuleException("Fecha de nacimiento no coincide con el padrón");

        if (voter.getDniExpiryDate() == null || !voter.getDniExpiryDate().equals(dto.getDniExpiryDate()))
            throw new BusinessRuleException("Fecha de vencimiento de DNI no coincide");

        if (!voter.getLocationCode().trim().equals(dto.getLocationCode().trim()))
            throw new BusinessRuleException("Código de ubigeo no coincide con el padrón");
    }
}
