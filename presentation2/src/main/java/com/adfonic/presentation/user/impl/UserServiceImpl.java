package com.adfonic.presentation.user.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.PaymentOptions_;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.adfonic.domain.User.Status;
import com.adfonic.domain.User_;
import com.adfonic.domain.VerificationCode;
import com.adfonic.dto.advertiser.AdvertiserCloudInformationDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.user.RoleDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.email.EmailService;
import com.adfonic.presentation.user.UserService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.exception.AdvertiserCloudManagerException;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserCloudManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("userService")
public class UserServiceImpl extends GenericServiceImpl implements UserService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CompanyManager companyManager;
	@Autowired
	private AccountManager accountManager;
	@Autowired
    private AdvertiserManager advertiserManager;
	@Autowired(required=false)
    private AdvertiserCloudManager advertiserCloudManager;
	@Autowired
	private CommonManager commonManager;
	@Autowired
    private org.dozer.Mapper mapper;
	@Autowired
	private EmailService emailService;
	
	private FetchStrategy userFs = new FetchStrategyBuilder()
    .addLeft(User_.roles)
    .addInner(User_.company)
    .addLeft(Company_.advertisers)
    .addLeft(Company_.publisher)
    .addLeft(Company_.paymentOptions)
    .addLeft(PaymentOptions_.postalAddress)
    .build();
	
	private static final FetchStrategy companyUsersFs = new FetchStrategyBuilder()
    .addLeft(Company_.users)
    .addLeft(User_.roles)
    .build();
	
	private static final FetchStrategy advertiserFs = new FetchStrategyBuilder()
    .addLeft(User_.company)
    .addLeft(User_.advertisers)
    .build();
	
	@Transactional(readOnly=false)
	public UserDTO createUser(UserDTO userDto,Long companyId,List<RoleDto> roles){
	    Company company = companyManager.getCompanyById(companyId);
	    User user = userManager.newUser(company, userDto.getFirstName(), userDto.getLastName(), userDto.getEmail(), userDto.getPassword(),null);
	    user = userManager.getUserById(user.getId(),userFs);
	    
	    for(RoleDto role : roles){
	        Role r = userManager.getRoleById(role.getId());
	        user.getRoles().add(r);
	    }
	    
	    //From agency console user is created automatically verified, no need for confirmation mail
	    user.setStatus(User.Status.VERIFIED);
	    user = userManager.update(user);
	    return getUserById(user.getId());
	}

	@Transactional(readOnly=true)
	public UserDTO getUserById(Long id){
	    User user = userManager.getUserById(id,userFs);
        if(user!=null){
            return mapper.map(user, UserDTO.class);
        }
        return null;
	}
	
	@Transactional(readOnly=true)
	public UserDTO getUserByEmail(String email){
	    User user = userManager.getUserByEmail(email);
        if(user!=null){
            return mapper.map(user, UserDTO.class);
        }
        return null;
	}
	
	@Transactional(readOnly=true)
	public List<UserDTO> getActiveUsersForUser(Long companyId,Long userId){
	    Company company = companyManager.getCompanyById(companyId, companyUsersFs);
        List<UserDTO> result = new ArrayList<UserDTO>();
        for(User user : company.getUsers()){
            if(!user.getStatus().equals(User.Status.DISABLED) && userId!=user.getId()){
                result.add(mapper.map(user, UserDTO.class));
            }
        }
        return result;
	}
	
	@Transactional(readOnly=false)
    public List<UserDTO> getRemovedUsersForUser(Long companyId,Long userId){
        Company company = companyManager.getCompanyById(companyId, companyUsersFs);
        List<UserDTO> result = new ArrayList<UserDTO>();
        for(User user : company.getUsers()){
            if(user.getStatus().equals(User.Status.DISABLED) && userId!=user.getId()){
                result.add(mapper.map(user, UserDTO.class));
            }
        }
        return result;
    }
	
    @Transactional(readOnly=false)
	public UserDTO changeStatus(long userId, User.Status status){
	    User entity = userManager.getUserById(userId);
	    entity.setStatus(status);
        userManager.update(entity);
        return getUserById(userId);
	}
	
    @Transactional(readOnly=true)
	public RoleDto getRoleByName(String name){
	    Role role = userManager.getRoleByName(name);
        if(role!=null){
            return mapper.map(role, RoleDto.class);
        }
        return null;
	}
	
	@Transactional(readOnly=true)
	public boolean isAdminUser(long userId){
	    User user = userManager.getUserById(userId,userFs);
	    Role admin = userManager.getRoleByName(Role.USER_ROLE_ADMINISTRATOR);
	    return user.getRoles().contains(admin);
	}
	
	@Transactional(readOnly=false)
	public UserDTO updateAdvertisersList(long userId,List<AdvertiserDto> lAdvertisers){
	    User entity = userManager.getUserById(userId,advertiserFs);
	    entity.getAdvertisers().clear();
	    for(AdvertiserDto advertiser : lAdvertisers){
	        Advertiser a = advertiserManager.getAdvertiserById(advertiser.getId());
	        entity.getAdvertisers().add(a);
	    }
	    userManager.update(entity);
	    User user = userManager.getUserById(userId,userFs);
	    
	    return mapper.map(user, UserDTO.class);
	}
	
	@Transactional(readOnly=false)
	public UserDTO saveUser(UserDTO userDto){
	    User user = userManager.getUserById(userDto.getId(),userFs);
	    user.setEmail(userDto.getEmail());
	    user.setFirstName(user.getFirstName());
	    user.setLastName(userDto.getLastName());
	    user.setPassword(userDto.getPassword());
	    
	    user = userManager.update(user);
	    user = userManager.getUserById(userDto.getId(),userFs);
	    return mapper.map(user, UserDTO.class);
	}
	
	@Transactional(readOnly=false)
	public UserDTO saveUserSettingsDetails (String oldEmail,
											String email,
										    String firstName,
										    String lastName,
										    CountryDto countryDto,
										    String phone,
										    String companyName,
										    String taxCode,
										    String timezone,
										    boolean invoiceDateInGMT,
										    List<String> accountTypes, 
										    boolean emailHaschanged){
	    User user = userManager.getUserByEmail(oldEmail,userFs);
	    user.setEmail(email);
	    user.setFirstName(firstName);
	    user.setLastName(lastName);
	    Country country = commonManager.getCountryById(countryDto.getId());
	    user.setCountry(country);
	    user.setPhoneNumber(phone);
	    if (emailHaschanged){
	    	user.setStatus(Status.UNVERIFIED);
	    }
	    
	    boolean isAgency = userManager.isAgencyUser(user);
	    boolean isAdmin = userManager.userHasRole(user, Role.USER_ROLE_ADMINISTRATOR);
	    
	    Company company = user.getCompany();
	    
	    if (!isAgency || (isAgency && isAdmin)) {
		    company.setName(companyName);
		    company.setTaxCode(taxCode);
		    company.setDefaultTimeZoneId(timezone);
		    company.setIsInvoiceDateInGMT(invoiceDateInGMT);
		    company.setCountry(country);
	    }
	    
        if (!isAgency){
		    company.clearAccountTypeFlags();
		    for(String type : accountTypes){
		    	company.setAccountTypeFlag(AccountType.valueOf(type));
		    }
        }
        
        company = companyManager.update(company);
        
	    user = userManager.update(user);
	    user = userManager.getUserByEmail(email,userFs);
	    
	    return mapper.map(user, UserDTO.class);
	}
	
	@Transactional(readOnly=true)
	public boolean isAgencyType(UserDTO userDto){
	    User user = userManager.getUserById(userDto.getId(), userFs);
	    return user.getCompany().isAccountType(AccountType.AGENCY);
	}
	
	@Transactional(readOnly=false)
	public VerificationCode getVerificationCode(UserDTO userDto){
		User user = userManager.getUserById(userDto.getId(),userFs);
		
		VerificationCode vc = null;
        try {
            vc = accountManager.newVerificationCode(user, VerificationCode.CodeType.CHANGE_EMAIL, userFs);
        } catch (Exception e) {
            LOGGER.error("Failed to create VerificationCode for user id=" + user.getId(), e);
        }
        
        return vc;
	}
	
	@Transactional(readOnly=true)
	public Set<RoleDto> getRoles(long userId){
	    User user = userManager.getUserById(userId,userFs);
	    return getSet(RoleDto.class, user.getRoles());
	}
	
	@Transactional(readOnly=false)
	public void updateRoles(long userId, Set<RoleDto> roles){
	    User user = userManager.getUserById(userId,userFs);
	    user.getRoles().clear();
	    for(RoleDto role : roles){
            Role r = userManager.getRoleById(role.getId());
            user.getRoles().add(r);
        }
        user = userManager.update(user);
	}
	
	//
    // Advertiser cloud information
	//
	@Transactional(readOnly=true)
	public String getFileMoverBucketName(){
	    return advertiserCloudManager.getFileMoverBucketName();
	}
	
	@Transactional(readOnly=true)
	public AdvertiserCloudInformationDto getAdvertiserCloudInformation(AdvertiserDto advertiserDto){
	    AdvertiserCloudInformation advertiserCloudInformation = advertiserCloudManager.getAdvertiserCloudInformation(advertiserDto.getId());
	    return (advertiserCloudInformation==null?null:mapper.map(advertiserCloudInformation, AdvertiserCloudInformationDto.class));
	}
	
	@Transactional(readOnly=false)
	public AdvertiserCloudInformationDto createAdvertiserCloudInformation(AdvertiserDto advertiserDto) throws AdvertiserCloudManagerException{
	    Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
	    AdvertiserCloudInformation advertiserCloudInformation = advertiserCloudManager.createAdvertiserCloudInformation(advertiser);
        return (advertiserCloudInformation==null?null:mapper.map(advertiserCloudInformation, AdvertiserCloudInformationDto.class));
	}
	
	@Transactional(readOnly=false)
	public void deleteAdvertiserCloudInformation(AdvertiserDto advertiserDto) throws AdvertiserCloudManagerException{
	    Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
        advertiserCloudManager.deleteAdvertiserCloudInformation(advertiser);
	}
	
}
