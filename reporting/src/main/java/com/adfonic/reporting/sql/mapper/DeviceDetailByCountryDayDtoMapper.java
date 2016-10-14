package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryDayDto;

public class DeviceDetailByCountryDayDtoMapper implements RowMapper<DeviceDetailByCountryDayDto> {

    public DeviceDetailByCountryDayDto mapRow(ResultSet rs, int rownum) throws SQLException {
        DeviceDetailByCountryDayDto row = new DeviceDetailByCountryDayDto();
        row.setCountry(rs.getString("country"));
        row.setDay(rs.getString("advertiser_day_unix_timestamp"));
        row.setDevice(rs.getString("model"));
        ReportUtil.rowMapperDevice(row, rs);
        return row;
    }

}
