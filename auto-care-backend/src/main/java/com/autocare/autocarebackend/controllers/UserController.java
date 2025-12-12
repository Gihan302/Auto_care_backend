package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.*;
import com.autocare.autocarebackend.payload.request.ConversationRequest;
import com.autocare.autocarebackend.payload.request.MessageRequest;
import com.autocare.autocarebackend.payload.request.SignupRequest;
import com.autocare.autocarebackend.payload.response.ConversationResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.*;
import com.autocare.autocarebackend.security.services.NormalUserImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    NormalUserImpl normalUser;

    @Autowired
    LeasingPlanRepository leasingPlanRepository;

    @Autowired
    InsurancePlanRepository insurancePlanRepository;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserInsuranceCompanyRepository userInsuranceCompanyRepository;

    @Autowired
    UserLeasingCompanyRepository userLeasingCompanyRepository;

    @Value("${upload.location}")
    private String fileLocation;

    // ==================== EXISTING ENDPOINTS ====================

    @GetMapping("/getallusers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/total")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public long getTotalUsers() {
        return userRepository.count();
    }

    @PutMapping("/editprofile")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> editNormalUserEditProfile(@RequestBody SignupRequest signupRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        user.setFname(signupRequest.getFname());
        user.setLname(signupRequest.getLname());
        user.setTnumber(signupRequest.getTnumber());
        user.setAddress(signupRequest.getAddress());
        normalUser.editNormalUserEditProfile(user);
        return ResponseEntity.ok(new MessageResponse("Account update successfully!"));
    }

    @PutMapping("/changepassword/{password}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> editPasswordProfile(@RequestBody SignupRequest signupRequest, @PathVariable String password, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        if (encoder.matches(password, (user.getPassword()))) {
            user.setPassword(encoder.encode(signupRequest.getPassword()));
            normalUser.editNormalUserEditProfile(user);
            return ResponseEntity.ok(new MessageResponse("Password Change successfully!"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password didn't match!"));
        }
    }

    @PutMapping("/changephoto")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> changeProfilePic(@RequestBody String image, Authentication authentication) {
        byte[] imageofwrite = Base64.getDecoder().decode(image.split(",")[1]);
        String imgId = UUID.randomUUID().toString();

        try (FileOutputStream fos = new FileOutputStream(fileLocation + "/" + imgId)) {
            fos.write(imageofwrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        user.setImgId(imgId);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Account update successfully!"));
    }

    @GetMapping("/currentuser")
    public User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }

    @GetMapping("/getleasingplan/{adId}")
    public List<LeasingPlan> getLeasingPlan(@PathVariable Long adId) {
        return leasingPlanRepository.findAllByAdvertisement_Id(adId);
    }

    @GetMapping("/getinsuranceplan/{adId}")
    public List<InsurancePlan> getInsurancePlan(@PathVariable Long adId) {
        return insurancePlanRepository.findAllByAdvertisement_Id(adId);
    }

    @GetMapping("getUserById/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id);
    }

    // ==================== NEW MESSAGING ENDPOINTS ====================

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

            String participantName = null;
            String companyName = null;

            if ("agent".equals(conv.getCompanyType())) {
                Optional<User> agent = userRepository.findById(conv.getAgentId());
                if (agent.isPresent()) {
                    participantName = agent.get().getFname() + " " + agent.get().getLname();
                } else {
                    participantName = "Unknown Agent"; // Fallback
                }
            } else {
                companyName = conv.getCompanyName();
            }


            return new ConversationResponse(
                    conv.getId(),
                    participantName,
                    companyName,
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
     * Create a new conversation or get existing one (UPDATED)
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createConversation(@RequestBody ConversationRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (request.getAgentId() != null) {
            // Handle agent conversation
            Optional<Conversation> existing = conversationRepository.findByUserIdAndAgentId(
                    userDetails.getId(), request.getAgentId()
            );

            if (existing.isPresent()) {
                return ResponseEntity.ok(Map.of("conversationId", existing.get().getId()));
            }

            Conversation conversation = new Conversation();
            conversation.setUserId(userDetails.getId());
            conversation.setAgentId(request.getAgentId());
            conversation.setCompanyType("agent"); // Differentiate agent conversations
            conversation.setCompanyName("Agent Conversation"); // Placeholder for not-null constraint
            conversation.setStatus("active");
            Conversation saved = conversationRepository.save(conversation);
            return ResponseEntity.ok(Map.of("conversationId", saved.getId()));

        } else {
            // Handle company conversation
            Optional<Conversation> existing = conversationRepository.findByUserIdAndCompanyName(
                    userDetails.getId(), request.getCompanyName()
            );

            if (existing.isPresent()) {
                return ResponseEntity.ok(Map.of("conversationId", existing.get().getId()));
            }

            Conversation conversation = new Conversation();
            conversation.setUserId(userDetails.getId());
            conversation.setCompanyType(request.getCompanyType());
            conversation.setCompanyName(request.getCompanyName());
            conversation.setStatus("active");

            if (request.getVehicleId() != null) {
                conversation.setVehicleId(request.getVehicleId());
            }
            if (request.getInquiryType() != null) {
                conversation.setInquiryType(request.getInquiryType());
            }

            Conversation saved = conversationRepository.save(conversation);
            return ResponseEntity.ok(Map.of("conversationId", saved.getId()));
        }
    }

    /**
     * Get all available companies for messaging (NEW ENDPOINT)
     */
    @GetMapping("/companies/all")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllAvailableCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get distinct insurance companies
        List<String> insuranceCompanies = userInsuranceCompanyRepository
                .findDistinctCompanyNamesByUserId(userDetails.getId());

        // Get distinct leasing companies
        List<String> leasingCompanies = userLeasingCompanyRepository
                .findDistinctCompanyNamesByUserId(userDetails.getId());

        Map<String, List<String>> response = new HashMap<>();
        response.put("insuranceCompanies", insuranceCompanies);
        response.put("leasingCompanies", leasingCompanies);

        return ResponseEntity.ok(response);
    }

    /**
     * Get company details with user's plans (NEW ENDPOINT)
     */
    @GetMapping("/companies/{companyName}/details")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getCompanyDetailsForMessaging(@PathVariable String companyName, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get insurance plans
        List<UserInsuranceCompany> insurancePlans = userInsuranceCompanyRepository
                .findByUserIdAndCompanyName(userDetails.getId(), companyName);

        // Get leasing plans
        List<UserLeasingCompany> leasingPlans = userLeasingCompanyRepository
                .findByUserIdAndCompanyName(userDetails.getId(), companyName);

        Map<String, Object> response = new HashMap<>();
        response.put("companyName", companyName);

        if (!insurancePlans.isEmpty()) {
            response.put("companyType", "insurance");
            response.put("insurancePlans", insurancePlans);
        } else if (!leasingPlans.isEmpty()) {
            response.put("companyType", "leasing");
            response.put("leasingPlans", leasingPlans);
        }

        return ResponseEntity.ok(response);
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
     * Get user's insurance companies
     */
    @GetMapping("/insurance-companies")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<UserInsuranceCompany>> getInsuranceCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserInsuranceCompany> companies = userInsuranceCompanyRepository.findByUserIdAndPlanStatus(
                userDetails.getId(), "active"
        );
        return ResponseEntity.ok(companies);
    }

    /**
     * Get user's leasing companies
     */
    @GetMapping("/leasing-companies")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<UserLeasingCompany>> getLeasingCompanies(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserLeasingCompany> companies = userLeasingCompanyRepository.findByUserIdAndLeaseStatus(
                userDetails.getId(), "active"
        );
        return ResponseEntity.ok(companies);
    }

    /**
     * Get company details and active plans
     */
    @GetMapping("/company/{companyName}")
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
        response.put("insurancePlans", insurancePlans);
        response.put("leasingPlans", leasingPlans);

        return ResponseEntity.ok(response);
    }

    /**
     * Get approved agents
     */
    @GetMapping("/approved-agents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<User>> getApprovedAgents() {
        Role agentRole = roleRepository.findByName(ERole.ROLE_AGENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        List<User> agents = userRepository.findAllByRolesContaining(agentRole);
        return ResponseEntity.ok(agents);
    }

    /**
     * Get unread message count
     */
    @GetMapping("/messages/unread-count")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Conversation> conversations = conversationRepository.findByUserId(userDetails.getId());
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());

        Long count = 0L;
        if (!conversationIds.isEmpty()) {
            // Count unread messages from both 'company' and 'agent'
            Long companyUnread = messageRepository.countUnreadMessagesByConversationIdsAndSenderType(conversationIds, "company");
            Long agentUnread = messageRepository.countUnreadMessagesByConversationIdsAndSenderType(conversationIds, "agent");
            count = companyUnread + agentUnread;
        }

        return ResponseEntity.ok(Map.of("count", count));
    }
}