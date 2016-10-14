package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.LocationReportDto;

public class LocationReportMapper extends BaseReportRowMapper<LocationReportDto> {

	@Override
	public LocationReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		LocationReportDto row = new LocationReportDto();
		
		row.setCountry(getString("country"));
		row.setCountryIso(getString("country_isocode"));
		row.setLocation(getString("location"));
		row.setPercentTotalImpressions(getDouble("perc_total_imps"));
		row.setChannel(getString("channel"));
		row.setIab(getString("iab_category"));
		row.setInventorySource(getString("inventory_source"));
		row.setDay(getDate("advertiser_day_unix_timestamp"));

		row = (LocationReportDto) mapRowCommon(row, rs, rowNum);
		return row;
	}
}
