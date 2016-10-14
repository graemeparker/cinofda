package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayByCategoryDto;

public class CreativeReportDetailByDayByCategoryDtoMapper implements RowMapper<CreativeDetailByDayByCategoryDto> {
	public CreativeDetailByDayByCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CreativeDetailByDayByCategoryDto row = new CreativeDetailByDayByCategoryDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCreative(rs.getString("creative"));
		row.setCampaign_external_id(rs.getString("campaign_external_id"));
		row.setCreative_external_id(rs.getString("creative_external_id"));
		row.setFormat(rs.getString("format"));
		row.setCategory(rs.getString("iab_category"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
