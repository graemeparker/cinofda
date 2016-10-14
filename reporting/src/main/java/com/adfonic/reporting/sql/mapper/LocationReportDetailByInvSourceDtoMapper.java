package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.CreativeDetailByInvSourceDto;
import com.adfonic.reporting.sql.dto.LocationDetailByInvSourceDto;

public class LocationReportDetailByInvSourceDtoMapper implements RowMapper<LocationDetailByInvSourceDto> {

	public LocationDetailByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		LocationDetailByInvSourceDto row = new LocationDetailByInvSourceDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setLocation(rs.getString("location"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setPercentTotalImpressions(rs.getDouble("perc_total_imps"));
		ReportUtil.rowMapperLocation(row, rs);
		return row;
	}
}
