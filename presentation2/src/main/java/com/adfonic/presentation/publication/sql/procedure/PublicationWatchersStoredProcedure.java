package com.adfonic.presentation.publication.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.publication.sql.mappers.PublicationWatcherDtoRowMapper;

public class PublicationWatchersStoredProcedure extends StoredProcedure {

    public PublicationWatchersStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);
        declareParameter(new SqlParameter("in_publication_id", Types.INTEGER));

        declareParameter(new SqlReturnResultSet("result", new PublicationWatcherDtoRowMapper()));
        compile();
    }

}
