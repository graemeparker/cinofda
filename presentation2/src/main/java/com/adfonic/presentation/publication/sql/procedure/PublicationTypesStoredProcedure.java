package com.adfonic.presentation.publication.sql.procedure;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationTypesNameIdDtoRowMapper;

public class PublicationTypesStoredProcedure extends StoredProcedure {

    public PublicationTypesStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);

        declareParameter(new SqlReturnResultSet("result", new PublicationTypesNameIdDtoRowMapper()));
        compile();
    }

}
