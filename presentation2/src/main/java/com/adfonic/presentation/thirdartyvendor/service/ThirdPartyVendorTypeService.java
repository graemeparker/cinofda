package com.adfonic.presentation.thirdartyvendor.service;

import java.util.List;

import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorTypeDto;

public interface ThirdPartyVendorTypeService {

	ThirdPartyVendorTypeDto getThirdPartyVendorTypeDtoById(long id);
	
	List<ThirdPartyVendorTypeDto> getAllThirdPartyVendorTypes();
}
