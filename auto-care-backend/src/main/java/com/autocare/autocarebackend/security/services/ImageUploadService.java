package com.autocare.autocarebackend.security.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Accepts a Base64 string in the form "data:image/...;base64,xxxxx" OR plain base64 payload.
     * Returns the secure_url from Cloudinary.
     */
    public String uploadBase64(String base64Data) throws IOException {
        if (base64Data == null || base64Data.isBlank()) return null;

        // If data URL prefix exists, strip it
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",", 2)[1];
        }

        byte[] bytes = Base64.getDecoder().decode(base64Data);

        Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", "auto-care/ads",
                "resource_type", "image",
                "overwrite", true
        ));

        Object secureUrl = result.get("secure_url");
        return secureUrl != null ? secureUrl.toString() : null;
    }
}
