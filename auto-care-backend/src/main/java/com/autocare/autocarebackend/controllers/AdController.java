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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/advertisement")
public class AdController {

    private static final Logger logger = LoggerFactory.getLogger(AdController.class);

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

    // Helper method to check if string is null or blank (Java 8 compatible)
    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @PostMapping("/postadd")
    public ResponseEntity<?> AddPost(@RequestBody AdRequest adRequest) {
        try {
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("🔍 Authentication check: " + (authentication != null ? "Found" : "NULL"));

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("❌ Authentication is null or not authenticated");
                return ResponseEntity.status(401).body(new MessageResponse("Authentication required"));
            }

            Object principal = authentication.getPrincipal();
            logger.info("🎯 Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "NULL"));

            if (!(principal instanceof UserDetailsImpl)) {
                logger.error("❌ Invalid principal type: " + (principal != null ? principal.getClass() : "null"));
                return ResponseEntity.status(401).body(new MessageResponse("Invalid authentication principal"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            logger.info("👤 Authenticated user: " + userDetails.getUsername());

            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("❌ User not found in database for ID: " + userDetails.getId());
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid user"));
            }

            logger.info("✅ User found: " + user.getUsername());

            Date datetime = new Date();
            String[] images = adRequest.getImages();

            // Validate and prepare images array
            String[] safeImages = new String[5];
            if (images != null && images.length > 0) {
                for (int i = 0; i < Math.min(5, images.length); i++) {
                    safeImages[i] = images[i];
                }
            }

            // Upload images to Cloudinary and collect URLs
            String image1Url = null, image2Url = null, image3Url = null, image4Url = null, image5Url = null;

            logger.info("🖼️ Processing " + (images != null ? images.length : 0) + " images");

            // Upload each image correctly with proper null checks
            if (!isNullOrBlank(safeImages[0])) {
                logger.info("📤 Uploading image 1 to Cloudinary...");
                image1Url = imageUploadService.uploadBase64(safeImages[0]);
                logger.info("✅ Image 1 uploaded: " + image1Url);
            }
            if (!isNullOrBlank(safeImages[1])) {
                logger.info("📤 Uploading image 2 to Cloudinary...");
                image2Url = imageUploadService.uploadBase64(safeImages[1]);
                logger.info("✅ Image 2 uploaded: " + image2Url);
            }
            if (!isNullOrBlank(safeImages[2])) {
                logger.info("📤 Uploading image 3 to Cloudinary...");
                image3Url = imageUploadService.uploadBase64(safeImages[2]);
                logger.info("✅ Image 3 uploaded: " + image3Url);
            }
            if (!isNullOrBlank(safeImages[3])) {
                logger.info("📤 Uploading image 4 to Cloudinary...");
                image4Url = imageUploadService.uploadBase64(safeImages[3]);
                logger.info("✅ Image 4 uploaded: " + image4Url);
            }
            if (!isNullOrBlank(safeImages[4])) {
                logger.info("📤 Uploading image 5 to Cloudinary...");
                image5Url = imageUploadService.uploadBase64(safeImages[4]);
                logger.info("✅ Image 5 uploaded: " + image5Url);
            }

            // Create advertisement with flag = 0 (pending approval)
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
                    0, // Always set to 0 for pending approval
                    adRequest.getlStatus(),
                    adRequest.getiStatus(),
                    user
            );

            Advertisement saved = adDetails.saveAdDetails(advertisement);
            logger.info("🎉 Advertisement saved successfully with ID: {} (Pending admin approval)", saved.getId());

            return ResponseEntity.ok()
                    .body(new MessageResponse("Advertisement submitted successfully! It will be visible after admin approval."));

        } catch (IOException ex) {
            logger.error("💥 Cloudinary upload failed: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Image upload failed: " + ex.getMessage()));
        } catch (Exception ex) {
            logger.error("💥 Server error: " + ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new MessageResponse("Server error: " + ex.getMessage()));
        }
    }
    /**
     * Returns the stored image URL (string) for a given advertisement id and image index.
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
    public List<Advertisement> GetAddsByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return List.of();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return List.of();
        return adRepository.findByUser(user);
    }

    @GetMapping("/countremainad")
    public Long CountremainAd() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return 0L;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.rcount(user);
    }

    @GetMapping("/countpostedad")
    public Long CountpostedAd() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return 0L;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return 0L;
        return adRepository.pcount(user);
    }
    // Add these methods to your AdController.java

    /**
     * Get all approved advertisements for comparison selection
     */
    @GetMapping("/compare/available")
    public ResponseEntity<?> getAvailableForComparison() {
        try {
            // Get only approved ads (flag = 1)
            List<Advertisement> approvedAds = adRepository.getConfirmAd();

            // Return simplified data for selection dropdown
            List<Map<String, Object>> simplifiedAds = approvedAds.stream()
                    .map(ad -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", ad.getId());
                        map.put("title", ad.getTitle());
                        map.put("manufacturer", ad.getManufacturer());
                        map.put("model", ad.getModel());
                        map.put("year", ad.getM_year());
                        map.put("v_type", ad.getV_type());
                        return map;
                    })
                    .collect(Collectors.toList());

            logger.info("✅ Retrieved {} vehicles available for comparison", simplifiedAds.size());
            return ResponseEntity.ok(simplifiedAds);
        } catch (Exception e) {
            logger.error("❌ Error fetching available vehicles: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching vehicles"));
        }
    }

    /**
     * Compare multiple vehicles by their IDs
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compareVehicles(@RequestParam List<Long> ids) {
        try {
            if (ids == null || ids.size() < 2) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Please select at least 2 vehicles to compare"));
            }

            if (ids.size() > 4) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Maximum 4 vehicles can be compared at once"));
            }

            List<Advertisement> vehicles = new ArrayList<>();
            for (Long id : ids) {
                Optional<Advertisement> ad = adRepository.findById(id);
                if (ad.isPresent() && ad.get().getFlag() == 1) {
                    vehicles.add(ad.get());
                }
            }

            if (vehicles.size() < 2) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Could not find enough valid vehicles to compare"));
            }

            logger.info("✅ Comparing {} vehicles: {}", vehicles.size(),
                    vehicles.stream().map(Advertisement::getTitle).collect(Collectors.joining(", ")));

            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            logger.error("❌ Error comparing vehicles: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error comparing vehicles"));
        }
    }

    /**
     * Get unique manufacturers for filter
     */
    @GetMapping("/compare/manufacturers")
    public ResponseEntity<?> getManufacturers() {
        try {
            List<Advertisement> approvedAds = adRepository.getConfirmAd();
            List<String> manufacturers = approvedAds.stream()
                    .map(Advertisement::getManufacturer)
                    .filter(m -> m != null && !m.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(manufacturers);
        } catch (Exception e) {
            logger.error("❌ Error fetching manufacturers: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching manufacturers"));
        }
    }
    /**
     * Search and filter advertisements with intelligent matching
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAdvertisements(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String vType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice) {
        try {
            logger.info("🔍 Search request - Query: {}, Manufacturer: {}, Model: {}",
                    query, manufacturer, model);

            // Get all confirmed advertisements (flag = 1)
            List<Advertisement> allAds = adRepository.getConfirmAd();

            // Filter based on general search query
            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = query.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> {
                            // Search in multiple fields
                            boolean matches = false;

                            if (ad.getTitle() != null)
                                matches |= ad.getTitle().toLowerCase().contains(searchTerm);

                            if (ad.getManufacturer() != null)
                                matches |= ad.getManufacturer().toLowerCase().contains(searchTerm);

                            if (ad.getModel() != null)
                                matches |= ad.getModel().toLowerCase().contains(searchTerm);

                            if (ad.getDescription() != null)
                                matches |= ad.getDescription().toLowerCase().contains(searchTerm);

                            if (ad.getV_type() != null)
                                matches |= ad.getV_type().toLowerCase().contains(searchTerm);

                            return matches;
                        })
                        .collect(Collectors.toList());
            }

            // Filter by manufacturer
            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                String manufacturerLower = manufacturer.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> ad.getManufacturer() != null &&
                                ad.getManufacturer().toLowerCase().contains(manufacturerLower))
                        .collect(Collectors.toList());
            }

            // Filter by model
            if (model != null && !model.trim().isEmpty()) {
                String modelLower = model.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> ad.getModel() != null &&
                                ad.getModel().toLowerCase().contains(modelLower))
                        .collect(Collectors.toList());
            }

            // Filter by vehicle type
            if (vType != null && !vType.trim().isEmpty()) {
                String vTypeLower = vType.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> ad.getV_type() != null &&
                                ad.getV_type().toLowerCase().contains(vTypeLower))
                        .collect(Collectors.toList());
            }

            // Filter by transmission
            if (transmission != null && !transmission.trim().isEmpty()) {
                String transmissionLower = transmission.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> ad.getTransmission() != null &&
                                ad.getTransmission().toLowerCase().contains(transmissionLower))
                        .collect(Collectors.toList());
            }

            // Filter by fuel type
            if (fuelType != null && !fuelType.trim().isEmpty()) {
                String fuelTypeLower = fuelType.toLowerCase().trim();
                allAds = allAds.stream()
                        .filter(ad -> ad.getFuel_type() != null &&
                                ad.getFuel_type().toLowerCase().contains(fuelTypeLower))
                        .collect(Collectors.toList());
            }

            // Filter by year
            if (year != null && !year.trim().isEmpty()) {
                allAds = allAds.stream()
                        .filter(ad -> ad.getM_year() != null &&
                                ad.getM_year().equals(year))
                        .collect(Collectors.toList());
            }

            // Filter by price range (parse string prices)
            if (minPrice != null && !minPrice.trim().isEmpty()) {
                try {
                    double minPriceValue = Double.parseDouble(minPrice);
                    allAds = allAds.stream()
                            .filter(ad -> {
                                if (ad.getPrice() == null) return false;
                                try {
                                    // Remove currency symbols and commas
                                    String cleanPrice = ad.getPrice()
                                            .replaceAll("[^0-9.]", "");
                                    double adPrice = Double.parseDouble(cleanPrice);
                                    return adPrice >= minPriceValue;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ Invalid minPrice format: {}", minPrice);
                }
            }

            if (maxPrice != null && !maxPrice.trim().isEmpty()) {
                try {
                    double maxPriceValue = Double.parseDouble(maxPrice);
                    allAds = allAds.stream()
                            .filter(ad -> {
                                if (ad.getPrice() == null) return false;
                                try {
                                    String cleanPrice = ad.getPrice()
                                            .replaceAll("[^0-9.]", "");
                                    double adPrice = Double.parseDouble(cleanPrice);
                                    return adPrice <= maxPriceValue;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ Invalid maxPrice format: {}", maxPrice);
                }
            }

            logger.info("✅ Search completed. Found {} vehicles matching criteria", allAds.size());
            return ResponseEntity.ok(allAds);

        } catch (Exception e) {
            logger.error("❌ Error searching advertisements: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error searching advertisements: " + e.getMessage()));
        }
    }

    /**
     * Get unique values for filters
     */
    @GetMapping("/filters/manufacturers")
    public ResponseEntity<?> getUniqueManufacturers() {
        try {
            List<String> manufacturers = adRepository.getConfirmAd().stream()
                    .map(Advertisement::getManufacturer)
                    .filter(m -> m != null && !m.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            logger.info("✅ Found {} unique manufacturers", manufacturers.size());
            return ResponseEntity.ok(manufacturers);
        } catch (Exception e) {
            logger.error("❌ Error fetching manufacturers: {}", e.getMessage());
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching manufacturers"));
        }
    }

    @GetMapping("/filters/models")
    public ResponseEntity<?> getUniqueModels(@RequestParam(required = false) String manufacturer) {
        try {
            List<Advertisement> ads = adRepository.getConfirmAd();

            // Filter by manufacturer if provided
            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                String manuLower = manufacturer.toLowerCase();
                ads = ads.stream()
                        .filter(ad -> ad.getManufacturer() != null &&
                                ad.getManufacturer().toLowerCase().equals(manuLower))
                        .collect(Collectors.toList());
            }

            List<String> models = ads.stream()
                    .map(Advertisement::getModel)
                    .filter(m -> m != null && !m.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            logger.info("✅ Found {} unique models", models.size());
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            logger.error("❌ Error fetching models: {}", e.getMessage());
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching models"));
        }
    }

    @GetMapping("/filters/vehicleTypes")
    public ResponseEntity<?> getUniqueVehicleTypes() {
        try {
            List<String> types = adRepository.getConfirmAd().stream()
                    .map(Advertisement::getV_type)
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(types);
        } catch (Exception e) {
            logger.error("❌ Error fetching vehicle types: {}", e.getMessage());
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching vehicle types"));
        }
    }

    @GetMapping("/filters/years")
    public ResponseEntity<?> getUniqueYears() {
        try {
            List<String> years = adRepository.getConfirmAd().stream()
                    .map(Advertisement::getM_year)
                    .filter(y -> y != null && !y.trim().isEmpty())
                    .distinct()
                    .sorted(Comparator.reverseOrder()) // Newest first
                    .collect(Collectors.toList());

            return ResponseEntity.ok(years);
        } catch (Exception e) {
            logger.error("❌ Error fetching years: {}", e.getMessage());
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching years"));
        }
    }

    /**
     * Get models for a specific manufacturer
     */
    @GetMapping("/compare/models/{manufacturer}")
    public ResponseEntity<?> getModelsByManufacturer(@PathVariable String manufacturer) {
        try {
            List<Advertisement> approvedAds = adRepository.getConfirmAd();
            List<Map<String, Object>> models = approvedAds.stream()
                    .filter(ad -> manufacturer.equalsIgnoreCase(ad.getManufacturer()))
                    .map(ad -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", ad.getId());
                        map.put("model", ad.getModel());
                        map.put("year", ad.getM_year());
                        map.put("title", ad.getTitle());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(models);
        } catch (Exception e) {
            logger.error("❌ Error fetching models: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error fetching models"));
        }
    }
}