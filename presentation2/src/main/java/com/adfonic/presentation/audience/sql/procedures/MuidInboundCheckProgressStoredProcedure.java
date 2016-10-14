package com.adfonic.presentation.audience.sql.procedures;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.audience.sql.mappers.MuidSessionModelResultSetExtractor;

public class MuidInboundCheckProgressStoredProcedure extends StoredProcedure {

    public MuidInboundCheckProgressStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);
        declareParameter(new SqlParameter("session_id", Types.NUMERIC));

        declareParameter(new SqlReturnResultSet("result", new MuidSessionModelResultSetExtractor()));
        compile();
    }

}
