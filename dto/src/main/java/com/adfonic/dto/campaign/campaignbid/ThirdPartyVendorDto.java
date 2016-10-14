package com.adfonic.dto.campaign.campaignbid;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class ThirdPartyVendorDto extends NameIdBusinessDto {

	private static final long serialVersionUID = 1L;

	@Source("thirdPartyVendorType")
	private ThirdPartyVendorTypeDto thirdPartyVendorType;

	public ThirdPartyVendorTypeDto getThirdPartyVendorType() {
		return thirdPartyVendorType;
	}

	public void setThirdPartyVendorType(ThirdPartyVendorTypeDto thirdPartyVendorType) {
		this.thirdPartyVendorType = thirdPartyVendorType;
	}

	@Override
	public String toString() {
		return "ThirdPartyVendorDto [thirdPartyVendorType=" + String.valueOf(thirdPartyVendorType) + ", name=" + name + "]";
	}

}
