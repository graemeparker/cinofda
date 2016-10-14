package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CreativeDetailByInvSourceDto;

public class CreativeReportDetailByInvSourceDtoMapper implements RowMapper<CreativeDetailByInvSourceDto> {

	public CreativeDetailByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CreativeDetailByInvSourceDto row = new CreativeDetailByInvSourceDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCreative(rs.getString("creative"));
		row.setCampaign_external_id(rs.getString("campaign_external_id"));
		row.setCreative_external_id(rs.getString("creative_external_id"));
		row.setFormat(rs.getString("format"));
		row.setInventorySource(rs.getString("inventory_source"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
