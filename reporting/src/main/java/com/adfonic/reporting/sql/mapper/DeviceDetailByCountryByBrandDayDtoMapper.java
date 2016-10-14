package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByBrandDayDto;

public class DeviceDetailByCountryByBrandDayDtoMapper implements RowMapper<DeviceDetailByCountryByBrandDayDto> {

    public DeviceDetailByCountryByBrandDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByCountryByBrandDayDto row = new DeviceDetailByCountryByBrandDayDto();
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        row.setCountry(rs.getString("country"));
        row.setBrand(rs.getString("vendor"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
