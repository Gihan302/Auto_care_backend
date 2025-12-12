package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.models.Payment;
import com.autocare.autocarebackend.models.Packages;
import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.security.services.PaymentDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class PaymentController {

    @Autowired
    PaymentDetailsImpl orderDetails;

    @PostMapping("/payment/{uid}/{pid}")
    public Payment savePayment(@RequestBody Payment payment, @PathVariable("uid") Long uid,@PathVariable("pid") Long pid) {
        System.out.println("=============================");
        User userObj = new User();
        userObj.setId(uid);

        Packages packageObj = new Packages();
        packageObj.setPkgId(pid);

        Payment paymentObj = new Payment();
        paymentObj.setUser(userObj);
        paymentObj.setPackages(packageObj);
        paymentObj.setId(payment.getId());

        return orderDetails.saveOrder(paymentObj);
    }

    @GetMapping("/payment")
    public List<Payment> getAllPayments() {
        return orderDetails.getPayments();
    }
}
