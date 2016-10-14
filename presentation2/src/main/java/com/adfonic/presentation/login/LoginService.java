package com.adfonic.presentation.login;

import com.adfonic.dto.user.UserDTO;


public interface LoginService {

	/**
	 * This method does the following
	 * <ul>
	 *   <li>Get user by email</li>
	 *   <li>Update last Login for this user
	 *   <li>creates a UserDTO from domain object
	 * </ul>
	 * @param email
	 * @return a user or <tt>null</tt> if the email if invalid.
	 */
	public UserDTO doLogin(final String email);
	
	static final String USER_TYPE_ADVERTISER = "advertiser";
    static final String USER_TYPE_AGENCY = "agency";
    static final String USER_TYPE_PUBLISHER = "publisher";
}
 