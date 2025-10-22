package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.UserInsuranceCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInsuranceCompanyRepository extends JpaRepository<UserInsuranceCompany, Long> {
    List<UserInsuranceCompany> findByUserIdAndPlanStatus(Long userId, String planStatus);

    List<UserInsuranceCompany> findByUserIdAndCompanyName(Long userId, String companyName);
}