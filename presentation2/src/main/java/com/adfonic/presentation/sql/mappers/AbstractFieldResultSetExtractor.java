package com.adfonic.presentation.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public abstract class AbstractFieldResultSetExtractor<T> implements ResultSetExtractor<T> {

    protected abstract String getFieldResultSetColumnName();
    
    /** Return this values once there were no result */
    protected T getFieldResultSetDefaultValue(){
        return null;
    };

    @Override
    @SuppressWarnings("unchecked")
    public T extractData(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.next()) {
            return (T)rs.getObject(getFieldResultSetColumnName());
        }
        return getFieldResultSetDefaultValue();
    }

}
