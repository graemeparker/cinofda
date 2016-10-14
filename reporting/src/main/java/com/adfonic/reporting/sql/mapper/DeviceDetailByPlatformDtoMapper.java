package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByPlatformDto;

public class DeviceDetailByPlatformDtoMapper implements RowMapper<DeviceDetailByPlatformDto> {

    public DeviceDetailByPlatformDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByPlatformDto row = new DeviceDetailByPlatformDto();
        row.setPlatform(rs.getString("platform"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
