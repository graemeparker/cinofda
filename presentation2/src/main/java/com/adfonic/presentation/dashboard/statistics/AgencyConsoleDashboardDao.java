package com.adfonic.presentation.dashboard.statistics;

import java.util.List;
import java.util.Map;

import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.dashboard.DashboardParameters.AgencyConsoleSortBy;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;

public interface AgencyConsoleDashboardDao {

    
    /**
     * Return the total number of records for the whole Reporting table for an Advertiser
     * 
     * @param advertisersIds
     * @param advertiserStatus
     * @param dateRange
     * @return the number of records
     */
    public Long getNumberOfRecordsForDashboardReportingTable(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange);
  
    /**
     * Returns a list of AgencyStatisticsDto for the advertisers passed
     * as param
     * 
     * @param advertisersIds
     *            - the ids of advertisers  as '~' separated, eg, 1~23~12~12
     * @param dateRange
     *            - the date range where this report runs
     * @param sortBy
     *            - the column to sort this report by
     * @param orderBy
     *            - order by ascending or descending
     * @param start
     *            - offset from the total list
     * @param numberOfRecords
     *            - number of records to return in the list          
     */
    public List<AgencyConsoleStatisticsDto> getDashboardReportingTable(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange, 
            AgencyConsoleSortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords);

    /**
     * Returns chart data as a map of TimeStamps and Numbers
     * @param advertisersIds
     * @param advertiserStatus
     * @param dateRange
     * @return the chart data
     */
    public Map<Object, Number> getChartData(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange);


}