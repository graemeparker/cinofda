package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailDto;

public class CampaignDetailDtoMapper implements RowMapper<CampaignDetailDto> {
	public CampaignDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailDto row = new CampaignDetailDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
