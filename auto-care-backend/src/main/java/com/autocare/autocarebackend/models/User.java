package com.autocare.autocarebackend.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "nic"),
                @UniqueConstraint(columnNames = "username")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String fname;

    @NotBlank
    @Size(max = 20)
    private String lname;

    @NotBlank
    @Size(max = 12)
    private String tnumber;

    @NotBlank
    @Size(max = 20)
    private String nic;

    @NotBlank
    @Size(max = 50)
    @Email
    private String username;

    @NotBlank
    @Size(max = 120)
    private String password;

    @Size(max = 120)
    private String cName;

    @Size(max = 120)
    private String regNum;

    @Size(max = 120)
    private String address;

    private String imgId;

    @Column(name = "register_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    // ============ APPROVAL SYSTEM FIELDS ============
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private EAccountStatus accountStatus; // ✅ REMOVED DEFAULT - will be set by controller

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    // ================================================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // ✅ DEFAULT CONSTRUCTOR - No default status set here
    public User() {
    }

    // ✅ PARAMETERIZED CONSTRUCTOR - No default status set here either
    public User(@NotBlank @Size(max = 20) String fname,
                @NotBlank @Size(max = 20) String lname,
                @NotBlank @Size(max = 12) String tnumber,
                @NotBlank @Size(max = 20) String nic,
                @NotBlank @Size(max = 50) @Email String username,
                @NotBlank @Size(max = 120) String password,
                @Size(max = 120) String cName,
                @Size String regNum,
                @Size String address,
                @NotBlank String imgId,
                Date date) {
        this.fname = fname;
        this.lname = lname;
        this.tnumber = tnumber;
        this.nic = nic;
        this.username = username;
        this.password = password;
        this.cName = cName;
        this.regNum = regNum;
        this.address = address;
        this.imgId = imgId;
        this.date = date;
        // ✅ REMOVED: this.accountStatus = EAccountStatus.APPROVED;
        // Let the controller set the status based on user type
    }

    // ============ EXISTING GETTERS AND SETTERS ============
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

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getRegNum() {
        return regNum;
    }

    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // ============ APPROVAL SYSTEM GETTERS AND SETTERS ============
    public EAccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(EAccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}