package com.adfonic.presentation.devicetarget.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Segment;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.devicetarget.DeviceTargetService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.device.service.DeviceManager.DeviceTargetType;

@Service("deviceTargetService")
public class DeviceTargetServiceImpl extends GenericServiceImpl implements DeviceTargetService {

	@Autowired
	private DeviceManager deviceManager;
	
	@Autowired
	private CampaignService campaignService;

	@Transactional(readOnly=true)
	public boolean isAndroidOnly(final CampaignDto dto) {
		Campaign campaign = campaignService.getCampaignEntityById(dto);
		Segment s = campaign.getSegments().get(0);
		DeviceTargetType deviceTarget = deviceManager
				.getPlatformDeviceTarget(s);
		return deviceTarget == DeviceTargetType.ANDROID_ONLY;
	}

	@Transactional(readOnly=true)
	public boolean isIOSOnly(final CampaignDto dto) {
		Campaign campaign = campaignService.getCampaignEntityById(dto);
		Segment s = campaign.getSegments().get(0);
		DeviceTargetType deviceTarget = deviceManager
				.getPlatformDeviceTarget(s);
		return deviceTarget == DeviceTargetType.IOS_ONLY;
	}

	@Transactional(readOnly=true)
	public boolean isIOSAndroidOnly(final CampaignDto dto) {
		Campaign campaign = campaignService.getCampaignEntityById(dto);
		Segment s = campaign.getSegments().get(0);
		DeviceTargetType deviceTarget = deviceManager
				.getPlatformDeviceTarget(s);
		return ((deviceTarget == DeviceTargetType.IOS_ANDROID_ONLY));
	}

	@Transactional(readOnly=true)
    public boolean isAppleTarget(CampaignDto dto) {
	    Campaign campaign = campaignService.getCampaignEntityById(dto);
        Segment s = campaign.getSegments().get(0);
        return deviceManager.isAppleTarget(s);
    }

	@Transactional(readOnly=true)
    public boolean isAndroidTarget(CampaignDto dto) {
	    Campaign campaign = campaignService.getCampaignEntityById(dto);
        Segment s = campaign.getSegments().get(0);
        return deviceManager.isAndroidTarget(s);
    }
	
}
