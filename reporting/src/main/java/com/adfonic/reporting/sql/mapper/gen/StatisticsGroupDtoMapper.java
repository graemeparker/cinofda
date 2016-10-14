package com.adfonic.reporting.sql.mapper.gen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.sql.dto.gen.StatisticsGroupDto;
import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;

/**
 * Attempts to map out a set of rows as a hierarchy without creating many objects
 * 
 * Use and throw object
 * 
 * Written to be generally thread-safe but should not be in a situation where mapper 
 * is used by multiple threads because that'll mean no ordering guarantee
 * 
 */
public class StatisticsGroupDtoMapper implements RowMapper<Tagged> {

    public StatisticsGroupDtoMapper(List<Tag> tagLevels, OrderedstatisticsMapper basicStatsMapper) {
        if (tagLevels == null || tagLevels.isEmpty()) {
            throw new RuntimeException("Cannot create an idle mapper!");
        }

        this.tagLevels = tagLevels;
        this.basicStatsMapper = basicStatsMapper;
    }

    private final List<Tag> tagLevels;

    private final OrderedstatisticsMapper basicStatsMapper;

    private final StatisticsGroupDto rootCollector = new StatisticsGroupDto(Tag.getTemplate(TAG.ROOT));


    @Override
    public Tagged mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(rs, rowNum, rootCollector, tagLevels);
    }


    private Tagged mapRow(ResultSet rs, int rowNum, StatisticsGroupDto currentGroup, List<Tag> tagLevels) throws SQLException {
        int level = tagLevels.size();
        Tag tag = tagLevels.get(0);
        String dispId = rs.getString(tag.getKey().column());
        Tag clonedTag = tag.cloneWith(dispId);

        if (level == 1) {
            StatisticsBasicDto stats = new StatisticsBasicDto(tag.cloneWith(dispId));
            if (currentGroup.getTaggedSet().contains(stats)) {
                throw new RuntimeException("Duplicate!");
            }

            currentGroup.addTagged(stats);
            stats.setStatistics(basicStatsMapper.mapRow(rs, rowNum));

            return stats;
        }

        StatisticsGroupDto group = currentGroup.getChildGroupByTag(clonedTag);
        mapRow(rs, rowNum, group, tagLevels.subList(1, level));
        return group;
    }
}
