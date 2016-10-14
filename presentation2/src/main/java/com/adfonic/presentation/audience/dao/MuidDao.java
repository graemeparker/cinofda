package com.adfonic.presentation.audience.dao;

public interface MuidDao {

	Long createMuidSegment(Long segmentReference, String segmentType);
	Long deleteMuidSegment(Long segmentId);
	
	Long linkDevicesToSegment(Long firstPartyAudienceId, String deviceIds);
}
