package com.adfonic.presentation.devicetarget;

import com.adfonic.dto.campaign.CampaignDto;

public interface DeviceTargetService {
	
	public boolean isAndroidOnly(final CampaignDto dto);

	public boolean isIOSOnly(final CampaignDto dto);

	public boolean isIOSAndroidOnly(final CampaignDto dto);
	
	public boolean isAppleTarget(final CampaignDto dto);
	
	public boolean isAndroidTarget(final CampaignDto dto);

}
