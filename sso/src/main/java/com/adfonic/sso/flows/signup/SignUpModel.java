package com.adfonic.sso.flows.signup;

import java.io.Serializable;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import com.adfonic.sso.beans.ApplicationContextProvider;
import com.adfonic.sso.services.UserService;
import com.adfonic.sso.utils.CaptchaUtils;
import com.adfonic.sso.utils.RequestContextUtils;
import com.adfonic.util.ValidationUtils;

public class SignUpModel implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private static final Short PWD_MIN_LENGHT = 6;
    private static final Short PWD_MAX_LENGHT = 32;
    
    // Message codes
    private static final String MSG_EMAIL_INVALID         = "signup.form.email.error.invalid";
    private static final String MSG_EMAIL_DUPLICATED      = "signup.form.email.error.duplicated";
    private static final String MSG_PWD_EMPTY             = "signup.form.password.error.empty";
    private static final String MSG_PWD_NOT_MATCH         = "signup.form.password.error.notmatch";
    private static final String MSG_PWD_LENGHT            = "signup.form.password.error.lenght";
    private static final String MSG_FIRSTNAME_EMPTY       = "signup.form.firstname.error.empty";
    private static final String MSG_LASTNAME_EMPTY        = "signup.form.lastname.error.empty";
    private static final String MSG_COMPANY_EMPTY         = "signup.form.companyname.error.empty";
    private static final String MSG_COUNTRY_EMPTY         = "signup.form.country.error.empty";
    private static final String MSG_ACCOUNTTYPE_EMPTY     = "signup.form.accounttype.error.empty";
    private static final String MSG_DEFAULTCURRENCY_EMPTY = "signup.form.defaultcurrency.error.empty";
    private static final String MSG_HEARABOUT_EMPTY       = "signup.form.hearabout.error.empty";
    private static final String MSG_HEARABOUT_OTHER_EMPTY = "signup.form.hearabout.telluswhere.error.empty";
    private static final String MSG_CAPTCHA_EMPTY         = "signup.form.captcha.error.empty";
    private static final String MSG_CAPTCHA_ERROR         = "signup.form.captcha.error.incorrect";
    private static final String MSG_NONE_VALUE            = "signup.form.country.item.pleaseselect.value";
    private static final String MSG_OTHER_VALUE           = "signup.form.hearabout.option.other.value";
    
    // Fields
    private String email;
    private String password;
    private String passwordRetype;
    private String firstName;
    private String lastName;
    private String company;
    private String country;
    private String timezone;
    private String accountType;
    private String hearAbout;
    private String hearAboutOther;
    private String keepMeInformed;
    private String captchaChallenge;
    private String captchaUserResponse;
    private String defaultCurrency;
    
    
    public void validateSignupFormView(ValidationContext context) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        Locale locale = LocaleContextHolder.getLocale();
        
        // email
        if (StringUtils.isEmpty(email) || !ValidationUtils.isValidEmailAddress(email)){
            RequestContextUtils.addError(context.getMessageContext(), "email", MSG_EMAIL_INVALID);
        }else{
            UserService userService = (UserService) applicationContext.getBean(UserService.class);
            if (userService.getUserByEmail(email)!=null){
                RequestContextUtils.addError(context.getMessageContext(), "email", MSG_EMAIL_DUPLICATED);
            }
        }
        
        // password
        if (StringUtils.isEmpty(password)){
            RequestContextUtils.addError(context.getMessageContext(), "password", MSG_PWD_EMPTY);
        }else{ // passwordRetype
            if (!password.equals(passwordRetype)){
                RequestContextUtils.addError(context.getMessageContext(), "passwordRetype", MSG_PWD_NOT_MATCH);
            }else if ((password!=null)&&
                      ((password.length()<PWD_MIN_LENGHT) || (password.length()>PWD_MAX_LENGHT) )){
                RequestContextUtils.addError(context.getMessageContext(), "password", MSG_PWD_LENGHT);
            }
        }
        
        // firstName
        if (StringUtils.isEmpty(firstName)){
            RequestContextUtils.addError(context.getMessageContext(), "firstName", MSG_FIRSTNAME_EMPTY);
        }
        
        // lastName
        if (StringUtils.isEmpty(lastName)){
            RequestContextUtils.addError(context.getMessageContext(), "lastName", MSG_LASTNAME_EMPTY);
        }
        
        // company
        if (StringUtils.isEmpty(company)){
            RequestContextUtils.addError(context.getMessageContext(), "company", MSG_COMPANY_EMPTY);
        }
        
        // country
        String noneValue = applicationContext.getMessage(MSG_NONE_VALUE, null, locale);
        if (StringUtils.isEmpty(country) || country.equals(noneValue)){
            RequestContextUtils.addError(context.getMessageContext(), "country", MSG_COUNTRY_EMPTY);
        }
        
        // accountType
        if (StringUtils.isEmpty(accountType)){
            RequestContextUtils.addError(context.getMessageContext(), "accountType", MSG_ACCOUNTTYPE_EMPTY);
        }
        
        // defaultCurrency
        if (StringUtils.isEmpty(defaultCurrency)){
            RequestContextUtils.addError(context.getMessageContext(), "defaultCurrency", MSG_DEFAULTCURRENCY_EMPTY);
        }
        
        // hearAbout
        if (StringUtils.isEmpty(hearAbout)){
            RequestContextUtils.addError(context.getMessageContext(), "hearAbout", MSG_HEARABOUT_EMPTY);
        }
        
        // hearAboutOther
        String otherValue = applicationContext.getMessage(MSG_OTHER_VALUE, null, locale);
        if (hearAbout.equals(otherValue) && StringUtils.isBlank(hearAboutOther)){
            RequestContextUtils.addError(context.getMessageContext(), "hearAboutOther", MSG_HEARABOUT_OTHER_EMPTY);
        }
        
        // Captcha (captchaChallenge and captchaUserResponse)
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        this.captchaChallenge = request.getParameter("recaptcha_challenge_field");
        this.captchaUserResponse = request.getParameter("recaptcha_response_field");
        
        if (StringUtils.isEmpty(captchaUserResponse)){
            RequestContextUtils.addError(context.getMessageContext(), "captchaChallenge", MSG_CAPTCHA_EMPTY);
        }else{
            if (!CaptchaUtils.verify(RequestContextUtils.getRemoteAddress(requestContext), 
                                     captchaChallenge, 
                                     captchaUserResponse)){
                RequestContextUtils.addError(context.getMessageContext(), "captchaChallenge", MSG_CAPTCHA_ERROR);
            }
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }

    public String getPasswordRetype() {
        return passwordRetype;
    }

    public void setPasswordRetype(String passwordRetype) {
        this.passwordRetype = passwordRetype.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getHearAbout() {
        return hearAbout;
    }

    public void setHearAbout(String hearAbout) {
        this.hearAbout = hearAbout;
    }

    public String getHearAboutOther() {
        return hearAboutOther;
    }

    public void setHearAboutOther(String hearAboutOther) {
        this.hearAboutOther = hearAboutOther;
    }

    public String getKeepMeInformed() {
        return keepMeInformed;
    }

    public void setKeepMeInformed(String keepMeInformed) {
        this.keepMeInformed = keepMeInformed;
    }

    public String getCaptchaChallenge() {
        return captchaChallenge;
    }

    public void setCaptchaChallenge(String captchaChallenge) {
        this.captchaChallenge = captchaChallenge;
    }

    public String getCaptchaUserResponse() {
        return captchaUserResponse;
    }

    public void setCaptchaUserResponse(String captchaUserResponse) {
        this.captchaUserResponse = captchaUserResponse;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
    
}
