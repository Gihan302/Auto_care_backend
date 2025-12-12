package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.service.AgentApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent/applications")
@RequiredArgsConstructor
public class AgentApplicationController {

    private static final Logger log = LoggerFactory.getLogger(AgentApplicationController.class);
    private final AgentApplicationService agentApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<Page<AgentApplicationDTO>> getAgentApplications(Authentication authentication,
                                                                        @RequestParam(required = false) String status,
                                                                        @RequestParam(required = false) String search,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "submittedAt,desc") String[] sort) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        log.info("Fetching applications for agent ID: {}", userDetails.getId());
        Page<AgentApplicationDTO> applications = agentApplicationService.getApplicationsForAgent(userDetails.getId(), status, search, page, sort);
        log.info("Found {} applications for agent ID: {}", applications.getTotalElements(), userDetails.getId());
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> approveApplication(@PathVariable Long id) {
        try {
            agentApplicationService.approveApplication(id);
            return ResponseEntity.ok(new MessageResponse("Application approved successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> rejectApplication(@PathVariable Long id) {
        try {
            agentApplicationService.rejectApplication(id);
            return ResponseEntity.ok(new MessageResponse("Application rejected successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
