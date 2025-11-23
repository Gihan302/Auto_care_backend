package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.Message;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.MessageRequest;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.ConversationRepository;
import com.autocare.autocarebackend.repository.MessageRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Company Message Controller
 * Allows insurance and leasing companies to manage customer conversations
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/company")
public class CompanyMessageController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.location}")
    private String fileLocation;

    /**
     * GET /api/company/companies
     * Get list of companies for dropdown based on user's role
     */
    @GetMapping("/companies")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getCompanyList(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Determine company type from user's role
            boolean isInsurance = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE"));

            String companyType = isInsurance ? "insurance" : "leasing";

            // Get distinct company names for this type
            List<String> companies = conversationRepository
                    .findDistinctCompanyNamesByType(companyType);

            Map<String, Object> response = new HashMap<>();
            response.put("companyType", companyType);
            response.put("companies", companies);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving companies: " + e.getMessage()));
        }
    }

    /**
     * GET /api/company/conversations?companyName=XXX
     * Get all conversations for selected company
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getCompanyConversations(
            @RequestParam String companyName,
            Authentication authentication) {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Verify company exists and user has permission
            List<Conversation> conversations = conversationRepository
                    .findByCompanyNameOrderByUpdatedAtDesc(companyName);

            if (conversations.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Check if user has permission for this company type
            String companyType = conversations.get(0).getCompanyType();
            boolean hasPermission = userDetails.getAuthorities().stream()
                    .anyMatch(a ->
                            (companyType.equals("insurance") && a.getAuthority().equals("ROLE_INSURANCE")) ||
                                    (companyType.equals("leasing") && a.getAuthority().equals("ROLE_LEASING"))
                    );

            if (!hasPermission) {
                return ResponseEntity.status(403)
                        .body(new MessageResponse("No permission for this company type"));
            }

            // Build response with customer info
            List<ConversationResponse> responses = conversations.stream().map(conv -> {
                // Get customer name
                String userName = userRepository.findById(conv.getUserId())
                        .map(User::getUsername)
                        .orElse("Unknown User");

                // Get last message
                Message lastMessage = messageRepository
                        .findFirstByConversationIdOrderByCreatedAtDesc(conv.getId());

                // Count unread messages from customers
                Long unreadCount = messageRepository
                        .countByConversationIdAndSenderTypeAndIsRead(
                                conv.getId(), "user", false
                        );

                return new ConversationResponse(
                        conv.getId(),
                        userName, // Display customer name instead of company name
                        conv.getCompanyType(),
                        conv.getStatus(),
                        lastMessage != null ? lastMessage.getMessageText() : "No messages yet",
                        lastMessage != null ? lastMessage.getCreatedAt() : conv.getCreatedAt(),
                        unreadCount
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving conversations: " + e.getMessage()));
        }
    }

    /**
     * GET /api/company/conversations/{conversationId}/messages
     * Get all messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getMessagesForCompany(
            @PathVariable Long conversationId,
            Authentication authentication) {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Get conversation
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Conversation not found"));
            }

            Conversation conversation = conversationOpt.get();

            // Verify user has permission for this company type
            boolean hasPermission = userDetails.getAuthorities().stream()
                    .anyMatch(a ->
                            (conversation.getCompanyType().equals("insurance") &&
                                    a.getAuthority().equals("ROLE_INSURANCE")) ||
                                    (conversation.getCompanyType().equals("leasing") &&
                                            a.getAuthority().equals("ROLE_LEASING"))
                    );

            if (!hasPermission) {
                return ResponseEntity.status(403)
                        .body(new MessageResponse("No permission for this company type"));
            }

            // Get all messages
            List<Message> messages = messageRepository
                    .findByConversationIdOrderByCreatedAtAsc(conversationId);

            // Mark user messages as read
            messageRepository.markMessagesAsRead(conversationId, "user");

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving messages: " + e.getMessage()));
        }
    }

    /**
     * POST /api/company/conversations/{conversationId}/messages
     * Send a reply to customer
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> sendCompanyReply(
            @PathVariable Long conversationId,
            @RequestBody MessageRequest request,
            Authentication authentication) {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Get conversation
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Conversation not found"));
            }

            Conversation conversation = conversationOpt.get();

            // Verify permission
            boolean hasPermission = userDetails.getAuthorities().stream()
                    .anyMatch(a ->
                            (conversation.getCompanyType().equals("insurance") &&
                                    a.getAuthority().equals("ROLE_INSURANCE")) ||
                                    (conversation.getCompanyType().equals("leasing") &&
                                            a.getAuthority().equals("ROLE_LEASING"))
                    );

            if (!hasPermission) {
                return ResponseEntity.status(403)
                        .body(new MessageResponse("No permission for this company type"));
            }

            // Validate message
            if (request.getMessageText() == null || request.getMessageText().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Message text cannot be empty"));
            }

            // Create reply
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderType("company");
            message.setSenderId(null); // Keep as NULL per your schema
            message.setMessageText(request.getMessageText());
            message.setIsRead(false);

            Message saved = messageRepository.save(message);

            // Update timestamp
            conversationRepository.updateTimestamp(conversationId);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error sending reply: " + e.getMessage()));
        }
    }

    /**
     * POST /api/company/conversations/{conversationId}/messages/attachment
     * Reply with file attachment
     */
    @PostMapping("/conversations/{conversationId}/messages/attachment")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> sendCompanyReplyWithAttachment(
            @PathVariable Long conversationId,
            @RequestParam("messageText") String messageText,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Get conversation and verify permission
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Conversation not found"));
            }

            Conversation conversation = conversationOpt.get();

            boolean hasPermission = userDetails.getAuthorities().stream()
                    .anyMatch(a ->
                            (conversation.getCompanyType().equals("insurance") &&
                                    a.getAuthority().equals("ROLE_INSURANCE")) ||
                                    (conversation.getCompanyType().equals("leasing") &&
                                            a.getAuthority().equals("ROLE_LEASING"))
                    );

            if (!hasPermission) {
                return ResponseEntity.status(403)
                        .body(new MessageResponse("No permission"));
            }

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("File is empty"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("File size exceeds 5MB limit"));
            }

            // Save file
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String attachmentPath = fileLocation + "/attachments/" + fileName;

            File dir = new File(fileLocation + "/attachments");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file.transferTo(new File(attachmentPath));

            // Create message
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderType("company");
            message.setSenderId(null);
            message.setMessageText(messageText);
            message.setAttachmentUrl("/uploads/attachments/" + fileName);
            message.setAttachmentName(file.getOriginalFilename());
            message.setIsRead(false);

            Message saved = messageRepository.save(message);
            conversationRepository.updateTimestamp(conversationId);

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error sending reply: " + e.getMessage()));
        }
    }

    /**
     * GET /api/company/conversations/{conversationId}/user-details
     * Get customer details for a conversation
     */
    @GetMapping("/conversations/{conversationId}/user-details")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getUserDetails(
            @PathVariable Long conversationId,
            Authentication authentication) {

        try {
            // Get conversation
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Conversation not found"));
            }

            Conversation conversation = conversationOpt.get();
            Long userId = conversation.getUserId();

            // Get user details
            return userRepository.findById(userId)
                    .map(user -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("email", user.getUsername());
                        return ResponseEntity.ok(userInfo);
                    })
                    .orElse(ResponseEntity.status(404).body((Map<String, Object>) new MessageResponse("User not found")));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving user details: " + e.getMessage()));
        }
    }

    /**
     * GET /api/company/messages/unread-count?companyName=XXX
     * Get unread message count for selected company
     */
    @GetMapping("/messages/unread-count")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getUnreadCount(
            @RequestParam String companyName,
            Authentication authentication) {

        try {
            List<Conversation> conversations = conversationRepository
                    .findByCompanyName(companyName);

            List<Long> conversationIds = conversations.stream()
                    .map(Conversation::getId)
                    .collect(Collectors.toList());

            Long count = 0L;
            if (!conversationIds.isEmpty()) {
                count = messageRepository.countUnreadMessagesByConversationIds(
                        conversationIds, "user");
            }

            return ResponseEntity.ok(Map.of("count", count));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving unread count: " + e.getMessage()));
        }
    }

    /**
     * GET /api/company/stats?companyName=XXX
     * Get statistics for selected company
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_INSURANCE') or hasRole('ROLE_LEASING')")
    public ResponseEntity<?> getStats(
            @RequestParam String companyName,
            Authentication authentication) {

        try {
            // Get all conversations for company
            List<Conversation> conversations = conversationRepository
                    .findByCompanyNameOrderByUpdatedAtDesc(companyName);

            // Count unread
            long unreadCount = conversations.stream()
                    .filter(conv -> {
                        Long count = messageRepository.countByConversationIdAndSenderTypeAndIsRead(
                                conv.getId(), "user", false);
                        return count > 0;
                    })
                    .count();

            // Count active
            long activeCount = conversations.stream()
                    .filter(c -> "active".equals(c.getStatus()))
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("companyName", companyName);
            stats.put("totalConversations", conversations.size());
            stats.put("unreadConversations", unreadCount);
            stats.put("activeConversations", activeCount);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error retrieving stats: " + e.getMessage()));
        }
    }
}