package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.LeasingApplication;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeasingApplicationRepository extends JpaRepository<LeasingApplication, Long> {
    List<LeasingApplication> findByPlan_User(User user);
    List<LeasingApplication> findByUser(User user);
}
