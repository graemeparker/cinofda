package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.OperatorDetailDto;

public class OperatorDetailDtoMapper implements RowMapper<OperatorDetailDto> {
	public OperatorDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
		OperatorDetailDto row = new OperatorDetailDto();
		row.setCountry(rs.getString("country"));
		row.setCountryIsocode(rs.getString("country_isocode"));
		row.setOperator(rs.getString("operator"));
		ReportUtil.rowMapperOperator(row, rs);
		return row;
	}
}
