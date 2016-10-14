package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByBrandDayDto;

public class DeviceDetailByBrandDayDtoMapper implements RowMapper<DeviceDetailByBrandDayDto> {

  public DeviceDetailByBrandDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
	  DeviceDetailByBrandDayDto row = new DeviceDetailByBrandDayDto();
	  row.setBrand(rs.getString("vendor"));
	  row.setDay(rs.getString("advertiser_day_unix_timestamp"));
	  ReportUtil.rowMapperDevice(row, rs);
	  return row;
  }
	
}
