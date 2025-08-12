package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AdRequest {

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

    @NotBlank
    private String[] images;

    @NotNull
    private Integer flag;

    @NotNull
    private Integer lStatus;

    @NotNull
    private Integer iStatus;

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
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

    public void setDescription(String description){this.description = description ;}

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
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
}
