package com.adfonic.presentation.publication.sql.procedure;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationAccountTypesNameIdDtoRowMapper;

public class PublicationAccountTypesStoredProcedure extends StoredProcedure {

    public PublicationAccountTypesStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);

        declareParameter(new SqlReturnResultSet("result", new PublicationAccountTypesNameIdDtoRowMapper()));
        compile();
    }

}
