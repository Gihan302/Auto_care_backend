package com.autocare.autocarebackend.controllers;


import com.autocare.autocarebackend.models.ReportAd;
import com.autocare.autocarebackend.repository.ReportAdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/reportad")
public class ReportAdController {

    @Autowired
    ReportAdRepository reportAdRepository;

    @GetMapping("/getallreport")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public List<ReportAd>getAllReportAd(){
        return reportAdRepository.findAll();
    }

}

