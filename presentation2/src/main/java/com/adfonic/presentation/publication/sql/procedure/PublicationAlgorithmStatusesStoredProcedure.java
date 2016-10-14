package com.adfonic.presentation.publication.sql.procedure;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationAlgorithmStatusesNameIdDtoRowMapper;

public class PublicationAlgorithmStatusesStoredProcedure extends StoredProcedure {

    public PublicationAlgorithmStatusesStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);

        declareParameter(new SqlReturnResultSet("result", new PublicationAlgorithmStatusesNameIdDtoRowMapper()));
        compile();
    }

}
