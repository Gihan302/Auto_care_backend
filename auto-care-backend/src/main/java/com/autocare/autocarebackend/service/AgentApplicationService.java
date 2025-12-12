
package com.autocare.autocarebackend.service;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import com.autocare.autocarebackend.models.InsuranceApplication;
import com.autocare.autocarebackend.models.LeasingApplication;
import com.autocare.autocarebackend.repository.AgentApplicationRepository;
import com.autocare.autocarebackend.repository.InsuranceApplicationRepository;
import com.autocare.autocarebackend.repository.LeasingApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentApplicationService {

    private final AgentApplicationRepository agentApplicationRepository;
    private final LeasingApplicationRepository leasingApplicationRepository;
    private final InsuranceApplicationRepository insuranceApplicationRepository;

    public Page<AgentApplicationDTO> getApplicationsForAgent(Long agentId, String status, String search, int page, String[] sort) {
        return agentApplicationRepository.findApplicationsByAgentId(agentId, status, search, page, sort);
    }

    public void approveApplication(Long applicationId) {
        Optional<LeasingApplication> leasingApplication = leasingApplicationRepository.findById(applicationId);
        if (leasingApplication.isPresent()) {
            LeasingApplication app = leasingApplication.get();
            app.setStatus("Approved");
            leasingApplicationRepository.save(app);
            return;
        }

        Optional<InsuranceApplication> insuranceApplication = insuranceApplicationRepository.findById(applicationId);
        if (insuranceApplication.isPresent()) {
            InsuranceApplication app = insuranceApplication.get();
            app.setStatus("Approved");
            insuranceApplicationRepository.save(app);
            return;
        }

        throw new RuntimeException("Application not found");
    }

    public void rejectApplication(Long applicationId) {
        Optional<LeasingApplication> leasingApplication = leasingApplicationRepository.findById(applicationId);
        if (leasingApplication.isPresent()) {
            LeasingApplication app = leasingApplication.get();
            app.setStatus("Rejected");
            leasingApplicationRepository.save(app);
            return;
        }

        Optional<InsuranceApplication> insuranceApplication = insuranceApplicationRepository.findById(applicationId);
        if (insuranceApplication.isPresent()) {
            InsuranceApplication app = insuranceApplication.get();
            app.setStatus("Rejected");
            insuranceApplicationRepository.save(app);
            return;
        }

        throw new RuntimeException("Application not found");
    }
}
