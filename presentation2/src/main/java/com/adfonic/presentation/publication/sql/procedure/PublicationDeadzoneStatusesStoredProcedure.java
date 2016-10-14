package com.adfonic.presentation.publication.sql.procedure;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationDeadzoneStatusesNameIdDtoRowMapper;

public class PublicationDeadzoneStatusesStoredProcedure extends StoredProcedure {

    public PublicationDeadzoneStatusesStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);

        declareParameter(new SqlReturnResultSet("result", new PublicationDeadzoneStatusesNameIdDtoRowMapper()));
        compile();
    }

}
