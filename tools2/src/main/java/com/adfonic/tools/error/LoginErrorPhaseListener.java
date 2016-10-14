package com.adfonic.tools.error;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.WebAttributes;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.util.Constants;

public class LoginErrorPhaseListener implements PhaseListener {
    private static final long serialVersionUID = -1216620620302322995L;

    @Override
    public void beforePhase(final PhaseEvent arg0) {
        Exception e = (Exception) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .get(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (e instanceof BadCredentialsException || e instanceof AuthenticationServiceException) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(WebAttributes.AUTHENTICATION_EXCEPTION, null);

            addFacesMessage(FacesMessage.SEVERITY_ERROR, Constants.LOGINFORM, null, Constants.KEY_MESSAGE_USER_NOT_FOUND);
        }
    }

    @Override
    public void afterPhase(final PhaseEvent arg0) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    protected void addFacesMessage(Severity severity, String formId, String summaryKey, String detailKey) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = FacesUtils.getFacesMessageById(severity, summaryKey, detailKey);
        fc.addMessage(formId, fm);
    }

}
