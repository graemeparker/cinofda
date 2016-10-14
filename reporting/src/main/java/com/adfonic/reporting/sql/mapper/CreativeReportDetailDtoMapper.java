package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CreativeDetailDto;

public class CreativeReportDetailDtoMapper implements RowMapper<CreativeDetailDto> {
	public CreativeDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CreativeDetailDto row = new CreativeDetailDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCreative(rs.getString("creative"));
		row.setCampaign_external_id(rs.getString("campaign_external_id"));
		row.setCreative_external_id(rs.getString("creative_external_id"));
		row.setFormat(rs.getString("format"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
