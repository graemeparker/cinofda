package com.adfonic.reporting.service.scheduled.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.mapper.BaseReportRowMapper;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportStatusDto;

public class ScheduledReportStatusMapper extends BaseReportRowMapper<ScheduledReportStatusDto> {

	@Override
	public ScheduledReportStatusDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		ScheduledReportStatusDto row = new ScheduledReportStatusDto();

		row.setId(getInteger("scheduled_report_status_id"));
		row.setName(getString("scheduled_report_status_name"));
		
		return row;
	}
}
