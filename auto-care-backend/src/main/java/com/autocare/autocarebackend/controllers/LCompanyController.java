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

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping("/lcompany")
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
        User user = userRepository.findById(userDetails.getId()).get();
        Advertisement advertisement = adRepository.findById(lPlanRequest.getAdId()).get();
        if(adRepository.existsById(lPlanRequest.getAdId())){
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
        }else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid Advertisment Id!"));
        }
    }

    @GetMapping("/getadconfrim")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<Advertisement> getConfrimad(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        return adRepository.getLConfrimAd(user.getId());
    }

    @GetMapping("/getpendingad")
    @PreAuthorize("hasRole('ROLE_LCOMPANY')")
    public List<Advertisement> getPending(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        System.out.println(user.getId());
        return adRepository.getLPendingAd(user.getId());
    }
}
