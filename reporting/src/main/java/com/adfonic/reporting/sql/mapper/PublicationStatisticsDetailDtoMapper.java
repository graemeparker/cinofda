package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.dto.PublicationStatisticsDetailDto;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;

/*
 * Not threadsafe. Use and throw object - mappers are being used that way
 */
public class PublicationStatisticsDetailDtoMapper implements RowMapper<PublicationStatisticsDetailDto> {

    Map<String, PublicationStatisticsDetailDto> pubStatsMap = new HashMap<String, PublicationStatisticsDetailDto>();


    @Override
    public PublicationStatisticsDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        String publicationId = rs.getString(TAG.PUBLICATION.column());
        PublicationStatisticsDetailDto pubDetailedStatsDto = pubStatsMap.get(publicationId);
        if (pubDetailedStatsDto == null) {
            pubDetailedStatsDto = new PublicationStatisticsDetailDto(publicationId);
            pubStatsMap.put(publicationId, pubDetailedStatsDto);
        }
        pubDetailedStatsDto.getTaggedSet().add(new PubSideStatisticsSummaryDtoMapper(TAG.ADSLOT).mapRow(rs, rowNum));
        return pubDetailedStatsDto;
    }

}
