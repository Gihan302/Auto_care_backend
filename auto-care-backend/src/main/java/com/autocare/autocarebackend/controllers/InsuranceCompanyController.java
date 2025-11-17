package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.ERole;
import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.SignupRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.RoleRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/insurance-companies")
public class InsuranceCompanyController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping
    public ResponseEntity<?> createInsuranceCompany(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        Date date = new Date();

        User user = new User(
                signupRequest.getFname(),
                signupRequest.getLname(),
                signupRequest.getTnumber(),
                signupRequest.getNic(),
                signupRequest.getUsername(),
                encoder.encode(signupRequest.getPassword()),
                signupRequest.getcName(),
                signupRequest.getRegNum(),
                signupRequest.getAddress(),
                signupRequest.getImgId(),
                date
        );

        Set<Role> roles = new HashSet<>();
        Role icompanyRole = roleRepository.findByName(ERole.ROLE_ICOMPANY)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(icompanyRole);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Insurance company registered successfully!"));
    }
}
