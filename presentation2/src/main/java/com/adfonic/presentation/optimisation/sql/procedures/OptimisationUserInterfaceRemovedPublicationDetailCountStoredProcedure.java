package com.adfonic.presentation.optimisation.sql.procedures;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.presentation.optimisation.sql.mappers.OptimisationUserInterfaceRecordCountResultSetExtractor;

public class OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure extends OptimisationUserInterfaceBaseStoredProcedure {

	public OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure(DataSource ds) {
		this(ds, "proc_return_adv_cam_opti_report_detail_record_count");
	}
	
	public OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_id", Types.VARCHAR));
		declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));

		declareParameter(new SqlParameter("in_removed_pubs", Types.NUMERIC));

		declareParameter(new SqlReturnResultSet("result", new OptimisationUserInterfaceRecordCountResultSetExtractor()));
		compile();
	}

	public Long run(
			Long advertiserId, 
			Long campaignId, 
			String dateRange) {
		return this.run(
				advertiserId, 
				campaignId.toString(), 
				new Integer(dateRange));
	}

	public Long run(
			Long advertiserId, 
			String campaignId, 
			Integer dateRange) {
        Map<String, Object> data = super.run(advertiserId, campaignId, dateRange, 1);

        Long count = (Long)data.get("result");
        return count;
	}
}
