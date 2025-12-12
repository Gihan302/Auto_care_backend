package com.autocare.autocarebackend.models;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "iplan")

public class IPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String planAmt;

    @NotBlank
    private String noOfInstallments;

    @NotBlank
    private String interest;

    @NotBlank
    private String instAmt;

    private String description;

    @ManyToOne
    private User user;

    @ManyToOne
    @JsonBackReference
    private Advertisement advertisement;

    public IPlan() {
    }

    public IPlan(@NotBlank String planAmt, @NotBlank String noOfInstallments, @NotBlank String interest, @NotBlank String instAmt, String description, User user, Advertisement advertisement) {
        this.planAmt = planAmt;
        this.noOfInstallments = noOfInstallments;
        this.interest = interest;
        this.instAmt = instAmt;
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
