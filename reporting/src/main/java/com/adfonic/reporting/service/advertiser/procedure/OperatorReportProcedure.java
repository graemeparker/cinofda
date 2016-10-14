package com.adfonic.reporting.service.advertiser.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.service.advertiser.mapper.OperatorReportMapper;
import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;

public class OperatorReportProcedure extends BaseStoredProcedure {
	
	public OperatorReportProcedure(DataSource ds, String name) {
		super(ds, name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_start_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_day_id", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result", new OperatorReportMapper()));
		compile();
	}
}
