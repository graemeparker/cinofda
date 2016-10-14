package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.LocationDetailByCategoryByInvSourceDto;

public class LocationReportDetailByCategoryByInvSourceDtoMapper implements RowMapper<LocationDetailByCategoryByInvSourceDto> {

	public LocationDetailByCategoryByInvSourceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		LocationDetailByCategoryByInvSourceDto row = new LocationDetailByCategoryByInvSourceDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setLocation(rs.getString("location"));
		row.setCategory(rs.getString("iab_category"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setPercentTotalImpressions(rs.getDouble("perc_total_imps"));
		ReportUtil.rowMapperLocation(row, rs);
		return row;
	}
}
