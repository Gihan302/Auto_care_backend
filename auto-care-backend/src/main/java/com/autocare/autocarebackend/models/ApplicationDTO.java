package com.autocare.autocarebackend.models;

import lombok.Data;

@Data
public class ApplicationDTO {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String income;
    private String employmentStatus;
    private Long adId;
    private String coverLetter;
    private String applicationType; // "leasing" or "insurance"
}
