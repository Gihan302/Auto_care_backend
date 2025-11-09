package com.autocare.autocarebackend.payload.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class BannerAdRequest {

    @NotBlank
    private String image;

    private String targetUrl;

    private String title;

    private String description;

    private Integer displayOrder;
}
