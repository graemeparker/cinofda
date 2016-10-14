package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignTotalByInvSourceDto;

public class CampaignTotalByInvSourceDtoMapper implements RowMapper<CampaignTotalByInvSourceDto> {
	public CampaignTotalByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignTotalByInvSourceDto row = new CampaignTotalByInvSourceDto();
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setInventorySource(rs.getString("inventory_source"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
