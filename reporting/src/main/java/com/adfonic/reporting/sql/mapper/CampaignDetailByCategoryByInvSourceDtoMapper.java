package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignDetailByCategoryByInvSourceDto;

public class CampaignDetailByCategoryByInvSourceDtoMapper implements RowMapper<CampaignDetailByCategoryByInvSourceDto> {
	public CampaignDetailByCategoryByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignDetailByCategoryByInvSourceDto row = new CampaignDetailByCategoryByInvSourceDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setCategory(rs.getString("iab_category"));
		row.setInventorySource(rs.getString("inventory_source"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
