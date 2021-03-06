package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByPlatformDto;

public class DeviceDetailByCountryByPlatformDtoMapper implements RowMapper<DeviceDetailByCountryByPlatformDto> {

    public DeviceDetailByCountryByPlatformDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByCountryByPlatformDto row = new DeviceDetailByCountryByPlatformDto();
        row.setCountry(rs.getString("country"));
        row.setPlatform(rs.getString("platform"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
