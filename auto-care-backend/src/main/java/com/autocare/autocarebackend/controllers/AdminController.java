package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.CarReview;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.payload.response.ReviewResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.ReviewRepository;
import com.autocare.autocarebackend.security.services.ReviewDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewDetailsImpl reviewDetails;

    @Autowired
    private com.autocare.autocarebackend.repository.UserRepository userRepository;

    // ============================================
    // USER MANAGEMENT ENDPOINTS
    // ============================================

    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<com.autocare.autocarebackend.models.User> users = userRepository.findAll();
            logger.info("📋 Retrieved {} total users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error fetching all users: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching users"));
        }
    }

    // ============================================
    // ADVERTISEMENT MANAGEMENT ENDPOINTS
    // ============================================

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

    // ============================================
    // REVIEW MANAGEMENT ENDPOINTS
    // ============================================

    /**
     * Get all reviews (pending, approved, rejected) for admin
     */
    @GetMapping("/reviews/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReviews() {
        try {
            List<CarReview> allReviews = reviewRepository.findAll();
            List<ReviewResponse> response = allReviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("📋 Retrieved {} total reviews for admin", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching all reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching reviews"));
        }
    }

    /**
     * Get pending reviews for admin approval
     */
    @GetMapping("/reviews/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingReviews() {
        try {
            List<CarReview> pendingReviews = reviewRepository.getPendingReviews();
            List<ReviewResponse> response = pendingReviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("📋 Retrieved {} pending reviews", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching pending reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching pending reviews"));
        }
    }

    /**
     * Get approved reviews
     */
    @GetMapping("/reviews/approved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getApprovedReviews() {
        try {
            List<CarReview> approvedReviews = reviewRepository.getApprovedReviews();
            List<ReviewResponse> response = approvedReviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("📋 Retrieved {} approved reviews", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching approved reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching approved reviews"));
        }
    }

    /**
     * Get rejected reviews
     */
    @GetMapping("/reviews/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRejectedReviews() {
        try {
            List<CarReview> rejectedReviews = reviewRepository.getRejectedReviews();
            List<ReviewResponse> response = rejectedReviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("📋 Retrieved {} rejected reviews", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching rejected reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching rejected reviews"));
        }
    }

    /**
     * Get single review by ID (admin)
     */
    @GetMapping("/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            Optional<CarReview> reviewOpt = reviewRepository.findById(id);
            if (reviewOpt.isEmpty()) {
                logger.warn("⚠️ Review not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Review not found"));
            }

            ReviewResponse response = new ReviewResponse(reviewOpt.get());
            logger.info("✅ Retrieved review with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error fetching review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching review"));
        }
    }

    /**
     * Approve review (Admin only)
     */
    @PutMapping("/reviews/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.approveReview(id);
            if (review == null) {
                logger.warn("⚠️ Review not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Review not found"));
            }

            logger.info("✅ Review approved - ID: {}, Title: {}", id, review.getTitle());
            return ResponseEntity.ok(new MessageResponse("Review approved successfully"));
        } catch (Exception e) {
            logger.error("❌ Error approving review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error approving review"));
        }
    }

    /**
     * Reject review (Admin only) - Sets flag to -1
     */
    @PutMapping("/reviews/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReview(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.rejectReview(id);  // ✅ Now returns CarReview
            if (review == null) {
                logger.warn("⚠️ Review not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Review not found"));
            }

            logger.info("❌ Review rejected - ID: {}, Title: {}", id, review.getTitle());
            return ResponseEntity.ok(new MessageResponse("Review rejected successfully"));
        } catch (Exception e) {
            logger.error("❌ Error rejecting review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error rejecting review"));
        }
    }

    /**
     * Delete review permanently (Admin only)
     */
    @DeleteMapping("/reviews/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            Optional<CarReview> reviewOpt = reviewRepository.findById(id);
            if (reviewOpt.isEmpty()) {
                logger.warn("⚠️ Review not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Review not found"));
            }

            CarReview review = reviewOpt.get();
            String title = review.getTitle();
            reviewRepository.deleteById(id);

            logger.info("🗑️ Review deleted permanently - ID: {}, Title: {}", id, title);
            return ResponseEntity.ok(new MessageResponse("Review deleted successfully"));
        } catch (Exception e) {
            logger.error("❌ Error deleting review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error deleting review"));
        }
    }

    /**
     * Get review statistics for admin dashboard
     */
    @GetMapping("/reviews/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewStats() {
        try {
            Long totalReviews = reviewRepository.count();
            Long pendingReviews = reviewRepository.getPendingReviews().size() > 0 ?
                    Long.valueOf(reviewRepository.getPendingReviews().size()) : 0L;
            Long approvedReviews = reviewRepository.countApprovedReviews();
            Long rejectedReviews = reviewRepository.getRejectedReviews().size() > 0 ?
                    Long.valueOf(reviewRepository.getRejectedReviews().size()) : 0L;

            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("total", totalReviews);
            stats.put("pending", pendingReviews);
            stats.put("approved", approvedReviews);
            stats.put("rejected", rejectedReviews);

            logger.info("📊 Review Stats - Total: {}, Pending: {}, Approved: {}, Rejected: {}",
                    totalReviews, pendingReviews, approvedReviews, rejectedReviews);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Error fetching review stats: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching review statistics"));
        }
    }

    /**
     * Get pending reviews count
     */
    @GetMapping("/reviews/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingReviewsCount() {
        try {
            long count = reviewRepository.getPendingReviews().size();
            logger.info("📊 Pending reviews count: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("❌ Error counting pending reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error counting reviews"));
        }
    }
}