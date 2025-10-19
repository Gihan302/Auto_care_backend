package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdRepository adRepository;

    @GetMapping("/advertisements/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingAdvertisements() {
        try {
            List<Advertisement> pendingAds = adRepository.getPendingAd();
            logger.info("📋 Retrieved {} pending advertisements", pendingAds.size());
            return ResponseEntity.ok(pendingAds);
        } catch (Exception e) {
            logger.error("❌ Error fetching pending advertisements: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching pending advertisements"));
        }
    }

    @GetMapping("/advertisements/approved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getApprovedAdvertisements() {
        try {
            List<Advertisement> approvedAds = adRepository.getConfirmAd();
            logger.info("📋 Retrieved {} approved advertisements", approvedAds.size());
            return ResponseEntity.ok(approvedAds);
        } catch (Exception e) {
            logger.error("❌ Error fetching approved advertisements: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching approved advertisements"));
        }
    }

    @GetMapping("/advertisements/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAdvertisements() {
        try {
            List<Advertisement> allAds = adRepository.findAll();
            logger.info("📋 Retrieved {} total advertisements", allAds.size());
            return ResponseEntity.ok(allAds);
        } catch (Exception e) {
            logger.error("❌ Error fetching all advertisements: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching advertisements"));
        }
    }

    @GetMapping("/advertisements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdvertisementById(@PathVariable Long id) {
        try {
            Optional<Advertisement> ad = adRepository.findById(id);
            if (ad.isEmpty()) {
                logger.warn("⚠️ Advertisement not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Advertisement not found"));
            }
            logger.info("✅ Retrieved advertisement with ID: {}", id);
            return ResponseEntity.ok(ad.get());
        } catch (Exception e) {
            logger.error("❌ Error fetching advertisement: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching advertisement"));
        }
    }

    @PutMapping("/advertisements/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveAdvertisement(@PathVariable Long id) {
        try {
            Optional<Advertisement> adOpt = adRepository.findById(id);
            if (adOpt.isEmpty()) {
                logger.warn("⚠️ Advertisement not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Advertisement not found"));
            }

            Advertisement ad = adOpt.get();
            ad.setFlag(1); // Approve the advertisement
            adRepository.save(ad);

            logger.info("✅ Advertisement approved - ID: {}, Title: {}", id, ad.getTitle());
            return ResponseEntity.ok(new MessageResponse("Advertisement approved successfully"));
        } catch (Exception e) {
            logger.error("❌ Error approving advertisement: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error approving advertisement"));
        }
    }

    @DeleteMapping("/advertisements/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectAdvertisement(@PathVariable Long id) {
        try {
            Optional<Advertisement> adOpt = adRepository.findById(id);
            if (adOpt.isEmpty()) {
                logger.warn("⚠️ Advertisement not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Advertisement not found"));
            }

            Advertisement ad = adOpt.get();
            String title = ad.getTitle();
            adRepository.deleteById(id);

            logger.info("🗑️ Advertisement rejected and deleted - ID: {}, Title: {}", id, title);
            return ResponseEntity.ok(new MessageResponse("Advertisement rejected successfully"));
        } catch (Exception e) {
            logger.error("❌ Error rejecting advertisement: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error rejecting advertisement"));
        }
    }

    @GetMapping("/advertisements/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingCount() {
        try {
            long count = adRepository.getPendingAd().size();
            logger.info("📊 Pending advertisements count: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("❌ Error counting pending advertisements: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error counting advertisements"));
        }
    }
}