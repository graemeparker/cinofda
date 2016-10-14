package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByCategoryDto;

public class CampaignDetailByHourByCategoryDtoMapper implements RowMapper<CampaignDetailByHourByCategoryDto> {

	public CampaignDetailByHourByCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByHourByCategoryDto row = new CampaignDetailByHourByCategoryDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setCategory(rs.getString("iab_category"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setHour(rs.getString("advertiser_hour"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
