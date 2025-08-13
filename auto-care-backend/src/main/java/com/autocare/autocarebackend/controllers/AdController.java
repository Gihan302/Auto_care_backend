package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.ReportAd;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.AdRequest;
import com.autocare.autocarebackend.payload.request.ReportAdRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.AdDetailsImpl;
import com.autocare.autocarebackend.security.services.ReportAdDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import com.autocare.autocarebackend.security.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/advertisement")
public class AdController {

    @Autowired
    AdDetailsImpl adDetails;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdRepository adRepository;

    @Autowired
    ReportAdDetailsImpl reportAdDetails;

    @Autowired
    ImageUploadService imageUploadService;

    // NOTE: fileLocation is no longer used for saving images to disk.
    // Keeping field to avoid breaking other config reads if present.
    // @Value("${upload.location}")
    // private String fileLocation;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/postadd")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_AGENT')")
    public ResponseEntity<?> AddPost(@RequestBody AdRequest adRequest, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid user"));
            }

            Date datetime = new Date();

            String[] images = adRequest.getImages();
            // Ensure array length at most 5; fill missing with nulls
            String[] safeImages = new String[5];
            for (int i = 0; i < 5; i++) {
                if (images != null && images.length > i) safeImages[i] = images[i];
                else safeImages[i] = null;
            }

            // Upload images to Cloudinary (if present) and collect uploaded URLs
            String image1Url = null, image2Url = null, image3Url = null, image4Url = null, image5Url = null;
            if (safeImages[0] != null && !safeImages[0].isBlank()) image1Url = imageUploadService.uploadBase64(safeImages[0]);
            if (safeImages[1] != null && !safeImages[1].isBlank()) image2Url = imageUploadService.uploadBase64(safeImages[1]);
            if (safeImages[2] != null && !safeImages[2].isBlank()) image3Url = imageUploadService.uploadBase64(safeImages[2]);
            if (safeImages[3] != null && !safeImages[3].isBlank()) image4Url = imageUploadService.uploadBase64(safeImages[3]);
            if (safeImages[4] != null && !safeImages[4].isBlank()) image5Url = imageUploadService.uploadBase64(safeImages[4]);

            Advertisement advertisement = new Advertisement(
                    adRequest.getName(),
                    adRequest.getT_number(),
                    adRequest.getEmail(),
                    adRequest.getLocation(),
                    adRequest.getTitle(),
                    adRequest.getPrice(),
                    adRequest.getV_type(),
                    adRequest.getManufacturer(),
                    adRequest.getModel(),
                    adRequest.getV_condition(),
                    adRequest.getM_year(),
                    adRequest.getR_year(),
                    adRequest.getMileage(),
                    adRequest.getE_capacity(),
                    adRequest.getTransmission(),
                    adRequest.getFuel_type(),
                    adRequest.getColour(),
                    adRequest.getDescription(),
                    image1Url, image2Url, image3Url, image4Url, image5Url,
                    datetime,
                    adRequest.getFlag(),
                    adRequest.getlStatus(),
                    adRequest.getiStatus(),
                    user
            );

            Advertisement saved = adDetails.saveAdDetails(advertisement);
            return ResponseEntity.ok(saved);

        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("Server error: " + ex.getMessage()));
        }
    }

    /**
     * Returns the stored image URL (string) for a given advertisement id and image index.
     * This keeps things simple: frontend should prefer to use the URLs returned in the Advertisement object.
     *
     * Example: GET /advertisement/getimage/{adId}/{index} where index is 1..5
     */
    @GetMapping(value = {"/getimage/{id}", "/getimage/{id}/{index}"})
    public ResponseEntity<?> getAddImage(@PathVariable("id") Long id,
                                         @PathVariable(name = "index", required = false) Integer index) {
        Optional<Advertisement> opt = adRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Advertisement ad = opt.get();

        // If index not provided, return first non-null image URL
        if (index == null) {
            if (ad.getImage1() != null) return ResponseEntity.ok(ad.getImage1());
            if (ad.getImage2() != null) return ResponseEntity.ok(ad.getImage2());
            if (ad.getImage3() != null) return ResponseEntity.ok(ad.getImage3());
            if (ad.getImage4() != null) return ResponseEntity.ok(ad.getImage4());
            if (ad.getImage5() != null) return ResponseEntity.ok(ad.getImage5());
            return ResponseEntity.noContent().build();
        }

        switch (index) {
            case 1:
                return ad.getImage1() != null ? ResponseEntity.ok(ad.getImage1()) : ResponseEntity.noContent().build();
            case 2:
                return ad.getImage2() != null ? ResponseEntity.ok(ad.getImage2()) : ResponseEntity.noContent().build();
            case 3:
                return ad.getImage3() != null ? ResponseEntity.ok(ad.getImage3()) : ResponseEntity.noContent().build();
            case 4:
                return ad.getImage4() != null ? ResponseEntity.ok(ad.getImage4()) : ResponseEntity.noContent().build();
            case 5:
                return ad.getImage5() != null ? ResponseEntity.ok(ad.getImage5()) : ResponseEntity.noContent().build();
            default:
                return ResponseEntity.badRequest().body(new MessageResponse("Index must be between 1 and 5"));
        }
    }

    @GetMapping("/getconfrimad")
    public List<Advertisement> getComnfirmAd() {
        return adRepository.getConfirmAd();
    }

    @GetMapping("/getnewad")
    public List<Advertisement> getPendingAd() {
        return adRepository.getPendingAd();
    }

    @GetMapping("/getAdById/{id}")
    public Optional<Advertisement> gedAdById(@PathVariable Long id) {
        System.out.println(id);
        System.out.println("get add");
        return adRepository.findById(id);
    }

    @PostMapping("/reportad/{id}")
    public ResponseEntity<?> ReportAdpost(@PathVariable Long id, @RequestBody ReportAdRequest reportAdRequest) {
        Advertisement advertisement = adRepository.findById(id).orElse(null);
        if (advertisement == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid advertisement id"));
        }

        ReportAd reportAd = new ReportAd(reportAdRequest.getReason(), reportAdRequest.getF_name(),
                reportAdRequest.getL_name(), reportAdRequest.getT_number(), reportAdRequest.getEmail(),
                reportAdRequest.getMessage(), advertisement);
        reportAdDetails.saveReportAdDetails(reportAd);
        return ResponseEntity.ok(new MessageResponse("Report Advertisement successfully!"));
    }

    @GetMapping("/getAddsByCurrentUser")
    public List<Advertisement> GetAddsByUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return List.of();
        return adRepository.findByUser(user);
    }

    @GetMapping("/countremainad")
    public Long CountremainAd(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.rcount(user);
    }

    @GetMapping("/countpostedad")
    public Long CountpostedAd(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.pcount(user);
    }
}
