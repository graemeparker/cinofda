package com.adfonic.webservices.interceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.adfonic.domain.Company_;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.adfonic.util.BasicAuthUtils;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.AuthenticationException;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class AuthenticationInterceptor implements WebRequestInterceptor {
    private static final transient Logger LOG = Logger.getLogger(AuthenticationInterceptor.class.getName());

    /**
     * Name of the request attribute where the authenticated User is stored
     */
    private static final String USER_REQUEST_ATTRIBUTE = "AuthenticationInterceptor.user";

    /**
     * Name of the request attribute where the exception is stored if authentication fails
     */
    private static final String EXCEPTION_REQUEST_ATTRIBUTE = "AuthenticationInterceptor.exception";

    private static final FetchStrategy USER_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(User_.company)
        .addLeft(User_.roles)
        .addLeft(User_.advertisers) // TODO: remove this, it's expensive-ish
        .addInner(Company_.publisher) // TODO: remove this, it's expensive-ish
        .addLeft(Company_.advertisers) // TODO: remove this, it's expensive-ish
        .build();

    private final CompanyManager companyManager;
    private final UserManager userManager;

    @Autowired
    public AuthenticationInterceptor(CompanyManager companyManager, UserManager userManager) {
        this.companyManager = companyManager;
        this.userManager = userManager;
    }

    public void preHandle(WebRequest webRequest) throws Exception {
        HttpServletRequest request = (HttpServletRequest)webRequest.resolveReference(WebRequest.REFERENCE_REQUEST);

        // First see if we've already authenticated the request
        User user = getAuthenticatedUserFromRequest(request);
        if (user != null) {
            LOG.warning("User already set on request, why is this interceptor getting invoked more than once?!");
            return;
        }

        try {
            // Try to authenticate the user from the request
            request.setAttribute(USER_REQUEST_ATTRIBUTE, authenticateUser(request));
        } catch (AuthenticationException e) {
            // Store the exception as a request attribute so that the controller
            // can access it later if/when it requires authentication.  As of this
            // point in the request, this auth failure is "silent" and doesn't inhibit
            // the completion request.  Controllers can handle this however they want.
            // See AbstractAdfonicWebService.authenticate()
            request.setAttribute(EXCEPTION_REQUEST_ATTRIBUTE, e);
        }
    }

    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        // Nothing to do here
    }

    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        // Nothing to do here
    }

    private User authenticateUser(HttpServletRequest request) throws AuthenticationException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new AuthenticationException(ErrorCode.AUTH_NO_AUTHORIZATION, "No Authorization header");
        }

        String[] creds;
        try {
            creds = BasicAuthUtils.decodeAuthorizationHeader(authorization);
        } catch (Exception e) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_AUTHORIZATION, "Invalid Authorization header", e);
        }

        String email = creds[0];
        User user = userManager.getUserByEmail(email, USER_FETCH_STRATEGY);
        if (user == null) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_EMAIL, "Invalid email: " + email);
        }

        String developerKey = creds[1];
        if (!developerKey.equals(user.getDeveloperKey())) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_DEVELOPER_KEY, "Invalid developer key for " + email);
        }
        
        boolean accessAllowed = companyManager.isIpInWhiteList(request.getRemoteAddr(), user.getCompany().getId());
        if(!accessAllowed){
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_IP_ADDRESS, "IP address " + request.getRemoteAddr() + " is restricted for " + user.getCompany().getName());
        }
        

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder message = new StringBuilder()
                .append("Authenticated User id=").append(user.getId()).append(" for ")
                .append(request.getMethod()).append(" ").append(request.getRequestURI());
            if (!StringUtils.isEmpty(request.getQueryString())) {
                message.append('?').append(request.getQueryString());
            }
            LOG.fine(message.toString());
        }

        return user;
    }

    /**
     * Get the authenticated user from the request
     * @return the User we authenticated when this interceptor was invoked, or null
     * if the user could not be authenticated (in which case an exception should have
     * been stored and will be available using getExceptionFromRequest)
     */
    public static User getAuthenticatedUserFromRequest(HttpServletRequest request) {
        return (User)request.getAttribute(USER_REQUEST_ATTRIBUTE);
    }

    /**
     * @return the AuthenticationException that occurred while trying to authenticate,
     * or null if no exception was thrown
     */
    public static AuthenticationException getAuthenticationExceptionFromRequest(HttpServletRequest request) {
        return (AuthenticationException)request.getAttribute(EXCEPTION_REQUEST_ATTRIBUTE);
    }
}
