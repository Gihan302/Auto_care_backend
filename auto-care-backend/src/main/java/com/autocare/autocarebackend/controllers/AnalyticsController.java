package com.autocare.autocarebackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOverview() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalAds", 100);
        response.put("totalUsers", 50);
        response.put("totalRevenue", 10000);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manufacturers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getManufacturers() {
        Map<String, Object> response = new HashMap<>();
        response.put("Toyota", 20);
        response.put("Honda", 15);
        response.put("Ford", 10);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timeseries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTimeseries() {
        Map<String, Object> response = new HashMap<>();
        response.put("2023-01", 10);
        response.put("2023-02", 15);
        response.put("2023-03", 20);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getVehicleTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("Sedan", 30);
        response.put("SUV", 40);
        response.put("Truck", 30);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("pending", 10);
        response.put("approved", 80);
        response.put("rejected", 10);
        return ResponseEntity.ok(response);
    }
}
