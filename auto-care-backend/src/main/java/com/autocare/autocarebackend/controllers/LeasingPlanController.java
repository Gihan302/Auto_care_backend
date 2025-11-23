package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.LeasingPlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.LeasingPlanRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
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
@RequestMapping("/api/leasing-plans")
public class LeasingPlanController {

    @Autowired
    LeasingPlanRepository leasingPlanRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> createPlan(@RequestBody LeasingPlanRequest leasingPlanRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        LeasingPlan leasingPlan = new LeasingPlan(
                leasingPlanRequest.getPlanName(),
                leasingPlanRequest.getVehicleType(),
                leasingPlanRequest.getLeaseTerm(),
                leasingPlanRequest.getInterestRate(),
                leasingPlanRequest.getMonthlyPayment(),
                leasingPlanRequest.getDescription(),
                user
        );

        leasingPlanRepository.save(leasingPlan);
        return ResponseEntity.ok(new MessageResponse("Leasing plan created successfully!"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<LeasingPlan> getPlans(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return leasingPlanRepository.findByUser(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<LeasingPlan> getPlanById(@PathVariable Long id) {
        Optional<LeasingPlan> leasingPlan = leasingPlanRepository.findById(id);
        return leasingPlan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody LeasingPlanRequest leasingPlanRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        Optional<LeasingPlan> existingPlanOptional = leasingPlanRepository.findById(id);
        if (existingPlanOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeasingPlan existingPlan = existingPlanOptional.get();
        if (!existingPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to update this plan."));
        }

        existingPlan.setPlanName(leasingPlanRequest.getPlanName());
        existingPlan.setVehicleType(leasingPlanRequest.getVehicleType());
        existingPlan.setLeaseTerm(leasingPlanRequest.getLeaseTerm());
        existingPlan.setInterestRate(leasingPlanRequest.getInterestRate());
        existingPlan.setMonthlyPayment(leasingPlanRequest.getMonthlyPayment());
        existingPlan.setDescription(leasingPlanRequest.getDescription());

        leasingPlanRepository.save(existingPlan);
        return ResponseEntity.ok(new MessageResponse("Leasing plan updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> deletePlan(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        Optional<LeasingPlan> existingPlanOptional = leasingPlanRepository.findById(id);
        if (existingPlanOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeasingPlan existingPlan = existingPlanOptional.get();
        if (!existingPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: You are not authorized to delete this plan."));
        }

        leasingPlanRepository.delete(existingPlan);
        return ResponseEntity.ok(new MessageResponse("Leasing plan deleted successfully!"));
    }

    @GetMapping("/public/all")
    public List<LeasingPlan> getAllPlans() {
        return leasingPlanRepository.findAll();
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<LeasingPlan> getPublicPlanById(@PathVariable Long id) {
        Optional<LeasingPlan> leasingPlan = leasingPlanRepository.findById(id);
        return leasingPlan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
