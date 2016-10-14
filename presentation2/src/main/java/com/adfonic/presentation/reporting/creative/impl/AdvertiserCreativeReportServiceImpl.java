package com.adfonic.presentation.reporting.creative.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeQueryParameters;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeReportService;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("creativeReportService")
public class AdvertiserCreativeReportServiceImpl implements AdvertiserCreativeReportService {

    @Autowired
    private ToolsSQLQuery toolsSqlQuery;
    private TimeZone companyTimeZone;

    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        toolsSqlQuery.init(userLocale, companyTimeZone);
        this.companyTimeZone = companyTimeZone;
    }
    
    @Override
    public Report getReport(AdvertiserReportFilter reportFilter, AdvertiserCreativeQueryParameters queryParams) {
        toolsSqlQuery.addParameters(new Parameter.CreativesZero());
        ReportUtil.addReportMetrics(toolsSqlQuery, false, reportFilter.isUseConversionTracking());
        if (reportFilter.isDetailedByDay()) {
            return getCreativeReportByDay(reportFilter, queryParams);
        }
        return getCreativeReport(reportFilter, queryParams);
    }

    private Report getCreativeReport(AdvertiserReportFilter reportFilter, AdvertiserCreativeQueryParameters queryParams) {
        // extra false parameter for show video tracking which is no longer required
        if (reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory());
            return toolsSqlQuery.getCreativeReportDetailByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory());
            return toolsSqlQuery.getCreativeReportDetailByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory());
            return toolsSqlQuery.getCreativeInvSourceReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        return toolsSqlQuery.getCreativeReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
    }

    private Report getCreativeReportByDay(AdvertiserReportFilter reportFilter, AdvertiserCreativeQueryParameters queryParams) {
        
        // extra false parameter for show video tracking which is no longer required
        if (reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCreativeReportDetailByDayByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCreativeReportDetailByDayByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCreativeReportDetailByDayByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                    queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        }
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
        return toolsSqlQuery.getCreativeReportDetailByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getCreativeIds(), 
                queryParams.getFormatIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
    }

}
