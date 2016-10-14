package com.adfonic.webservices.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.reporting.sql.WebServicesSQLQuery;
import com.adfonic.reporting.sql.dto.PublicationStatisticsDetailDto;
import com.adfonic.reporting.sql.dto.gen.BasicPubSideStatsDto;
import com.adfonic.reporting.sql.dto.gen.OrderedStatistics;
import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.StatisticsGroupDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;
import com.adfonic.reporting.sql.dto.gen.TagGroup;
import com.adfonic.reporting.sql.dto.gen.Tagged;

@Component
public class Reporting {

    private static final String PFX_ID_2ndPART = "id-";


    public static String getIdPfx(boolean pub) {
        return (pub ? "p" : "a") + PFX_ID_2ndPART;
    }

    private static final int PFX_ID_LEN = PFX_ID_2ndPART.length() + 1;


    public static String getPfxIdSfx() {
        return PFX_ID_2ndPART;
    }


    public static int getPfxIdLen() {
        return PFX_ID_LEN;
    }

    private static final transient Logger LOG = Logger.getLogger(Reporting.class.getName());

    @Autowired
    WebServicesSQLQuery wsSqlQuery;


    public Map<?, ?> getAdSpaceStatsLegacy(Long adSpaceId, String from, String to) {
        StatisticsBasicDto adSpaceStats = wsSqlQuery.getAdSpaceStatistics(adSpaceId, from, to);

        if (adSpaceStats == null) {
            Map<Long, Object> result = new HashMap<Long, Object>();
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Empty result map, manually setting all zeros");
            }
            result.put(adSpaceId, new Object[] { 0, 0, 0, 0 });
            return result;
        }

        return getLegacyMap(adSpaceStats);

    }


    public Map<?, ?> getPubStatsGroupedByPublicationLegacy(Long publisherId, Long publicationId, String from, String to) {
        List<Tagged> pubStats = wsSqlQuery.getPubStatsGroupedByPublications(publisherId, publicationId, from, to);

        return getLegacyMap(pubStats);
    }


    public Map<?, ?> getPubStatsGroupedByAdSpaceLegacy(Long publisherId, Long publicationId, String from, String to) {
        Set<PublicationStatisticsDetailDto> pubStats = wsSqlQuery.getPubStatsGroupedByAdSpaces(publisherId, publicationId, from, to);

        return getLegacyMap(pubStats);
    }


    private Map<?, ?> getLegacyMap(StatisticsBasicDto summaryDto) {
        Map<String, Object[]> result = new HashMap<String, Object[]>();
        return addToLegacyMap(summaryDto, false, result);
    }


    private Map<?, ?> addToLegacyMap(Tagged summaryDto, boolean pub, Map<String, Object[]> result) {
        OrderedStatistics stats = ((StatisticsBasicDto)summaryDto).getStatistics();
        String summaryDtoId = summaryDto.getTag().getValue();
        if (summaryDtoId == null) {
            return Collections.EMPTY_MAP;
        }
        result.put(getIdPfx(pub) + summaryDtoId, stats.asObjectArray());
        return result;
    }


    private Map<?, ?> getLegacyMap(List<Tagged> pubStats) {
        return getLegacyMap(pubStats, true);
    }


    private Map<?, ?> getLegacyMap(List<Tagged> pubStats, boolean pub) {
        Map<String, Object[]> result = new HashMap<String, Object[]>();
        for (Tagged stats : pubStats) {
            addToLegacyMap(stats, pub, result);
        }
        return result;
    }


    private Map<?, ?> getLegacyMap(Set<PublicationStatisticsDetailDto> pubStats) {
        Map<String, Map<?, ?>> result = new HashMap<String, Map<?, ?>>();
        for (PublicationStatisticsDetailDto stats : pubStats) {
            String statsId = stats.getTag().getValue();
            if (statsId == null) {
                return Collections.EMPTY_MAP;
            }
            result.put(getIdPfx(true) + statsId, getLegacyMap(new LinkedList<Tagged>(stats.getTaggedSet()), false));
        }
        return result;
    }

    public List<? extends TagGroup> getPubStatsGroupedByAdSpace(Long publisherId, Long publicationId, String from, String to) {
        return new ArrayList<>((Set<? extends TagGroup>)wsSqlQuery.getPubStatsGroupedByAdSpaces(publisherId, publicationId, from, to));
    }


    public Set<Tagged> getAdSpaceStatsByCountry(Long adSpaceId, String from, String to) {
        Tagged adSpaceStats = wsSqlQuery.getAdSpaceStatisticsByCountry(adSpaceId, from, to);
        
        if (adSpaceStats == null) {
            BasicPubSideStatsDto emptyStats=new BasicPubSideStatsDto();
            emptyStats.setClicks(0); emptyStats.setImpressions(0); emptyStats.setPayout(0); emptyStats.setRequests(0);
            StatisticsBasicDto stats = new StatisticsBasicDto(new Tag(TAG.COUNTRY, "OO"));
            stats.setStatistics(emptyStats);
            adSpaceStats=new StatisticsGroupDto(new Tag(TAG.ADSLOT, "0"));
            ((StatisticsGroupDto)adSpaceStats).addTagged(stats);
        }
        
        Set<Tagged> un=new HashSet<>();
        un.add(adSpaceStats);
        return un;
    }
    
    public Set<Tagged> getPubStatsGroupedByPublicationByCountry(Long publisherId, Long publicationId, String from, String to) {
        return wsSqlQuery.getPubStatsGroupedByPublicationsByCountry(publisherId, publicationId, from, to);
    }
    
    public Set<Tagged> getPubStatsGroupedByAdSpaceByCountry(Long publisherId, Long publicationId, String from, String to) {
        return (Set<Tagged>)wsSqlQuery.getPubStatsGroupedByAdSpacesByCountry(publisherId, publicationId, from, to);
    }

    public Set<Tagged> getAdvertiserStats(Long advertiserId, Long campaignId, String from, String to) {
        return wsSqlQuery.getAdvertiserStats(advertiserId, campaignId, from, to);
    }
    
    public Set<Tagged> getAdvStatsGroupedByCreativesByDay(Long advertiserId, Long campaignId, String from, String to){
        return wsSqlQuery.getAdvStatsGroupedByCreativesByDay(advertiserId, campaignId, from, to);
    }

    public Set<Tagged> getAdvStatsGroupedByCreatives(Long advertiserId, Long campaignId, String from, String to){
        return wsSqlQuery.getAdvStatsGroupedByCreatives(advertiserId, campaignId, from, to);
    }
}
