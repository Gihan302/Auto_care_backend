package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.LPlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.LPlanRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.LPlanRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.LPlanDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Import Optional
import java.util.stream.Collectors;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping("/api/lcompany")
public class LCompanyController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    LPlanRepository lPlanRepository;

    @Autowired
    LPlanDetailsImpl lPlanDetails;

    @Autowired
    AdRepository adRepository;

    @PostMapping("/postlplan")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public ResponseEntity<?> lPlanPost(@RequestBody LPlanRequest lPlanRequest , Authentication authentication){
        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();

        // --- SAFER LOGIC ---
        // Get the logged-in user
        // Use orElseThrow for a user that *should* exist
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Find the advertisement
        Optional<Advertisement> advertisementOptional = adRepository.findById(lPlanRequest.getAdId());

        // Check if the advertisement exists
        if(advertisementOptional.isPresent()){
            // Get the actual advertisement object
            Advertisement advertisement = advertisementOptional.get();

            LPlan lPlan = new LPlan(
                    lPlanRequest.getPlanAmount(),
                    lPlanRequest.getNoOfInstallments(),
                    lPlanRequest.getInterest(),
                    lPlanRequest.getInstAmount(),
                    lPlanRequest.getDescription(),
                    user,
                    advertisement
            );

            lPlanDetails.saveLPlanDetails(lPlan);
            return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
        } else {
            // If the Optional was empty, return the Bad Request
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid Advertisment Id!"));
        }
        // --- END SAFER LOGIC ---
    }

    // --- FIX: Reconstructed the method correctly ---
    @GetMapping("/getadconfrim")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<Advertisement> getConfrimad(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return adRepository.getLConfrimAd(user.getId());
    }

    // --- FIX: Reconstructed the method correctly ---
    @GetMapping("/getpendingad")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<Advertisement> getPending(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        System.out.println(user.getId());
        return adRepository.getLPendingAd(user.getId());
    }

    @GetMapping("/myplans")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<LPlan> getMyPlans(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        // This query finds all LPlan entities associated with the logged-in user (LCompany)
        return lPlanRepository.findByUser(user);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<User> getUsers(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User companyUser = userRepository.findById(userDetails.getId()).get();
        List<LPlan> plans = lPlanRepository.findByUser(companyUser);
        return plans.stream()
                .map(plan -> plan.getAdvertisement().getUser())
                .distinct()
                .collect(Collectors.toList());
    }
}
