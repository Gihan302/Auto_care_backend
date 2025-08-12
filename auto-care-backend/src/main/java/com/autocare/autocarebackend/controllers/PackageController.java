package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Packages;
import com.autocare.autocarebackend.payload.request.PackagesRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.PackagesDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping("/package")


public class PackageController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PackagesDetailsImpl packagesDetails;

    @PostMapping("/addpackage")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addPackage(@RequestBody PackagesRequest packagesRequest,Authentication authentication){
        Packages packages=new Packages(
                packagesRequest.getPackageName(),
                packagesRequest.getPrice(),
                packagesRequest.getMaxAd(),
                packagesRequest.getCreationDate(),
                packagesRequest.getEndingDate()
        );
        packagesDetails.savePackages(packages);
        return ResponseEntity.ok(new MessageResponse("Package Add Succeefully!"));
    }
}
