package com.ems.backend.service;

import com.ems.backend.entity.Account;

public interface AuthService {

    Account authenticateAdmin(String dni, String password);

    Account authenticateVoter(String dni, String password);
}
