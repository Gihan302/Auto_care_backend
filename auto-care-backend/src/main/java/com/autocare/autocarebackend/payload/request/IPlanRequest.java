package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class IPlanRequest {
    @NotNull
    private Long adId;

    @NotBlank
    private String planAmt;

    @NotBlank
    private String noOfInstallments;

    @NotBlank
    private String interest;

    @NotBlank
    private String instAmt;

    private String description;

    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }

    public String getPlanAmt() {
        return planAmt;
    }

    public void setPlanAmt(String planAmt) {
        this.planAmt = planAmt;
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

    public String getInstAmt() {
        return instAmt;
    }

    public void setInstAmt(String instAmt) {
        this.instAmt = instAmt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
