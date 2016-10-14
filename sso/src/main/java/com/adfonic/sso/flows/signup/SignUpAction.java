package com.adfonic.sso.flows.signup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.email.EmailAddressType;
import com.adfonic.sso.beans.ConfigurationBean;
import com.adfonic.sso.services.EmailService;
import com.adfonic.sso.services.SystemService;
import com.adfonic.sso.services.UserService;
import com.adfonic.sso.services.VerificationCodeService;
import com.adfonic.sso.utils.RequestContextUtils;
import com.adfonic.util.AdfonicTimeZone;

public class SignUpAction extends AbstractAction {
    
    private static final Logger LOG = Logger.getLogger(SignUpAction.class.getName());

    private static final String SIGNUP_MODEL_NAME = "signupModel";
    
    private static final String MSG_CREATION_ERROR = "signup.form.generic.error";
    
    private static final String EMAIL_SUBJECT = "Registration confirmation";
    private static final String EMAIL_TEMPLATE_REGISTRATION= "/templates/user_registered.html";
    
    @Autowired
    UserService userService;
    
    @Autowired
    SystemService systemService;
    
    @Autowired
    VerificationCodeService verificationCodeService;
    
    @Autowired
    protected EmailService emailService;
    
    @Autowired
    protected ConfigurationBean configurationBean;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        // Catching flow model
        SignUpModel signUpModel = (SignUpModel) RequestContextUtils.getModel(context, SIGNUP_MODEL_NAME);
        
        User user = null;
        try{
            // Create user
            user = createUser(signUpModel);
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Failed create User", e);
            RequestContextUtils.addError(context.getMessageContext(), "email", MSG_CREATION_ERROR);
            return error();
        }
        
        /*
         * Lets send off the email so we can confirm the address is theirs
         * Assume that the email was sent as the user has been inserted into
         * the database at this point.
         */
        try{
            sendEmail(user, context);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send email to user id=" + (user == null ? "null" : String.valueOf(user.getId())), e);
        }
        
        return success();
    }
    
    public Event resendEmail(SignUpModel signUpModel){
        // Catching the spring webflow context
        RequestContext context = RequestContextHolder.getRequestContext();
        
        // Catching the user
        User user = userService.getUserByEmail(signUpModel.getEmail());
        
        try{
            sendEmail(user, context);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send email to user id=" + (user == null ? "null" : String.valueOf(user.getId())), e);
        }
        
        return success();
    }
    

    private User createUser(SignUpModel signUpModel) {
        // Preparing user roles
        final List<String> roleNames = new ArrayList<String>();
        roleNames.add(Role.USER_ROLE_USER);
        roleNames.add(Role.USER_ROLE_ADMINISTRATOR);
        
        // Preparing country information
        Country country = systemService.getCountry(signUpModel.getCountry());
        
        // Preparing timezone information
        AdfonicTimeZone timezone = AdfonicTimeZone.getAdfonicTimeZoneById(signUpModel.getTimezone());
        
        // Preparing account type
        AccountType accountType = AccountType.valueOf(AccountType.class, signUpModel.getAccountType());
        
        // Preparing default currency
        CurrencyExchangeRate defaultCurrencyExchangeRate = systemService.getDefaultCurrency(signUpModel.getDefaultCurrency());
        
        // Preparing newsletter flag
        Boolean keepmeInformed = Boolean.valueOf(signUpModel.getKeepMeInformed());
        
        // Creating user
        return userService.createUser(signUpModel.getCompany(), 
                                      country, 
                                      signUpModel.getEmail(), 
                                      signUpModel.getFirstName(), 
                                      signUpModel.getLastName(), 
                                      signUpModel.getPassword(), 
                                      signUpModel.getHearAbout(), 
                                      signUpModel.getHearAboutOther(), 
                                      roleNames,  
                                      accountType,
                                      defaultCurrencyExchangeRate,
                                      timezone,
                                      keepmeInformed);
    }
    
    private void sendEmail(User user, RequestContext context){
        VerificationCode vc = verificationCodeService.newVerificationCode(user, VerificationCode.CodeType.REGISTRATION);
        Map<String,String> values = new HashMap<String,String>();
        values.put("wordpressBaseUrl", configurationBean.getWordpressBaseUrl());
        values.put("companyName", configurationBean.getCompanyName());
        values.put("code", vc.getCode());
        values.put("urlRoot", RequestContextUtils.getURLRoot(context, true));
        values.put("urlCustomerSupport",  configurationBean.getTools2BaseUrl() + configurationBean.getCustomerSupportLink());
        
        emailService.sendEmail(EmailAddressType.NOREPLY, 
                               user.getFormattedEmail(), 
                               EMAIL_SUBJECT, 
                               values, 
                               EMAIL_TEMPLATE_REGISTRATION);
        
    }
}
