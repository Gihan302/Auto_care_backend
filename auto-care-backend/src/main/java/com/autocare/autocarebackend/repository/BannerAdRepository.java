package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.BannerAdvertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerAdRepository extends JpaRepository<BannerAdvertisement, Long> {

    @Query("SELECT b FROM BannerAdvertisement b ORDER BY b.createdAt DESC")
    List<BannerAdvertisement> findAllOrderByCreatedAtDesc();

    @Query("SELECT b FROM BannerAdvertisement b WHERE b.isActive = true ORDER BY b.displayOrder ASC, b.createdAt DESC")
    List<BannerAdvertisement> findAllActive();

    @Query("SELECT COUNT(b) FROM BannerAdvertisement b WHERE b.isActive = true")
    Long countActive();
}
