package com.adfonic.webservices.interceptor;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.adfonic.domain.PluginVendor;
import com.adfonic.util.BasicAuthUtils;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.AuthenticationException;
import com.byyd.middleware.integrations.service.IntegrationsManager;

public class PluginVendorAuthenticationInterceptor implements WebRequestInterceptor {
    private static final transient Logger LOG = Logger.getLogger(PluginVendorAuthenticationInterceptor.class.getName());

    /**
     * Name of the request attribute where the authenticated User is stored
     */
    private static final String PLUGIN_VENDOR_REQUEST_ATTRIBUTE = "PluginVendorAuthenticationInterceptor.user";
    

    /**
     * Name of the request attribute where the exception is stored if authentication fails
     */
    private static final String EXCEPTION_REQUEST_ATTRIBUTE = "PluginVendorAuthenticationInterceptor.exception";

    private final IntegrationsManager integrationsManager;

    @Autowired
    public PluginVendorAuthenticationInterceptor(IntegrationsManager integrationsManager) {
        this.integrationsManager = integrationsManager;
    }

    public void preHandle(WebRequest webRequest) throws Exception {
        
        HttpServletRequest request = (HttpServletRequest)webRequest.resolveReference(WebRequest.REFERENCE_REQUEST);

        // First see if we've already authenticated the request
        PluginVendor pluginVendor = getAuthenticatedPluginVendorFromRequest(request);
        if (pluginVendor != null) {
            LOG.warning("PluginVendor already set on request, why is this interceptor getting invoked more than once?!");
            return;
        }

        try {
            // Try to authenticate the user from the request
            request.setAttribute(PLUGIN_VENDOR_REQUEST_ATTRIBUTE, authenticatePluginVendor(request));
            
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

    private PluginVendor authenticatePluginVendor(HttpServletRequest request) throws AuthenticationException {
        
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new AuthenticationException(ErrorCode.AUTH_NO_AUTHORIZATION, "No Authorization header");
        }
        
        String[] credentials;
        try {
            credentials = BasicAuthUtils.decodeAuthorizationHeader(authorization);
        } catch (Exception e) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_AUTHORIZATION, "Invalid Authorization header", e);
        }
        
        String email = credentials[0];
        
        PluginVendor pluginVendor = integrationsManager.getPluginVendorByEmail(email);
        
        if (pluginVendor == null) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_EMAIL, "Invalid email: " + email);
        }
        
        String vendorPassword = credentials[1];
        if (!vendorPassword.equals(pluginVendor.getApiPassword())) {
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_DEVELOPER_KEY, "Invalid developer key for " + email);
        }
        
        return pluginVendor;
    }
    
    /**
     * Get the authenticated PluginVendor from the request
     * @return the PluginVendor we authenticated when this interceptor was invoked, or null
     * if the PluginVendor could not be authenticated (in which case an exception should have
     * been stored and will be available using getExceptionFromRequest)
     */
    public static PluginVendor getAuthenticatedPluginVendorFromRequest(HttpServletRequest request) {
        return (PluginVendor)request.getAttribute(PLUGIN_VENDOR_REQUEST_ATTRIBUTE);
    }
    
    /**
     * @return the AuthenticationException that occurred while trying to authenticate,
     * or null if no exception was thrown
     */
    public static AuthenticationException getAuthenticationExceptionFromRequest(HttpServletRequest request) {
        return (AuthenticationException)request.getAttribute(EXCEPTION_REQUEST_ATTRIBUTE);
    }
}
