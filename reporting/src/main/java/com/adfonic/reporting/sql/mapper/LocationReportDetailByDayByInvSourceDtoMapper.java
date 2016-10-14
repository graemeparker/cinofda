package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.LocationDetailByDayByInvSourceDto;

public class LocationReportDetailByDayByInvSourceDtoMapper implements RowMapper<LocationDetailByDayByInvSourceDto> {

	public LocationDetailByDayByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		LocationDetailByDayByInvSourceDto row = new LocationDetailByDayByInvSourceDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setLocation(rs.getString("location"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		row.setPercentTotalImpressions(rs.getDouble("perc_total_imps"));
		ReportUtil.rowMapperLocation(row, rs);
		return row;
	}
}
