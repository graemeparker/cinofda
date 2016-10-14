package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.CampaignReportDto;

public class CampaignReportMapper extends BaseReportRowMapper<CampaignReportDto> {

	@Override
	public CampaignReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		CampaignReportDto row = new CampaignReportDto();
		
		row.setCampaign(getString("campaign"));
		row.setCampaignExternalId(getString("campaign_external_id"));
		row.setChannel(getString("channel"));
		row.setIab(getString("iab_category"));
		row.setInventorySource(getString("inventory_source"));
		row.setDay(getDate("advertiser_day_unix_timestamp"));
		row.setHour(getString("advertiser_hour"));

		row = (CampaignReportDto) mapRowCommon(row, rs, rowNum);
		return row;
	}
}
