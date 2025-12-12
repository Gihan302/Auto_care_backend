package com.autocare.autocarebackend.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

public class PackagesRequest {
    private String packageName;
    private String price;
    private Integer maxAd;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date creationDate;

    @Column(name="ending_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endingDate;

    public String getPackageName(){
        return packageName;
    }
    public void setPackageName(String packageName){
        this.packageName=packageName;
    }
    public String getPrice(){
        return price;
    }
    public void setPrice(String price){
        this.price=price;
    }
    public Integer getMaxAd(){
        return maxAd;
    }
    public void setMaxAd(Integer maxAd){
        this.maxAd=maxAd;
    }
    public Date getCreationDate(){
        return creationDate;
    }
    public void setCreationDate(Date creationDate){
        this.creationDate=creationDate;
    }
    public Date getEndingDate(){
        return endingDate;
    }
    public void setEndingDate(Date endingDate){
        this.endingDate=endingDate;
    }
}
