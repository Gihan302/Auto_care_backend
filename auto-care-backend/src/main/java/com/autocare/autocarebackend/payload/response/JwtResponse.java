package com.autocare.autocarebackend.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;
import java.util.Date;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String fname;
    private String lname;
    private String tnumber;
    private String username;
    private String nic;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String cName;
    private String address;
    private String regNum;
    private List<String> roles;
    private String imgId;



    public JwtResponse(String accessToken, Long id, String fname, String lname, String tnumber, String username, String nic, Date date,String cName, String address, String regNum, List<String> roles, String imgId) {
        this.token = accessToken;
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.tnumber = tnumber;
        this.username = username;
        this.nic = nic;
        this.date = date;
        this.cName = cName;
        this.address = address;
        this.regNum = regNum;
        this.roles = roles;
        this.imgId = imgId;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getTnumber() {
        return tnumber;
    }

    public void setTnumber(String tnumber) {
        this.tnumber = tnumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegNum() {
        return regNum;
    }

    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }
}
