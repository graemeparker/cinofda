package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.DeviceReportDto;

public class DeviceReportMapper extends BaseReportRowMapper<DeviceReportDto> {

	public DeviceReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		DeviceReportDto row = new DeviceReportDto();
		
		row.setCountry(getString("country"));
		row.setRegion(getString("region"));
		row.setModel(getString("model"));
		row.setVendor(getString("vendor"));
		row.setPlatform(getString("platform"));
		row.setDay(getDate("advertiser_day_unix_timestamp"));
		
		row = (DeviceReportDto) mapRowCommon(row, rs, rowNum);
		return row;
	}
}
