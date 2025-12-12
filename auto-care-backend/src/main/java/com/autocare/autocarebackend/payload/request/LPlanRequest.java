package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LPlanRequest {

    @NotNull
    private Long adId;

    @NotBlank
    private String planAmount;

    @NotBlank
    private String noOfInstallments;

    @NotBlank
    private String interest;

    @NotBlank
    private String instAmount;

    private String description;

    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }

    public String getPlanAmount() {
        return planAmount;
    }

    public void setPlanAmount(String planAmount) {
        this.planAmount = planAmount;
    }

    public String getNoOfInstallments() {
        return noOfInstallments;
    }

    public void setNoOfInstallments(String noOfInstallments) {
        this.noOfInstallments = noOfInstallments;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getInstAmount() {
        return instAmount;
    }

    public void setInstAmount(String instAmount) {
        this.instAmount = instAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}