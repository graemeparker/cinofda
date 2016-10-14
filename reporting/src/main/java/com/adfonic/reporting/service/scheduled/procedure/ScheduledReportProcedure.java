package com.adfonic.reporting.service.scheduled.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;

public class ScheduledReportProcedure extends BaseStoredProcedure {
	
	private static final String name = "proc_add_scheduled_report_adv";
	
	public ScheduledReportProcedure(DataSource ds) {
		super(ds, name);
		declareParameter(new SqlParameter("in_scheduled_report_type_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_scheduled_report_frequency_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_scheduled_report_time_period_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_cre_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_fmt_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_mdl_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_vndr_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_custom_start_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_custom_end_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_report_email_addresses", Types.VARCHAR));
		declareParameter(new SqlParameter("in_date_format", Types.VARCHAR)); // Same as the date formatting data you'd pass to DB's DATE_FORMAT() built in function. Can not be null, default to empty string
		declareParameter(new SqlParameter("in_run_hour", Types.NUMERIC)); // cannot be null, default to 0
		declareParameter(new SqlOutParameter("out_result", Types.VARCHAR));
		compile();
	}
}
