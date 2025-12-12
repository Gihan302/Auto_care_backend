package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner_advertisements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerAdvertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    private String targetUrl;

    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer displayOrder = 0;

    private int clicksCount = 0;

    private int impressionsCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BannerAdvertisement(String imageUrl, String targetUrl, String title, String description, User createdBy) {
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
    }
}
