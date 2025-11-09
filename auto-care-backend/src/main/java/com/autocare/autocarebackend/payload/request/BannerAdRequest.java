package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;

public class BannerAdRequest {

    @NotBlank(message = "Image is required")
    private String image; // Base64 encoded image

    @NotBlank(message = "Target URL is required")
    private String targetUrl;

    private String title;

    private String description;

    private Integer displayOrder;

    // Constructors
    public BannerAdRequest() {
    }

    public BannerAdRequest(String image, String targetUrl, String title, String description) {
        this.image = image;
        this.targetUrl = targetUrl;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}