package com.adfonic.presentation.audience.sql.mappers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.adfonic.presentation.audience.model.MuidSessionModel;

public class MuidSessionModelResultSetExtractor implements ResultSetExtractor<MuidSessionModel> {

    @Override
    public MuidSessionModel extractData(ResultSet rs) throws SQLException, DataAccessException {
        MuidSessionModel tableRow = null;
        if (rs.next()) {
            tableRow = new MuidSessionModel();
            tableRow.setStatus(rs.getString(1));
            tableRow.setIngested(BigDecimal.valueOf(Long.valueOf(rs.getString(2))));
        }
        return tableRow;
    }

}
