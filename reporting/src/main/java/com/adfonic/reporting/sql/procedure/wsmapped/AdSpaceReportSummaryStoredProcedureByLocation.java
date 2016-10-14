package com.adfonic.reporting.sql.procedure.wsmapped;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;
import com.adfonic.reporting.sql.mapper.gen.BasicPubSideStatsDtoMapper;
import com.adfonic.reporting.sql.mapper.gen.StatisticsGroupDtoMapper;
import com.adfonic.reporting.sql.procedure.BaseStoredProcedure;

public class AdSpaceReportSummaryStoredProcedureByLocation extends BaseStoredProcedure {

    public AdSpaceReportSummaryStoredProcedureByLocation(DataSource ds) {
        super(ds, "proc_return_pub_adsp_loc_summary");
        declareParameter(new SqlParameter("in_adsp_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_start_time_id", Types.NUMERIC));
        declareParameter(new SqlParameter("in_end_time_id", Types.NUMERIC));
        declareParameter(new SqlReturnResultSet("result", 
                new StatisticsGroupDtoMapper(Tag.getTemplateTags(TAG.ADSLOT, TAG.COUNTRY), new BasicPubSideStatsDtoMapper())));
    }

}
