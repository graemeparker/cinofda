package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByPlatformDto;

public class DeviceDetailByRegionByPlatformDtoMapper implements RowMapper<DeviceDetailByRegionByPlatformDto> {

    public DeviceDetailByRegionByPlatformDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByRegionByPlatformDto row = new DeviceDetailByRegionByPlatformDto();
        row.setRegion(rs.getString("region"));
        row.setPlatform(rs.getString("platform"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
