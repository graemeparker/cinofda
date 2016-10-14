package com.adfonic.presentation.dashboard;

import java.util.List;
import java.util.Map;

import com.adfonic.dto.dashboard.BaseDashboardDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.Report;

public interface DashboardService {

    /** 
     * Returns data to populate chart on Dashboard
     * @param searchDto 
     * @param report
     * @param interval
     * @return
     */
    public List<Map<Object, Number>> getChartData(final DashboardDto dashbaordDto, Report type, boolean bigChart);

    public DashboardDto getDashboardHeader(final DashboardDto searchDto);

    public DashboardDto getReportingTable(final DashboardDto searchDto);
    
    public List<Map<Object, Number>> getChartData(final BaseDashboardDto dashbaordDto, Report type, PublisherReport pType, Interval interval,boolean bigChart);

    public BaseDashboardDto getDashboardHeader(final BaseDashboardDto searchDto);

    public BaseDashboardDto getReportingTable(final BaseDashboardDto searchDto);
    
    
}
