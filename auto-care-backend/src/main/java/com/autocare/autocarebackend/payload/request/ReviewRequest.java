package com.autocare.autocarebackend.payload.request;

import jakarta.validation.constraints.*;

public class ReviewRequest {

    // Car Information
    @NotBlank(message = "Car make is required")
    private String carMake;

    @NotBlank(message = "Car model is required")
    private String carModel;

    @NotNull(message = "Year is required")
    private Integer year;

    private String variant;
    private String purchaseType;

    // Overall Rating
    @NotNull(message = "Overall rating is required")
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    private Double overallRating;

    // Category Ratings
    @NotNull @Min(1) @Max(5)
    private Integer performance;

    @NotNull @Min(1) @Max(5)
    private Integer comfort;

    @NotNull @Min(1) @Max(5)
    private Integer fuelEconomy;

    @NotNull @Min(1) @Max(5)
    private Integer safety;

    @NotNull @Min(1) @Max(5)
    private Integer value;

    // Review Details
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Review text is required")
    @Size(min = 100, max = 2000)
    private String reviewText;

    // Pros and Cons
    private String pros;  // Comma-separated
    private String cons;  // Comma-separated

    // Images (Base64 strings)
    private String[] images;

    // Ownership Details
    @NotBlank(message = "Mileage is required")
    private String mileage;

    @NotBlank(message = "Ownership duration is required")
    private String ownershipDuration;

    private String purchaseDate;
    private String purchasePrice;
    private Boolean verifiedOwner;

    // Additional Info
    private String maintenanceExperience;
    private String finalThoughts;

    // Constructors
    public ReviewRequest() {}

    // Getters and Setters
    public String getCarMake() {
        return carMake;
    }

    public void setCarMake(String carMake) {
        this.carMake = carMake;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Double overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getPerformance() {
        return performance;
    }

    public void setPerformance(Integer performance) {
        this.performance = performance;
    }

    public Integer getComfort() {
        return comfort;
    }

    public void setComfort(Integer comfort) {
        this.comfort = comfort;
    }

    public Integer getFuelEconomy() {
        return fuelEconomy;
    }

    public void setFuelEconomy(Integer fuelEconomy) {
        this.fuelEconomy = fuelEconomy;
    }

    public Integer getSafety() {
        return safety;
    }

    public void setSafety(Integer safety) {
        this.safety = safety;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getPros() {
        return pros;
    }

    public void setPros(String pros) {
        this.pros = pros;
    }

    public String getCons() {
        return cons;
    }

    public void setCons(String cons) {
        this.cons = cons;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getOwnershipDuration() {
        return ownershipDuration;
    }

    public void setOwnershipDuration(String ownershipDuration) {
        this.ownershipDuration = ownershipDuration;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Boolean getVerifiedOwner() {
        return verifiedOwner;
    }

    public void setVerifiedOwner(Boolean verifiedOwner) {
        this.verifiedOwner = verifiedOwner;
    }

    public String getMaintenanceExperience() {
        return maintenanceExperience;
    }

    public void setMaintenanceExperience(String maintenanceExperience) {
        this.maintenanceExperience = maintenanceExperience;
    }

    public String getFinalThoughts() {
        return finalThoughts;
    }

    public void setFinalThoughts(String finalThoughts) {
        this.finalThoughts = finalThoughts;
    }
}