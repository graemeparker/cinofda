package com.adfonic.reporting.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.mapper.LocationReportDetailByCategoryDtoMapper;

public class LocationReportDetailByCategoryStoredProcedure extends BaseStoredProcedure {

	public LocationReportDetailByCategoryStoredProcedure(DataSource ds,
			String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
		declareParameter(new SqlParameter("in_start_day_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_day_id", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result", new LocationReportDetailByCategoryDtoMapper()));
		compile();
	}

}
