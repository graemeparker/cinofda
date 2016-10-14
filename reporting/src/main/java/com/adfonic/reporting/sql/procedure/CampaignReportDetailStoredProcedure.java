package com.adfonic.reporting.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

public class CampaignReportDetailStoredProcedure<T> extends BaseStoredProcedure {
	
   public CampaignReportDetailStoredProcedure(DataSource ds, RowMapper<T> mapper) {
        super(ds,"proc_return_adv_cam_report_detail");
        declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
        declareParameter(new SqlParameter("in_start_day_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_end_day_id", Types.NUMERIC));
        declareParameter(new SqlReturnResultSet("result", mapper));
        compile();
    }
}
