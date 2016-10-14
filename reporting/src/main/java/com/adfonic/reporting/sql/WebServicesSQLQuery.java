package com.adfonic.reporting.sql;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.SqlReturnResultSet;

import com.adfonic.reporting.sql.dto.PublicationStatisticsDetailDto;
import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.reporting.sql.mapper.gen.BasicAdvSideStatsDtoMapper;
import com.adfonic.reporting.sql.mapper.gen.StatisticsGroupDtoMapper;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.wsmapped.AdSpaceReportSummaryStoredProcedure;
import com.adfonic.reporting.sql.procedure.wsmapped.AdSpaceReportSummaryStoredProcedureByLocation;
import com.adfonic.reporting.sql.procedure.wsmapped.PublicationReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.wsmapped.PublicationReportDetailStoredProcedureByLocation;
import com.adfonic.reporting.sql.procedure.wsmapped.PublicationReportSummaryStoredProcedure;
import com.adfonic.reporting.sql.procedure.wsmapped.PublicationReportSummaryStoredProcedureByLocation;

public class WebServicesSQLQuery extends BaseSQLQuery {

    public StatisticsBasicDto getAdSpaceStatistics(Long adSpaceId, String from, String to) {
        AdSpaceReportSummaryStoredProcedure proc = new AdSpaceReportSummaryStoredProcedure(getDataSource());
        Map<String, Object> data = proc.execute(adSpaceId, from, to);
        List<StatisticsBasicDto> rowData = (List<StatisticsBasicDto>) data.get("result");
        return rowData.isEmpty() ? null : rowData.get(0);
    }


    public List<Tagged> getPubStatsGroupedByPublications(Long publisherId, Long publicationId, String from, String to) {
        PublicationReportSummaryStoredProcedure proc = new PublicationReportSummaryStoredProcedure(getDataSource());
        Map<String, Object> data = proc.execute(publisherId, publicationId, from, to);
        List<Tagged> rowData = (List<Tagged>) data.get("result");
        return rowData;
    }


    public Set<PublicationStatisticsDetailDto> getPubStatsGroupedByAdSpaces(Long publisherId, Long publicationId, String from, String to) {
        PublicationReportDetailStoredProcedure proc = new PublicationReportDetailStoredProcedure(getDataSource());
        Map<String, Object> data = proc.execute(publisherId, publicationId, from, to);
        List<PublicationStatisticsDetailDto> rowData = (List<PublicationStatisticsDetailDto>) data.get("result");
        return new LinkedHashSet<PublicationStatisticsDetailDto>(rowData);// to take out the duplicate entries returned by the mapper; id hashing
    }

    // new format
    
    public Tagged getAdSpaceStatisticsByCountry(Long adSpaceId, String from, String to) {
        AdSpaceReportSummaryStoredProcedureByLocation proc = new AdSpaceReportSummaryStoredProcedureByLocation(getDataSource());
        Map<String, Object> data = proc.execute(adSpaceId, from, to);
        List<Tagged> rowData = (List<Tagged>) data.get("result");
        return rowData.isEmpty() ? null : rowData.get(0);
    }


    public Set<Tagged> getPubStatsGroupedByPublicationsByCountry(Long publisherId, Long publicationId, String from, String to) {
        PublicationReportSummaryStoredProcedureByLocation proc = new PublicationReportSummaryStoredProcedureByLocation(getDataSource());
        Map<String, Object> data = proc.execute(publisherId, publicationId, from, to);
        List<Tagged> rowData = (List<Tagged>) data.get("result");
        return new LinkedHashSet<Tagged>(rowData);// to take out the duplicate entries returned by the mapper; id hashing
    }


    public Set<Tagged> getPubStatsGroupedByAdSpacesByCountry(Long publisherId, Long publicationId, String from, String to) {
        PublicationReportDetailStoredProcedureByLocation proc = new PublicationReportDetailStoredProcedureByLocation(getDataSource());
        Map<String, Object> data = proc.execute(publisherId, publicationId, from, to);
        List<Tagged> rowData = (List<Tagged>) data.get("result");
        return new LinkedHashSet<Tagged>(rowData);// to take out the duplicate entries returned by the mapper; id hashing
    }
    
    
    // Advertiser 
 
    public Set<Tagged> getAdvertiserStats(Long advertiserId, Long campaignId, String from, String to) {
        CampaignReportDetailStoredProcedure<Tagged> proc = new CampaignReportDetailStoredProcedure<Tagged>(getDataSource(), new StatisticsGroupDtoMapper(Tag.getTemplateTags(TAG.CAMPAIGN), new BasicAdvSideStatsDtoMapper()));
        List<Tagged> rowData = (List<Tagged>) proc.execute(advertiserId, campaignId.toString(), from, to).get("result");
        return new LinkedHashSet<>(rowData);
    }
    
    public Set<Tagged> getAdvStatsGroupedByCreativesByDay(Long advertiserId, Long campaignId, String from, String to){
        CreativeReportDetailByDayStoredProcedure<Tagged> proc=new CreativeReportDetailByDayStoredProcedure<>(getDataSource(), 
                                                                new StatisticsGroupDtoMapper(Tag.getTemplateTags(TAG.CAMPAIGN, TAG.CREATIVE, TAG.DATE_YYYYMMDD), 
                                                                                                new BasicAdvSideStatsDtoMapper()));
        List<Tagged> rowData = proc.resultInList(advertiserId, campaignId.toString(), null, null, from, to);
        return new LinkedHashSet<>(rowData);
    }

    public Set<Tagged> getAdvStatsGroupedByCreatives(Long advertiserId, Long campaignId, String from, String to){
        CreativeReportDetailStoredProcedure<Tagged> proc=new CreativeReportDetailStoredProcedure<>(getDataSource(), 
                                                                new StatisticsGroupDtoMapper(Tag.getTemplateTags(TAG.CAMPAIGN, TAG.CREATIVE), 
                                                                                                new BasicAdvSideStatsDtoMapper()));
        List<Tagged> rowData = proc.resultInList(advertiserId, campaignId.toString(), null, null, from, to);
        return new LinkedHashSet<>(rowData);
    }
}
