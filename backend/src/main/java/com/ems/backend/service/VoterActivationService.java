package com.ems.backend.service;

import com.ems.backend.dto.VoterActivationDto;
import com.ems.backend.entity.Account;

public interface VoterActivationService {
    Account activate(VoterActivationDto dto);
}
