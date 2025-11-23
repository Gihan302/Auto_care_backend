package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.service.AgentApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent/applications")
@RequiredArgsConstructor
public class AgentApplicationController {

    private static final Logger log = LoggerFactory.getLogger(AgentApplicationController.class);
    private final AgentApplicationService agentApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<List<AgentApplicationDTO>> getAgentApplications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        log.info("Fetching applications for agent ID: {}", userDetails.getId());
        List<AgentApplicationDTO> applications = agentApplicationService.getApplicationsForAgent(userDetails.getId());
        log.info("Found {} applications for agent ID: {}", applications.size(), userDetails.getId());
        return ResponseEntity.ok(applications);
    }
}
