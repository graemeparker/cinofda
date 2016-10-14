package com.adfonic.reporting.service.advertiser.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.service.advertiser.mapper.DeviceReportMapper;
import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;

public class DeviceReportProcedure extends BaseStoredProcedure {
	
	public DeviceReportProcedure(DataSource ds, String name) {
		super(ds, name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_mdl_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_vndr_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_start_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_day_id", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result", new DeviceReportMapper()));
		compile();
	}
}
