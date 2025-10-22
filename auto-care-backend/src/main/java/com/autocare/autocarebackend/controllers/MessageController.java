package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.Message;
import com.autocare.autocarebackend.models.UserInsuranceCompany;
import com.autocare.autocarebackend.models.UserLeasingCompany;
import com.autocare.autocarebackend.payload.request.ConversationRequest;
import com.autocare.autocarebackend.payload.request.MessageRequest;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.*;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserInsuranceCompanyRepository userInsuranceCompanyRepository;

    @Autowired
    private UserLeasingCompanyRepository userLeasingCompanyRepository;

    @Value("${upload.location}")
    private String fileLocation;

    /**
     * Get all conversations for the authenticated user
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<ConversationResponse>> getAllConversations(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userDetails.getId());

        List<ConversationResponse> responses = conversations.stream().map(conv -> {
            Message lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId());
            Long unreadCount = messageRepository.countByConversationIdAndSenderTypeAndIsRead(
                    conv.getId(), "company", false
            );

            return new ConversationResponse(
                    conv.getId(),
                    conv.getCompanyName(),
                    conv.getCompanyType(),
                    conv.getStatus(),
                    lastMessage != null ? lastMessage.getMessageText() : "",
                    lastMessage != null ? lastMessage.getCreatedAt() : conv.getCreatedAt(),
                    unreadCount
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Create a new conversation or get existing one
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createConversation(@RequestBody ConversationRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findByUserIdAndCompanyName(
                userDetails.getId(), request.getCompanyName()
        );

        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("conversationId", existing.get().getId()));
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setUserId(userDetails.getId());
        conversation.setCompanyType(request.getCompanyType());
        conversation.setCompanyName(request.getCompanyName());
        conversation.setStatus("active");

        Conversation saved = conversationRepository.save(conversation);
        return ResponseEntity.ok(Map.of("conversationId", saved.getId()));
    }

    /**
     * Get all messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Mark company messages as read
        messageRepository.markMessagesAsRead(conversationId, "company");

        return ResponseEntity.ok(messages);
    }

    /**
     * Send a message in a conversation
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody MessageRequest request,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        // Create message
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("user");
        message.setSenderId(userDetails.getId());
        message.setMessageText(request.getMessageText());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        // Update conversation timestamp
        conversationRepository.updateTimestamp(conversationId);

        return ResponseEntity.ok(saved);
    }

    /**
     * Send a message with file attachment
     */
    @PostMapping("/conversations/{conversationId}/messages/attachment")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> sendMessageWithAttachment(
            @PathVariable Long conversationId,
            @RequestParam("messageText") String messageText,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("File is empty"));
        }

        // Check file size (5MB limit)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(new MessageResponse("File size exceeds 5MB limit"));
        }

        // Save file
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String attachmentPath = fileLocation + "/attachments/" + fileName;

        try {
            File dir = new File(fileLocation + "/attachments");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(attachmentPath));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("Failed to upload file"));
        }

        // Create message
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("user");
        message.setSenderId(userDetails.getId());
        message.setMessageText(messageText);
        message.setAttachmentUrl("/uploads/attachments/" + fileName);
        message.setAttachmentName(file.getOriginalFilename());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        // Update conversation timestamp
        conversationRepository.updateTimestamp(conversationId);

        return ResponseEntity.ok(saved);
    }

    /**
     * Get user's insurance companies with active plans
     */
    @GetMapping("/companies/insurance")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<UserInsuranceCompany>> getInsuranceCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserInsuranceCompany> companies = userInsuranceCompanyRepository.findByUserIdAndPlanStatus(
                userDetails.getId(), "active"
        );
        return ResponseEntity.ok(companies);
    }

    /**
     * Get user's leasing companies with active leases
     */
    @GetMapping("/companies/leasing")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<UserLeasingCompany>> getLeasingCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserLeasingCompany> companies = userLeasingCompanyRepository.findByUserIdAndLeaseStatus(
                userDetails.getId(), "active"
        );
        return ResponseEntity.ok(companies);
    }

    /**
     * Get all companies (both insurance and leasing) for the user
     */
    @GetMapping("/companies/all")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<UserInsuranceCompany> insuranceCompanies = userInsuranceCompanyRepository.findByUserIdAndPlanStatus(
                userDetails.getId(), "active"
        );

        List<UserLeasingCompany> leasingCompanies = userLeasingCompanyRepository.findByUserIdAndLeaseStatus(
                userDetails.getId(), "active"
        );

        // Get unique company names
        Set<String> insuranceNames = insuranceCompanies.stream()
                .map(UserInsuranceCompany::getCompanyName)
                .collect(Collectors.toSet());

        Set<String> leasingNames = leasingCompanies.stream()
                .map(UserLeasingCompany::getCompanyName)
                .collect(Collectors.toSet());

        Map<String, Object> response = new HashMap<>();
        response.put("insuranceCompanies", insuranceNames);
        response.put("leasingCompanies", leasingNames);

        return ResponseEntity.ok(response);
    }

    /**
     * Get company details and active plans
     */
    @GetMapping("/companies/{companyName}/details")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getCompanyDetails(@PathVariable String companyName, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<UserInsuranceCompany> insurancePlans = userInsuranceCompanyRepository.findByUserIdAndCompanyName(
                userDetails.getId(), companyName
        );

        List<UserLeasingCompany> leasingPlans = userLeasingCompanyRepository.findByUserIdAndCompanyName(
                userDetails.getId(), companyName
        );

        Map<String, Object> response = new HashMap<>();
        response.put("companyName", companyName);
        response.put("insurancePlans", insurancePlans);
        response.put("leasingPlans", leasingPlans);

        // Determine company type
        String type = null;
        if (!insurancePlans.isEmpty()) {
            type = "insurance";
        } else if (!leasingPlans.isEmpty()) {
            type = "leasing";
        }
        response.put("companyType", type);

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread message count for all conversations
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Conversation> conversations = conversationRepository.findByUserId(userDetails.getId());
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());

        Long count = 0L;
        if (!conversationIds.isEmpty()) {
            count = messageRepository.countUnreadMessagesByConversationIds(conversationIds, "company");
        }

        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark all messages in a conversation as read
     */
    @PutMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> markConversationAsRead(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        messageRepository.markMessagesAsRead(conversationId, "company");

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Close a conversation
     */
    @PutMapping("/conversations/{conversationId}/close")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> closeConversation(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty() || !conversationOpt.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        Conversation conversation = conversationOpt.get();
        conversation.setStatus("closed");
        conversationRepository.save(conversation);

        return ResponseEntity.ok(new MessageResponse("Conversation closed successfully"));
    }

    /**
     * Delete a conversation and all its messages
     */
    @DeleteMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify user has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        conversationRepository.deleteById(conversationId);

        return ResponseEntity.ok(new MessageResponse("Conversation deleted successfully"));
    }
}