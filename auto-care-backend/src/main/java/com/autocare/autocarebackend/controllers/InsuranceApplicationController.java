package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.InsuranceApplication;
import com.autocare.autocarebackend.models.InsurancePlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.InsuranceApplicationRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.InsuranceApplicationRepository;
import com.autocare.autocarebackend.repository.InsurancePlanRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/insurance-applications")
public class InsuranceApplicationController {

    @Autowired
    private InsuranceApplicationRepository insuranceApplicationRepository;

    @Autowired
    private InsurancePlanRepository insurancePlanRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> submitApplication(@RequestBody InsuranceApplicationRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: User not found."));

        InsurancePlan plan = insurancePlanRepository.findById(request.getPlanId()).orElseThrow(() -> new RuntimeException("Error: Insurance plan not found."));

        InsuranceApplication application = new InsuranceApplication();
        application.setPlan(plan);
        application.setUser(user);
        application.setFullName(request.getFullName());
        application.setEmail(request.getEmail());
        application.setPhone(request.getPhone());
        application.setAddress(request.getAddress());

        insuranceApplicationRepository.save(application);

        return ResponseEntity.ok(new MessageResponse("Insurance application submitted successfully!"));
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<List<InsuranceApplication>> getCompanyApplications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User companyUser = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: Company not found."));
        List<InsuranceApplication> applications = insuranceApplicationRepository.findByPlan_User(companyUser);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<InsuranceApplication>> getMyApplications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: User not found."));
        List<InsuranceApplication> applications = insuranceApplicationRepository.findByUser(user);
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id, @RequestBody String status, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User companyUser = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: Company not found."));

        Optional<InsuranceApplication> applicationOptional = insuranceApplicationRepository.findById(id);
        if (applicationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InsuranceApplication application = applicationOptional.get();
        if (!application.getPlan().getUser().getId().equals(companyUser.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to update this application."));
        }

        application.setStatus(status);
        insuranceApplicationRepository.save(application);
        return ResponseEntity.ok(new MessageResponse("Application status updated successfully!"));
    }
}
