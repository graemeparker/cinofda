package com.adfonic.sso.exceptions;

import org.springframework.webflow.execution.RequestContext;

import com.adfonic.domain.User;
import com.adfonic.sso.utils.RequestContextUtils;

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
public class SsoGeneralException extends IllegalStateException {
    
    private static final long serialVersionUID = -1015943853064663967L;
    
    private final User user;
    private final RequestContext requestContext;
    
    public SsoGeneralException(RequestContext context, User user, String message) {
        super(message);
        this.requestContext = context;
        this.user = user;
    }
    
    public SsoGeneralException(RequestContext context, User user, String message, Throwable t) {
        super(message, t);
        this.requestContext = context;
        this.user = user;
    }
    
    public SsoGeneralException(RequestContext context, User user, Throwable t) {
        super(t);
        this.requestContext = context;
        this.user = user;
    }

    @Override
    public String getMessage() {
        // Append our context to the end of the exception's existing message
        return new StringBuilder(super.getMessage())
            .append(" (User=")
            .append(user == null ? "null" : user.getEmail())
            .append(", uri=")
            .append(requestContext == null ? "null" : RequestContextUtils.getRequestURI(requestContext))
            .append(", parameters=")
            .append(requestContext == null ? "null" : RequestContextUtils.getParameterMap(requestContext))
            .append(")")
            .toString();
    }
}