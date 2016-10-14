package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionDayDto;

public class DeviceDetailByRegionDayDtoMapper implements RowMapper<DeviceDetailByRegionDayDto> {

    public DeviceDetailByRegionDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByRegionDayDto row = new DeviceDetailByRegionDayDto();
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        row.setRegion(rs.getString("region"));
        row.setDevice(rs.getString("model"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
