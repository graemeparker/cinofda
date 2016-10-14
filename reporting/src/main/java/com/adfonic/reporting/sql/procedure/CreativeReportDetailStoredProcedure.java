package com.adfonic.reporting.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;

public class CreativeReportDetailStoredProcedure<T> extends ResultSetProcedure<T> {
	public CreativeReportDetailStoredProcedure(DataSource ds, RowMapper<T> mapper) {
		super(ds, "proc_return_adv_cre_report_detail", mapper);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_cre_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_fmt_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_start_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_day_id", Types.NUMERIC));
		compile();
	}
}
