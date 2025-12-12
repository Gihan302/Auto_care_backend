package com.autocare.autocarebackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BannerAd {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
}
