//package com.autocare.autocarebackend.models;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//
//@Entity
//@Table(name = "insurance_plans")
//public class InsurancePlan {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank
//    private String planName;
//
//    @NotBlank
//    private String coverage;
//
//    @NotBlank
//    private String price;
//
//    private String description;
//
//    @ManyToOne
//    private User user;
//
//    public InsurancePlan() {
//    }
//
//    public InsurancePlan(String planName, String coverage, String price, String description, User user) {
//        this.planName = planName;
//        this.coverage = coverage;
//        this.price = price;
//        this.description = description;
//        this.user = user;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getPlanName() {
//        return planName;
//    }
//
//    public void setPlanName(String planName) {
//        this.planName = planName;
//    }
//
//    public String getCoverage() {
//        return coverage;
//    }
//
//    public void setCoverage(String coverage) {
//        this.coverage = coverage;
//    }
//
//    public String getPrice() {
//        return price;
//    }
//
//    public void setPrice(String price) {
//        this.price = price;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//}
