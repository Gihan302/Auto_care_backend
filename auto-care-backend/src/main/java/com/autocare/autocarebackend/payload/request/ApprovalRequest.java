package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;

public class ApprovalRequest {

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}