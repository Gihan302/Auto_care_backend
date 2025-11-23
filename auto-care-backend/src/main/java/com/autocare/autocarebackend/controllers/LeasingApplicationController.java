package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.LeasingApplication;
import com.autocare.autocarebackend.models.LeasingPlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.LeasingApplicationRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.LeasingApplicationRepository;
import com.autocare.autocarebackend.repository.LeasingPlanRepository;
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
@RequestMapping("/api/leasing-applications")
public class LeasingApplicationController {

    @Autowired
    private LeasingApplicationRepository leasingApplicationRepository;

    @Autowired
    private LeasingPlanRepository leasingPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> submitApplication(@RequestBody LeasingApplicationRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: User not found."));

        LeasingPlan plan = leasingPlanRepository.findById(request.getPlanId()).orElseThrow(() -> new RuntimeException("Error: Leasing plan not found."));

        LeasingApplication application = new LeasingApplication();
        application.setPlan(plan);
        application.setUser(user);
        application.setFullName(request.getFullName());
        application.setEmail(request.getEmail());
        application.setPhone(request.getPhone());
        application.setAddress(request.getAddress());
        application.setIncome(request.getIncome());
        application.setEmploymentStatus(request.getEmploymentStatus());
        application.setCreditScore(request.getCreditScore());

        leasingApplicationRepository.save(application);

        return ResponseEntity.ok(new MessageResponse("Application submitted successfully!"));
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<List<LeasingApplication>> getCompanyApplications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User companyUser = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: Company not found."));
        List<LeasingApplication> applications = leasingApplicationRepository.findByPlan_User(companyUser);
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id, @RequestBody String status, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User companyUser = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("Error: Company not found."));

        Optional<LeasingApplication> applicationOptional = leasingApplicationRepository.findById(id);
        if (applicationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeasingApplication application = applicationOptional.get();
        if (!application.getPlan().getUser().getId().equals(companyUser.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to update this application."));
        }

        application.setStatus(status);
        leasingApplicationRepository.save(application);
        return ResponseEntity.ok(new MessageResponse("Application status updated successfully!"));
    }

}
