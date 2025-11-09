package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "banner_advertisements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdBy"})
public class BannerAdvertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "clicks_count")
    private Integer clicksCount = 0;

    @Column(name = "impressions_count")
    private Integer impressionsCount = 0;

    // Constructors
    public BannerAdvertisement() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public BannerAdvertisement(String imageUrl, String targetUrl, String title, String description, User createdBy) {
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.isActive = true;
        this.displayOrder = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.clicksCount = 0;
        this.impressionsCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getClicksCount() {
        return clicksCount;
    }

    public void setClicksCount(Integer clicksCount) {
        this.clicksCount = clicksCount;
    }

    public Integer getImpressionsCount() {
        return impressionsCount;
    }

    public void setImpressionsCount(Integer impressionsCount) {
        this.impressionsCount = impressionsCount;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}