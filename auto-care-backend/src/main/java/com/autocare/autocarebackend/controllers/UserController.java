package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.IPlan;
import com.autocare.autocarebackend.models.LPlan;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.SignupRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.IPlanRepository;
import com.autocare.autocarebackend.repository.LPlanRepository;
import com.autocare.autocarebackend.repository.RoleRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.NormalUserImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    NormalUserImpl normalUser;

    @Value("${upload.location}")
    private String fileLocation;

    @Autowired
    LPlanRepository lPlanRepository;

    @Autowired
    IPlanRepository iPlanRepository;

    @GetMapping("/getallusers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ New endpoint for total users count
    @GetMapping("/total")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public long getTotalUsers() {
        return userRepository.count();
    }

    @PutMapping("/editprofile")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> editNormalUserEditProfile(@RequestBody SignupRequest signupRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        user.setFname(signupRequest.getFname());
        user.setLname(signupRequest.getLname());
        user.setTnumber(signupRequest.getTnumber());
        user.setAddress(signupRequest.getAddress());
        normalUser.editNormalUserEditProfile(user);
        return ResponseEntity.ok(new MessageResponse("Account update successfully!"));
    }

    @PutMapping("/changepassword/{password}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> editPasswordProfile(@RequestBody SignupRequest signupRequest, @PathVariable String password, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        if (encoder.matches(password, (user.getPassword()))) {
            user.setPassword(encoder.encode(signupRequest.getPassword()));
            normalUser.editNormalUserEditProfile(user);
            return ResponseEntity.ok(new MessageResponse("Password Change successfully!"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password didn't match!"));
        }
    }

    @PutMapping("/changephoto")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_LCOMPANY') or hasRole('ROLE_ICOMPANY') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> changeProfilePic(@RequestBody String image, Authentication authentication) {
        byte[] imageofwrite = Base64.getDecoder().decode(image.split(",")[1]);
        String imgId = UUID.randomUUID().toString();

        try (FileOutputStream fos = new FileOutputStream(fileLocation + "/" + imgId)) {
            fos.write(imageofwrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        user.setImgId(imgId);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Account update successfully!"));
    }

    @GetMapping("/currentuser")
    public User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }

    @GetMapping("/getlplan/{adId}")
    public List<LPlan> getLPlan(@PathVariable Long adId) {
        return lPlanRepository.findAllByAdvertisement_Id(adId);
    }

    @GetMapping("/getiplan/{adId}")
    public List<IPlan> getIPlan(@PathVariable Long adId) {
        return iPlanRepository.findAllByAdvertisement_Id(adId);
    }

    @GetMapping("/getUserById/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id);
    }
}
