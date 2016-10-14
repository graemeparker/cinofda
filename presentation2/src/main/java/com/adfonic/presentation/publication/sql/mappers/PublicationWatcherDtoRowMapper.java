package com.adfonic.presentation.publication.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.presentation.publication.model.PublicationWatcherModel;

public class PublicationWatcherDtoRowMapper implements RowMapper<PublicationWatcherModel> {

    @Override
    public PublicationWatcherModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        PublicationWatcherModel tableRow = new PublicationWatcherModel();

        tableRow.setId(Long.valueOf(rs.getString("ID")));
        tableRow.setName(WordUtils.capitalize(rs.getString("watcher_name")));
        tableRow.setIsWatcher((BooleanUtils.toBooleanObject(Integer.valueOf(rs.getString("is_watcher")))));

        return tableRow;
    }

}
