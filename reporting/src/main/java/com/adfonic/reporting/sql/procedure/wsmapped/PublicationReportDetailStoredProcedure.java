package com.adfonic.reporting.sql.procedure.wsmapped;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.mapper.PublicationStatisticsDetailDtoMapper;
import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;

public class PublicationReportDetailStoredProcedure extends BaseStoredProcedure {

    public PublicationReportDetailStoredProcedure(DataSource ds) {
        super(ds, "proc_return_pub_pubn_detail");
        declareParameter(new SqlParameter("in_pubr_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_pubn_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_start_time_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_end_time_id", Types.NUMERIC));
        declareParameter(new SqlReturnResultSet("result", new PublicationStatisticsDetailDtoMapper()));
    }

}
