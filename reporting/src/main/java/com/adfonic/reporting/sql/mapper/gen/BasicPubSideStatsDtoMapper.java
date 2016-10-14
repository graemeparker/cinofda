package com.adfonic.reporting.sql.mapper.gen;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.sql.dto.gen.BasicPubSideStatsDto;

public class BasicPubSideStatsDtoMapper implements OrderedstatisticsMapper {

    /* (non-Javadoc)
     * @see com.adfonic.reporting.sql.mapper.gen.OrderedstatisticsMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public BasicPubSideStatsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicPubSideStatsDto stats = new BasicPubSideStatsDto();
        stats.setRequests(rs.getLong("requests"));
        stats.setImpressions(rs.getLong("impressions"));
        stats.setClicks(rs.getLong("clicks"));
        stats.setPayout(rs.getDouble("payout"));
        return stats;
    }

}
