package com.adfonic.dto.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.dashboard.DashboardParameters.AgencyConsoleSortBy;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;

public class AgencyConsoleDashboardDto extends BaseDashboardDto {

    private static final long serialVersionUID = 1L;

    private List<AdvertiserDto> advertisersRequested = new ArrayList<AdvertiserDto>();

    private List<AdvertiserDto> advertisers = new ArrayList<AdvertiserDto>();

    private List<Long> advertisersIdFiltered = new ArrayList<Long>(0);

    private List<AgencyConsoleStatisticsDto> reportingTable = new ArrayList<AgencyConsoleStatisticsDto>(0);

    private AgencyConsoleSortBy sortBy = AgencyConsoleSortBy.ADVERTISER_NAME;

    private AdvertiserStatus statusFilter;

    private boolean statusFiltered;

    private boolean recentlyFiltered;

    public List<AdvertiserDto> getAdvertisers() {
        return advertisers;
    }

    public void setAdvertisers(List<AdvertiserDto> advertisers) {
        this.advertisers = advertisers;
    }

    public List<Long> getAdvertisersIdFiltered() {
        return advertisersIdFiltered;
    }

    public void setAdvertisersIdFiltered(List<Long> advertisersIdFiltered) {
        this.advertisersIdFiltered = advertisersIdFiltered;
    }

    public List<AgencyConsoleStatisticsDto> getReportingTable() {
        return reportingTable;
    }

    public void setReportingTable(List<AgencyConsoleStatisticsDto> reportingTable) {
        this.reportingTable = reportingTable;
    }

    public AgencyConsoleSortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(AgencyConsoleSortBy sortBy) {
        this.sortBy = sortBy;
    }

    public AdvertiserStatus getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(AdvertiserStatus statusFilter) {
        this.statusFilter = statusFilter;
    }

    public boolean isStatusFiltered() {
        return statusFiltered;
    }

    public void setStatusFiltered(boolean statusFiltered) {
        this.statusFiltered = statusFiltered;
    }

    public boolean isRecentlyFiltered() {
        return recentlyFiltered;
    }

    public void setRecentlyFiltered(boolean recentlyFiltered) {
        this.recentlyFiltered = recentlyFiltered;
    }

    public List<AdvertiserDto> getAdvertisersRequested() {
        return advertisersRequested;
    }

    public void setAdvertisersRequested(List<AdvertiserDto> advertisersRequested) {
        this.advertisersRequested = advertisersRequested;
    }

}
