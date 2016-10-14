package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignInvSourceDetailDto;

public class CampaignInvSourceDetailDtoMapper implements RowMapper<CampaignInvSourceDetailDto> {
	
	public CampaignInvSourceDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignInvSourceDetailDto row = new CampaignInvSourceDetailDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setInventorySource(rs.getString("inventory_source"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
