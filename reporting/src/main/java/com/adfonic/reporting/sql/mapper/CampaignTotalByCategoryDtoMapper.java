package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignTotalByCategoryDto;

public class CampaignTotalByCategoryDtoMapper implements RowMapper<CampaignTotalByCategoryDto> {
	public CampaignTotalByCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignTotalByCategoryDto row = new CampaignTotalByCategoryDto();
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setCategory(rs.getString("iab_category"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
