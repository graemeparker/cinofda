package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignTotalDto;

public class CampaignTotalDtoMapper implements RowMapper<CampaignTotalDto> {
	public CampaignTotalDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignTotalDto row = new CampaignTotalDto();
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
