package com.adfonic.reporting.sql.mapper.gen;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.sql.dto.gen.BasicAdvSideStatsDto;
import com.adfonic.reporting.sql.dto.gen.OrderedStatistics;

public class BasicAdvSideStatsDtoMapper implements OrderedstatisticsMapper {

    @Override
    public OrderedStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
        BasicAdvSideStatsDto stats = new BasicAdvSideStatsDto();
        stats.setImpressions(rs.getLong("impressions"));
        stats.setClicks(rs.getLong("clicks"));
        stats.setCtr(rs.getDouble("ctr"));
        stats.setEcpm(rs.getDouble("ecpm"));
        stats.setEcpc(rs.getDouble("ecpc"));
        stats.setConversions(rs.getLong("conversions"));
        stats.setCostPerConversion(rs.getDouble("cost_per_conversion"));
        stats.setSpend(rs.getDouble("cost"));
        stats.setClickConversion(rs.getDouble("click_conversion"));
        return stats;
    }

}
