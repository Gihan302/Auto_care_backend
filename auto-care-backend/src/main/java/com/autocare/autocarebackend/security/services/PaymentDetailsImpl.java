package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.Payment;
import com.autocare.autocarebackend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentDetailsImpl {
    @Autowired
    private PaymentRepository orderRepo;


    public Payment saveOrder(Payment order){
        return orderRepo.save(order);
    }

    public List<Payment> getPayments() {
        return orderRepo.findAll();
    }
}
