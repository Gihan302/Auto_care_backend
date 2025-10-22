package com.autocare.autocarebackend.payload.request;

public class ConversationRequest {
    private String companyType;
    private String companyName;

    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}