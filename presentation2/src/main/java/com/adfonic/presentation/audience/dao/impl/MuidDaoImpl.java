package com.adfonic.presentation.audience.dao.impl;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.adfonic.presentation.audience.dao.MuidDao;
import com.adfonic.presentation.audience.sql.procedures.MuidCreateSegmentStoredProcedure;
import com.adfonic.presentation.audience.sql.procedures.MuidDeleteSegmentStoredProcedure;
import com.adfonic.presentation.audience.sql.procedures.MuidLinkDeviceToSegmentStoredProc;

public class MuidDaoImpl extends JdbcDaoSupport implements MuidDao {

	public Long createMuidSegment(Long segmentReference, String segmentType) {
		MuidCreateSegmentStoredProcedure proc = new MuidCreateSegmentStoredProcedure(getDataSource());
		Long segmentId = proc.run(segmentReference, segmentType);
		return segmentId;
	}
	
	public Long deleteMuidSegment(Long segmentId) {
		MuidDeleteSegmentStoredProcedure proc = new MuidDeleteSegmentStoredProcedure(getDataSource());
		Long rc = proc.run(segmentId);
		return rc;
	}
	
	public Long linkDevicesToSegment(Long firstPartyAudienceId, String deviceIds) {
		MuidLinkDeviceToSegmentStoredProc proc = new MuidLinkDeviceToSegmentStoredProc(getDataSource());
		Long rowsInserted = proc.run(firstPartyAudienceId, deviceIds);
		return rowsInserted;
	}

}
