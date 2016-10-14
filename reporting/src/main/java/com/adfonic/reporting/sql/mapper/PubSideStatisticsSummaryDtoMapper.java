package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;
import com.adfonic.reporting.sql.mapper.gen.BasicPubSideStatsDtoMapper;

public class PubSideStatisticsSummaryDtoMapper implements RowMapper<StatisticsBasicDto> {

    Tag tag;


    public PubSideStatisticsSummaryDtoMapper(TAG tag) {
        this.tag = Tag.getTemplate(tag);
    }


    @Override
    public StatisticsBasicDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        StatisticsBasicDto stats = new StatisticsBasicDto(tag.cloneWith(rs.getString(tag.getKey().column())));
        stats.setStatistics(new BasicPubSideStatsDtoMapper().mapRow(rs, rowNum));
        return stats;
    }

}
