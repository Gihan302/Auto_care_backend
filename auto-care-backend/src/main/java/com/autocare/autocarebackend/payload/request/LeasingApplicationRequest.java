package com.autocare.autocarebackend.payload.request;

import lombok.Data;

@Data
public class LeasingApplicationRequest {
    private Long planId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String income;
    private String employmentStatus;
    private String creditScore;
}
