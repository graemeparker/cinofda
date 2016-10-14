package com.adfonic.dto.user;

import org.jdto.annotation.Source;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.dto.BusinessKeyDTO;

public class AdfonicUserDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "email")
    private String email;

    @Source(value = "loginName")
    private String loginName;

    @Source(value = "firstName")
    private String firstName;

    @Source(value = "lastName")
    private String lastName;

    @Source(value = "password")
    private String password;

    @Source(value = "status")
    private AdfonicUser.Status status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AdfonicUser.Status getStatus() {
        return status;
    }

    public void setStatus(AdfonicUser.Status status) {
        this.status = status;
    }
}
