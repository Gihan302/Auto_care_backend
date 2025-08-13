package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "advertisement")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String t_number;

    @NotBlank
    private String email;

    @NotBlank
    private String location;

    @NotBlank
    private String title;

    @NotBlank
    private String price;

    @NotBlank
    private String v_type;

    @NotBlank
    private String manufacturer;

    @NotBlank
    private String model;

    @NotBlank
    private String v_condition;

    @NotBlank
    private String m_year;

    @NotBlank
    private String r_year;

    @NotBlank
    private String mileage;

    @NotBlank
    private String e_capacity;

    @NotBlank
    private String transmission;

    @NotBlank
    private String fuel_type;

    @NotBlank
    private String colour;

    @NotBlank
    private String description;

    @Column(name = "image1")
    private String image1;

    private String image2;
    private String image3;
    private String image4;
    private String image5;

    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date datetime;

    @NotNull
    private Integer falg;

    @NotNull
    private Integer lStatus;

    @NotNull
    private Integer iStatus;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "advertisement")
    @JsonManagedReference
    private List<IPlan> iPlan;

    @JsonManagedReference
    @OneToMany(mappedBy = "advertisement")
    Set<ReportAd> reportAds;

    @OneToMany(mappedBy = "advertisement")
    @JsonManagedReference
    private List<LPlan> lPlans;

    public Advertisement() {
    }

    public Advertisement(String name, String t_number, String email, String location, String title, String price,
                         String v_type, String manufacturer, String model, String v_condition, String m_year,
                         String r_year, String mileage, String e_capacity, String transmission, String fuel_type,
                         String colour, String description, String image1, String image2, String image3, String image4,
                         String image5, Date datetime, Integer falg, Integer lStatus, Integer iStatus, User user) {
        this.name = name;
        this.t_number = t_number;
        this.email = email;
        this.location = location;
        this.title = title;
        this.price = price;
        this.v_type = v_type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.v_condition = v_condition;
        this.m_year = m_year;
        this.r_year = r_year;
        this.mileage = mileage;
        this.e_capacity = e_capacity;
        this.transmission = transmission;
        this.fuel_type = fuel_type;
        this.colour = colour;
        this.description = description;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.image4 = image4;
        this.image5 = image5;
        this.datetime = datetime;
        this.falg = falg;
        this.lStatus = lStatus;
        this.iStatus = iStatus;
        this.user = user;
    }

    // Getters and setters (omitted here to save space — keep your previous ones)
    // Include all getters & setters exactly as in your original file for compilation.
    // e.g. getImage1(), setImage1(...), getId(), setId(...), ...
    // (Paste your original getters/setters in your project)
    //    public Date getDatetime() {
//        return datetime;
//    }
//
//    public void setDatetime(Date datetime) {
//        this.datetime = datetime;
//    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getImage5() {
        return image5;
    }

    public void setImage5(String image5) {
        this.image5 = image5;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getT_number() {
        return t_number;
    }

    public void setT_number(String t_number) {
        this.t_number = t_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getV_type() {
        return v_type;
    }

    public void setV_type(String v_type) {
        this.v_type = v_type;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getV_condition() {
        return v_condition;
    }

    public void setV_condition(String v_condition) {
        this.v_condition = v_condition;
    }

    public String getM_year() {
        return m_year;
    }

    public void setM_year(String m_year) {
        this.m_year = m_year;
    }

    public Integer getFalg() {
        return falg;
    }

    public void setFalg(Integer falg) {
        this.falg = falg;
    }

    public String getR_year() {
        return r_year;
    }

    public void setR_year(String r_year) {
        this.r_year = r_year;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getE_capacity() {
        return e_capacity;
    }

    public void setE_capacity(String e_capacity) {
        this.e_capacity = e_capacity;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getFuel_type() {
        return fuel_type;
    }

    public void setFuel_type(String fuel_type) {
        this.fuel_type = fuel_type;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
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

    public List<LPlan> getlPlans() {
        return lPlans;
    }

    public void setlPlans(List<LPlan> lPlans) {
        this.lPlans = lPlans;
    }

    public Set<ReportAd> getReportAds() {
        return reportAds;
    }

    public void setReportAds(Set<ReportAd> reportAds) {
        this.reportAds = reportAds;
    }

    public Integer getlStatus() {
        return lStatus;
    }

    public void setlStatus(Integer lStatus) {
        this.lStatus = lStatus;
    }

    public Integer getiStatus() {
        return iStatus;
    }

    public void setiStatus(Integer iStatus) {
        this.iStatus = iStatus;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public List<IPlan> getiPlan() {
        return iPlan;
    }

    public void setiPlan(List<IPlan> iPlan) {
        this.iPlan = iPlan;
    }
}
