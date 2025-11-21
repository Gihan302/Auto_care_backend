package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.LeasingPlan;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeasingPlanRepository extends JpaRepository<LeasingPlan, Long> {
    List<LeasingPlan> findByUser(User user);
    List<LeasingPlan> findAllByAdvertisement_Id(Long adId);
}
