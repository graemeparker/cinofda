package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayDto;

public class CreativeReportDetailByDayDtoMapper implements RowMapper<CreativeDetailByDayDto> {
	public CreativeDetailByDayDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CreativeDetailByDayDto row = new CreativeDetailByDayDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCreative(rs.getString("creative"));
		row.setCampaign_external_id(rs.getString("campaign_external_id"));
		row.setCreative_external_id(rs.getString("creative_external_id"));
		row.setFormat(rs.getString("format"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
