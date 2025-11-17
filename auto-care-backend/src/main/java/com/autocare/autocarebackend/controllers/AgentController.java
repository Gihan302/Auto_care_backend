package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.models.PackagePurchase;
import com.autocare.autocarebackend.models.Packages;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.PackagePurchaseRequest;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.PackagesPurchaseRepository;
import com.autocare.autocarebackend.repository.PackagesRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.NormalUserImpl;
import com.autocare.autocarebackend.security.services.PackagesPurchaseDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdRepository adRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    NormalUserImpl normalUser;

    @Autowired
    PackagesRepository packagesRepository;

    @Autowired
    PackagesPurchaseDetailsImpl packagesPurchaseDetails;

    @Autowired
    PackagesPurchaseRepository packagesPurchaseRepository;

    @GetMapping("/getad")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public List<Advertisement> getAgentAd(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        System.out.println(user.getId());
        return adRepository.findAllByUser(user);
    }
    @PostMapping("/packagepurchase/{pkgId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> packagePurchase(@PathVariable Long pkgId, @RequestBody PackagePurchaseRequest packagePurchaseRequest, Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        Packages packages = packagesRepository.findById(pkgId).get();
        Date date = new Date();

        PackagePurchase packagePurchase = new PackagePurchase(
                date,
                0,
                packages.getMaxAd(),
                packages,
                user
        );

        packagesPurchaseDetails.savePackagesPurchase(packagePurchase);

        return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
    }

    @PutMapping("/packageupdate/{pkgId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?>packageUpdate(@PathVariable Long pkgId,Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        Packages packages = packagesRepository.findById(pkgId).get();
        Optional <PackagePurchase> packagePurchase = packagesPurchaseRepository.findAllByUser(user);
        System.out.println(packagePurchase.get().getMaxAdCount());
        if(packagePurchase.get().getMaxAdCount() == packagePurchase.get().getCurrentAdCount()){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Package is over!"));
        }else{
            Integer currendAdCount = packagePurchase.get().getCurrentAdCount() + 1;
            System.out.println(currendAdCount);
            packagePurchase.get().setCurrentAdCount(currendAdCount);
            packagesPurchaseRepository.save(packagePurchase.get());
            return ResponseEntity.ok(new MessageResponse("Plan Add sucessfully!"));
        }

    }



}
