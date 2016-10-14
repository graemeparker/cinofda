package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByDayDto;

public class CampaignDetailByDayDtoMapper implements RowMapper<CampaignDetailByDayDto> {

	public CampaignDetailByDayDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByDayDto row = new CampaignDetailByDayDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}

}
