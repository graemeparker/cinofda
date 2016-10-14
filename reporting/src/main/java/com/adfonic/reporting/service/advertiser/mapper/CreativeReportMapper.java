package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.CampaignReportDto;
import com.adfonic.reporting.service.advertiser.dto.CreativeReportDto;

public class CreativeReportMapper extends BaseReportRowMapper<CreativeReportDto> {

	public CreativeReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		CreativeReportDto row = new CreativeReportDto();
		
		row.setCampaign(getString("campaign"));
		row.setCampaignExternalId(getString("campaign_external_id"));
		row.setChannel(getString("channel"));
		row.setIab(getString("iab_category"));
		row.setCreative(getString("creative"));
		row.setCreativeExternalId(getString("creative_external_id"));
		row.setFormat(getString("format"));
		row.setInventorySource(getString("inventory_source"));
		row.setDay(getDate("advertiser_day_unix_timestamp"));
		
		row = (CreativeReportDto) mapRowCommon(row, rs, rowNum);
		return row;
	}
}
