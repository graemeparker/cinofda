package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByInvSourceDto;

public class CampaignDetailByHourByInvSourceDtoMapper implements RowMapper<CampaignDetailByHourByInvSourceDto> {

	public CampaignDetailByHourByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByHourByInvSourceDto row = new CampaignDetailByHourByInvSourceDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setHour(rs.getString("advertiser_hour"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}

}
