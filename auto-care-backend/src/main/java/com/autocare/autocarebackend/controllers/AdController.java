package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.ReportAd;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.AdRequest;
import com.autocare.autocarebackend.payload.request.ReportAdRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.AdDetailsImpl;
import com.autocare.autocarebackend.security.services.ReportAdDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.security.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/advertisement")
public class AdController {

    private static final Logger logger = LoggerFactory.getLogger(AdController.class);

    @Autowired
    AdDetailsImpl adDetails;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdRepository adRepository;

    @Autowired
    ReportAdDetailsImpl reportAdDetails;

    @Autowired
    ImageUploadService imageUploadService;

    // Helper method to check if string is null or blank (Java 8 compatible)
    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @PostMapping("/postadd")
    public ResponseEntity<?> AddPost(@RequestBody AdRequest adRequest) {
        try {
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("🔍 Authentication check: " + (authentication != null ? "Found" : "NULL"));

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("❌ Authentication is null or not authenticated");
                return ResponseEntity.status(401).body(new MessageResponse("Authentication required"));
            }

            Object principal = authentication.getPrincipal();
            logger.info("🎯 Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "NULL"));

            if (!(principal instanceof UserDetailsImpl)) {
                logger.error("❌ Invalid principal type: " + (principal != null ? principal.getClass() : "null"));
                return ResponseEntity.status(401).body(new MessageResponse("Invalid authentication principal"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            logger.info("👤 Authenticated user: " + userDetails.getUsername());

            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("❌ User not found in database for ID: " + userDetails.getId());
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid user"));
            }

            logger.info("✅ User found: " + user.getUsername());

            Date datetime = new Date();
            String[] images = adRequest.getImages();

            // Validate and prepare images array
            String[] safeImages = new String[5];
            if (images != null && images.length > 0) {
                for (int i = 0; i < Math.min(5, images.length); i++) {
                    safeImages[i] = images[i];
                }
            }

            // Upload images to Cloudinary and collect URLs
            String image1Url = null, image2Url = null, image3Url = null, image4Url = null, image5Url = null;

            logger.info("🖼️ Processing " + (images != null ? images.length : 0) + " images");

            // FIXED: Upload each image correctly with proper null checks
            if (!isNullOrBlank(safeImages[0])) {
                logger.info("📤 Uploading image 1 to Cloudinary...");
                image1Url = imageUploadService.uploadBase64(safeImages[0]);
                logger.info("✅ Image 1 uploaded: " + image1Url);
            }
            if (!isNullOrBlank(safeImages[1])) {
                logger.info("📤 Uploading image 2 to Cloudinary...");
                image2Url = imageUploadService.uploadBase64(safeImages[1]);
                logger.info("✅ Image 2 uploaded: " + image2Url);
            }
            if (!isNullOrBlank(safeImages[2])) {
                logger.info("📤 Uploading image 3 to Cloudinary...");
                image3Url = imageUploadService.uploadBase64(safeImages[2]);
                logger.info("✅ Image 3 uploaded: " + image3Url);
            }
            if (!isNullOrBlank(safeImages[3])) {
                logger.info("📤 Uploading image 4 to Cloudinary...");
                image4Url = imageUploadService.uploadBase64(safeImages[3]);
                logger.info("✅ Image 4 uploaded: " + image4Url);
            }
            if (!isNullOrBlank(safeImages[4])) {
                logger.info("📤 Uploading image 5 to Cloudinary...");
                image5Url = imageUploadService.uploadBase64(safeImages[4]);
                logger.info("✅ Image 5 uploaded: " + image5Url);
            }

            Advertisement advertisement = new Advertisement(
                    adRequest.getName(),
                    adRequest.getT_number(),
                    adRequest.getEmail(),
                    adRequest.getLocation(),
                    adRequest.getTitle(),
                    adRequest.getPrice(),
                    adRequest.getV_type(),
                    adRequest.getManufacturer(),
                    adRequest.getModel(),
                    adRequest.getV_condition(),
                    adRequest.getM_year(),
                    adRequest.getR_year(),
                    adRequest.getMileage(),
                    adRequest.getE_capacity(),
                    adRequest.getTransmission(),
                    adRequest.getFuel_type(),
                    adRequest.getColour(),
                    adRequest.getDescription(),
                    image1Url, image2Url, image3Url, image4Url, image5Url,
                    datetime,
                    adRequest.getFlag(), // Fixed typo from getlStatus
                    adRequest.getlStatus(),
                    adRequest.getiStatus(),
                    user
            );

            Advertisement saved = adDetails.saveAdDetails(advertisement);
            logger.info("🎉 Advertisement saved successfully with ID: " + saved.getId());

            return ResponseEntity.ok(saved);

        } catch (IOException ex) {
            logger.error("💥 Cloudinary upload failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            logger.error("💥 Server error: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Server error: " + ex.getMessage()));
        }
    }

    /**
     * Returns the stored image URL (string) for a given advertisement id and image index.
     */
    @GetMapping(value = {"/getimage/{id}", "/getimage/{id}/{index}"})
    public ResponseEntity<?> getAddImage(@PathVariable("id") Long id,
                                         @PathVariable(name = "index", required = false) Integer index) {
        Optional<Advertisement> opt = adRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Advertisement ad = opt.get();

        // If index not provided, return first non-null image URL
        if (index == null) {
            if (ad.getImage1() != null) return ResponseEntity.ok(ad.getImage1());
            if (ad.getImage2() != null) return ResponseEntity.ok(ad.getImage2());
            if (ad.getImage3() != null) return ResponseEntity.ok(ad.getImage3());
            if (ad.getImage4() != null) return ResponseEntity.ok(ad.getImage4());
            if (ad.getImage5() != null) return ResponseEntity.ok(ad.getImage5());
            return ResponseEntity.noContent().build();
        }

        switch (index) {
            case 1:
                return ad.getImage1() != null ? ResponseEntity.ok(ad.getImage1()) : ResponseEntity.noContent().build();
            case 2:
                return ad.getImage2() != null ? ResponseEntity.ok(ad.getImage2()) : ResponseEntity.noContent().build();
            case 3:
                return ad.getImage3() != null ? ResponseEntity.ok(ad.getImage3()) : ResponseEntity.noContent().build();
            case 4:
                return ad.getImage4() != null ? ResponseEntity.ok(ad.getImage4()) : ResponseEntity.noContent().build();
            case 5:
                return ad.getImage5() != null ? ResponseEntity.ok(ad.getImage5()) : ResponseEntity.noContent().build();
            default:
                return ResponseEntity.badRequest().body(new MessageResponse("Index must be between 1 and 5"));
        }
    }

    @GetMapping("/getconfrimad")
    public List<Advertisement> getComnfirmAd() {
        return adRepository.getConfirmAd();
    }

    @GetMapping("/getnewad")
    public List<Advertisement> getPendingAd() {
        return adRepository.getPendingAd();
    }

    @GetMapping("/getAdById/{id}")
    public Optional<Advertisement> gedAdById(@PathVariable Long id) {
        return adRepository.findById(id);
    }

    @PostMapping("/reportad/{id}")
    public ResponseEntity<?> ReportAdpost(@PathVariable Long id, @RequestBody ReportAdRequest reportAdRequest) {
        Advertisement advertisement = adRepository.findById(id).orElse(null);
        if (advertisement == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid advertisement id"));
        }

        ReportAd reportAd = new ReportAd(reportAdRequest.getReason(), reportAdRequest.getF_name(),
                reportAdRequest.getL_name(), reportAdRequest.getT_number(), reportAdRequest.getEmail(),
                reportAdRequest.getMessage(), advertisement);
        reportAdDetails.saveReportAdDetails(reportAd);
        return ResponseEntity.ok(new MessageResponse("Report Advertisement successfully!"));
    }

    @GetMapping("/getAddsByCurrentUser")
    public List<Advertisement> GetAddsByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return List.of();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return List.of();
        return adRepository.findByUser(user);
    }

    @GetMapping("/countremainad")
    public Long CountremainAd() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return 0L;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.rcount(user);
    }

    @GetMapping("/countpostedad")
    public Long CountpostedAd() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return 0L;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.pcount(user);
    }
}