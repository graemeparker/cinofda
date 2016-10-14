package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignTotalByHourDto;

public class CampaignTotalByHourDtoMapper implements RowMapper<CampaignTotalByHourDto> {

	public CampaignTotalByHourDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignTotalByHourDto row = new CampaignTotalByHourDto();
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setHour(rs.getString("advertiser_hour"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}

}
