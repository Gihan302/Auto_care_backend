package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.UserLeasingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserLeasingCompanyRepository extends JpaRepository<UserLeasingCompany, Long> {

    List<UserLeasingCompany> findByUserIdAndLeaseStatus(Long userId, String leaseStatus);

    List<UserLeasingCompany> findByUserIdAndCompanyName(Long userId, String companyName);

    // ADD THIS NEW METHOD
    @Query("SELECT DISTINCT u.companyName FROM UserLeasingCompany u WHERE u.userId = :userId")
    List<String> findDistinctCompanyNamesByUserId(Long userId);
}