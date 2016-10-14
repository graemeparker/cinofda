package com.adfonic.presentation.thirdartyvendor.service;

import java.util.List;
import java.util.Set;

import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorDto;

public interface ThirdPartyVendorService {

	ThirdPartyVendorDto getThirdPartyVendorDtoById(long id);
	
	List<ThirdPartyVendorDto> getAllThirdPartyVendors();
	
	List<ThirdPartyVendorDto> getAllThirdPartyVendorsByTypeIds(Set<Long> thirdPartyVendorTypeIds);
}
