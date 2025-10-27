package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.CarReview;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.ReviewRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.payload.response.ReviewResponse;
import com.autocare.autocarebackend.repository.ReviewRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.ImageUploadService;
import com.autocare.autocarebackend.security.services.ReviewDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewDetailsImpl reviewDetails;

    @Autowired
    private ImageUploadService imageUploadService;

    // Helper method to check if string is null or blank
    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Submit a new review (requires authentication)
     * Review goes to admin for approval (flag = 0)
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@Valid @RequestBody ReviewRequest request) {
        try {
            // Get authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("🔍 Authentication check: " + (authentication != null ? "Found" : "NULL"));

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("❌ Authentication is null or not authenticated");
                return ResponseEntity.status(401).body(new MessageResponse("Authentication required"));
            }

            Object principal = authentication.getPrincipal();
            logger.info("🎯 Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "NULL"));

            if (!(principal instanceof UserDetailsImpl)) {
                logger.error("❌ Invalid principal type");
                return ResponseEntity.status(401).body(new MessageResponse("Invalid authentication"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            logger.info("👤 Authenticated user: " + userDetails.getUsername());

            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("❌ User not found in database");
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid user"));
            }

            logger.info("✅ User found: " + user.getUsername());

            // Validate and prepare images array
            String[] images = request.getImages();
            String[] safeImages = new String[8];
            if (images != null && images.length > 0) {
                for (int i = 0; i < Math.min(8, images.length); i++) {
                    safeImages[i] = images[i];
                }
            }

            // Upload images to Cloudinary
            String img1 = null, img2 = null, img3 = null, img4 = null;
            String img5 = null, img6 = null, img7 = null, img8 = null;

            logger.info("🖼️ Processing " + (images != null ? images.length : 0) + " images");

            if (!isNullOrBlank(safeImages[0])) {
                logger.info("📤 Uploading image 1 to Cloudinary...");
                img1 = imageUploadService.uploadBase64(safeImages[0]);
                logger.info("✅ Image 1 uploaded: " + img1);
            }
            if (!isNullOrBlank(safeImages[1])) {
                logger.info("📤 Uploading image 2 to Cloudinary...");
                img2 = imageUploadService.uploadBase64(safeImages[1]);
                logger.info("✅ Image 2 uploaded");
            }
            if (!isNullOrBlank(safeImages[2])) {
                img3 = imageUploadService.uploadBase64(safeImages[2]);
            }
            if (!isNullOrBlank(safeImages[3])) {
                img4 = imageUploadService.uploadBase64(safeImages[3]);
            }
            if (!isNullOrBlank(safeImages[4])) {
                img5 = imageUploadService.uploadBase64(safeImages[4]);
            }
            if (!isNullOrBlank(safeImages[5])) {
                img6 = imageUploadService.uploadBase64(safeImages[5]);
            }
            if (!isNullOrBlank(safeImages[6])) {
                img7 = imageUploadService.uploadBase64(safeImages[6]);
            }
            if (!isNullOrBlank(safeImages[7])) {
                img8 = imageUploadService.uploadBase64(safeImages[7]);
            }

            // Create review with flag = 0 (pending approval)
            CarReview review = new CarReview(
                    user,
                    request.getCarMake(),
                    request.getCarModel(),
                    request.getYear(),
                    request.getVariant(),
                    request.getPurchaseType(),
                    request.getOverallRating(),
                    request.getPerformance(),
                    request.getComfort(),
                    request.getFuelEconomy(),
                    request.getSafety(),
                    request.getValue(),
                    request.getTitle(),
                    request.getReviewText(),
                    request.getPros(),
                    request.getCons(),
                    img1, img2, img3, img4, img5, img6, img7, img8,
                    request.getMileage(),
                    request.getOwnershipDuration(),
                    request.getPurchaseDate(),
                    request.getPurchasePrice(),
                    request.getVerifiedOwner() != null ? request.getVerifiedOwner() : false,
                    request.getMaintenanceExperience(),
                    request.getFinalThoughts(),
                    0  // flag = 0 (pending admin approval)
            );

            CarReview saved = reviewDetails.saveReview(review);
            logger.info("🎉 Review submitted successfully with ID: {} (Pending admin approval)", saved.getId());

            return ResponseEntity.ok()
                    .body(new MessageResponse("Review submitted successfully! It will be visible after admin approval."));

        } catch (IOException ex) {
            logger.error("💥 Cloudinary upload failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            logger.error("💥 Server error: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Server error: " + ex.getMessage()));
        }
    }

    /**
     * Get all approved reviews with filtering and sorting
     */
    @GetMapping
    public ResponseEntity<?> getAllReviews(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rating,
            @RequestParam(defaultValue = "recent") String sort) {

        try {
            logger.info("📋 Fetching reviews - search: {}, rating: {}, sort: {}", search, rating, sort);

            List<CarReview> reviews;

            // Apply search filter
            if (search != null && !search.isEmpty()) {
                reviews = reviewRepository.searchReviews(search);
            } else if (rating != null && !rating.equals("all")) {
                // Filter by minimum rating
                Double minRating = Double.parseDouble(rating);
                reviews = reviewRepository.findByMinimumRating(minRating);
            } else {
                // Get all approved reviews
                reviews = reviewRepository.getApprovedReviews();
            }

            // Apply sorting
            switch (sort) {
                case "helpful":
                    reviews = reviewRepository.findMostHelpfulReviews();
                    break;
                case "rating-high":
                    reviews = reviewRepository.findHighestRatedReviews();
                    break;
                case "rating-low":
                    reviews = reviewRepository.findLowestRatedReviews();
                    break;
                case "recent":
                default:
                    // Already sorted by createdAt DESC in queries
                    break;
            }

            // Convert to response DTOs
            List<ReviewResponse> response = reviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("✅ Returning {} reviews", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching reviews"));
        }
    }

    /**
     * Get review statistics for dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getReviewStats() {
        try {
            Long totalReviews = reviewRepository.countApprovedReviews();
            Double averageRating = reviewRepository.getAverageRating();
            Long verifiedOwners = reviewRepository.countVerifiedOwners();
            Long thisMonth = reviewRepository.countReviewsThisMonth();

            // Calculate percentage of verified owners
            int verifiedPercentage = totalReviews > 0 ?
                    (int) ((verifiedOwners.doubleValue() / totalReviews.doubleValue()) * 100) : 0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReviews", totalReviews);
            stats.put("averageRating", averageRating != null ? averageRating : 0.0);
            stats.put("verifiedOwners", verifiedPercentage);
            stats.put("thisMonth", thisMonth);

            logger.info("📊 Stats - Total: {}, Avg Rating: {}, Verified: {}%, This Month: {}",
                    totalReviews, averageRating, verifiedPercentage, thisMonth);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("❌ Error fetching stats: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching statistics"));
        }
    }

    /**
     * Get single review by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            Optional<CarReview> opt = reviewRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            CarReview review = opt.get();

            // Increment view count
            reviewDetails.incrementViewCount(id);

            ReviewResponse response = new ReviewResponse(review);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching review"));
        }
    }

    /**
     * Mark review as helpful
     */
    @PostMapping("/{id}/helpful")
    public ResponseEntity<?> markHelpful(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.incrementHelpfulCount(id);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            logger.info("👍 Review {} marked as helpful (count: {})", id, review.getHelpfulCount());
            return ResponseEntity.ok(new MessageResponse("Marked as helpful"));

        } catch (Exception e) {
            logger.error("❌ Error marking helpful: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error marking helpful"));
        }
    }

    /**
     * Get reviews by current authenticated user
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                return ResponseEntity.status(401).body(new MessageResponse("Authentication required"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }

            List<CarReview> reviews = reviewRepository.findByUser(user);
            List<ReviewResponse> response = reviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("✅ User {} has {} reviews", user.getUsername(), reviews.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching user reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching reviews"));
        }
    }

    /**
     * Count reviews by current user
     */
    @GetMapping("/my-reviews/count")
    public ResponseEntity<?> countMyReviews() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                return ResponseEntity.status(401).body(new MessageResponse("Authentication required"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) return ResponseEntity.ok(0L);

            Long count = reviewRepository.countByUser(user);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("❌ Error counting reviews: {}", e.getMessage());
            return ResponseEntity.ok(0L);
        }
    }

    /**
     * Get pending reviews (Admin only - you should add role check)
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingReviews() {
        try {
            List<CarReview> reviews = reviewRepository.getPendingReviews();
            List<ReviewResponse> response = reviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("📋 Returning {} pending reviews for admin", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching pending reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching pending reviews"));
        }
    }

    /**
     * Approve review (Admin only - you should add role check)
     */
    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.approveReview(id);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            logger.info("✅ Review {} approved by admin", id);
            return ResponseEntity.ok(new MessageResponse("Review approved successfully"));

        } catch (Exception e) {
            logger.error("❌ Error approving review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error approving review"));
        }
    }

    /**
     * Reject review (Admin only - you should add role check)
     */
    @PutMapping("/admin/reject/{id}")
    public ResponseEntity<?> rejectReview(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.rejectReview(id);  // ✅ FIXED
            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            logger.info("❌ Review {} rejected by admin", id);
            return ResponseEntity.ok(new MessageResponse("Review rejected"));

        } catch (Exception e) {
            logger.error("❌ Error rejecting review: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error rejecting review"));
        }
    }

    /**
     * Get reviews for specific car
     */
    @GetMapping("/car/{make}/{model}")
    public ResponseEntity<?> getReviewsForCar(
            @PathVariable String make,
            @PathVariable String model) {
        try {
            List<CarReview> reviews = reviewRepository.findByCarMakeAndModelApproved(make, model);
            List<ReviewResponse> response = reviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            logger.info("✅ Found {} reviews for {} {}", response.size(), make, model);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching reviews for car: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching reviews"));
        }
    }

    /**
     * Get verified owner reviews only
     */
    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedReviews() {
        try {
            List<CarReview> reviews = reviewRepository.findVerifiedOwnerReviews();
            List<ReviewResponse> response = reviews.stream()
                    .map(ReviewResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error fetching verified reviews: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching reviews"));
        }
    }

    /**
     * Get unique car makes
     */
    @GetMapping("/makes")
    public ResponseEntity<?> getCarMakes() {
        try {
            List<String> makes = reviewRepository.findDistinctCarMakes();
            return ResponseEntity.ok(makes);
        } catch (Exception e) {
            logger.error("❌ Error fetching car makes: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching car makes"));
        }
    }
}