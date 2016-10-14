package com.adfonic.presentation.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.presentation.NameIdModel;

public abstract class AbstractNameIdDtoRowMapper implements RowMapper<NameIdModel> {

    protected static final String TILDE = "~";
    
    abstract protected String getNameColumnName();
    
    /** Default id column name in DB can be overwritten in children */
    protected String getIdColumnName() {
        return "ID";
    }
    
    /** Transformation on column name values like capitalize, default is none */
    protected String transformNameColumnValue(String nameColumnValue) {
        return nameColumnValue;
    }
    
    @Override
    public NameIdModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        NameIdModel tableRow = new NameIdModel();
        
        // Id is not required
        if(!StringUtils.isEmpty(getIdColumnName())) {
            tableRow.setId(rs.getLong(getIdColumnName()));
        }
        tableRow.setName(transformNameColumnValue(rs.getString(getNameColumnName())));

        return tableRow;
    }

}
