package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.IPlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.IPlanRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.IPlanRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.IPlanDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/icompany")

public class ICompanyController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    IPlanRepository iPlanRepository;

    @Autowired
    IPlanDetailsImpl iPlanDetails;

    @Autowired
    AdRepository adRepository;



    @PostMapping("/postiplan")
    @PreAuthorize("hasRole('ROLE_ICOMPANY')")

    public ResponseEntity<?> iPlanPost(@RequestBody IPlanRequest iPlanRequest , Authentication authentication){
        UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        Advertisement advertisement = adRepository.findById(iPlanRequest.getAdId()).get();
        if(adRepository.existsById(iPlanRequest.getAdId())){
            IPlan iPlan = new IPlan(
                    iPlanRequest.getPlanAmt(),
                    iPlanRequest.getNoOfInstallments(),
                    iPlanRequest.getInterest(),
                    iPlanRequest.getInstAmt(),
                    iPlanRequest.getDescription(),
                    user,
                    advertisement
            );
            iPlanDetails.saveIPlanDetails(iPlan);
            return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
        }else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid Advertisment Id!"));
        }
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
        System.out.println(user.getId());
        return adRepository.getIPendingAd(user.getId());
    }
}
