package com.autocare.autocarebackend.payload.request;

public class LPlanRequest {
    private String planAmount;
    private String noOfInstallments;
    private String interest;
    private String instAmount;
    private String description;
    private Long adId;

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

    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }
}