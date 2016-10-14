package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByBrandDto;

public class DeviceDetailByCountryByBrandDtoMapper implements RowMapper<DeviceDetailByCountryByBrandDto> {

    public DeviceDetailByCountryByBrandDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByCountryByBrandDto row = new DeviceDetailByCountryByBrandDto();
        row.setCountry(rs.getString("country"));
        row.setBrand(rs.getString("vendor"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
