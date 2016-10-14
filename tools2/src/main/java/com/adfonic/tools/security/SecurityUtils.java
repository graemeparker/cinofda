package com.adfonic.tools.security;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

public class SecurityUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);
    
    
    public static Authentication getAdfonicUserFromSecurityContextHolder() {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
          Authentication switchUserAuthentication = null;
    
        // iterate over granted authorities and find the 'switch user' authority
        Collection<? extends GrantedAuthority> authorities = current.getAuthorities();

        for (GrantedAuthority auth : authorities) {
            // check for switch user type of authority
            if (auth instanceof SwitchUserGrantedAuthority) {
                switchUserAuthentication = ((SwitchUserGrantedAuthority) auth).getSource();
                LOGGER.debug("Found original switch user granted authority [{}]", switchUserAuthentication);
                break;
            }
        }
        return switchUserAuthentication;
    }
    
    public static boolean hasUserRoles(List<String> roles) {
        boolean result = false;
            if(roles!=null){
                Authentication current = SecurityContextHolder.getContext().getAuthentication();
                Collection<? extends GrantedAuthority> authorities = current.getAuthorities();
                Iterator<? extends GrantedAuthority> grantIt = authorities.iterator();
                boolean cont = true;
                    while (grantIt.hasNext() && cont){
                        GrantedAuthority grAuth = grantIt.next();
                            String roleName = grAuth.getAuthority();
                                if(roles.contains(roleName)){
                                    cont = false;
                                    result = true;
                                }
                    }
                    return result;
            }else{
                return result;
            }
    }
    
    public static void cleanAuthenticationSecurityInfo() {
        //Removing current authentication
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
