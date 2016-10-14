package com.adfonic.tools.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AdfonicUser_;
import com.adfonic.domain.AdminRole;
import com.adfonic.domain.User;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.security.SecurityUtils;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class CustomRestrictFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRestrictFilter.class);

    @Autowired
    protected UserManager userManager;
    @Autowired
    protected CompanyManager companyManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        boolean accessAllowed = true;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isUserLoggedIn()) { // Check user logged in.

            AdfonicUser adfonicUser = getAdfonicUserFromSecurityContextHolder();

            // If no admin user check ip restriction
            if (adfonicUser == null) {
                UserDTO userDto = getUserDtoFromUserSessionBean(request, response);
                if (userDto != null) {
                    LOGGER.debug("Accessing from " + httpRequest.getRemoteAddr());
                    accessAllowed = companyManager.isIpInWhiteList(httpRequest.getRemoteAddr(), userDto.getCompany().getId());
                }
            }
            // Check accounts restriction for restricted admins
            else if (isRestrictedAdminUser(adfonicUser)) {
                UserDTO userDto = getUserDtoFromUserSessionBean(request, response);
                boolean userFound = false;
                if (userDto != null) {
                    for (User u : adfonicUser.getUsers()) {
                        if (u.getEmail().equals(userDto.getEmail())) {
                            userFound = true;
                        }
                    }
                }
                accessAllowed = userFound;
            }
        } else {
            LOGGER.debug("No user logged in");
        }
        if (!accessAllowed) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect(httpRequest.getContextPath() + Constants.ACCESS_DENIED_URL);
        }
        chain.doFilter(request, response);
        return;
    }

    protected AdfonicUser getAdfonicUserFromSecurityContextHolder() {
        AdfonicUser adfonicUser = null;
        Authentication switchUserAuthentication = SecurityUtils.getAdfonicUserFromSecurityContextHolder();

        if (switchUserAuthentication == null) {
            LOGGER.debug("No Adfonic User found");
            return adfonicUser;
        }

        FetchStrategy fs = new FetchStrategyBuilder().addLeft(AdfonicUser_.roles).addLeft(AdfonicUser_.users).build();

        adfonicUser = userManager.getAdfonicUserByEmail(switchUserAuthentication.getName(), fs);
        return adfonicUser;
    }

    protected UserDTO getUserDtoFromUserSessionBean(ServletRequest request, ServletResponse response) {
        UserSessionBean bean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

        if (bean == null) {
            LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                    + " is setup on the Session scope. Returning false as we have no UserDTO on UserSessionBean." + " Expect problems.");
            return null;
        }

        LOGGER.debug("We have a UserSessionBean in the FacesContext");

        if (bean.getMap().containsKey(Constants.USERDTO)) {
            LOGGER.debug("We have a UserSessionBean in the FacesContext which contain UserDTO");
            return (UserDTO) bean.getMap().get(Constants.USERDTO);
        }

        return null;
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
            LOGGER.debug("We have a valid SecurityContext which has a Principal");
            return true;
        }
        LOGGER.debug("No SecurityContext present");
        return false;
    }

    @Override
    public void destroy() {
        userManager = null;
        LOGGER.debug("Destroying filter with name={} of class={}", getFilterName(), getClass());
    }

    private boolean isRestrictedAdminUser(AdfonicUser user) {
        for (AdminRole role : user.getRoles()) {
            if (role.getName().equals("RestrictedAdmin")) {
                return true;
            }
        }
        return false;
    }

}
