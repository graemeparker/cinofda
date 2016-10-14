package com.adfonic.presentation.publication.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.presentation.publication.model.PublicationHistoryModel;

public class PublicationHistoryDtoRowMapper implements RowMapper<PublicationHistoryModel> {

    @Override
    public PublicationHistoryModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        PublicationHistoryModel tableRow = new PublicationHistoryModel();

        tableRow.setEventTime(rs.getString("EVENT_TIME"));
        tableRow.setLoggedBy(rs.getString("logged_by"));
        tableRow.setAssignedTo(rs.getString("assigned_to"));
        tableRow.setStatus(rs.getString("STATUS"));
        tableRow.setAdOpsStatus(rs.getString("AD_OPS_STATUS"));
        tableRow.setComment(rs.getString("COMMENT"));

        return tableRow;
    }

}
