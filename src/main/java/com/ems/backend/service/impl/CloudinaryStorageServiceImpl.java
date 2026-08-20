package com.ems.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.ems.backend.service.CloudinaryStorageService;
import com.ems.backend.service.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryStorageServiceImpl implements CloudinaryStorageService {

    private static final String BASE_FOLDER = "EMS/parties";
    private static final String CANDIDATES_FOLDER = "EMS/candidates";

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadPartyLogo(MultipartFile file, Integer partyId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<String, Object> options = new HashMap<>();
        options.put("folder", BASE_FOLDER + "/" + partyId);
        options.put("public_id", "logo");
        options.put("overwrite", true);
        options.put("resource_type", "image");

        try {
            Map<?, ?> response = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = response.get("secure_url");
            if (secureUrl == null) {
                throw new BusinessRuleException("No se pudo obtener la URL del logo en Cloudinary.");
            }
            return secureUrl.toString();
        } catch (IOException | RuntimeException ex) {
            throw new BusinessRuleException("No se pudo subir la imagen del partido.");
        }
    }

    @Override
    public String uploadCandidatePhoto(MultipartFile file, Integer candidateId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<String, Object> options = new HashMap<>();
        options.put("folder", CANDIDATES_FOLDER + "/" + candidateId);
        options.put("public_id", "photo");
        options.put("overwrite", true);
        options.put("resource_type", "image");

        try {
            Map<?, ?> response = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = response.get("secure_url");
            if (secureUrl == null) {
                throw new BusinessRuleException("No se pudo obtener la URL de la foto del candidato en Cloudinary.");
            }
            return secureUrl.toString();
        } catch (IOException | RuntimeException ex) {
            throw new BusinessRuleException("No se pudo subir la foto del candidato.");
        }
    }
}
