package com.adfonic.reporting.service.scheduled.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.mapper.BaseReportRowMapper;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportFrequencyDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportStatusDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportTimePeriodDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportTypeDto;

public class ScheduledReportTimePeriodMapper extends BaseReportRowMapper<ScheduledReportTimePeriodDto> {

	@Override
	public ScheduledReportTimePeriodDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		ScheduledReportTimePeriodDto row = new ScheduledReportTimePeriodDto();

		row.setId(getInteger("scheduled_report_time_period_id"));
		row.setName(getString("scheduled_report_time_period_name"));
		row.setStartSql(getString("start_day_sql"));
		row.setEndSql(getString("end_day_sql"));
		
		return row;
	}
}
