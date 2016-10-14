package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByBrandDayDto;

public class DeviceDetailByRegionByBrandDayDtoMapper implements RowMapper<DeviceDetailByRegionByBrandDayDto> {

    public DeviceDetailByRegionByBrandDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByRegionByBrandDayDto row = new DeviceDetailByRegionByBrandDayDto();
        row.setBrand(rs.getString("vendor"));
        row.setRegion(rs.getString("region"));
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
