package com.autocare.autocarebackend.payload.response;

import com.autocare.autocarebackend.models.CarReview;
import java.util.*;

public class ReviewResponse {

    private Long id;
    private ReviewerInfo reviewer;
    private String carMake;
    private String carModel;
    private Integer year;
    private String variant;
    private Double overallRating;
    private Map<String, Integer> categoryRatings;
    private String title;
    private String reviewText;
    private String pros;
    private String cons;
    private List<String> images;
    private String mileage;
    private String ownershipDuration;
    private Boolean verifiedOwner;
    private Integer helpfulCount;
    private Integer viewCount;
    private Integer flag;  // ✅ ADDED THIS
    private Date createdAt;

    // Constructor from CarReview entity
    public ReviewResponse(CarReview review) {
        this.id = review.getId();
        this.reviewer = new ReviewerInfo(review.getUser());
        this.carMake = review.getCarMake();
        this.carModel = review.getCarModel();
        this.year = review.getYear();
        this.variant = review.getVariant();
        this.overallRating = review.getOverallRating();

        // Build category ratings map
        this.categoryRatings = new HashMap<>();
        this.categoryRatings.put("performance", review.getPerformance());
        this.categoryRatings.put("comfort", review.getComfort());
        this.categoryRatings.put("fuelEconomy", review.getFuelEconomy());
        this.categoryRatings.put("safety", review.getSafety());
        this.categoryRatings.put("value", review.getValue());

        this.title = review.getTitle();
        this.reviewText = review.getReviewText();
        this.pros = review.getPros();
        this.cons = review.getCons();

        // Collect image URLs
        this.images = new ArrayList<>();
        if (review.getImage1() != null) this.images.add(review.getImage1());
        if (review.getImage2() != null) this.images.add(review.getImage2());
        if (review.getImage3() != null) this.images.add(review.getImage3());
        if (review.getImage4() != null) this.images.add(review.getImage4());
        if (review.getImage5() != null) this.images.add(review.getImage5());
        if (review.getImage6() != null) this.images.add(review.getImage6());
        if (review.getImage7() != null) this.images.add(review.getImage7());
        if (review.getImage8() != null) this.images.add(review.getImage8());

        this.mileage = review.getMileage();
        this.ownershipDuration = review.getOwnershipDuration();
        this.verifiedOwner = review.getVerifiedOwner();
        this.helpfulCount = review.getHelpfulCount();
        this.viewCount = review.getViewCount();
        this.flag = review.getFlag();  // ✅ ADDED THIS
        this.createdAt = review.getCreatedAt();
    }

    // Inner class for reviewer info
    public static class ReviewerInfo {
        private String fname;
        private String lname;

        public ReviewerInfo(com.autocare.autocarebackend.models.User user) {
            this.fname = user.getFname();
            this.lname = user.getLname();
        }

        public String getFname() { return fname; }
        public void setFname(String fname) { this.fname = fname; }
        public String getLname() { return lname; }
        public void setLname(String lname) { this.lname = lname; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReviewerInfo getReviewer() { return reviewer; }
    public void setReviewer(ReviewerInfo reviewer) { this.reviewer = reviewer; }

    public String getCarMake() { return carMake; }
    public void setCarMake(String carMake) { this.carMake = carMake; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }

    public Map<String, Integer> getCategoryRatings() { return categoryRatings; }
    public void setCategoryRatings(Map<String, Integer> categoryRatings) { this.categoryRatings = categoryRatings; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getPros() { return pros; }
    public void setPros(String pros) { this.pros = pros; }

    public String getCons() { return cons; }
    public void setCons(String cons) { this.cons = cons; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getMileage() { return mileage; }
    public void setMileage(String mileage) { this.mileage = mileage; }

    public String getOwnershipDuration() { return ownershipDuration; }
    public void setOwnershipDuration(String ownershipDuration) { this.ownershipDuration = ownershipDuration; }

    public Boolean getVerifiedOwner() { return verifiedOwner; }
    public void setVerifiedOwner(Boolean verifiedOwner) { this.verifiedOwner = verifiedOwner; }

    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    // ✅ ADDED GETTER AND SETTER FOR FLAG
    public Integer getFlag() { return flag; }
    public void setFlag(Integer flag) { this.flag = flag; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}