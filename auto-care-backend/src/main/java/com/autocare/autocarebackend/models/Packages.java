package com.autocare.autocarebackend.models;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.util.Date;
@Entity
@Table(name = "packages")

public class Packages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkgId;

    private String packageName;

    private String price;

    private Integer maxAd;

    @Column(name="creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern="yyyy-MM--dd")
    private Date creationDate;

    @Column(name="ending_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date endingDate;

    public Packages(){

    }

    public Packages(String packageName,String price,Integer maxAd,Date creationDate,Date endingDate){
        this.packageName=packageName;
        this.price=price;
        this.maxAd=maxAd;
        this.creationDate=creationDate;
        this.endingDate=endingDate;
    }
    public Long getPkgId(){
        return pkgId;
    }
    public void setPkgId(Long pkgId){
        this.pkgId=pkgId;
    }
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