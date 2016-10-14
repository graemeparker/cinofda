package com.adfonic.reporting.service.scheduled.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.mapper.BaseReportRowMapper;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportFrequencyDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportStatusDto;

public class ScheduledReportFrequencyMapper extends BaseReportRowMapper<ScheduledReportFrequencyDto> {

	@Override
	public ScheduledReportFrequencyDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		ScheduledReportFrequencyDto row = new ScheduledReportFrequencyDto();

		row.setId(getInteger("scheduled_report_frequency_id"));
		row.setName(getString("scheduled_report_frequency_name"));
		row.setHoursOfDay(getInteger("hours_of_the_day"));
		row.setDaysOfWeek(getInteger("days_of_week_to_run"));
		row.setDaysOfMonth(getInteger("days_of_month_to_run"));
		
		
		return row;
	}
}
