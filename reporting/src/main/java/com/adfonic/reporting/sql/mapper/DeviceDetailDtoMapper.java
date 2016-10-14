package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.dto.DeviceDetailDto;

public class DeviceDetailDtoMapper implements RowMapper<DeviceDetailDto> {

	public DeviceDetailDto mapRow(ResultSet rs,int rownum) throws SQLException {
		DeviceDetailDto row = new DeviceDetailDto();
		row.setDevice(rs.getString("model"));
		ReportUtil.rowMapperDevice(row, rs);
		return row;
	}
	
	/*public static void mapRowHelper(DeviceBaseDto row, ResultSet rs) throws SQLException {
		DeviceDtoMapper.mapRowHelper(row, rs);
		row.setTotalViews(rs.getLong("total_views"));
		row.setCompletedViews(rs.getLong("completed_views"));
		row.setAverageDuration(rs.getLong("average_duration"));
		row.setCostPerView(rs.getDouble("cost_per_view"));
		row.setQ1Percent(rs.getDouble("q1_per"));
		row.setQ2Percent(rs.getDouble("q2_per"));
		row.setQ3Percent(rs.getDouble("q3_per"));
		row.setQ4Percent(rs.getDouble("q4_per"));
		row.setEngagementScore(rs.getDouble("engagement"));
		row.setClickConversions(rs.getDouble("click_conv"));
		row.setTotalConversions(rs.getLong("conversions"));
		row.setCostPerConversion(rs.getDouble("cost_per_conv"));
	}*/
	
}
