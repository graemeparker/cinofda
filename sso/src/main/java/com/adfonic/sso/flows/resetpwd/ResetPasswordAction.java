package com.adfonic.sso.flows.resetpwd;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.adfonic.domain.User;
import com.adfonic.sso.flows.verify.VerifyModel;
import com.adfonic.sso.services.UserService;
import com.adfonic.sso.utils.RequestContextUtils;

public class ResetPasswordAction extends AbstractAction {
    
    //Action's parameters
    private static final String VERIFY_MODEL_NAME = "verifyModel";
    
    // Messages
    private static final String MSG_PWD_NOT_CHANGED = "resetpwd.action.error.general";
    
    // Action events
    private static final String EVENT_ERROR    = "error";
    private static final String EVENT_SUCCESS  = "success";
    
    @Autowired
    private UserService userService;

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        String event = EVENT_ERROR;
        
        // Catching flow model
        VerifyModel verifyModel = (VerifyModel) RequestContextUtils.getModel(context, VERIFY_MODEL_NAME);
        
        if ((verifyModel.getUserId()==null)||(StringUtils.isEmpty(verifyModel.getPassword()))){
            RequestContextUtils.addError(context.getMessageContext(), MSG_PWD_NOT_CHANGED); 
        }else{
            // Updating user
            User user = userService.updateUserPwd(verifyModel.getUserId(), verifyModel.getPassword());
            
            if (user==null){
                RequestContextUtils.addError(context.getMessageContext(), MSG_PWD_NOT_CHANGED); 
            }else{
                event = EVENT_SUCCESS;
            }
        }
        
        return result(event);
    }
}
