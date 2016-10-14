package com.adfonic.tools.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.login.LoginService;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.security.SecurityUtils;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;

public class UserSessionManagmentFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSessionManagmentFilter.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserManager userManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (isUserLoggedIn()) { // Check user logged in.

            // Get User from Spring Security Context
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            AdfonicUser adfonicUser = getAdfonicUserFromSecurityContextHolder();

            if (adfonicUser != null) { // Check if we have an Admin User

                LOGGER.debug("We have a Admin User - {}", adfonicUser.getEmail());

                if (!isUserDtoOnUserSessionBean(request, response)) { // We have
                                                                      // no
                                                                      // UserSessionBean
                    // Create UserSessionBean
                    UserDTO userDto = loginService.doLogin(userDetails.getUsername());
                    putUserDtoOnUserSessionBean(request, response, userDto);
                } else {
                    // We have a User on the User Session Bean
                    // Let's check if it the same user, if not replace
                    UserSessionBean userSessionBean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);
                    UserDTO userDTO = (UserDTO) userSessionBean.getMap().get(Constants.USERDTO);

                    if (!userDTO.getEmail().equals(userDetails.getUsername())) {
                        UserDTO userDto = loginService.doLogin(userDetails.getUsername());
                        putUserDtoOnUserSessionBean(request, response, userDto);
                    }
                }

                if (!isAdfonicUserDtoOnUserSessionBean(request, response)) {
                    putAdfonicUserOnUserSessionBean(request, response, adfonicUser);
                }
            } else if (!isUserDtoOnUserSessionBean(request, response)) { // No
                                                                         // admin
                                                                         // user

                UserDTO userDto = loginService.doLogin(userDetails.getUsername());
                putUserDtoOnUserSessionBean(request, response, userDto);

            }
        } else {
            LOGGER.debug("No user logged in");
        }

        chain.doFilter(request, response);
        return;
    }

    private void putAdfonicUserOnUserSessionBean(ServletRequest request, ServletResponse response, AdfonicUser adfonicUser) {
        UserSessionBean bean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

        if (bean == null) {
            LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                    + " is setup on the Session scope. Returning without putting AdfonicUser on UserSessionBean." + " Expect problems.");
        }

        LOGGER.debug("We have a UserSessionBean in the FacesContext");

        if (bean.getMap().containsKey(Constants.ADFONIC_USER)) {
            LOGGER.warn("UserSessionBean already contains a AdfonicUser [{}]. Something's may have gone wrong somewhere."
                    + " We going to replace it with this one [{}]", bean.getMap().get(Constants.ADFONIC_USER), adfonicUser);
        }

        bean.getMap().put(Constants.ADFONIC_USER, adfonicUser);
        LOGGER.debug("Put UserDTO on the UserSessionBean");
    }

    private AdfonicUser getAdfonicUserFromSecurityContextHolder() {
        AdfonicUser adfonicUser = null;
        Authentication switchUserAuthentication = SecurityUtils.getAdfonicUserFromSecurityContextHolder();

        if (switchUserAuthentication == null) {
            LOGGER.debug("No Adfonic User found");
            return adfonicUser;
        }

        FetchStrategy fs = null;
        adfonicUser = userManager.getAdfonicUserByEmail(switchUserAuthentication.getName(), fs);
        return adfonicUser;
    }

    private void putUserDtoOnUserSessionBean(ServletRequest request, ServletResponse response, UserDTO userDto) {
        UserSessionBean bean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

        if (bean == null) {
            LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                    + " is setup on the Session scope. Returning without putting UserDTO on UserSessionBean." + " Expect problems.");
        }

        LOGGER.debug("We have a UserSessionBean in the FacesContext");

        if (bean.getMap().containsKey(Constants.USERDTO)) {
            LOGGER.warn("UserSessionBean already contains a UserDTO [{}]." + " We are going to replace it with this one [{}]", bean
                    .getMap().get(Constants.USERDTO), userDto);
        }

        bean.getMap().put(Constants.USERDTO, userDto);
        LOGGER.debug("Put UserDTO on the UserSessionBean");
    }

    private boolean isUserDtoOnUserSessionBean(ServletRequest request, ServletResponse response) {
        UserSessionBean bean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

        if (bean == null) {
            LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                    + " is setup on the Session scope. Returning false as we have no UserDTO on UserSessionBean." + " Expect problems.");
            return false;
        }

        LOGGER.debug("We have a UserSessionBean in the FacesContext");

        if (bean.getMap().containsKey(Constants.USERDTO)) {
            LOGGER.debug("We have a UserSessionBean in the FacesContext which contain UserDTO");
            return true;
        }

        LOGGER.debug("We have a no UserSessionBean in the FacesContext");
        return false;
    }

    private boolean isAdfonicUserDtoOnUserSessionBean(ServletRequest request, ServletResponse response) {
        UserSessionBean bean = Utils.findBean(FacesUtils.getFacesContext(request, response), Constants.USER_SESSION_BEAN);

        if (bean == null) {
            LOGGER.warn("No UserSessionBean found on the FacesContext. Please make sure UserSessionBean"
                    + " is setup on the Session scope. Returning false as we have no Adfonic User on UserSessionBean."
                    + " Expect problems.");
            return false;
        }

        LOGGER.debug("We have a UserSessionBean in the FacesContext");

        if (bean.getMap().containsKey(Constants.ADFONIC_USER)) {
            LOGGER.debug("We have a UserSessionBean in the FacesContext which contains Adfonic User");
            return true;
        }

        LOGGER.debug("We have a no UserSessionBean in the FacesContext");
        return false;
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
        loginService = null;
        LOGGER.debug("Destroying filter with name={} of class={}", getFilterName(), getClass());
    }

}
