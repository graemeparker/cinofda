package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CampaignInvSourceDetailByDayDto;

public class CampaignInvSourceDetailByDayDtoMapper implements RowMapper<CampaignInvSourceDetailByDayDto> {

	public CampaignInvSourceDetailByDayDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		CampaignInvSourceDetailByDayDto row = new CampaignInvSourceDetailByDayDto();
		row.setCampaign(rs.getString("campaign"));
		row.setCampaignExternalId(rs.getString("campaign_external_id"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapper(row, rs);
		return row;
	}
}
