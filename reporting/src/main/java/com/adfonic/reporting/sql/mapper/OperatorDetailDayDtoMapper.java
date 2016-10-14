package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.OperatorDetailDayDto;

public class OperatorDetailDayDtoMapper implements RowMapper<OperatorDetailDayDto> {
	public OperatorDetailDayDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		OperatorDetailDayDto row = new OperatorDetailDayDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setOperator(rs.getString("operator"));
		row.setDayUnixTimestamp(rs.getString("advertiser_day_unix_timestamp"));
		ReportUtil.rowMapperOperator(row, rs);
		return row;
	}
}
