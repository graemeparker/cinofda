package com.adfonic.beans;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.adfonic.domain.AdfonicUser;

/**
 * General unchecked exception that includes contextual info about the
 * request when generating its message.  In the app, if an unexpected and
 * unrecoverable situation occurs (i.e. some required thing is somehow
 * null), this exception is preferred to other unchecked exceptions (i.e.
 * RuntimeException or IllegalStateException) which won't provide any
 * useful information when being displayed in a stack trace.  This class
 * will end up displaying the logged-in User, the request URI, and
 * any posted parameters (or query string) whenever that info is available.
 * @author Dan Checkoway
 */
public class AdminGeneralException extends IllegalStateException {
    private static final transient Logger LOG = Logger.getLogger(AdminGeneralException.class.getName());
    
    private static final long serialVersionUID = 1L;

    public AdminGeneralException(String message) {
        super(message);
    }

    public AdminGeneralException(String message, Throwable t) {
        super(message, t);
    }

    public AdminGeneralException(Throwable t) {
        super(t);
    }

    @Override
    public String getMessage() {
        // Grab as much contextual info about the request as possible,
        // and include it in the message.  Just make sure we don't cause
        // another exception to be thrown just by doing this.

        AdfonicUser adfonicUser = null;
        try {
            adfonicUser = BaseBean.adfonicUser();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get AdfonicUser", e);
        }

        HttpServletRequest request = null;
        try {
            request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get HttpServletRequest", e);
        }

        // Append our context to the end of the exception's existing message
        return new StringBuilder(super.getMessage())
            .append(" (AdfonicUser=")
            .append(adfonicUser == null ? "null" : adfonicUser.getEmail())
            .append(", uri=")
            .append(request == null ? "null" : request.getRequestURI())
            .append(", parameters=")
            .append(request == null ? "null" : request.getParameterMap())
            .append(")")
            .toString();
    }
}
