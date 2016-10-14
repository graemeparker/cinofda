package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignTotalByCategoryByInvSourceDto;

public class CampaignTotalByCategoryByInvSourceDtoMapper implements RowMapper<CampaignTotalByCategoryByInvSourceDto> {
	public CampaignTotalByCategoryByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignTotalByCategoryByInvSourceDto row = new CampaignTotalByCategoryByInvSourceDto();
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setCategory(rs.getString("iab_category"));
		row.setInventorySource(rs.getString("inventory_source"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
