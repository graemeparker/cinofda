package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.LocationDetailByCategoryDto;

public class LocationReportDetailByCategoryDtoMapper implements RowMapper<LocationDetailByCategoryDto> {
	public LocationDetailByCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		LocationDetailByCategoryDto row = new LocationDetailByCategoryDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setLocation(rs.getString("location"));
		row.setCategory(rs.getString("iab_category"));
		row.setPercentTotalImpressions(rs.getDouble("perc_total_imps"));
		ReportUtil.rowMapperLocation(row, rs);
		return row;
	}
}
