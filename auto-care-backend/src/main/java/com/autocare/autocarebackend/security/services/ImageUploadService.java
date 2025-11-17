package com.autocare.autocarebackend.security.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);
    private Cloudinary cloudinary;

    public ImageUploadService() {
        this.cloudinary = null;
    }

    @Autowired(required = false)
    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Accepts a Base64 string in the form "data:image/...;base64,xxxxx" OR plain base64 payload.
     * Returns the secure_url from Cloudinary.
     */
    public String uploadBase64(String base64Data) throws IOException {
        if (cloudinary == null) {
            logger.warn("⚠️ Cloudinary is not configured. Image upload is disabled.");
            return "https://res.cloudinary.com/demo/image/upload/v1629978997/placeholder.jpg";
        }

        if (base64Data == null || base64Data.isBlank()) {
            logger.warn("⚠️ Base64 data is null or empty");
            return null;
        }

        try {
            logger.info("📤 Starting Cloudinary upload...");

            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",", 2)[1];
                logger.info("🔧 Stripped data URL prefix");
            }

            byte[] bytes = Base64.getDecoder().decode(base64Data);
            logger.info("✅ Base64 decoded, size: " + bytes.length + " bytes");

            Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", "auto_care",             // match folder name exactly here
                    "resource_type", "image",
                    "overwrite", true,
                    "quality", "auto:good",
                    "fetch_format", "auto",
                    "transformation", "w_800,h_600,c_limit"  // Correct string-based transformation
            ));

            Object secureUrl = result.get("secure_url");
            String imageUrl = secureUrl != null ? secureUrl.toString() : null;

            if (imageUrl != null) {
                logger.info("✅ Image uploaded successfully: " + imageUrl);
            } else {
                logger.error("❌ Upload succeeded but no secure_url returned");
            }

            return imageUrl;

        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid Base64 format: " + e.getMessage());
            throw new IOException("Invalid Base64 image format", e);
        } catch (Exception e) {
            logger.error("💥 Cloudinary upload failed: " + e.getMessage(), e);

            if (e.getMessage().contains("Invalid Signature") || e.getMessage().contains("signature")) {
                throw new IOException("Cloudinary authentication failed. Please check your API credentials.", e);
            } else if (e.getMessage().contains("Invalid image")) {
                throw new IOException("Invalid image format. Please ensure the image is properly encoded.", e);
            } else {
                throw new IOException("Image upload failed: " + e.getMessage(), e);
            }
        }
    }
}
