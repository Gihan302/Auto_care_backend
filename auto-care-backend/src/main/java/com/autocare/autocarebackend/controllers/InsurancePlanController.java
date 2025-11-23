package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.InsurancePlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.InsurancePlanRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
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
@RequestMapping("/api/insurance-plans")
public class InsurancePlanController {

    @Autowired
    InsurancePlanRepository insurancePlanRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public List<InsurancePlan> getPlans(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return insurancePlanRepository.findByUser(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<InsurancePlan> getPlanById(@PathVariable Long id) {
        Optional<InsurancePlan> insurancePlan = insurancePlanRepository.findById(id);
        return insurancePlan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody InsurancePlanRequest insurancePlanRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        Optional<InsurancePlan> existingPlanOptional = insurancePlanRepository.findById(id);
        if (existingPlanOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InsurancePlan existingPlan = existingPlanOptional.get();
        if (!existingPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to update this plan."));
        }

        existingPlan.setPlanName(insurancePlanRequest.getPlanName());
        existingPlan.setCoverage(insurancePlanRequest.getCoverage());
        existingPlan.setPrice(Double.parseDouble(insurancePlanRequest.getPrice()));
        existingPlan.setDescription(insurancePlanRequest.getDescription());

        insurancePlanRepository.save(existingPlan);
        return ResponseEntity.ok(new MessageResponse("Insurance plan updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<?> deletePlan(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        Optional<InsurancePlan> existingPlanOptional = insurancePlanRepository.findById(id);
        if (existingPlanOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InsurancePlan existingPlan = existingPlanOptional.get();
        if (!existingPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to delete this plan."));
        }

        insurancePlanRepository.delete(existingPlan);
        return ResponseEntity.ok(new MessageResponse("Insurance plan deleted successfully!"));
    }

    @GetMapping("/public/all")
    public List<InsurancePlan> getAllPlans() {
        return insurancePlanRepository.findAll();
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<InsurancePlan> getPublicPlanById(@PathVariable Long id) {
        Optional<InsurancePlan> insurancePlan = insurancePlanRepository.findById(id);
        return insurancePlan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
