package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.PackagePurchase;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackagesPurchaseRepository extends JpaRepository<PackagePurchase,Long>{
    @Override
    Optional<PackagePurchase> findById(Long aLong);
    Optional <PackagePurchase> findAllByUser(User user);
}
