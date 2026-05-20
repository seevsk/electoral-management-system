package com.ems.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryStorageService {

    String uploadPartyLogo(MultipartFile file, Integer partyId);
}
