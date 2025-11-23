package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.ApplicationDTO;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> submitApplication(@RequestBody ApplicationDTO applicationDTO, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            applicationService.createApplication(applicationDTO, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Application submitted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
