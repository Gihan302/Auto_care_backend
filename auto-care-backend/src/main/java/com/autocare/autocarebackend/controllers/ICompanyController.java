package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.InsurancePlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.InsurancePlanRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.InsurancePlanRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/icompany")
public class ICompanyController {

    @Autowired
    private InsurancePlanRepository iPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @PostMapping("/postplan")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<?> iPlanPost(@RequestBody InsurancePlanRequest iPlanRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        InsurancePlan iPlan = new InsurancePlan(
                iPlanRequest.getPlanName(),
                iPlanRequest.getCoverage(),
                iPlanRequest.getPrice(),
                iPlanRequest.getDescription(),
                user
        );

        iPlanRepository.save(iPlan);
        return ResponseEntity.ok(new MessageResponse("Plan added successfully!"));
    }

    @GetMapping("/myplans")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public List<InsurancePlan> getMyPlans(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return iPlanRepository.findByUser(user);
    }

    @GetMapping("/getadconfrim")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public List<Advertisement> getConfrimad(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return adRepository.getIConfrimAd(user.getId());
    }

    @GetMapping("/getpendingad")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public List<Advertisement> getPending(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return adRepository.getIPendingAd(user.getId());
    }
}
