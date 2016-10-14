package com.adfonic.reporting.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.mapper.DeviceDetailDtoMapper;

public class DevicesReportDetailStoredProcedure extends BaseStoredProcedure {

	public DevicesReportDetailStoredProcedure(DataSource ds, String name){
		super(ds,name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC)); // Advertiser Id
		declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR)); // Campaign Ids
		declareParameter(new SqlParameter("in_mdl_ids", Types.VARCHAR)); // Model Ids 
		declareParameter(new SqlParameter("in_vnd_ids", Types.VARCHAR)); // Vendor Ids
		declareParameter(new SqlParameter("in_start_date", Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_date", Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result",new DeviceDetailDtoMapper()));
		compile();
	}
	
}
