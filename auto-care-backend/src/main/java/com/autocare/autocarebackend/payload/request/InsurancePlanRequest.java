package com.autocare.autocarebackend.payload.request;

import jakarta.validation.constraints.NotBlank;

public class InsurancePlanRequest {

    @NotBlank
    private String planName;

    @NotBlank
    private String coverage;

    @NotBlank
    private String price;

    private String description;

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
