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
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/icompany")
public class ICompanyController {

    @Autowired
    private InsurancePlanRepository insurancePlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @PostMapping("/postplan")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public ResponseEntity<?> insurancePlanPost(@RequestBody InsurancePlanRequest insurancePlanRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Optional<Advertisement> advertisementOptional = adRepository.findById(insurancePlanRequest.getAdId());
        if (advertisementOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid Advertisement Id!"));
        }
        Advertisement advertisement = advertisementOptional.get();

        InsurancePlan insurancePlan = new InsurancePlan(
                insurancePlanRequest.getPlanName(),
                insurancePlanRequest.getDescription(),
                insurancePlanRequest.getCoverage(),
                Double.parseDouble(insurancePlanRequest.getPrice()),
                user,
                insurancePlanRequest.getPlanType(),
                insurancePlanRequest.getPremium(),
                advertisement
        );

        insurancePlanRepository.save(insurancePlan);
        return ResponseEntity.ok(new MessageResponse("Plan added successfully!"));
    }

    @GetMapping("/myplans")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")
    public List<InsurancePlan> getMyPlans(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return insurancePlanRepository.findByUser(user);
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