package com.adfonic.sso.flows.forgottenpwd;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.email.EmailAddressType;
import com.adfonic.sso.beans.ConfigurationBean;
import com.adfonic.sso.services.EmailService;
import com.adfonic.sso.services.UserService;
import com.adfonic.sso.utils.RequestContextUtils;

public class ForgottenPasswordAction extends AbstractAction {
    private static final Logger LOG = Logger.getLogger(ForgottenPasswordAction.class.getName());
    
    //Action's parameters
    private static final String PARAMETER_EMAIL = "email";

    //Other constants
    private static final String EMAIL_SUBJET = "Password reset";
    private static final String EMAIL_TEMPLATE_FORGOTTEN_PASSWORD = "/templates/forgotten_password.html";

    private static final String MSG_USER_NOT_FOUND = "forgottenpwd.form.usernotfound";
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected EmailService emailService;
    
    @Autowired
    private ConfigurationBean configurationBean;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        
        String email = (String) RequestContextUtils.getRequestParameter(context, PARAMETER_EMAIL);
        
        final User user = userService.getUserByEmail(email);
        if ((user == null) || (user.getStatus() == User.Status.DISABLED)) {
            RequestContextUtils.addError(context.getMessageContext(), MSG_USER_NOT_FOUND);
            return error();
        }else{
            // Set the user's status to require a password reset
            VerificationCode vc = null;
            try {
                vc = userService.resetUserPwd(user);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to update status/create vc for user id=" + user.getId(), e);
            }

            if (vc != null) {
                Map<String,String> values = new HashMap<String,String>();
                values.put("companyName", configurationBean.getCompanyName());
                values.put("user.firstName", user.getFirstName());
                values.put("user.lastName", user.getLastName());
                values.put("user.email", user.getEmail());
                values.put("urlRoot", RequestContextUtils.getURLRoot(context, true));
                values.put("urlCustomerSupport",  configurationBean.getTools2BaseUrl() + configurationBean.getCustomerSupportLink());
                values.put("code", vc.getCode());
                
                emailService.sendEmail(EmailAddressType.NOREPLY, 
                                       user.getFormattedEmail(), 
                                       EMAIL_SUBJET, 
                                       values, 
                                       EMAIL_TEMPLATE_FORGOTTEN_PASSWORD);
            }
        }
        
        return success();
    }
}
