package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByBrandDto;

public class DeviceDetailByBrandDtoMapper implements RowMapper<DeviceDetailByBrandDto> {

    public DeviceDetailByBrandDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByBrandDto row = new DeviceDetailByBrandDto();
        row.setBrand(rs.getString("vendor"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
