package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.*;
import com.autocare.autocarebackend.payload.request.AdRequest;
import com.autocare.autocarebackend.payload.response.MessageResponse;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.PackagesPurchaseRepository;
import com.autocare.autocarebackend.repository.PackagesRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import com.autocare.autocarebackend.security.services.AdService;
import com.autocare.autocarebackend.security.services.NormalUserImpl;
import com.autocare.autocarebackend.security.services.PackagesPurchaseDetailsImpl;
import com.autocare.autocarebackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    AdService adService;


    @PostMapping("/create-ad")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> createAd(@RequestBody AdRequest adRequest, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            adService.createAdvertisement(adRequest, userDetails);
            return ResponseEntity.ok(new MessageResponse("Advertisement created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/getad")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public List<Advertisement> getAgentAd(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        System.out.println(user.getId());
        return adRepository.findAllByUser(user);
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<List<AgentActivityDTO>> getRecentActivity(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User agent = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Agent not found."));

        List<AgentActivityDTO> activities = new ArrayList<>();

        // Get ad-related activities
        List<Advertisement> ads = adRepository.findAllByUser(agent);
        for (Advertisement ad : ads) {
            // Ad creation
            activities.add(AgentActivityDTO.builder()
                    .id(ad.getId())
                    .user("You")
                    .action("created a new ad for '" + ad.getTitle() + "'")
                    .time(ad.getDatetime())
                    .status("Active")
                    .initials("Y")
                    .avatarColor("bg-purple-500")
                    .link("/agent/my-ads")
                    .build());

            // Ad sold
            if (ad.getFlag() == 2) {
                activities.add(AgentActivityDTO.builder()
                        .id(ad.getId() * -1) // a hack to get a unique id
                        .user("You")
                        .action("marked '" + ad.getTitle() + "' as sold")
                        .time(ad.getDatetime()) // This should be the sold time, but we don't have it
                        .status("Sold")
                        .initials("Y")
                        .avatarColor("bg-green-500")
                        .link("/agent/sold-ads")
                        .build());
            }
        }

        // Sort activities by time (descending)
        activities.sort(Comparator.comparing(AgentActivityDTO::getTime).reversed());

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<AgentStatsDTO> getAgentStats(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User agent = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Agent not found."));

        List<Advertisement> agentAds = adRepository.findAllByUser(agent);

        long totalAds = agentAds.size();
        long activeAds = agentAds.stream().filter(ad -> ad.getFlag() == 0 || ad.getFlag() == 1).count(); // Assuming 0 or 1 is active/pending
        long soldAds = agentAds.stream().filter(ad -> ad.getFlag() == 2).count(); // Assuming 2 is sold

        // Package Usage
        Optional<PackagePurchase> latestPackagePurchase = packagesPurchaseRepository.findTopByUserOrderByPurchaseDateDesc(agent);
        
        String packageUsage = "N/A";
        String packageDaysLeft = "N/A";

        if (latestPackagePurchase.isPresent()) {
            PackagePurchase pp = latestPackagePurchase.get();
            long maxAd = pp.getMaxAdCount();
            long currentAd = pp.getCurrentAdCount();
            packageUsage = currentAd + "/" + maxAd;

            // Calculate days left
            Packages purchasedPackage = pp.getPackages(); // Assuming Packages entity is eager loaded or fetched
            if (purchasedPackage != null && purchasedPackage.getEndingDate() != null) {
                Date endingDate = purchasedPackage.getEndingDate();
                long diffInMillies = endingDate.getTime() - new Date().getTime(); // Removed Math.abs, calculate difference

                // Use Math.max to ensure days left is not negative
                long diff = Math.max(0, TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS));

                packageDaysLeft = diff + " days left";
            } else {
                packageDaysLeft = "Unlimited"; // Or some other default
            }

        } else {
            packageUsage = "0/0";
            packageDaysLeft = "No package";
        }

        AgentStatsDTO stats = AgentStatsDTO.builder()
                .totalAds(totalAds)
                .activeAds(activeAds)
                .soldAds(soldAds)
                .packageUsage(packageUsage)
                .packageDaysLeft(packageDaysLeft)
                .build();

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/packagepurchase/{pkgId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> packagePurchase(@PathVariable Long pkgId, @RequestBody(required = false) com.autocare.autocarebackend.payload.request.PackagePurchaseRequest packagePurchaseRequest, Authentication authentication){
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

    @PutMapping("/update-ad/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> updateAd(@PathVariable Long adId, @RequestBody AdRequest adRequest, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to update this advertisement!"));
            }

            adService.updateAdvertisement(adId, adRequest);

            return ResponseEntity.ok(new MessageResponse("Advertisement updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-ad/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> deleteAd(@PathVariable Long adId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to delete this advertisement!"));
            }

            adRepository.delete(advertisement);

            return ResponseEntity.ok(new MessageResponse("Advertisement deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/mark-as-sold/{adId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<?> markAdAsSold(@PathVariable Long adId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();

            Optional<Advertisement> advertisementOptional = adRepository.findById(adId);
            if (advertisementOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Advertisement not found!"));
            }

            Advertisement advertisement = advertisementOptional.get();

            if (!advertisement.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to update this advertisement!"));
            }

            advertisement.setFlag(2); // 2 for sold
            adRepository.save(advertisement);

            return ResponseEntity.ok(new MessageResponse("Advertisement marked as sold successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}