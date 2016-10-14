package com.adfonic.presentation.publication.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.presentation.publication.model.PublicationAssignedToUserModel;

public class PublicationAssignedToUsersDtoRowMapper implements RowMapper<PublicationAssignedToUserModel> {

    @Override
    public PublicationAssignedToUserModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        PublicationAssignedToUserModel tableRow = new PublicationAssignedToUserModel();

        tableRow.setId(Long.valueOf(rs.getString("ID")));
        tableRow.setName(WordUtils.capitalize(rs.getString("assigned_to_user")));
        tableRow.setAssignedToCurrent((BooleanUtils.toBooleanObject(Integer.valueOf(rs.getString("assigned_to_current")))));
        tableRow.setAssignedToAny((BooleanUtils.toBooleanObject(Integer.valueOf(rs.getString("assigned_to_any_publication")))));

        return tableRow;
    }

}
