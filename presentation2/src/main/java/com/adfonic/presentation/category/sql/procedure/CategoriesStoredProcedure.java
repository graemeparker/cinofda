package com.adfonic.presentation.category.sql.procedure;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.presentation.category.sql.mappers.CategoriesNameIdRowMapper;

public class CategoriesStoredProcedure extends StoredProcedure {

    public CategoriesStoredProcedure(DataSource dataSource, String procedureCall) {
        super(dataSource, procedureCall);
        declareParameter(new SqlParameter("in_search_str", Types.VARCHAR));

        declareParameter(new SqlReturnResultSet("result", new CategoriesNameIdRowMapper()));
        compile();
    }

}
