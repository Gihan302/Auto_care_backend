package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.InsuranceApplication;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceApplicationRepository extends JpaRepository<InsuranceApplication, Long> {
    List<InsuranceApplication> findByPlan_User(User user);
    List<InsuranceApplication> findByUser(User user);
}
