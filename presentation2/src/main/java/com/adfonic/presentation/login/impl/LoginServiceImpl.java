package com.adfonic.presentation.login.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdto.DTOBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.login.LoginService;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;

@Service
public class LoginServiceImpl implements LoginService {

	private static Logger LOGGER = LoggerFactory.getLogger(LoginServiceImpl.class);

	@Autowired
	private UserManager userManager;

	@Autowired
	private AdvertiserManager advertiserManager;

	@Autowired
	private DTOBinder dtoBinder;
    @Autowired
    private org.dozer.Mapper mapper;
    
	@Override
	@Transactional(readOnly=false)
	public UserDTO doLogin(final String email) {
		LOGGER.debug("Trying to login for user {}", email);

		User user = userManager.getUserByEmail(email);

		if (user == null) {
			LOGGER.info("No user found with email {}", email);
			return null;
		}

		user = userManager.updateLastLogin(user);

		//UserDTO userDTO = dtoBinder.bindFromBusinessObject(UserDTO.class, user);
		
		//Mapper mapper = new DozerBeanMapper();
		UserDTO userDTO = mapper.map(user,UserDTO.class);

		userDTO = setUserType(userDTO, user);

		userDTO = getAdvertiser(userDTO, user);

		userDTO = getPublisher(userDTO, user);

		// Now lets get the User with no FetchStrategy and put it on the DTO
		userDTO.setUser(userManager.getUserByEmail(email, (FetchStrategy) null));

		LOGGER.debug("Returning = {}", userDTO);
		return userDTO;
	}

	/***
	 * Gets the AdvertiserDto associated with the user.
	 * 
	 * @param UserDTO
	 *            dto - which will have the advertiserDto
	 * @para User - Domain class with the Advertiser domain
	 * @return UserDTO
	 * */
	private UserDTO getAdvertiser(final UserDTO dto, final User user) {
		LOGGER.debug("Getting advertiser for user - {} who belongs to company - {}", user.getEmail(), user.getCompany());

		List<Advertiser> advertisers = null;
		if (user.getCompany().isAccountType(AccountType.AGENCY)){
		    advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user,"");
		}
		else{
		    AdvertiserFilter advertiserFilter = new AdvertiserFilter().setCompany(user.getCompany());
	        advertisers = advertiserManager.getAllAdvertisers(advertiserFilter);
		}
        
		// Agency users
		if (user.getCompany().isAccountType(AccountType.AGENCY)) {
		    if(!CollectionUtils.isEmpty(advertisers)){
    			LOGGER.debug("Company - {} has account type of Agency and has one or more advertisers", user.getCompany());	
    
    			Collection<AdvertiserDto> advertisersListDto = getList(AdvertiserDto.class, advertisers);
    			dto.setAdvertiserListDto(advertisersListDto);
    
    			// select the first one
    			dto.setAdvertiserDto(advertisersListDto.iterator().next());
		    }
		    else{
		        LOGGER.debug("User - {} has no advertisers assigned", user);
		        dto.setAdvertiserListDto(new ArrayList<AdvertiserDto>(0));
		    }
		}
		else {
			LOGGER.debug("Company - {} is NOT account type of Agency", user.getCompany());
			// One user, not agency.
			
			Advertiser adv = advertisers.get(0);
			AdvertiserDto advertiserDto = dtoBinder.bindFromBusinessObject(AdvertiserDto.class, adv);
			dto.setAdvertiserListDto(new ArrayList<AdvertiserDto>());
			dto.setAdvertiserDto(advertiserDto);
		}
		return dto;
	}

	/***
	 * Gets the AdvertiserDto associated with the user.
	 * 
	 * @param UserDTO
	 *            dto - which will have the advertiserDto
	 * @para User - Domain class with the Advertiser domain
	 * @return UserDTO
	 * */
	private UserDTO getPublisher(final UserDTO dto, final User user) {
		LOGGER.debug("Getting publisher for user - {} who belongs to company - {}", user.getEmail(), user.getCompany());
		Publisher publisher = user.getCompany().getPublisher();
		if (!user.getCompany().isAccountType(AccountType.AGENCY) && publisher != null) {
			LOGGER.debug("Company - {} is NOT account type of Agency and has one Publisher - {}", user.getCompany(), publisher);
			PublisherDto publisherDto = dtoBinder.bindFromBusinessObject(PublisherDto.class, publisher);
			dto.setPublisherDto(publisherDto);
		}
		return dto;
	}

	/***
	 * Given a User sets the correct accountType to the UserDTO in string mode.
	 * 
	 * @param dto
	 *            - the UserDTO to which we want to set the accountType
	 * @param user
	 *            - the User domain object
	 * @return a UserDTO
	 * */
	private UserDTO setUserType(final UserDTO dto, final User user) {
		LOGGER.debug("setting the User Type for user - {}", user);
		dto.setUserTypes(new ArrayList<String>());
		if (user.getCompany().isAccountType(AccountType.AGENCY)) {
			LOGGER.debug("This user - {} is being set to AGENCY");
			dto.setUserType(USER_TYPE_AGENCY);
		}
		if (user.getCompany().isAccountType(AccountType.ADVERTISER)) {
		    if(StringUtils.isEmpty(dto.getUserType())){
    			LOGGER.debug("This user - {} is being set to ADVERTISER");
    			dto.setUserType(USER_TYPE_ADVERTISER);
		    }
	        LOGGER.debug("This user - {} is also an ADVERTISER");
	        dto.getUserTypes().add(USER_TYPE_ADVERTISER);
		}
		if (user.getCompany().isAccountType(AccountType.PUBLISHER)) {
			if(StringUtils.isEmpty(dto.getUserType())){
    		    LOGGER.debug("This user - {} is being set to PUBLISHER");
    			dto.setUserType(USER_TYPE_PUBLISHER);
			}
		    LOGGER.debug("This user - {} is also a PUBLISHER");
		    dto.getUserTypes().add(USER_TYPE_PUBLISHER);
			
		}
		if(CollectionUtils.isEmpty(dto.getUserTypes()) && StringUtils.isEmpty(dto.getUserType())) {
			LOGGER.debug("Defaulting this user - {} to userType advertiser");
			dto.setUserType("advertiser");
		}
		return dto;
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<T> getList(final Class<T> type, final Collection<?> col) {
		return (Collection<T>) dtoBinder.bindFromBusinessObjectCollection(type, col);
	}
}
