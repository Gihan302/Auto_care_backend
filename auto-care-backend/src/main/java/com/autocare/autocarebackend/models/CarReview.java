package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Date;

@Entity
@Table(name = "car_reviews")
public class CarReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Car Information
    @NotBlank(message = "Car make is required")
    @Column(nullable = false, length = 100)
    private String carMake;

    @NotBlank(message = "Car model is required")
    @Column(nullable = false, length = 100)
    private String carModel;

    @NotNull(message = "Year is required")
    @Column(nullable = false)
    private Integer year;

    @Column(length = 100)
    private String variant;

    @Column(length = 50)
    private String purchaseType;

    // Overall Rating
    @NotNull(message = "Overall rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    @Column(nullable = false)
    private Double overallRating;

    // Category Ratings
    @NotNull
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer performance;

    @NotNull
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer comfort;

    @NotNull
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer fuelEconomy;

    @NotNull
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer safety;

    @NotNull
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer value;

    // Review Details
    @NotBlank(message = "Review title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Review text is required")
    @Size(min = 100, max = 2000, message = "Review must be between 100 and 2000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reviewText;

    // Pros and Cons (stored as comma-separated)
    @Column(columnDefinition = "TEXT")
    private String pros;

    @Column(columnDefinition = "TEXT")
    private String cons;

    // Images (Cloudinary URLs)
    @Column(length = 500)
    private String image1;

    @Column(length = 500)
    private String image2;

    @Column(length = 500)
    private String image3;

    @Column(length = 500)
    private String image4;

    @Column(length = 500)
    private String image5;

    @Column(length = 500)
    private String image6;

    @Column(length = 500)
    private String image7;

    @Column(length = 500)
    private String image8;

    // Ownership Details
    @NotBlank(message = "Mileage is required")
    @Column(nullable = false, length = 50)
    private String mileage;

    @NotBlank(message = "Ownership duration is required")
    @Column(nullable = false, length = 50)
    private String ownershipDuration;

    @Column(length = 20)
    private String purchaseDate;

    @Column(length = 50)
    private String purchasePrice;

    @Column(nullable = false)
    private Boolean verifiedOwner = false;

    // Additional Info
    @Column(columnDefinition = "TEXT")
    private String maintenanceExperience;

    @Column(columnDefinition = "TEXT")
    private String finalThoughts;

    // Engagement Metrics
    @Column(nullable = false)
    private Integer helpfulCount = 0;

    @Column(nullable = false)
    private Integer viewCount = 0;

    // Approval Status (0 = Pending, 1 = Approved, 2 = Rejected - will be deleted)
    @Column(nullable = false)
    private Integer flag = 0;

    // Timestamps
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (flag == null) {
            flag = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Constructors
    public CarReview() {}

    public CarReview(User user, String carMake, String carModel, Integer year, String variant,
                     String purchaseType, Double overallRating, Integer performance, Integer comfort,
                     Integer fuelEconomy, Integer safety, Integer value, String title, String reviewText,
                     String pros, String cons, String image1, String image2, String image3, String image4,
                     String image5, String image6, String image7, String image8, String mileage,
                     String ownershipDuration, String purchaseDate, String purchasePrice,
                     Boolean verifiedOwner, String maintenanceExperience, String finalThoughts, Integer flag) {
        this.user = user;
        this.carMake = carMake;
        this.carModel = carModel;
        this.year = year;
        this.variant = variant;
        this.purchaseType = purchaseType;
        this.overallRating = overallRating;
        this.performance = performance;
        this.comfort = comfort;
        this.fuelEconomy = fuelEconomy;
        this.safety = safety;
        this.value = value;
        this.title = title;
        this.reviewText = reviewText;
        this.pros = pros;
        this.cons = cons;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.image4 = image4;
        this.image5 = image5;
        this.image6 = image6;
        this.image7 = image7;
        this.image8 = image8;
        this.mileage = mileage;
        this.ownershipDuration = ownershipDuration;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.verifiedOwner = verifiedOwner;
        this.maintenanceExperience = maintenanceExperience;
        this.finalThoughts = finalThoughts;
        this.flag = flag != null ? flag : 0;
    }

    // Helper Methods
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getImage5() {
        return image5;
    }

    public void setImage5(String image5) {
        this.image5 = image5;
    }

    public String getImage6() {
        return image6;
    }

    public void setImage6(String image6) {
        this.image6 = image6;
    }

    public String getImage7() {
        return image7;
    }

    public void setImage7(String image7) {
        this.image7 = image7;
    }

    public String getImage8() {
        return image8;
    }

    public void setImage8(String image8) {
        this.image8 = image8;
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

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}