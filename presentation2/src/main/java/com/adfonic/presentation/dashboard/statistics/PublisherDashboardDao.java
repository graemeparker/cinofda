package com.adfonic.presentation.dashboard.statistics;

import java.util.List;
import java.util.Map;

import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherSortBy;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;

public interface PublisherDashboardDao {

    /**
     * TODO change to Enum for publicationStatus, platform, approval
     * 
     * @param publisherId
     * @param publicationStatus
     * @param platform
     * @param Approval
     * @param backFill
     * @param from
     * @param to
     * @return
     */
    public Long getNumberOfRecordsForDashboardReportingTableForPublisher(Long publisherId, PublicationStatus publicationStatus,
            PublicationtypeDto platform, Approval approval, Backfill backFill, int dateRange, PublisherSortBy sortBy);

    public Long getNumberOfRecordsForDashboardReportingTableForPublications(Long publisherId,
            List<Long> publicationIds, PublicationStatus publicationStatus, PublicationtypeDto platform, Approval approval, Backfill backFill,
            int dateRange, PublisherSortBy sortBy);

    public List<PublisherStatisticsDto> getDashboardReportingTableForPublisher(Long publisherId, PublicationStatus publicationStatus, PublicationtypeDto platform,
            Approval approval, Backfill backFill, int dateRange, PublisherSortBy sortBy, OrderBy orderBy, Long start,
            Long numberOfRecords);

    public List<PublisherStatisticsDto> getDashboardReportingTableForPublications(Long publisherId, List<Long> publicationIds,
            PublicationStatus publicationStatus, PublicationtypeDto platform, Approval approval, Backfill backFill, int dateRange,
            PublisherSortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords);

    /**
     * Returns chart data as a map of TimeStamps and Numbers
     * 
     * @param advertiserId
     *            - the id of advertiser
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @param report
     *            - the type of which chart data
     * @param interval
     *            - the interval of the chart data
     * @return the chart data
     */
    public Map<Object, Number> getChartDataForPublisher(Long publisherId, PublicationStatus publicationStatus, PublicationtypeDto platformDto, 
            Approval approval, Backfill backFill,int dateRange, PublisherReport report, Interval interval);

    /**
     * Returns chart data as a map of TimeStamps and Number
     * 
     * @param advertiserId
     *            - the id of advertiser
     * @param campaignIds
     *            - one or more campaign id as '~' separated, e.g, 1~23~12~12
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @param report
     *            - the type of which chart data
     * @param interval
     *            - the interval of the chart data
     * @return the chart data
     * @return
     */
    public Map<Object, Number> getChartDataForPublication(Long publisherId, List<Long> publicationIds, PublicationStatus publicationStatus, 
            PublicationtypeDto platformDto, Approval approval, Backfill backFill,int dateRange, PublisherReport report, Interval interval);

    /**
     * Returns Headline figure for the dashboard
     * 
     * @param advertiserId
     *            - the id of advertiser
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @return the data
     */
    public PublisherHeadlineStatsDto getHeadlineFiguresForPublisher(Long publisherId, PublicationStatus publicationStatus, 
            PublicationtypeDto platformDto, Approval approval, Backfill backFill,int dateRange);

    /**
     * Returns Headline figure for the dashboard
     * 
     * @param advertiserId
     *            - the id of advertiser
     * @param campaignIds
     *            - one or more campaign id as '~' separated, e.g, 1~23~12~12
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @return the data
     */
    public PublisherHeadlineStatsDto getHeadlineFiguresForPublications(Long publisherId, List<Long> publicationIds, 
            PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval, Backfill backFill,int dateRange);

}
