package com.adfonic.presentation.credentials.impl;

import org.jdto.DTOBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Company;
import com.adfonic.domain.User;
import com.adfonic.dto.auth.AdfonicUserDetailsDto;
import com.adfonic.presentation.credentials.AdfonicUserDetailsService;
import com.adfonic.presentation.login.impl.LoginServiceImpl;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;

/**
 * This is the Adfonic implementation of UserDetailsService for Spring Security.
 * It loads the User by email and Return a UserDetail object with Roles from both Company and User.
 * If no user is found it return a <tt>NoUserFoundUserDetails</tt>
 * 
 * @author antonysohal
 * 
 */
public class AdfonicUserDetailsServiceImpl implements AdfonicUserDetailsService {

    private static Logger LOGGER = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private UserManager userManager;

    @Autowired
    private DTOBinder dtoBinder;
    
    private boolean fetchAdfonicUser = false;

    public boolean isFetchAdfonicUser() {
		return fetchAdfonicUser;
	}


	public void setFetchAdfonicUser(boolean fetchAdfonicUser) {
		this.fetchAdfonicUser = fetchAdfonicUser;
	}


	/**
     * This is used by Spring Security to get the User from the credentials
     * provided by end user.
     * 
     */
    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
 
    	if(fetchAdfonicUser) {
    		return loadAdfonicUserByUsername(email);
    	}
    	
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(User.class, "company", JoinType.INNER);
        fs.addEagerlyLoadedFieldForClass(Company.class, "roles", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(User.class, "roles", JoinType.LEFT);

        User user = userManager.getUserByEmail(email, fs);

        if (user == null) {
            LOGGER.info("No User found with email {}", email);
            throw new UsernameNotFoundException("No User found with username: " + email);
        }

        AdfonicUserDetailsDto result = dtoBinder.bindFromBusinessObject(AdfonicUserDetailsDto.class, user);
        LOGGER.debug("Returning = {}", result);
        return result;
    }
    
    
    public UserDetails loadAdfonicUserByUsername(final String email) throws UsernameNotFoundException {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(AdfonicUser.class, "roles", JoinType.LEFT);

        AdfonicUser adfonicUser = userManager.getAdfonicUserByEmail(email, fs);

        if (adfonicUser == null) {
            LOGGER.info("No user found with email {}", email);
            throw new UsernameNotFoundException("No byyd User found with username: " + email);
        }

        AdfonicUserDetailsDto result = dtoBinder.bindFromBusinessObject(AdfonicUserDetailsDto.class, adfonicUser);
        LOGGER.debug("Returning = {}", result);
        return result;
    }
    

}
