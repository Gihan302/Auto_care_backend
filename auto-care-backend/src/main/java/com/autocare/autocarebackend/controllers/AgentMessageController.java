package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Conversation;
import com.autocare.autocarebackend.models.Message;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.ConversationRepository;
import com.autocare.autocarebackend.repository.MessageRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent/messages")
public class AgentMessageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> getAgentConversations(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> agent = userRepository.findById(userDetails.getId());

        if (agent.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Agent not found!"));
        }

        List<Conversation> conversations = conversationRepository.findByAgentId(agent.get().getId());

        List<ConversationResponse> response = conversations.stream()
                .map(conversation -> {
                    User participant = userRepository.findById(conversation.getUserId()).orElse(null);
                    Objects.requireNonNull(participant, "Participant user not found for conversation: " + conversation.getId());

                    Message lastMessage = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId());
                    long unreadCount = messageRepository.countByConversationIdAndSenderIdNotAndIsRead(conversation.getId(), agent.get().getId(), false);

                    return new ConversationResponse(
                            conversation.getId(),
                            participant.getFname() + " " + participant.getLname(), // participantName
                            null, // companyName (no company for agent-user conv)
                            "agent", // companyType for agent-user conversations
                            "active", // status for agent-user conversations
                            lastMessage != null ? lastMessage.getMessageText() : "No messages yet",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .sorted(Comparator.comparing(ConversationResponse::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> getAgentUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> agent = userRepository.findById(userDetails.getId());

        if (agent.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Agent not found!"));
        }

        List<Conversation> agentConversations = conversationRepository.findByAgentId(agent.get().getId());
        long totalUnreadCount = 0;
        for (Conversation conversation : agentConversations) {
            totalUnreadCount += messageRepository.countByConversationIdAndSenderIdNotAndIsRead(conversation.getId(), agent.get().getId(), false);
        }

        return ResponseEntity.ok(Map.of("count", totalUnreadCount));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);

        if (conversation.isEmpty() || !conversation.get().getAgentId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access Denied"));
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        // Mark messages from the user as read
        messageRepository.markMessagesAsRead(conversationId, "user");

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations/{conversationId}/user-details")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> getUserDetailsForConversation(@PathVariable Long conversationId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);

        if (conversation.isEmpty() || !conversation.get().getAgentId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Access Denied"));
        }

        Optional<User> participant = userRepository.findById(conversation.get().getUserId());
        if (participant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(participant.get());
    }
}
