package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "leasing_plans")
public class LeasingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planName;

    private String vehicleType;

    private String leaseTerm;

    private String interestRate;

    private String monthlyPayment;

    private String description;

    private Double planAmount;

    private Integer noOfInstallments;

    private Double downPayment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "ad_id")
    @JsonIgnore
    private Advertisement advertisement;

    public LeasingPlan() {
    }

    public LeasingPlan(String planName, String vehicleType, String leaseTerm, String interestRate, String monthlyPayment, String description, User user, Advertisement advertisement, Double planAmount, Integer noOfInstallments, Double downPayment) {
        this.planName = planName;
        this.vehicleType = vehicleType;
        this.leaseTerm = leaseTerm;
        this.interestRate = interestRate;
        this.monthlyPayment = monthlyPayment;
        this.description = description;
        this.user = user;
        this.advertisement = advertisement;
        this.planAmount = planAmount;
        this.noOfInstallments = noOfInstallments;
        this.downPayment = downPayment;
    }

    public LeasingPlan(String planName, String vehicleType, String leaseTerm, String interestRate, String monthlyPayment, String description, User user) {
        this.planName = planName;
        this.vehicleType = vehicleType;
        this.leaseTerm = leaseTerm;
        this.interestRate = interestRate;
        this.monthlyPayment = monthlyPayment;
        this.description = description;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLeaseTerm() {
        return leaseTerm;
    }

    public void setLeaseTerm(String leaseTerm) {
        this.leaseTerm = leaseTerm;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(String monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getPlanAmount() {
        return planAmount;
    }

    public void setPlanAmount(Double planAmount) {
        this.planAmount = planAmount;
    }

    public Integer getNoOfInstallments() {
        return noOfInstallments;
    }

    public void setNoOfInstallments(Integer noOfInstallments) {
        this.noOfInstallments = noOfInstallments;
    }

    public Double getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(Double downPayment) {
        this.downPayment = downPayment;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }
}
