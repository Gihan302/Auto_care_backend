package com.autocare.autocarebackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentApplicationDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String income;
    private String employmentStatus;
    private String coverLetter;
    private String status;
    private Date submittedAt;
    private String applicationType; // "Leasing" or "Insurance"
    private String adTitle;
    private Long adId;
    private String planName;
    private String creditScore; // Specific to Leasing, can be null for Insurance
}