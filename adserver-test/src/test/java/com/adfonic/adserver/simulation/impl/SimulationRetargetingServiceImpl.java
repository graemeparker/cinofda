package com.adfonic.adserver.simulation.impl;

import java.util.Map;

import com.adfonic.retargeting.RetargetingData;
import com.adfonic.retargeting.RetargetingException;
import com.adfonic.retargeting.RetargetingService;

public class SimulationRetargetingServiceImpl implements RetargetingService {

	@Override
	public void trackClick(long campaignId, long deviceIdentifierTypeId,
			String deviceIdentifier, long clickDate)
			throws RetargetingException {
	}

	@Override
	public void trackClick(long campaignId, Map<Long, String> deviceIdentifiers)
			throws RetargetingException {
	}

	@Override
	public void trackConversion(long campaignId, long deviceIdentifierTypeId,
			String deviceIdentifier, long conversionDate)
			throws RetargetingException {
	}

	@Override
	public void trackConversion(long campaignId,
			Map<Long, String> deviceIdentifiers) throws RetargetingException {
	}

	@Override
	public void trackInstall(String applicationId, Long deviceIdentifierTypeId,
			String deviceIdentifier, long installDate)
			throws RetargetingException {
	}

	@Override
	public void trackInstall(String applicationId,
			Map<Long, String> deviceIdentifiers) throws RetargetingException {
		
	}

	@Override
	public RetargetingData getRetargetingData(long deviceIdentifierTypeId,
			String deviceIdentifier) throws RetargetingException {
		return null;
	}

	@Override
	public Map<Long, RetargetingData> getRetargetingData(
			Map<Long, String> deviceIdentifiers) throws RetargetingException {
		return null;
	}

}
