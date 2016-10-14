package com.adfonic.reporting.service.scheduled.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.CampaignReportDto;
import com.adfonic.reporting.service.advertiser.mapper.BaseReportRowMapper;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportDto;

public class ScheduledReportMapper extends BaseReportRowMapper<ScheduledReportDto> {

	@Override
	public ScheduledReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		ScheduledReportDto row = new ScheduledReportDto();
		
		row.setExternalId(getString("scheduled_report_hash_id"));
		row.setReportType(getInteger("scheduled_report_type_id"));
		row.setFrequency(getInteger("scheduled_report_frequency_id"));
		row.setTimePeriod(getInteger("scheduled_report_time_period_id"));
		row.setAdvertiser(getLong("adv_id"));
		row.setCampaigns(getString("cam_ids"));
		row.setCreatives(getString("cre_ids"));
		row.setFormats(getString("fmt_ids"));
		row.setModels(getString("mdl_ids"));
		row.setVendors(getString("vndr_ids"));
		row.setStartDay(getInteger("custom_start_day_id"));
		row.setEndDay(getInteger("custom_end_day_id"));
		row.setStatus(getInteger("report_status_id"));
		row.setEmails(getString("report_email_addresses"));
		row.setDateFormat(getString("date_format"));
		row.setRunHour(getInteger("advertiser_run_hour"));
		
		return row;
	}
}
