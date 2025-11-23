package com.autocare.autocarebackend.payload.request;

import lombok.Data;

@Data
public class InsuranceApplicationRequest {
    private Long planId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
}
