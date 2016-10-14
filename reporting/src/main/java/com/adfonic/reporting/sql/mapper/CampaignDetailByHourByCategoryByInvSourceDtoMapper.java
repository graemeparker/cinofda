package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByCategoryByInvSourceDto;

public class CampaignDetailByHourByCategoryByInvSourceDtoMapper implements RowMapper<CampaignDetailByHourByCategoryByInvSourceDto> {

	public CampaignDetailByHourByCategoryByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByHourByCategoryByInvSourceDto row = new CampaignDetailByHourByCategoryByInvSourceDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setCategory(rs.getString("iab_category"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setHour(rs.getString("advertiser_hour"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}

}
