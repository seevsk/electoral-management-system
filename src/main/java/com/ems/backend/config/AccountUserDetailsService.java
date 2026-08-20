package com.ems.backend.config;

import com.ems.backend.entity.Account;
import com.ems.backend.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Account account = accountRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + dni));

        String passwordHash = account.getPasswordHash() != null ? account.getPasswordHash() : "";

        return User.withUsername(account.getDni())
                .password(passwordHash)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().toUpperCase())))
                .accountLocked(!account.getIsActive())
                .build();
    }
}
