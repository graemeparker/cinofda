package com.adfonic.reporting.sql.procedure;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

public class ResultSetProcedure<T> extends BaseStoredProcedure {

    private static final String RESULT = "result";


    public ResultSetProcedure(DataSource ds, String name, RowMapper<T> mapper) {
        super(ds, name);
        declareParameter(new SqlReturnResultSet(RESULT, mapper));
    }


    @SuppressWarnings("unchecked")
    public List<T> resultInList(Object... inParams) {
        return (List<T>) super.execute(inParams).get(RESULT);
    }


    @Override
    public Map<String, Object> execute(Object... inParams) {
        throw new RuntimeException("no thoroughfare!");
    }

    // util
    protected ResultSetProcedure<T> declareInParam(String paramName, int paramType) {
        declareParameter(new SqlParameter(paramName, paramType));
        return this;
    }
}
