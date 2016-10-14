package com.adfonic.reporting.sql.procedure;

import javax.sql.DataSource;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.mapper.BudgetReportCampaignDailyDtoMapper;

public class BudgetReportCampaignDailyProcedure extends BaseStoredProcedure {

	public BudgetReportCampaignDailyProcedure(DataSource ds, String name) {
		super(ds, name);
		declareParameter(new SqlParameter("in_adv_id",Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_id",Types.VARCHAR));
		declareParameter(new SqlParameter("in_start_date",Types.NUMERIC));
		declareParameter(new SqlParameter("in_end_date",Types.NUMERIC));
		declareParameter(new SqlReturnResultSet("result",new BudgetReportCampaignDailyDtoMapper()));
	}

}
