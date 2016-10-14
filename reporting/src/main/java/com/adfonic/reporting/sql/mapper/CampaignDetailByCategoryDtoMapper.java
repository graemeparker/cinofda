package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByCategoryDto;

public class CampaignDetailByCategoryDtoMapper implements RowMapper<CampaignDetailByCategoryDto> {
	public CampaignDetailByCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByCategoryDto row = new CampaignDetailByCategoryDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setCategory(rs.getString("iab_category"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
