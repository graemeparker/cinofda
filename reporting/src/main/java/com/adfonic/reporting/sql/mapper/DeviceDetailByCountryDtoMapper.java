package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryDto;

public class DeviceDetailByCountryDtoMapper implements RowMapper<DeviceDetailByCountryDto> {

    public DeviceDetailByCountryDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByCountryDto row = new DeviceDetailByCountryDto();
        row.setCountry(rs.getString("country"));
        row.setDevice(rs.getString("model"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
