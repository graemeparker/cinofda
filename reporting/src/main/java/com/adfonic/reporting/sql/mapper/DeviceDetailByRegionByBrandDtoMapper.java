package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByBrandDto;

public class DeviceDetailByRegionByBrandDtoMapper implements RowMapper<DeviceDetailByRegionByBrandDto> {

    public DeviceDetailByRegionByBrandDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByRegionByBrandDto row = new DeviceDetailByRegionByBrandDto();
        row.setBrand(rs.getString("vendor"));
        row.setRegion(rs.getString("region"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
