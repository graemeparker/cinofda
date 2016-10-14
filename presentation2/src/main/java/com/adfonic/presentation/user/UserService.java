package com.adfonic.presentation.user;

import java.util.List;
import java.util.Set;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.dto.advertiser.AdvertiserCloudInformationDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.user.RoleDto;
import com.adfonic.dto.user.UserDTO;
import com.byyd.middleware.account.exception.AdvertiserCloudManagerException;


public interface UserService {
	
    public UserDTO getUserById(Long id);
    public UserDTO getUserByEmail(String email);
    public UserDTO createUser(UserDTO user,Long companyId,List<RoleDto> roles);
    public List<UserDTO> getActiveUsersForUser(Long companyId,Long userId);
    public List<UserDTO> getRemovedUsersForUser(Long companyId,Long userId);
    public UserDTO changeStatus(long userId, User.Status status);
    public RoleDto getRoleByName(String name);
    public boolean isAdminUser(long userId);
    public UserDTO updateAdvertisersList(long userId,List<AdvertiserDto> lAdvertisers);
    public UserDTO saveUser(UserDTO userDto);
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
										    boolean emailHaschanged);
    public boolean isAgencyType(UserDTO userDto);
    public VerificationCode getVerificationCode(UserDTO userDto);
    public Set<RoleDto> getRoles(long userId);
    public void updateRoles(long userId, Set<RoleDto> roles);
    
    
    // Advertiser cloud information
    String getFileMoverBucketName();
    AdvertiserCloudInformationDto getAdvertiserCloudInformation(AdvertiserDto advertiserDto);
    AdvertiserCloudInformationDto createAdvertiserCloudInformation(AdvertiserDto advertiserDto) throws AdvertiserCloudManagerException;  
    void deleteAdvertiserCloudInformation(AdvertiserDto advertiserDto) throws AdvertiserCloudManagerException;

}
