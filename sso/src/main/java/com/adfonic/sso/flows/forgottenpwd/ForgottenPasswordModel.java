package com.adfonic.sso.flows.forgottenpwd;

import java.io.Serializable;

import org.springframework.binding.validation.ValidationContext;

import com.adfonic.sso.utils.RequestContextUtils;
import com.adfonic.util.ValidationUtils;

public class ForgottenPasswordModel implements Serializable{
    
    private static final long serialVersionUID = -1L;
    
    private static final String MSG_EMAIL_INVALID  = "forgottenpwd.form.emailinvalid";
    
    private String email = null;
    
    public void validateForgottenPwdFormView(ValidationContext context) {
        if (!ValidationUtils.isValidEmailAddress(email)){
            RequestContextUtils.addError(context.getMessageContext(), MSG_EMAIL_INVALID);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    } 
}
