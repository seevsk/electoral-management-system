package com.ems.backend.service;

import com.ems.backend.dto.VoterActivationDto;

public interface VoterPasswordRecoveryService {
    void verifyEligibility(String dni);
    void resetPassword(VoterActivationDto dto);
}
