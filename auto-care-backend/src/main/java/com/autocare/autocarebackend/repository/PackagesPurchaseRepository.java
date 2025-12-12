package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.PackagePurchase;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender; // NOTE: This import seems unnecessary for a repository
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackagesPurchaseRepository extends JpaRepository<PackagePurchase,Long>{

    // Method to find a purchase by ID (Inherited from JpaRepository, so not strictly needed here)
    @Override
    Optional<PackagePurchase> findById(Long aLong);

    // Find ALL purchases for a specific user (returns Optional of a single purchase, which may be a bug)
    // You should probably change this to List<PackagePurchase> findAllByUser(User user);
    Optional <PackagePurchase> findAllByUser(User user);

    // 🎯 ADD THE MISSING METHOD HERE TO FIX THE ERROR:
    Optional<PackagePurchase> findTopByUserOrderByPurchaseDateDesc(User user);

}