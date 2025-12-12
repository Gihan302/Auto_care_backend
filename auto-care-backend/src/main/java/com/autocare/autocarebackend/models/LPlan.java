package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "lplan")
public class LPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String planAmount;

    @NotBlank
    private String noOfInstallments;

    @NotBlank
    private String interest;

    @NotBlank
    private String instAmount;

    private String description;

    @ManyToOne
    private User user;

    @ManyToOne
    @JsonBackReference
    private Advertisement advertisement;

    public LPlan() {
    }

    public LPlan(@NotBlank String planAmount, @NotBlank String noOfInstallments, @NotBlank String interest, @NotBlank String instAmount, String description, User user, Advertisement advertisement) {
        this.planAmount = planAmount;
        this.noOfInstallments = noOfInstallments;
        this.interest = interest;
        this.instAmount = instAmount;
        this.description = description;
        this.user = user;
        this.advertisement = advertisement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }
}
