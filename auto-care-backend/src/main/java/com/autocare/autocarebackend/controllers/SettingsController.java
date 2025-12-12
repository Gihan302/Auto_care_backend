package com.autocare.autocarebackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {

    private Map<String, Object> settings = new HashMap<>();

    public SettingsController() {
        // Initialize with default settings
        settings.put("siteName", "Auto Care");
        settings.put("adminEmail", "admin@autocare.com");
        settings.put("maintenanceMode", false);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> newSettings) {
        settings.putAll(newSettings);
        return ResponseEntity.ok(settings);
    }
}
