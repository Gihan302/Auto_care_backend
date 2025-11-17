package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.Message;
import com.autocare.autocarebackend.payload.request.CompanyConversationRequest;
import com.autocare.autocarebackend.payload.request.MessageRequest;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.ConversationRepository;
import com.autocare.autocarebackend.repository.MessageRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/company/messages")
public class CompanyMessageController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Value("${upload.location}")
    private String fileLocation;

    /**
     * Create a new conversation with a user
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> createConversation(@RequestBody CompanyConversationRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findByUserIdAndCompanyName(
                request.getUserId(), companyName
        );

        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("conversationId", existing.get().getId()));
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setUserId(request.getUserId());
        conversation.setCompanyType(userDetails.getAuthorities().stream().findFirst().get().getAuthority());
        conversation.setCompanyName(companyName);
        conversation.setStatus("active");

        Conversation savedConversation = conversationRepository.save(conversation);

        // Create initial message
        Message message = new Message();
        message.setConversationId(savedConversation.getId());
        message.setSenderType("company");
        message.setSenderId(userDetails.getId());
        message.setMessageText(request.getMessage());
        message.setIsRead(false);

        messageRepository.save(message);

        return ResponseEntity.ok(Map.of("conversationId", savedConversation.getId()));
    }

    /**
     * Get all conversations for the authenticated company
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<List<ConversationResponse>> getAllConversations(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        List<Conversation> conversations = conversationRepository.findByCompanyNameOrderByUpdatedAtDesc(companyName);

        List<ConversationResponse> responses = conversations.stream().map(conv -> {
            Message lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId());
            Long unreadCount = messageRepository.countByConversationIdAndSenderTypeAndIsRead(
                    conv.getId(), "user", false
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
     * Get all messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        // Verify company has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getCompanyName().equals(companyName)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Mark user messages as read
        messageRepository.markMessagesAsRead(conversationId, "user");

        return ResponseEntity.ok(messages);
    }

    /**
     * Send a message in a conversation
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody MessageRequest request,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        // Verify company has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getCompanyName().equals(companyName)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        // Create message
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("company");
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
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> sendMessageWithAttachment(
            @PathVariable Long conversationId,
            @RequestParam("messageText") String messageText,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        // Verify company has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getCompanyName().equals(companyName)) {
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
        message.setSenderType("company");
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
     * Get unread message count for all conversations
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        List<Conversation> conversations = conversationRepository.findByCompanyName(companyName);
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());

        Long count = 0L;
        if (!conversationIds.isEmpty()) {
            count = messageRepository.countUnreadMessagesByConversationIds(conversationIds, "user");
        }

        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark all messages in a conversation as read
     */
    @PutMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> markConversationAsRead(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String companyName = userDetails.getcName();

        // Verify company has access to this conversation
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || !conversation.get().getCompanyName().equals(companyName)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }

        messageRepository.markMessagesAsRead(conversationId, "user");

        return ResponseEntity.ok(Map.of("success", true));
    }
}