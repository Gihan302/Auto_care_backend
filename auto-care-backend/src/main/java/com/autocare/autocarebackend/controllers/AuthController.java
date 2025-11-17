package com.autocare.autocarebackend.controllers;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*" ,allowedHeaders = "*")
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
    public ResponseEntity<?>authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        try {
            logger.info("Request /signin");

            logger.info("Username: "+loginRequest.getUsername()+"\t Password: "+loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item->item.getAuthority())
                    .collect(Collectors.toList());

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
        } catch (Exception e) {
            logger.error("Error in authenticateUser: ", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error in authenticateUser"));
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/signup")
    public  ResponseEntity<?>registerUSer(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUsername(signupRequest.getUsername())){
            return  ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        if(userRepository.existsByNic(signupRequest.getNic())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: NIC is Already use!"));
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
        System.out.println(user.getcName());

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if(strRoles == null){
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(()->new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }else {
            strRoles.forEach(role -> {
                switch (role){
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(()->new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;

                    case "agent":
                        Role agentRole = roleRepository.findByName(ERole.ROLE_AGENT)
                                .orElseThrow(()->new RuntimeException("Error: Role is not found."));
                        roles.add(agentRole);

                        break;

                    case "lcompany":
                        Role lcompanyRole = roleRepository.findByName(ERole.ROLE_LCOMPANY)
                                .orElseThrow(()->new RuntimeException("Error: Role is not found."));
                        roles.add(lcompanyRole);

                        break;

                    case "icompany":
                        Role icompanyRole = roleRepository.findByName(ERole.ROLE_ICOMPANY)
                                .orElseThrow(()->new RuntimeException("Error: Role is not found."));
                        roles.add(icompanyRole);

                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(()->new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);

                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);


        return ResponseEntity.ok(new MessageResponse("User registered sucessfully!"));
    }
}