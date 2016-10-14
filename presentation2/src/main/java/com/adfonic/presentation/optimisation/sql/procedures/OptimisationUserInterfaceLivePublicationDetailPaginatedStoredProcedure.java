package com.adfonic.presentation.optimisation.sql.procedures;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.dto.optimisation.OptimisationUserInterfaceLivePublicationDto;
import com.adfonic.presentation.optimisation.sql.mappers.OptimisationUserInterfaceLivePublicationDtoMapper;

public class OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure extends OptimisationUserInterfaceBaseStoredProcedure {

	public OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure(DataSource ds) {
		this(ds, "proc_return_adv_cam_opti_report_detail");
	}
	
	public OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_id", Types.VARCHAR));
		declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));

		declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
		declareParameter(new SqlParameter("in_sort_direction", Types.VARCHAR));
		declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
		declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
		declareParameter(new SqlParameter("in_removed_pubs", Types.NUMERIC));

		declareParameter(new SqlReturnResultSet("result", new OptimisationUserInterfaceLivePublicationDtoMapper(false)));
		compile();
	}
	
	public List<OptimisationUserInterfaceLivePublicationDto> run(
			long advertiserId, 
			long campaignId, 
			String dateRange, 
			String sortColumn,
			String sortDirection,
			int recordsPerPage,
			int startPage) {
		return this.run(
				advertiserId, 
				Long.toString(campaignId), 
				new Integer(dateRange), 
				getSortNumberValue(sortColumn), 
				sortDirection, recordsPerPage, 
				startPage);
	}

	@SuppressWarnings("unchecked")
	public List<OptimisationUserInterfaceLivePublicationDto> run(
			long advertiserId, 
			String campaignId, 
			int dateRange, 
			int sortColumn,
			String sortDirection,
			int recordsPerPage,
			int startPage) {
        Map<String, Object> data = super.run(advertiserId, campaignId, dateRange, sortColumn, sortDirection, recordsPerPage, startPage, 0);

        List<OptimisationUserInterfaceLivePublicationDto> list = (List<OptimisationUserInterfaceLivePublicationDto>)data.get("result");
        return list;
	}

}
