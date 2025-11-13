package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.EAccountStatus;
import com.autocare.autocarebackend.models.ERole;
import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.LoginRequest;
import com.autocare.autocarebackend.payload.request.SignupRequest;
import com.autocare.autocarebackend.payload.response.JwtResponse;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.RoleRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.jwt.JwtUtils;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Request /signin");
        logger.info("Username: " + loginRequest.getUsername());

        // First check if user exists and get their status
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check account status before authentication
            if (user.getAccountStatus() == EAccountStatus.PENDING) {
                logger.warn("Login attempt for pending account: {}", loginRequest.getUsername());
                return ResponseEntity
                        .status(403)
                        .body(new MessageResponse("Your account is pending admin approval. Please wait for approval."));
            }

            if (user.getAccountStatus() == EAccountStatus.REJECTED) {
                logger.warn("Login attempt for rejected account: {}", loginRequest.getUsername());
                String message = "Your account registration was rejected.";
                if (user.getRejectionReason() != null && !user.getRejectionReason().isEmpty()) {
                    message += " Reason: " + user.getRejectionReason();
                }
                return ResponseEntity
                        .status(403)
                        .body(new MessageResponse(message));
            }
        }

        // Proceed with normal authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        logger.info("✅ User logged in successfully: {} | Roles: {}", userDetails.getUsername(), roles);

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getFname(),
                userDetails.getLname(),
                userDetails.getTnumber(),
                userDetails.getUsername(),
                userDetails.getNic(),
                userDetails.getDate(),
                userDetails.getcName(),
                userDetails.getAddress(),
                userDetails.getRegNum(),
                roles,
                userDetails.getImgId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Request /signup | Email: {}", signupRequest.getUsername());

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            logger.warn("Signup failed - Email already in use: {}", signupRequest.getUsername());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByNic(signupRequest.getNic())) {
            logger.warn("Signup failed - NIC already in use: {}", signupRequest.getNic());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: NIC is already in use!"));
        }

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

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        AtomicBoolean needsApproval = new AtomicBoolean(false);

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;

                    case "agent":
                        Role agentRole = roleRepository.findByName(ERole.ROLE_AGENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(agentRole);
                        break;

                    case "lcompany":
                        Role lcompanyRole = roleRepository.findByName(ERole.ROLE_LCOMPANY)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(lcompanyRole);
                        needsApproval.set(true);
                        logger.info("Leasing company registration - needs approval");
                        break;

                    case "icompany":
                        Role icompanyRole = roleRepository.findByName(ERole.ROLE_ICOMPANY)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(icompanyRole);
                        needsApproval.set(true);
                        logger.info("Insurance company registration - needs approval");
                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);

        // Set account status based on user type - FIXED: Use APPROVED instead of ACTIVE
        if (needsApproval.get()) {
            user.setAccountStatus(EAccountStatus.PENDING);
            logger.info("✅ Company user created with PENDING status: {}", signupRequest.getUsername());
        } else {
            user.setAccountStatus(EAccountStatus.APPROVED); // Changed from ACTIVE to APPROVED
            logger.info("✅ Regular user created with APPROVED status: {}", signupRequest.getUsername());
        }

        userRepository.save(user);

        // Send different response based on approval requirement
        if (needsApproval.get()) {
            return ResponseEntity.ok(new MessageResponse(
                    "Registration submitted successfully! Your account is pending admin approval. " +
                            "You will receive an email once your account is approved."
            ));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}