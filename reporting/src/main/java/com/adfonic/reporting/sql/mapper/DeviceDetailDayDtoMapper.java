package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailDayDto;

public class DeviceDetailDayDtoMapper implements RowMapper<DeviceDetailDayDto> {

    public DeviceDetailDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailDayDto row = new DeviceDetailDayDto();
        row.setDevice(rs.getString("model"));
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
