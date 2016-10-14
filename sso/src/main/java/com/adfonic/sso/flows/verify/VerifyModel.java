package com.adfonic.sso.flows.verify;

import java.io.Serializable;

import org.springframework.binding.validation.ValidationContext;

import com.adfonic.sso.utils.RequestContextUtils;

public class VerifyModel implements Serializable{
    
    private static final long serialVersionUID = -4945828562747890839L;

    private static final Short PWD_MIN_LENGHT = 6;
    private static final Short PWD_MAX_LENGHT = 32;
    
    private static final String MSG_PWD_NOT_MATCH  = "resetpwd.form.error.notmatch";
    private static final String MSG_PWD_LENGHT = "resetpwd.form.error.lenght";
    
    private String code = null;
    private Long userId = null;
    private String email = null;
    private String password = null;
    private String passwordRetype = null;
    
    public void validatePwdResetFormView(ValidationContext context) {
        if ((password == null) || (!password.equals(passwordRetype))) {
            RequestContextUtils.addError(context.getMessageContext(), MSG_PWD_NOT_MATCH);
        }
        
        if ((password!=null)&&
            ( (password.length()<PWD_MIN_LENGHT) || (password.length()>PWD_MAX_LENGHT) )){
            RequestContextUtils.addError(context.getMessageContext(), MSG_PWD_LENGHT);
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRetype() {
        return passwordRetype;
    }

    public void setPasswordRetype(String passwordRetype) {
        this.passwordRetype = passwordRetype;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
