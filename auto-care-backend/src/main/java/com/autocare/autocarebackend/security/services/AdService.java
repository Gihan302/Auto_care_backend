package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.ReportAd;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.payload.request.AdRequest;
import com.autocare.autocarebackend.payload.request.ReportAdRequest;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class AdService {

    private static final Logger logger = LoggerFactory.getLogger(AdService.class);

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ReportAdDetailsImpl reportAdDetails;

    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public Advertisement createAdvertisement(AdRequest adRequest, UserDetailsImpl userDetails) throws IOException {
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("Invalid user");
        }

        Date datetime = new Date();
        String[] images = adRequest.getImages();
        String[] safeImages = new String[5];
        if (images != null && images.length > 0) {
            for (int i = 0; i < Math.min(5, images.length); i++) {
                safeImages[i] = images[i];
            }
        }

        String image1Url = null, image2Url = null, image3Url = null, image4Url = null, image5Url = null;

        if (!isNullOrBlank(safeImages[0])) {
            image1Url = imageUploadService.uploadBase64(safeImages[0]);
        }
        if (!isNullOrBlank(safeImages[1])) {
            image2Url = imageUploadService.uploadBase64(safeImages[1]);
        }
        if (!isNullOrBlank(safeImages[2])) {
            image3Url = imageUploadService.uploadBase64(safeImages[2]);
        }
        if (!isNullOrBlank(safeImages[3])) {
            image4Url = imageUploadService.uploadBase64(safeImages[3]);
        }
        if (!isNullOrBlank(safeImages[4])) {
            image5Url = imageUploadService.uploadBase64(safeImages[4]);
        }

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
                0, // Pending approval
                adRequest.getlStatus(),
                adRequest.getiStatus(),
                user
        );

        return adRepository.save(advertisement);
    }

    public Optional<Advertisement> getAdById(Long id) {
        return adRepository.findById(id);
    }

    public List<Advertisement> getConfirmedAds() {
        return adRepository.getConfirmAd();
    }

    public List<Advertisement> getPendingAds() {
        return adRepository.getPendingAd();
    }

    public ReportAd createReportAd(Long adId, ReportAdRequest reportAdRequest) {
        Advertisement advertisement = adRepository.findById(adId).orElse(null);
        if (advertisement == null) {
            throw new RuntimeException("Invalid advertisement id");
        }

        ReportAd reportAd = new ReportAd(
                reportAdRequest.getReason(),
                reportAdRequest.getF_name(),
                reportAdRequest.getL_name(),
                reportAdRequest.getT_number(),
                reportAdRequest.getEmail(),
                reportAdRequest.getMessage(),
                advertisement
        );
        return reportAdDetails.saveReportAdDetails(reportAd);
    }

    public List<Advertisement> getAdsByCurrentUser(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return List.of();
        }
        return adRepository.findByUser(user);
    }

    public Long countRemainingAds(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return 0L;
        }
        return adRepository.rcount(user);
    }

    public Long countPostedAds(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return 0L;
        }
        return adRepository.pcount(user);
    }

    public List<Map<String, Object>> getAvailableForComparison() {
        List<Advertisement> approvedAds = adRepository.getConfirmAd();
        return approvedAds.stream()
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
    }

    public List<Advertisement> compareVehicles(List<Long> ids) {
        if (ids == null || ids.size() < 2) {
            throw new RuntimeException("Please select at least 2 vehicles to compare");
        }
        if (ids.size() > 4) {
            throw new RuntimeException("Maximum 4 vehicles can be compared at once");
        }

        List<Advertisement> vehicles = new ArrayList<>();
        for (Long id : ids) {
            Optional<Advertisement> ad = adRepository.findById(id);
            if (ad.isPresent() && ad.get().getFlag() == 1) {
                vehicles.add(ad.get());
            }
        }

        if (vehicles.size() < 2) {
            throw new RuntimeException("Could not find enough valid vehicles to compare");
        }
        return vehicles;
    }

    public List<String> getManufacturers() {
        return adRepository.getConfirmAd().stream()
                .map(Advertisement::getManufacturer)
                .filter(m -> m != null && !m.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getModelsByManufacturer(String manufacturer) {
        return adRepository.getConfirmAd().stream()
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
    }

    public Advertisement updateAdvertisement(Long adId, AdRequest adRequest) throws IOException {
        Advertisement advertisement = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        // Update fields
        advertisement.setName(adRequest.getName());
        advertisement.setT_number(adRequest.getT_number());
        advertisement.setEmail(adRequest.getEmail());
        advertisement.setLocation(adRequest.getLocation());
        advertisement.setTitle(adRequest.getTitle());
        advertisement.setPrice(adRequest.getPrice());
        advertisement.setV_type(adRequest.getV_type());
        advertisement.setManufacturer(adRequest.getManufacturer());
        advertisement.setModel(adRequest.getModel());
        advertisement.setV_condition(adRequest.getV_condition());
        advertisement.setM_year(adRequest.getM_year());
        advertisement.setR_year(adRequest.getR_year());
        advertisement.setMileage(adRequest.getMileage());
        advertisement.setE_capacity(adRequest.getE_capacity());
        advertisement.setTransmission(adRequest.getTransmission());
        advertisement.setFuel_type(adRequest.getFuel_type());
        advertisement.setColour(adRequest.getColour());
        advertisement.setDescription(adRequest.getDescription());

        // Handle images
        String[] images = adRequest.getImages();
        if (images != null && images.length > 0) {
            String[] safeImages = new String[5];
            for (int i = 0; i < Math.min(5, images.length); i++) {
                safeImages[i] = images[i];
            }

            if (!isNullOrBlank(safeImages[0])) {
                advertisement.setImage1(imageUploadService.uploadBase64(safeImages[0]));
            }
            if (!isNullOrBlank(safeImages[1])) {
                advertisement.setImage2(imageUploadService.uploadBase64(safeImages[1]));
            }
            if (!isNullOrBlank(safeImages[2])) {
                advertisement.setImage3(imageUploadService.uploadBase64(safeImages[2]));
            }
            if (!isNullOrBlank(safeImages[3])) {
                advertisement.setImage4(imageUploadService.uploadBase64(safeImages[3]));
            }
            if (!isNullOrBlank(safeImages[4])) {
                advertisement.setImage5(imageUploadService.uploadBase64(safeImages[4]));
            }
        }

        return adRepository.save(advertisement);
    }
}
