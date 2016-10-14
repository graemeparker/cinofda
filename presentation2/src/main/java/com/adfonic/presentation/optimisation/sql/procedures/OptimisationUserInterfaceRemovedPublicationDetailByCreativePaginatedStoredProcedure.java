package com.adfonic.presentation.optimisation.sql.procedures;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.dto.optimisation.OptimisationUserInterfaceRemovedPublicationDto;
import com.adfonic.presentation.optimisation.sql.mappers.OptimisationUserInterfaceRemovedPublicationDtoMapper;

public class OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure extends OptimisationUserInterfaceBaseStoredProcedure {

	public OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure(DataSource ds) {
		this(ds, "proc_return_adv_cre_opti_report_detail");
	}
	
	public OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure(DataSource ds, String name) {
		super(ds,name);
		declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
		declareParameter(new SqlParameter("in_cam_id", Types.VARCHAR));
		declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));

		declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
		declareParameter(new SqlParameter("in_sort_direction", Types.VARCHAR));
		declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
		declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
		declareParameter(new SqlParameter("in_removed_pubs", Types.NUMERIC));

		declareParameter(new SqlReturnResultSet("result", new OptimisationUserInterfaceRemovedPublicationDtoMapper(true)));
		compile();
	}

	public List<OptimisationUserInterfaceRemovedPublicationDto> run(
			Long advertiserId, 
			Long campaignId, 
			String dateRange, 
			String sortColumn,
			String sortDirection,
			Integer recordsPerPage,
			Integer startPage) {
		return this.run(
				advertiserId, 
				campaignId.toString(), 
				new Integer(dateRange), 
				getSortNumberValue(sortColumn), 
				sortDirection, recordsPerPage, 
				startPage);
	}

	@SuppressWarnings("unchecked")
	public List<OptimisationUserInterfaceRemovedPublicationDto> run(
			Long advertiserId, 
			String campaignId, 
			Integer dateRange, 
			Integer sortColumn,
			String sortDirection,
			Integer recordsPerPage,
			Integer startPage) {
        Map<String, Object> data = super.run(advertiserId, campaignId, dateRange, sortColumn, sortDirection, recordsPerPage, startPage, 1);

        List<OptimisationUserInterfaceRemovedPublicationDto> list = (List<OptimisationUserInterfaceRemovedPublicationDto>)data.get("result");
        return list;
	}

}
