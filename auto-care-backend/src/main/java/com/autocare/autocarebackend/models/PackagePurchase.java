package com.autocare.autocarebackend.models;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
@Entity
@Table(name="package_purchase")

public class PackagePurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date purchaseDate;
    @NotNull
    private Integer currentAdCount;
    @NotNull
    private Integer maxAdCount;
    @ManyToOne
    private Packages packages;
    @ManyToOne
    private User user;
    public PackagePurchase(){

    }
    public PackagePurchase(Date purchaseDate,@NotNull Integer currentAdCount,@NotNull Integer maxAdCount,Packages packages,User user){
        this.purchaseDate=purchaseDate;
        this.currentAdCount=currentAdCount;
        this.maxAdCount=maxAdCount;
        this.packages=packages;
        this.user=user;

    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id=id;
    }
    public Date getPurchaseDate(){
        return purchaseDate;
    }
    public void setPurchaseDate(Date purchaseDate){
        this.purchaseDate=purchaseDate;
    }
    public Integer getCurrentAdCount(){
        return currentAdCount;
    }
    public void setCurrentAdCount(Integer currentAdCount){
        this.currentAdCount=currentAdCount;
    }
    public Packages getPackages(){
        return packages;
    }
    public void setPackages(Packages packages){
        this.packages=packages;
    }
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user=user;
    }
    public Integer getMaxAdCount(){
        return maxAdCount;
    }
    public void setMaxAdCount(Integer maxAdCount){
        this.maxAdCount=maxAdCount;
    }
}

