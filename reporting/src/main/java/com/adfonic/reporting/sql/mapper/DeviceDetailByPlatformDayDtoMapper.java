package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByPlatformDayDto;

public class DeviceDetailByPlatformDayDtoMapper implements RowMapper<DeviceDetailByPlatformDayDto> {

    public DeviceDetailByPlatformDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByPlatformDayDto row = new DeviceDetailByPlatformDayDto();
        row.setPlatform(rs.getString("platform"));
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
