package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.LPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LPlanRepository extends JpaRepository<LPlan, Long> {

    @Override
    Optional<LPlan> findById(Long id);

    @Override
    List<LPlan> findAll();

    // Get all leasing plans created for a specific advertisement
    List<LPlan> findAllByAdvertisement_Id(Long adId);

    // ✅ NEW: Get all leasing plans created by a specific Leasing Company (User)
    List<LPlan> findByUser_Id(Long userId);

    List<LPlan> findByUserId(Long id);
}
