package com.adfonic.tools.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.User;
import com.adfonic.dto.user.UserDTO;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;

public class EntityAuditorFilter extends GenericFilterBean {

    protected Logger LOGGER = LoggerFactory.getLogger(EntityAuditorFilter.class);

    @Autowired
    private EntityAuditor entityAuditor;

    @Autowired(required = false)
    private AuditLogJpaListener auditLogJpaListener;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        User user = null;
        AdfonicUser adfonicUser = null;

        if (isUserLoggedIn()) {

            UserSessionBean userSessionBean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

            if (userSessionBean == null) {
                LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                        + " is setup on the Session scope. Expect problems as we cannot bind a user to the EntityAuditor context");
            }

            LOGGER.debug("We have a UserSessionBean in the FacesContext");

            if (!userSessionBean.getMap().containsKey(Constants.USERDTO)) {
                LOGGER.warn("UserSessionBean does not contains a UserDTO. Something's gone wrong somewhere.");
            }

            UserDTO userDTO = (UserDTO) userSessionBean.getMap().get(Constants.USERDTO);
            user = userDTO.getUser();

            if (userSessionBean.getMap().containsKey(Constants.ADFONIC_USER)) {
                adfonicUser = (AdfonicUser) userSessionBean.getMap().get(Constants.ADFONIC_USER);
            }

            try {
                LOGGER.debug("Setting EntityAuditor context to User={} and AdfonicUser={}", user, adfonicUser);
                entityAuditor.bindContext(user, adfonicUser);
                if (auditLogJpaListener != null) {
                    auditLogJpaListener.setContextInfo(user, adfonicUser);
                }
                chain.doFilter(request, response);
            } finally {
                entityAuditor.unbindContext();
                if (auditLogJpaListener != null) {
                    auditLogJpaListener.cleanContextInfo();
                }
            }
            return;
        }

        LOGGER.warn("No User is logged in and as a result we can not bind a User to the EntityAuditor context."
                + "This filter must be placed after a User is authenticated and the user is placed on the UserSessionBean");
    }

    @Override
    public void destroy() {
        entityAuditor = null;
        LOGGER.debug("Destroying filter with name={} of class={}", getFilterName(), getClass());
    }

    /**
     * Checks Spring Security if user is logged in. If not logged SpringSecurity
     * returns Principal as "UNAUTHORISED" string object
     *
     *
     * @return
     */
    private boolean isUserLoggedIn() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null
                && (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails)) {
            LOGGER.debug("We have a valid SecurityContext which has a Principal. Returning true");
            return true;
        }
        LOGGER.debug("No SecurityContext present. Returning false");
        return false;
    }
}
