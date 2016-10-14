package com.adfonic.presentation.dashboard.statistics;

import java.util.List;

import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;

public interface StatisticsService {
    public final long NUM_RECORDS = 10;
    
	public AdvertiserHeadlineStatsDto getDashboardStatistics(final DashboardDto searchDto);
	public List<StatisticsDto> getDashboardReportingTable(final DashboardDto searchDto);
	
	/* Data for publishers*/
	public PublisherHeadlineStatsDto getPublisherDashboardStatistics();
    public List<PublisherStatisticsDto> getPublisherDashboardReportingTable(final PublisherDashboardDto searchDto);
}
