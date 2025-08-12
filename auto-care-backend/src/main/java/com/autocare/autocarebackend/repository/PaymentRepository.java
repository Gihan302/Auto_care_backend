package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
