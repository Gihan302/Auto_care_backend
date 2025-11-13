package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.*;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.payload.response.ReviewResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.ReviewRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.EmailService;
import com.autocare.autocarebackend.security.services.ReviewDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(
        origins = "*",
        maxAge = 3600,
        allowedHeaders = {"Authorization", "Content-Type", "X-User-Id"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReviewDetailsImpl reviewDetails;

    // ============================================
    // USER MANAGEMENT ENDPOINTS
    // ============================================

    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            logger.info("📋 Retrieved {} total users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error fetching all users: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching users"));
        }
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (!userOptional.isPresent()) {
                logger.warn("⚠️ User not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("fname", user.getFname());
            userMap.put("lname", user.getLname());
            userMap.put("email", user.getUsername());
            userMap.put("companyName", user.getcName());
            userMap.put("regNum", user.getRegNum());
            userMap.put("address", user.getAddress());
            userMap.put("telephone", user.getTnumber());
            userMap.put("nic", user.getNic());
            userMap.put("registerDate", user.getDate());
            userMap.put("accountStatus", user.getAccountStatus());
            userMap.put("approvedBy", user.getApprovedBy());
            userMap.put("approvedAt", user.getApprovedAt());
            userMap.put("rejectionReason", user.getRejectionReason());
            userMap.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().toString())
                    .collect(Collectors.toList()));

            logger.info("✅ Retrieved user with ID: {}", id);
            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            logger.error("❌ Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching user"));
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, String> updateRequest) {

        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (!userOptional.isPresent()) {
                logger.warn("⚠️ User not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();

            if (updateRequest.containsKey("fname")) {
                user.setFname(updateRequest.get("fname"));
            }
            if (updateRequest.containsKey("lname")) {
                user.setLname(updateRequest.get("lname"));
            }
            if (updateRequest.containsKey("companyName")) {
                user.setcName(updateRequest.get("companyName"));
            }
            if (updateRequest.containsKey("telephone")) {
                user.setTnumber(updateRequest.get("telephone"));
            }
            if (updateRequest.containsKey("address")) {
                user.setAddress(updateRequest.get("address"));
            }

            userRepository.save(user);

            logger.info("✅ User updated - ID: {}, Email: {}", id, user.getUsername());
            return ResponseEntity.ok(new MessageResponse("User updated successfully"));
        } catch (Exception e) {
            logger.error("❌ Error updating user: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error updating user"));
        }
    }

    // Replace your updateUserStatus method with this fixed version

    // In your AdminController - improve the adminId handling
    // ============================================
// COMPLETE FIXED updateUserStatus METHOD
// Replace the entire method in AdminController.java
// ============================================

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> statusRequest) {

        logger.info("🔄 Processing status update request for user ID: {}", id);
        logger.info("📦 Request body: {}", statusRequest);

        try {
            // Validate request
            if (statusRequest == null || statusRequest.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Request body is required"));
            }

            // Find user
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                logger.warn("⚠️ User not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();
            logger.info("✅ User found: {} {} ({})", user.getFname(), user.getLname(), user.getUsername());

            // Extract and validate status
            Object statusObj = statusRequest.get("status");
            if (statusObj == null) {
                logger.error("❌ Status is null");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Status is required"));
            }

            String statusString = statusObj.toString().trim().toUpperCase();
            logger.info("📋 Status string: '{}'", statusString);

            // Convert to enum
            EAccountStatus newStatus;
            try {
                newStatus = EAccountStatus.valueOf(statusString);
                logger.info("✅ Status converted to enum: {}", newStatus);
            } catch (IllegalArgumentException e) {
                logger.error("❌ Invalid status: '{}'", statusString);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid status. Must be PENDING, APPROVED, or REJECTED"));
            }

            // Check if already set
            if (user.getAccountStatus() == newStatus) {
                logger.info("ℹ️ User already has status: {}", newStatus);
                return ResponseEntity.ok(new MessageResponse("User already has this status"));
            }

            // Update user
            user.setAccountStatus(newStatus);
            user.setApprovedAt(new Date());

            // Handle admin ID
            Object adminIdObj = statusRequest.get("adminId");
            Long adminId = 1L;
            if (adminIdObj != null) {
                try {
                    if (adminIdObj instanceof Number) {
                        adminId = ((Number) adminIdObj).longValue();
                    } else {
                        adminId = Long.parseLong(adminIdObj.toString().trim());
                    }
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ Invalid adminId, using default: 1");
                }
            }
            user.setApprovedBy(adminId);
            logger.info("✅ Admin ID: {}", adminId);

            // Handle rejection reason
            if (newStatus == EAccountStatus.REJECTED) {
                Object reasonObj = statusRequest.get("reason");
                String reason = (reasonObj != null && !reasonObj.toString().trim().isEmpty())
                        ? reasonObj.toString().trim()
                        : "No specific reason provided";
                user.setRejectionReason(reason);
                logger.info("📝 Rejection reason: {}", reason);
            } else {
                user.setRejectionReason(null);
            }

            // Save to database
            logger.info("💾 Saving user...");
            userRepository.save(user);
            logger.info("✅ User saved successfully");

            // Send email
            boolean emailSent = false;
            String emailError = null;

            try {
                if (newStatus == EAccountStatus.APPROVED) {
                    logger.info("📧 Sending approval email...");
                    emailService.sendApprovalEmail(user);
                    emailSent = true;
                    logger.info("✅ Approval email sent");
                } else if (newStatus == EAccountStatus.REJECTED) {
                    logger.info("📧 Sending rejection email...");
                    emailService.sendRejectionEmail(user);
                    emailSent = true;
                    logger.info("✅ Rejection email sent");
                }
            } catch (Exception emailException) {
                emailError = emailException.getMessage();
                logger.error("❌ Email failed: {}", emailError);
                // Don't fail the request
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User status updated successfully");
            response.put("userId", id);
            response.put("newStatus", newStatus.toString());
            response.put("emailSent", emailSent);

            if (emailError != null) {
                response.put("emailError", emailError);
            }

            logger.info("✅ STATUS UPDATE COMPLETED");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ CRITICAL ERROR:");
            logger.error("❌ Type: {}", e.getClass().getName());
            logger.error("❌ Message: {}", e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(new MessageResponse("Internal server error: " + e.getMessage()));
        }
    }

// ============================================
// OPTIONAL: Add this debug endpoint temporarily
// Remove it after fixing the issue
// ============================================

    @PostMapping("/users/debug-status-request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugStatusRequest(@RequestBody Map<String, Object> request) {
        logger.info("🔧 ===== DEBUG STATUS REQUEST =====");
        logger.info("🔧 Request object type: {}", request.getClass().getName());
        logger.info("🔧 Request size: {}", request.size());
        logger.info("🔧 Request keys: {}", request.keySet());

        Map<String, Object> debugInfo = new HashMap<>();

        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String type = value != null ? value.getClass().getName() : "null";

            logger.info("🔧 Key: '{}' | Value: '{}' | Type: {}", key, value, type);

            Map<String, String> fieldInfo = new HashMap<>();
            fieldInfo.put("value", String.valueOf(value));
            fieldInfo.put("type", type);
            debugInfo.put(key, fieldInfo);
        }

        logger.info("🔧 ===== END DEBUG =====");

        return ResponseEntity.ok(debugInfo);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (!userOptional.isPresent()) {
                logger.warn("⚠️ User not found with ID: {}", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();

            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);

            if (isAdmin) {
                logger.warn("⚠️ Cannot delete admin user - ID: {}", id);
                return ResponseEntity.status(403)
                        .body(new MessageResponse("Cannot delete admin users"));
            }

            String email = user.getUsername();

            // Soft delete
            user.setAccountStatus(EAccountStatus.REJECTED);
            userRepository.save(user);

            logger.info("🗑️ User deleted - ID: {}, Email: {}", id, email);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        } catch (Exception e) {
            logger.error("❌ Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error deleting user"));
        }
    }

    @GetMapping("/users/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStatistics() {
        try {
            List<User> allUsers = userRepository.findAll();

            long totalUsers = allUsers.size();
            long pendingUsers = allUsers.stream()
                    .filter(user -> user.getAccountStatus() == EAccountStatus.PENDING)
                    .count();
            long approvedUsers = allUsers.stream()
                    .filter(user -> user.getAccountStatus() == EAccountStatus.APPROVED)
                    .count();
            long rejectedUsers = allUsers.stream()
                    .filter(user -> user.getAccountStatus() == EAccountStatus.REJECTED)
                    .count();

            long insuranceCompanies = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName() == ERole.ROLE_ICOMPANY))
                    .count();

            long leasingCompanies = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName() == ERole.ROLE_LCOMPANY))
                    .count();

            long regularUsers = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName() == ERole.ROLE_USER))
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalUsers);
            stats.put("pending", pendingUsers);
            stats.put("approved", approvedUsers);
            stats.put("rejected", rejectedUsers);
            stats.put("insuranceCompanies", insuranceCompanies);
            stats.put("leasingCompanies", leasingCompanies);
            stats.put("regularUsers", regularUsers);

            logger.info("📊 User Statistics - Total: {}, Pending: {}, Approved: {}, Rejected: {}",
                    totalUsers, pendingUsers, approvedUsers, rejectedUsers);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Error fetching user statistics: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching statistics"));
        }
    }

    @GetMapping("/users/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleName) {
        try {
            ERole roleEnum;
            try {
                roleEnum = ERole.valueOf("ROLE_" + roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("❌ Invalid role name: {}", roleName);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid role name"));
            }

            List<User> allUsers = userRepository.findAll();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName() == roleEnum))
                    .collect(Collectors.toList());

            logger.info("✅ Retrieved {} users with role: {}", filteredUsers.size(), roleName);
            return ResponseEntity.ok(filteredUsers);
        } catch (Exception e) {
            logger.error("❌ Error fetching users by role: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching users"));
        }
    }

    @GetMapping("/users/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status) {
        try {
            EAccountStatus accountStatus;
            try {
                accountStatus = EAccountStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("❌ Invalid status: {}", status);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid status value"));
            }

            List<User> users = userRepository.findAllByAccountStatus(accountStatus);

            logger.info("✅ Retrieved {} users with status: {}", users.size(), status);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error fetching users by status: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching users"));
        }
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            List<User> allUsers = userRepository.findAll();

            String searchQuery = query.toLowerCase();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user ->
                            (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchQuery)) ||
                                    (user.getFname() != null && user.getFname().toLowerCase().contains(searchQuery)) ||
                                    (user.getLname() != null && user.getLname().toLowerCase().contains(searchQuery)) ||
                                    (user.getcName() != null && user.getcName().toLowerCase().contains(searchQuery)) ||
                                    (user.getNic() != null && user.getNic().toLowerCase().contains(searchQuery)))
                    .collect(Collectors.toList());

            logger.info("🔍 Search query: '{}' - Found {} users", query, filteredUsers.size());
            return ResponseEntity.ok(filteredUsers);
        } catch (Exception e) {
            logger.error("❌ Error searching users: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error searching users"));
        }
    }

    @PostMapping("/users/bulk-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkApproveUsers(
            @Valid @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");

            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User IDs are required"));
            }

            if (adminId == null) {
                adminId = 1L;
            }

            int approvedCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long userId : userIds) {
                try {
                    Optional<User> userOptional = userRepository.findById(userId);

                    if (!userOptional.isPresent()) {
                        errors.add("User " + userId + " not found");
                        continue;
                    }

                    User user = userOptional.get();

                    if (user.getAccountStatus() != EAccountStatus.PENDING) {
                        errors.add("User " + userId + " is not in pending status");
                        continue;
                    }

                    user.setAccountStatus(EAccountStatus.APPROVED);
                    user.setApprovedBy(adminId);
                    user.setApprovedAt(new Date());
                    userRepository.save(user);

                    try {
                        emailService.sendApprovalEmail(user);
                    } catch (Exception e) {
                        logger.warn("⚠️ Failed to send email to user {}", userId);
                    }

                    approvedCount++;
                } catch (Exception e) {
                    errors.add("Error processing user " + userId + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", approvedCount + " users approved successfully");
            response.put("approvedCount", approvedCount);
            response.put("totalRequested", userIds.size());

            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            logger.info("✅ Bulk approval completed - Approved: {}/{}", approvedCount, userIds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in bulk approval: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error performing bulk approval"));
        }
    }

    @PostMapping("/users/bulk-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkRejectUsers(
            @Valid @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            String reason = (String) request.get("reason");

            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User IDs are required"));
            }

            if (adminId == null) {
                adminId = 1L;
            }

            int rejectedCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long userId : userIds) {
                try {
                    Optional<User> userOptional = userRepository.findById(userId);

                    if (!userOptional.isPresent()) {
                        errors.add("User " + userId + " not found");
                        continue;
                    }

                    User user = userOptional.get();

                    if (user.getAccountStatus() != EAccountStatus.PENDING) {
                        errors.add("User " + userId + " is not in pending status");
                        continue;
                    }

                    user.setAccountStatus(EAccountStatus.REJECTED);
                    user.setApprovedBy(adminId);
                    user.setApprovedAt(new Date());
                    user.setRejectionReason(reason);
                    userRepository.save(user);

                    try {
                        emailService.sendRejectionEmail(user);
                    } catch (Exception e) {
                        logger.warn("⚠️ Failed to send email to user {}", userId);
                    }

                    rejectedCount++;
                } catch (Exception e) {
                    errors.add("Error processing user " + userId + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", rejectedCount + " users rejected successfully");
            response.put("rejectedCount", rejectedCount);
            response.put("totalRequested", userIds.size());

            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            logger.info("❌ Bulk rejection completed - Rejected: {}/{}", rejectedCount, userIds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error in bulk rejection: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error performing bulk rejection"));
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
            ad.setFlag(1);
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
    public ResponseEntity<?> getPendingAdvertisementsCount() {
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

    @PutMapping("/reviews/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReview(@PathVariable Long id) {
        try {
            CarReview review = reviewDetails.rejectReview(id);
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

            Map<String, Object> stats = new HashMap<>();
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