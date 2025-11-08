//package com.autocare.autocarebackend.models;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//
//@Entity
//@Table(name = "leasing_plans")
//public class LeasingPlan {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank
//    private String planName;
//
//    @NotBlank
//    private String vehicleType;
//
//    @NotBlank
//    private String leaseTerm;
//
//    @NotBlank
//    private String interestRate;
//
//    @NotBlank
//    private String monthlyPayment;
//
//    private String description;
//
//    @ManyToOne
//    private User user;
//
//    public LeasingPlan() {
//    }
//
//    public LeasingPlan(String planName, String vehicleType, String leaseTerm, String interestRate, String monthlyPayment, String description, User user) {
//        this.planName = planName;
//        this.vehicleType = vehicleType;
//        this.leaseTerm = leaseTerm;
//        this.interestRate = interestRate;
//        this.monthlyPayment = monthlyPayment;
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
//    public String getVehicleType() {
//        return vehicleType;
//    }
//
//    public void setVehicleType(String vehicleType) {
//        this.vehicleType = vehicleType;
//    }
//
//    public String getLeaseTerm() {
//        return leaseTerm;
//    }
//
//    public void setLeaseTerm(String leaseTerm) {
//        this.leaseTerm = leaseTerm;
//    }
//
//    public String getInterestRate() {
//        return interestRate;
//    }
//
//    public void setInterestRate(String interestRate) {
//        this.interestRate = interestRate;
//    }
//
//    public String getMonthlyPayment() {
//        return monthlyPayment;
//    }
//
//    public void setMonthlyPayment(String monthlyPayment) {
//        this.monthlyPayment = monthlyPayment;
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
