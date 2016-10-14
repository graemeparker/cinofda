package com.adfonic.dto.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;

public class DashboardDto extends BaseDashboardDto {
    
    private static final long serialVersionUID = 1L;
    
    private static final long DEFAULT_NUMBER_OF_RECORDS = 25L;

    private AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto;

    private List<NameIdBusinessDto> campaigns = new ArrayList<NameIdBusinessDto>(0);

    private CampaignStatus campaignStatusFilter = CampaignStatus.ALL;

    private BidType bidTypeFilter = BidType.ALL;

    private List<NameIdBusinessDto> campaignsFiltered = new ArrayList<NameIdBusinessDto>(0);

    private List<Long> campaignsIdFiltered = new ArrayList<Long>(0);

    private AdvertiserDto advertiser;

    private boolean isStatusFiltered = false;

    private boolean isBidTypeFiltered = false;

    private SortBy sortBy = SortBy.CAMPAIGN_NAME;

    private Report report = Report.SPEND;

    private boolean recentlyFiltered = false;

    private boolean showDeletedCampaigns = false;
    
    private List<StatisticsDto> reportingTable = new ArrayList<StatisticsDto>(0);
    
    public DashboardDto() {
        numberOfRecords = DEFAULT_NUMBER_OF_RECORDS;
    }

    public boolean isFiltered() {
        return isStatusFiltered || isBidTypeFiltered || !CollectionUtils.isEmpty(campaigns);
    }

    public List<NameIdBusinessDto> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<NameIdBusinessDto> campaigns) {
        this.campaigns = campaigns;
    }

    public List<NameIdBusinessDto> getCampaignsFiltered() {
        return campaignsFiltered;
    }

    public void setCampaignsFiltered(List<NameIdBusinessDto> campaignsFiltered) {
        this.campaignsFiltered = campaignsFiltered;
    }

    public AdvertiserHeadlineStatsDto getStatisticsDto() {
        return advertiserHeadlineStatsDto;
    }

    public void setStatisticsDto(AdvertiserHeadlineStatsDto statisticsDto) {
        this.advertiserHeadlineStatsDto = statisticsDto;
    }

    public List<StatisticsDto> getReportingTable() {
        return reportingTable;
    }

    public void setReportingTable(List<StatisticsDto> reportingTable) {
        this.reportingTable = reportingTable;
    }

    public CampaignStatus getCampaignStatusFilter() {
        return campaignStatusFilter;
    }

    public void setCampaignStatusFilter(CampaignStatus campaignStatusFilter) {
        if (campaignStatusFilter != null) {
            this.campaignStatusFilter = campaignStatusFilter;
        }
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public BidType getBidTypeFilter() {
        return bidTypeFilter;
    }

    public void setBidTypeFilter(BidType bidTypeFilter) {
        if (bidTypeFilter != null) {
            this.bidTypeFilter = bidTypeFilter;
        }
    }

    public AdvertiserHeadlineStatsDto getAdvertiserHeadlineStatsDto() {
        return advertiserHeadlineStatsDto;
    }

    public void setAdvertiserHeadlineStatsDto(AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto) {
        this.advertiserHeadlineStatsDto = advertiserHeadlineStatsDto;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public List<Long> getCampaignsIdFiltered() {
        return campaignsIdFiltered;
    }

    public void setCampaignsIdFiltered(List<Long> campaignsIdFiltered) {
        this.campaignsIdFiltered = campaignsIdFiltered;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DashboardDto [advertiserHeadlineStatsDto=");
        builder.append(advertiserHeadlineStatsDto);
        builder.append(", campaigns=");
        builder.append(campaigns);
        builder.append(", individualLines=");
        builder.append(individualLines);
        builder.append(", from=");
        builder.append(from);
        builder.append(", to=");
        builder.append(to);
        builder.append(", datePickerPresetValue=");
        builder.append(datePickerPresetValue);
        builder.append(", campaignStatusFilter=");
        builder.append(campaignStatusFilter);
        builder.append(", bidTypeFilter=");
        builder.append(bidTypeFilter);
        builder.append(", campaignsFiltered=");
        builder.append(campaignsFiltered);
        builder.append(", graphs=");
        builder.append(graphs);
        builder.append(", advertiser=");
        builder.append(advertiser);
        builder.append(", isFiltered=");
        builder.append(isFiltered());
        builder.append(", interval=");
        builder.append(interval);
        builder.append(", sortBy=");
        builder.append(sortBy);
        builder.append(", report=");
        builder.append(report);
        builder.append(", orderBy=");
        builder.append(orderBy);
        builder.append(", reportingTable=");
        builder.append(reportingTable);
        builder.append("]");
        return builder.toString();
    }

    public boolean isStatusFiltered() {
        return isStatusFiltered;
    }

    public void setStatusFiltered(boolean isStatusFiltered) {
        this.isStatusFiltered = isStatusFiltered;
    }

    public boolean isBidTypeFiltered() {
        return isBidTypeFiltered;
    }

    public void setBidTypeFiltered(boolean isBidTypeFiltered) {
        this.isBidTypeFiltered = isBidTypeFiltered;
    }

    public boolean isRecentlyFiltered() {
        return recentlyFiltered;
    }

    public void setRecentlyFiltered(boolean recentlyFiltered) {
        this.recentlyFiltered = recentlyFiltered;
    }

    public boolean isShowDeletedCampaigns() {
        return showDeletedCampaigns;
    }

    public void setShowDeletedCampaigns(boolean showDeletedCampaigns) {
        this.showDeletedCampaigns = showDeletedCampaigns;
    }

}
