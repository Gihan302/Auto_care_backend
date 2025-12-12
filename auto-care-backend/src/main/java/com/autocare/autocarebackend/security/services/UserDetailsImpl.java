package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String fname;
    private String lname;
    private String tnumber;
    private String nic;
    private Date date;
    private String imgId;
    private String cName;
    private String address;
    private String regNum;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority>authorities;

//    public UserDetailsImpl(Long id,String fname,String lname,String tnumber,String nic,String username,Collection<?extends  GrantedAuthority>authorities){
//        this.id=id;
//        this.fname=fname;
//        this.lname=lname;
//        this.tnumber=tnumber;
//        this.nic=nic;
//        this.username=username;
//        this.authorities=authorities;
//    }

    public UserDetailsImpl(Long id,String fname,String lname,String tnumber,String nic,String username,Date date, String imgId, String cName, String address, String regNum ,String password,Collection<?extends  GrantedAuthority>authorities){
        this.id=id;
        this.fname=fname;
        this.lname=lname;
        this.tnumber=tnumber;
        this.nic=nic;
        this.username=username;
        this.date = date;
        this.imgId = imgId;
        this.cName = cName;
        this.address = address;
        this.regNum = regNum;
        this.password=password;
        this.authorities=authorities;
    }

    public static UserDetailsImpl build(User user){
        List<GrantedAuthority>authorities=user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getFname(),
                user.getLname(),
                user.getTnumber(),
                user.getNic(),
                user.getUsername(),
                user.getDate(),
                user.getImgId(),
                user.getcName(),
                user.getAddress(),
                user.getRegNum(),
                user.getPassword(),
                authorities);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getTnumber() {
        return tnumber;
    }

    public String getNic() {
        return nic;
    }

    public String getcName() {
        return cName;
    }

    public String getAddress() {
        return address;
    }

    public String getRegNum() {
        return regNum;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getImgId() {
        return imgId;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }



    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o){
        if(this== o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id,user.id);
    }
}
