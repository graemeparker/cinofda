package com.adfonic.presentation.dashboard.statistics;

import java.util.List;
import java.util.Map;

import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;

public interface AdvertiserDashboardDao {

    
    /**
     * Return the total number of records for the whole Reporting table for an Advertiser
     * 
     * @param advertiserId
     * @param campaignStatus
     * @param bidType
     * @param from
     * @param to
     * @return the number of records
     */
    public Long getNumberOfRecordsForDashboardReportingTableForAdvertiser(Long advertiserId, CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns);
    
    /**
     * Return the total number of record for the whole Reporting table for a given set of Campaigns
     * 
     * @param advertiserId
     * @param campaignIds
     * @param campaignStatus
     * @param bidType
     * @param from
     * @param to
     * @return the number of records
     */
    public Long getNumberOfRecordsForDashboardReportingTableForCampaigns(Long advertiserId,  List<Long> campaignIds, CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns);
    
    
    /**
     * Returns a list of StatisticsDto for the advertiser passed as param
     * 
     * @param advertiserId
     *            - the id advertiser
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @param sortBy
     *            - the column to sort this report by
     * @return the report
     */
    public List<StatisticsDto> getDashboardReportingTableForAdvertiser(Long advertiserId, CampaignStatus campaignStatus, BidType bidType, int dateRange,
            SortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords,boolean showDeletedCampaigns);

    /**
     * Returns a list of StatisticsDto for the advertiser and campaignId passed
     * as param
     * 
     * @param advertiserId
     *            - the id of advertiser
     * @param campaignIds
     *            - the campaigns as '~' separated, eg, 1~23~12~12
     * @param from
     *            - the date from where this report runs
     * @param to
     *            - the date to where this report runs
     * @param sortBy
     *            - the column to sort this report by
     * @return the report
     */
    public List<StatisticsDto> getDashboardReportingTableForCampaigns(Long advertiserId,
            List<Long> campaignIds, CampaignStatus campaignStatus, BidType bidType, int dateRange, SortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords,boolean showDeletedCampaigns);

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
    public Map<Object, Number> getChartDataForAdvertiser(Long advertiserId, int dateRange, Report report, CampaignStatus campaignStatus,
            BidType bidType,boolean showDeletedCampaigns);

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
    public Map<Object, Number> getChartDataForCampaign(Long advertiserId, List<Long> campaignIds, CampaignStatus campaignStatus, BidType bidType, int dateRange,
            Report report,boolean showDeletedCampaigns);

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
    public AdvertiserHeadlineStatsDto getHeadlineFiguresForAdvertiser(Long advertiserId, CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns);

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
    public AdvertiserHeadlineStatsDto getHeadlineFiguresForCampaigns(Long advertiserId, List<Long> campaignIds, CampaignStatus campaignStatus, BidType bidType,
            int dateRange,boolean showDeletedCampaigns);

}