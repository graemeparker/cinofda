package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionDto;

public class DeviceDetailByRegionDtoMapper implements RowMapper<DeviceDetailByRegionDto> {

    public DeviceDetailByRegionDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByRegionDto row = new DeviceDetailByRegionDto();
        row.setDevice(rs.getString("model"));
        row.setRegion(rs.getString("region"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
