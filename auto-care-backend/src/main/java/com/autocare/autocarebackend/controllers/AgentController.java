package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.*;
import com.autocare.autocarebackend.payload.request.AdRequest;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.payload.request.PackagePurchaseRequest;
import com.autocare.autocarebackend.repository.*;
import com.autocare.autocarebackend.security.services.AdService;
import com.autocare.autocarebackend.security.services.NormalUserImpl;
import com.autocare.autocarebackend.security.services.PackagesPurchaseDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdRepository adRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    NormalUserImpl normalUser;

    @Autowired
    PackagesRepository packagesRepository;

    @Autowired
    PackagesPurchaseDetailsImpl packagesPurchaseDetails;

    @Autowired
    PackagesPurchaseRepository packagesPurchaseRepository;

    @Autowired
    AdService adService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/messages/conversations")
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


    @GetMapping("/messages/unread-count")
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

    @GetMapping("/messages/conversations/{conversationId}/messages")
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

    @GetMapping("/messages/conversations/{conversationId}/user-details")
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


    @PostMapping("/create-ad")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> createAd(@RequestBody AdRequest adRequest, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            adService.createAdvertisement(adRequest, userDetails);
            return ResponseEntity.ok(new MessageResponse("Advertisement created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/getad")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public List<Advertisement> getAgentAd(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        System.out.println(user.getId());
        return adRepository.findAllByUser(user);
    }
    @PostMapping("/packagepurchase/{pkgId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> packagePurchase(@PathVariable Long pkgId, @RequestBody PackagePurchaseRequest packagePurchaseRequest, Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        Packages packages = packagesRepository.findById(pkgId).get();
        Date date = new Date();

        PackagePurchase packagePurchase = new PackagePurchase(
                date,
                0,
                packages.getMaxAd(),
                packages,
                user
        );

        packagesPurchaseDetails.savePackagesPurchase(packagePurchase);

        return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
    }

    @PutMapping("/packageupdate/{pkgId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?>packageUpdate(@PathVariable Long pkgId,Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        Packages packages = packagesRepository.findById(pkgId).get();
        Optional <PackagePurchase> packagePurchase = packagesPurchaseRepository.findAllByUser(user);
        System.out.println(packagePurchase.get().getMaxAdCount());
        if(packagePurchase.get().getMaxAdCount() == packagePurchase.get().getCurrentAdCount()){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Package is over!"));
        }else{
            Integer currendAdCount = packagePurchase.get().getCurrentAdCount() + 1;
            System.out.println(currendAdCount);
            packagePurchase.get().setCurrentAdCount(currendAdCount);
            packagesPurchaseRepository.save(packagePurchase.get());
            return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
        }

    }

    @PutMapping("/update-ad/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> updateAd(@PathVariable Long adId, @RequestBody AdRequest adRequest, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to update this advertisement!"));
            }

            adService.updateAdvertisement(adId, adRequest);

            return ResponseEntity.ok(new MessageResponse("Advertisement updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-ad/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> deleteAd(@PathVariable Long adId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to delete this advertisement!"));
            }

            adRepository.delete(advertisement);

            return ResponseEntity.ok(new MessageResponse("Advertisement deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/mark-as-sold/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> markAdAsSold(@PathVariable Long adId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to update this advertisement!"));
            }

            advertisement.setFlag(2); // 2 for sold
            adRepository.save(advertisement);

            return ResponseEntity.ok(new MessageResponse("Advertisement marked as sold successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}