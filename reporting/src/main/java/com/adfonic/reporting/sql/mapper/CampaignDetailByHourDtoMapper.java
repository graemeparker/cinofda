package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourDto;

public class CampaignDetailByHourDtoMapper implements RowMapper<CampaignDetailByHourDto> {

	public CampaignDetailByHourDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByHourDto row = new CampaignDetailByHourDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setHour(rs.getString("advertiser_hour"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}

}
