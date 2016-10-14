package com.adfonic.sso.flows.verify;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.sso.services.UserService;
import com.adfonic.sso.services.VerificationCodeService;
import com.adfonic.sso.utils.RequestContextUtils;

public class VerifyAction extends AbstractAction {
    private static final Logger LOG = Logger.getLogger(VerifyAction.class.getName());
    
    //Action's parameters
    private static final String VERIFY_MODEL_NAME = "verifyModel";

    //Error messages
    private static final String MSG_VC_DOES_NOT_EXIST = "verify.action.error.vcdoesnotexist";
    private static final String MSG_RESETPWD_VERIFICATION_ERROR = "verify.action.error.resetpwd";
    private static final String MSG_CHANGEEMAIL_VERIFICATION_ERROR = "verify.action.error.changeemail";
    private static final String MSG_VC_DOES_NOT_VERIFICABLE = "verify.action.error.notverificable";
    
    // Action events
    private static final String EVENT_ERROR                 = "error";
    private static final String EVENT_HOME                  = "gohome";
    private static final String EVENT_SIGNUP_COMPLETED      = "signupcompleted";
    private static final String EVENT_SIGNUP_ERROR          = "signuperror";
    private static final String EVENT_PWDRESET              = "pwdreset";
    private static final String EVENT_CHANGEEMAIL_COMPLETED = "changeemailcompleted";
    
    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserService userService;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        String event = EVENT_ERROR;
        
        VerifyModel verifyModel = (VerifyModel) RequestContextUtils.getModel(context, VERIFY_MODEL_NAME);
        
        User userInSession = RequestContextUtils.getUserInSession(context);
        // logged in user check
        if ((userInSession != null) && (User.Status.UNVERIFIED != userInSession.getStatus())) {
            // send home if there's no pending verification
            event = EVENT_HOME;
        }
        
        if (!EVENT_HOME.equals(event)){
            final VerificationCode vc = verificationCodeService.getVerificationCode(verifyModel.getCode());
            
            if (vc == null) {
                LOG.log(Level.FINE, "vc lookup null");
                RequestContextUtils.addError(context.getMessageContext(), MSG_VC_DOES_NOT_EXIST);
            }else {
                switch (vc.getCodeType()) {
                    case REGISTRATION:
                        event = verifyRegistrationCode(verifyModel, vc);
                        break;
                    case RESET_PASSWORD:
                        event = verifyResetPasswordCode(context, event, verifyModel, vc);
                        break;
                    case CHANGE_EMAIL:
                        event = verifyChangeEmailCode(context, event, vc);
                        break;
                    case REMEMBER_ME:
                        RequestContextUtils.addError(context.getMessageContext(), MSG_VC_DOES_NOT_VERIFICABLE);
                        break;
                    default:
                        break;
                }
            }
        }
        return result(event);
    }

    private String verifyRegistrationCode(VerifyModel verifyModel, final VerificationCode vc) {
        String event;
        verifyModel.setEmail(vc.getUser().getEmail());
        // Activate the user
        if (userService.verifyUser(vc.getUser(), vc)){
            event = EVENT_SIGNUP_COMPLETED;
        }else{
            event = EVENT_SIGNUP_ERROR;
        }
        return event;
    }

    private String verifyResetPasswordCode(RequestContext context, String event, VerifyModel verifyModel, final VerificationCode vc) {
        String result = event;
        if (verificationCodeService.deleteVerificationCode(vc)){
            // Introduce user id in flow scope
            verifyModel.setUserId(vc.getUser().getId());
            result = EVENT_PWDRESET;
        }else{
            RequestContextUtils.addError(context.getMessageContext(), MSG_RESETPWD_VERIFICATION_ERROR);
        }
        return result;
    }
    
    private String verifyChangeEmailCode(RequestContext context, String event, final VerificationCode vc) {
        String result = event;
        // Activate the user
        if (userService.verifyUser(vc.getUser(), vc)) {
            result = EVENT_CHANGEEMAIL_COMPLETED;
        }else {
            RequestContextUtils.addError(context.getMessageContext(), MSG_CHANGEEMAIL_VERIFICATION_ERROR);
        }
        return result;
    }
}
