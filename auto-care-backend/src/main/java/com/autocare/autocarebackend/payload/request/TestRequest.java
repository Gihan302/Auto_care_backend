package com.autocare.autocarebackend.payload.request;

import javax.validation.constraints.NotBlank;


public class TestRequest {

    @NotBlank
    private String name;

    @NotBlank
    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
