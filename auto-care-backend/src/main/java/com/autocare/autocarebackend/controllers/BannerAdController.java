package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.BannerAdvertisement;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.BannerAdRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.BannerAdRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.security.services.ImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/banner-ads")
public class BannerAdController {

    private static final Logger logger = LoggerFactory.getLogger(BannerAdController.class);

    @Autowired
    private BannerAdRepository bannerAdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Create a new banner advertisement (Admin only)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBannerAd(@Valid @RequestBody BannerAdRequest request) {
        try {
            logger.info("📢 Creating new banner advertisement");

            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                return ResponseEntity.status(401)
                        .body(new MessageResponse("Authentication required"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Upload image to Cloudinary
            logger.info("📤 Uploading banner image to Cloudinary...");
            String imageUrl = imageUploadService.uploadBase64(request.getImage());
            logger.info("✅ Banner image uploaded: {}", imageUrl);

            // Create banner advertisement
            BannerAdvertisement bannerAd = new BannerAdvertisement(
                    imageUrl,
                    request.getTargetUrl(),
                    request.getTitle(),
                    request.getDescription(),
                    user
            );

            if (request.getDisplayOrder() != null) {
                bannerAd.setDisplayOrder(request.getDisplayOrder());
            }

            BannerAdvertisement saved = bannerAdRepository.save(bannerAd);
            logger.info("🎉 Banner advertisement created successfully with ID: {}", saved.getId());

            return ResponseEntity.ok()
                    .body(new MessageResponse("Banner advertisement created successfully"));

        } catch (IOException ex) {
            logger.error("💥 Image upload failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            logger.error("💥 Server error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Server error: " + ex.getMessage()));
        }
    }

    /**
     * Get all banner advertisements (Admin panel)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBannerAds() {
        try {
            List<BannerAdvertisement> bannerAds = bannerAdRepository.findAllOrderByCreatedAtDesc();
            logger.info("✅ Retrieved {} banner advertisements", bannerAds.size());
            return ResponseEntity.ok(bannerAds);
        } catch (Exception ex) {
            logger.error("❌ Error fetching banner ads: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching banner ads"));
        }
    }

    /**
     * Get all active banner advertisements (Public endpoint for frontend display)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBannerAds() {
        try {
            List<BannerAdvertisement> bannerAds = bannerAdRepository.findAllActive();
            logger.info("✅ Retrieved {} active banner advertisements", bannerAds.size());
            return ResponseEntity.ok(bannerAds);
        } catch (Exception ex) {
            logger.error("❌ Error fetching active banner ads: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching banner ads"));
        }
    }

    /**
     * Get banner advertisement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBannerAdById(@PathVariable Long id) {
        try {
            Optional<BannerAdvertisement> bannerAd = bannerAdRepository.findById(id);
            if (bannerAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(bannerAd.get());
        } catch (Exception ex) {
            logger.error("❌ Error fetching banner ad: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching banner ad"));
        }
    }

    /**
     * Update banner advertisement
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBannerAd(
            @PathVariable Long id,
            @Valid @RequestBody BannerAdRequest request) {
        try {
            Optional<BannerAdvertisement> existingAd = bannerAdRepository.findById(id);
            if (existingAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BannerAdvertisement bannerAd = existingAd.get();

            // Update image if new image is provided
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                logger.info("📤 Uploading new banner image to Cloudinary...");
                String imageUrl = imageUploadService.uploadBase64(request.getImage());
                bannerAd.setImageUrl(imageUrl);
                logger.info("✅ Banner image updated: {}", imageUrl);
            }

            // Update other fields
            if (request.getTargetUrl() != null) {
                bannerAd.setTargetUrl(request.getTargetUrl());
            }
            if (request.getTitle() != null) {
                bannerAd.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                bannerAd.setDescription(request.getDescription());
            }
            if (request.getDisplayOrder() != null) {
                bannerAd.setDisplayOrder(request.getDisplayOrder());
            }

            bannerAdRepository.save(bannerAd);
            logger.info("✅ Banner advertisement updated successfully: {}", id);

            return ResponseEntity.ok()
                    .body(new MessageResponse("Banner advertisement updated successfully"));

        } catch (IOException ex) {
            logger.error("💥 Image upload failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            logger.error("💥 Error updating banner ad: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error updating banner ad"));
        }
    }

    /**
     * Toggle banner advertisement active status
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleActiveStatus(@PathVariable Long id) {
        try {
            Optional<BannerAdvertisement> existingAd = bannerAdRepository.findById(id);
            if (existingAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BannerAdvertisement bannerAd = existingAd.get();
            bannerAd.setIsActive(!bannerAd.getIsActive());
            bannerAdRepository.save(bannerAd);

            String status = bannerAd.getIsActive() ? "activated" : "deactivated";
            logger.info("✅ Banner advertisement {} successfully: {}", status, id);

            return ResponseEntity.ok()
                    .body(new MessageResponse("Banner advertisement " + status + " successfully"));

        } catch (Exception ex) {
            logger.error("💥 Error toggling banner ad status: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error toggling banner ad status"));
        }
    }

    /**
     * Delete banner advertisement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBannerAd(@PathVariable Long id) {
        try {
            Optional<BannerAdvertisement> existingAd = bannerAdRepository.findById(id);
            if (existingAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            bannerAdRepository.deleteById(id);
            logger.info("🗑️ Banner advertisement deleted successfully: {}", id);

            return ResponseEntity.ok()
                    .body(new MessageResponse("Banner advertisement deleted successfully"));

        } catch (Exception ex) {
            logger.error("💥 Error deleting banner ad: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error deleting banner ad"));
        }
    }

    /**
     * Track banner ad click
     */
    @PostMapping("/{id}/click")
    public ResponseEntity<?> trackClick(@PathVariable Long id) {
        try {
            Optional<BannerAdvertisement> existingAd = bannerAdRepository.findById(id);
            if (existingAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BannerAdvertisement bannerAd = existingAd.get();
            bannerAd.setClicksCount(bannerAd.getClicksCount() + 1);
            bannerAdRepository.save(bannerAd);

            logger.info("👆 Click tracked for banner ad: {}", id);
            return ResponseEntity.ok()
                    .body(new MessageResponse("Click tracked successfully"));

        } catch (Exception ex) {
            logger.error("💥 Error tracking click: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error tracking click"));
        }
    }

    /**
     * Track banner ad impression
     */
    @PostMapping("/{id}/impression")
    public ResponseEntity<?> trackImpression(@PathVariable Long id) {
        try {
            Optional<BannerAdvertisement> existingAd = bannerAdRepository.findById(id);
            if (existingAd.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BannerAdvertisement bannerAd = existingAd.get();
            bannerAd.setImpressionsCount(bannerAd.getImpressionsCount() + 1);
            bannerAdRepository.save(bannerAd);

            return ResponseEntity.ok()
                    .body(new MessageResponse("Impression tracked successfully"));

        } catch (Exception ex) {
            logger.error("💥 Error tracking impression: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error tracking impression"));
        }
    }

    /**
     * Get banner ad statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBannerAdStats() {
        try {
            List<BannerAdvertisement> allAds = bannerAdRepository.findAll();
            Long activeCount = bannerAdRepository.countActive();

            int totalClicks = allAds.stream()
                    .mapToInt(BannerAdvertisement::getClicksCount)
                    .sum();

            int totalImpressions = allAds.stream()
                    .mapToInt(BannerAdvertisement::getImpressionsCount)
                    .sum();

            double avgCTR = totalImpressions > 0
                    ? (double) totalClicks / totalImpressions * 100
                    : 0.0;

            var stats = new java.util.HashMap<String, Object>();
            stats.put("totalAds", allAds.size());
            stats.put("activeAds", activeCount);
            stats.put("totalClicks", totalClicks);
            stats.put("totalImpressions", totalImpressions);
            stats.put("averageCTR", String.format("%.2f%%", avgCTR));

            return ResponseEntity.ok(stats);

        } catch (Exception ex) {
            logger.error("💥 Error fetching stats: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching statistics"));
        }
    }
}