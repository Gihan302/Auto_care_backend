package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Advertisement, Long> {

    @Override
    Optional<Advertisement> findById(Long id);

    boolean existsById(Long id);

    @Override
    List<Advertisement> findAll();

    List<Advertisement> findByUser(User user);

    List<Advertisement> findAllByUser(User user);

    // Get all approved advertisements (flag = 1)
    @Query(value = "SELECT u FROM Advertisement u WHERE u.flag = 1")
    List<Advertisement> getConfirmAd();

    // Get all pending advertisements (flag = 0)
    @Query(value = "SELECT u FROM Advertisement u WHERE u.flag = 0")
    List<Advertisement> getPendingAd();

    // Count remaining/pending ads for a user (flag = 0)
    @Query(value = "SELECT COUNT(a.id) FROM Advertisement a WHERE a.user = :user AND a.flag = 0")
    Long rcount(@Param("user") User user);

    // Count posted/approved ads for a user (flag = 1)
    @Query(value = "SELECT COUNT(a.id) FROM Advertisement a WHERE a.user = :user AND a.flag = 1")
    Long pcount(@Param("user") User user);

    // --- LOGIC FIX ---
    // Get ads not in IPlan for a specific Insurance company.
    // Removed "a.user.id = :uid" to find ads from ALL users.
    @Query(value = "SELECT a FROM Advertisement a WHERE a.id NOT IN (SELECT i.advertisement.id FROM InsurancePlan i WHERE i.user.id = :uid)")
    List<Advertisement> getIPendingAd(@Param("uid") Long uid);

    // --- LOGIC FIX ---
    // Get ads in IPlan for a specific Insurance company.
    // Removed "a.user.id = :uid" to find ads from ALL users.
    @Query(value = "SELECT a FROM Advertisement a WHERE a.id IN (SELECT i.advertisement.id FROM InsurancePlan i WHERE i.user.id = :uid)")
    List<Advertisement> getIConfrimAd(@Param("uid") Long uid);

    // --- LOGIC FIX ---
    // Get ads not in LPlan for a specific Leasing company.
    // Removed "a.user.id = :uid" to find ads from ALL users that this company hasn't added a plan to.
    @Query(value = "SELECT a FROM Advertisement a WHERE a.id NOT IN (SELECT l.advertisement.id FROM LeasingPlan l WHERE l.user.id = :uid)")
    List<Advertisement> getLPendingAd(@Param("uid") Long uid);

    // --- LOGIC FIX ---
    // Get ads in LPlan for a specific Leasing company.
    // Removed "a.user.id = :uid" to find ads from ALL users that this company HAS added a plan to.
    @Query(value = "SELECT a FROM Advertisement a WHERE a.id IN (SELECT l.advertisement.id FROM LeasingPlan l WHERE l.user.id = :uid)")
    List<Advertisement> getLConfrimAd(@Param("uid") Long uid);
}