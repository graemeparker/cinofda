package com.adfonic.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adfonic.beans.BaseBean;
import com.adfonic.domain.AdfonicUser;
import com.byyd.middleware.account.service.UserManager;

/**
 * Filter that takes care of two things:
 *
 * 1. When somebody gets bounced back to us after Spring Security sent them
 * to SSO login, this filter recognizes the AdfonicUser and stores it in the
 * session.  Formerly what AdminLoginBean did.
 *
 * 2. If they got bounced back to us from SSO login and we detect a saved
 * request, this filter will redirect to that originally requested URL.
 */
public class PostSecurityLoginFilter extends OncePerRequestFilter {
    private static final transient Logger LOG = Logger.getLogger(PostSecurityLoginFilter.class.getName());

    private final UserManager userManager;
    private final RequestCache requestCache;
    
    @Autowired
    public PostSecurityLoginFilter(UserManager UserManager) {
        this(UserManager, new HttpSessionRequestCache());
    }

    public PostSecurityLoginFilter(UserManager UserManager, RequestCache requestCache) {
        this.userManager = UserManager;
        this.requestCache = requestCache;
    }
    
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        AdfonicUser adfonicUser = detectAdfonicUser(request);

        // Only bother checking for a saved request if the user is logged in
        if (adfonicUser != null) {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null && savedRequest.getRedirectUrl() != null) {
                requestCache.removeRequest(request, response);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Detected saved request, redirecting to: " + savedRequest.getRedirectUrl());
                }
                response.sendRedirect(savedRequest.getRedirectUrl());
                return;
            }
        }

        // No redirect...just handle the request (normal logged-in scenario)
        filterChain.doFilter(request, response);
    }

    /**
     * Detect a logged-in AdfonicUser on the request.  If an AdfonicUser is
     * already established, return that.  Otherwise, see if the user has
     * just bounced back from SSO login.
     * @see BaseBean.adfonicUser
     * @see BaseBean.setAdfonicUser
     */
    AdfonicUser detectAdfonicUser(HttpServletRequest request) {
        AdfonicUser adfonicUser = BaseBean.adfonicUser(request);
        if (adfonicUser != null) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Already logged in: " + adfonicUser.getLoginName());
            }
            return adfonicUser;
        }

        // See if the user just bounced back from SSO login
        String email = request.getRemoteUser();
        
        if (StringUtils.isNotBlank(email)) {
            adfonicUser = userManager.getAdfonicUserByEmail(email);
            if (adfonicUser != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Successfully authenticated: " + adfonicUser.getLoginName());
                }
                // Set 'em in session
                BaseBean.setAdfonicUser(request, adfonicUser);
                return adfonicUser;
            } else {
                LOG.warning("Unrecognized email: " + email);
            }
        }

        return null; // not logged in
    }
}