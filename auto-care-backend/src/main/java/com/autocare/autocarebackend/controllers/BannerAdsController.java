package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.BannerAd;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/banner-ads")
public class BannerAdsController {

    @GetMapping("/active")
    public List<BannerAd> getActiveBannerAds() {
        return Arrays.asList(
                new BannerAd(1L, "Ad 1", "Description 1", "https://placehold.co/300x150", "https://example.com"),
                new BannerAd(2L, "Ad 2", "Description 2", "https://placehold.co/300x150", "https://example.com"),
                new BannerAd(3L, "Ad 3", "Description 3", "https://placehold.co/300x150", "https://example.com")
        );
    }

    @PostMapping("/{adId}/impression")
    public void trackImpression(@PathVariable Long adId) {
        // In a real application, you would save this impression to a database
        System.out.println("Impression tracked for ad: " + adId);
    }

    @PostMapping("/{adId}/click")
    public void trackClick(@PathVariable Long adId) {
        // In a real application, you would save this click to a database
        System.out.println("Click tracked for ad: " + adId);
    }
}
