package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "package_id")
    private Packages packages;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Payment() {
    }

    public Payment(Long id, Packages packages, Advertisement advertisement, User user) {
        this.id = id;
        this.packages = packages;
        this.advertisement = advertisement;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Packages getPackages() {
        return packages;
    }

    public void setPackages(Packages packages) {
        this.packages = packages;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
