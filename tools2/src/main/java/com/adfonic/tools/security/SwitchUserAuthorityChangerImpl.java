package com.adfonic.tools.security;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.switchuser.SwitchUserAuthorityChanger;

/**
 * We need to add 'Admin' role to the Granted Authorities.
 *
 * This is also needed as the Switch User Filter places a granted authority as
 * <tt>GrantedAuthority switchAuthority = new SwitchUserGrantedAuthority(ROLE_PREVIOUS_ADMINISTRATOR, currentAuth);</tt>
 *
 * However the Spring Security tags create a SimpleGrantedAuthority for each
 * roles places in the tag and as a result cannot check simple if the role of
 * ROLE_PREVIOUS_ADMINISTRATOR exists. By adding a new SimpleGrantedAuthority
 * with role we can check now if an admin user is logged in.
 *
 *
 * @author antonysohal
 */
public class SwitchUserAuthorityChangerImpl implements SwitchUserAuthorityChanger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchUserAuthorityChangerImpl.class);

    private String[] rolesToAdd;

    public String[] getRolesToAdd() {
        return rolesToAdd;
    }

    public void setRolesToAdd(String[] rolesToAdd) {
        this.rolesToAdd = rolesToAdd;
    }

    /**
     * Add a new SimpleGrantedAuthority with role 'Admin'
     */
    @Override
    public Collection<? extends GrantedAuthority> modifyGrantedAuthorities(UserDetails targetUser, Authentication currentAuthentication,
            Collection<? extends GrantedAuthority> authoritiesToBeGranted) {
        Collection<GrantedAuthority> result = new ArrayList<GrantedAuthority>(authoritiesToBeGranted);

        for (int i = 0; i < rolesToAdd.length; i++) {
            LOGGER.debug("adding role - [{}]", rolesToAdd[i]);
            result.add(new SimpleGrantedAuthority(rolesToAdd[i]));
        }

        LOGGER.debug("returning modified GrantedAuthorities - {}", result);

        return result;
    }

}
