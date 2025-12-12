package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.ReportAd;
import com.autocare.autocarebackend.repository.ReportAdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportAdDetailsImpl {

    @Autowired
    private ReportAdRepository reportAdRepository;

    public ReportAd saveReportAdDetails(ReportAd reportAd) {
        return reportAdRepository.save(reportAd);
    }
}