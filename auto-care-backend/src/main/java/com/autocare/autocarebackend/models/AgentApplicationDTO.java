package com.autocare.autocarebackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentApplicationDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address; // New field
    private String income; // New field
    private String employmentStatus; // New field
    private String status;
    private Date submittedAt;
    private String applicationType; // "Leasing" or "Insurance"
    private String adTitle;
    private Long adId;
}
