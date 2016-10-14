package com.adfonic.reporting.service.scheduled.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.mapper.BaseReportRowMapper;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportFrequencyDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportStatusDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportTypeDto;

public class ScheduledReportTypeMapper extends BaseReportRowMapper<ScheduledReportTypeDto> {
	
	@Override
	public ScheduledReportTypeDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		ScheduledReportTypeDto row = new ScheduledReportTypeDto();

		row.setId(getInteger("scheduled_report_type_id"));
		row.setName(getString("report_display_name"));
		row.setColumnHeadings(getString("report_column_headings"));
		row.setColumnNames(getString("report_column_names"));
		row.setProcedureId(getInteger("report_stored_procedure_id"));
		row.setTransformColumns(getString("transform_columns"));
		
		return row;
	}
}
