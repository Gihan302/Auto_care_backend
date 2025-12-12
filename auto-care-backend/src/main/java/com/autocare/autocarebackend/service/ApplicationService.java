package com.autocare.autocarebackend.service;

import com.autocare.autocarebackend.models.ApplicationDTO;
import com.autocare.autocarebackend.models.*;
import com.autocare.autocarebackend.repository.AdRepository;
import com.autocare.autocarebackend.repository.InsuranceApplicationRepository;
import com.autocare.autocarebackend.repository.LeasingApplicationRepository;
import com.autocare.autocarebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final AdRepository adRepository;
    private final LeasingApplicationRepository leasingApplicationRepository;
    private final InsuranceApplicationRepository insuranceApplicationRepository;
    private final UserRepository userRepository;

    public void createApplication(ApplicationDTO applicationDTO, Long userId) {
        Advertisement advertisement = adRepository.findById(applicationDTO.getAdId())
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("leasing".equalsIgnoreCase(applicationDTO.getApplicationType())) {
            LeasingPlan leasingPlan = advertisement.getLeasingPlan();
            if (leasingPlan == null) {
                throw new RuntimeException("No leasing plan found for this advertisement");
            }

            LeasingApplication application = LeasingApplication.builder()
                    .plan(leasingPlan)
                    .user(user)
                    .fullName(applicationDTO.getFullName())
                    .email(applicationDTO.getEmail())
                    .phone(applicationDTO.getPhone())
                    .address(applicationDTO.getAddress())
                    .income(applicationDTO.getIncome())
                    .employmentStatus(applicationDTO.getEmploymentStatus())
                    .coverLetter(applicationDTO.getCoverLetter())
                    .status("Pending")
                    .submittedAt(new Date())
                    .build();
            leasingApplicationRepository.save(application);

        } else if ("insurance".equalsIgnoreCase(applicationDTO.getApplicationType())) {
            if (advertisement.getInsurancePlans() == null || advertisement.getInsurancePlans().isEmpty()) {
                throw new RuntimeException("No insurance plan found for this advertisement");
            }
            // For simplicity, we are taking the first insurance plan associated with the ad
            InsurancePlan insurancePlan = advertisement.getInsurancePlans().get(0);

            InsuranceApplication application = InsuranceApplication.builder()
                    .plan(insurancePlan)
                    .user(user)
                    .fullName(applicationDTO.getFullName())
                    .email(applicationDTO.getEmail())
                    .phone(applicationDTO.getPhone())
                    .address(applicationDTO.getAddress())
                    .coverLetter(applicationDTO.getCoverLetter())
                    .status("Pending")
                    .submittedAt(new Date())
                    .build();
            insuranceApplicationRepository.save(application);
        } else {
            throw new IllegalArgumentException("Invalid application type");
        }
    }
}
