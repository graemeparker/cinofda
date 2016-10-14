package com.adfonic.presentation.publication.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationApprovalDtoRowMapper;

public class PublicationApprovalSearchStoredProcedure extends StoredProcedure {

    public PublicationApprovalSearchStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);
        declareParameter(new SqlParameter("in_publication_ids", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_name_search", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_friendly_name_search", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publisher_name_search", Types.VARCHAR));
        declareParameter(new SqlParameter("in_supplier_user_names", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_external_ids", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_types", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_statuses", Types.VARCHAR));
        declareParameter(new SqlParameter("in_assigned_to_emails", Types.VARCHAR));
        declareParameter(new SqlParameter("in_account_types", Types.VARCHAR));
        declareParameter(new SqlParameter("in_publication_rtb_id_search", Types.VARCHAR));
        declareParameter(new SqlParameter("in_seller_network_ids", Types.VARCHAR));
        declareParameter(new SqlParameter("in_bundle_external_ids_search", Types.VARCHAR));
        declareParameter(new SqlParameter("in_algorithm_status", Types.VARCHAR));
        declareParameter(new SqlParameter("in_dead_zone_status", Types.VARCHAR));
        declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
        declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
        declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
        declareParameter(new SqlParameter("in_sort_direction", Types.VARCHAR));
        
        declareParameter(new SqlOutParameter("out_record_count", Types.NUMERIC));

        declareParameter(new SqlReturnResultSet("result", new PublicationApprovalDtoRowMapper()));
        compile();
    }

}
