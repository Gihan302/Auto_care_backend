package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.IPlan;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPlanRepository extends JpaRepository<IPlan, Long> {

    // This method allows us to find all plans created by a specific user
    List<IPlan> findByUser(User user);

    // --- ADD THIS METHOD ---
    // Finds all IPlans linked to a specific Advertisement ID
    List<IPlan> findAllByAdvertisement_Id(Long advertisementId);
}