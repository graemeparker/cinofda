package com.adfonic.presentation.reporting.location.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.location.AdvertiserLocationReportService;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("locationReportService")
public class AdvertiserLocationReportServiceImpl implements AdvertiserLocationReportService {

    @Autowired
    private ToolsSQLQuery toolsSqlQuery;
    private TimeZone companyTimeZone;
    
    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        toolsSqlQuery.init(userLocale, companyTimeZone);
        this.companyTimeZone = companyTimeZone;
    }

    @Override
    public Report getReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        toolsSqlQuery.addParameters(new Parameter.LocationByCountries());
        toolsSqlQuery.addMetrics(Metric.LOCATION_PERCENT_IMPRESSIONS);
        ReportUtil.addReportMetrics(toolsSqlQuery, false, filter.isUseConversionTracking());
        if (filter.isUseGeotargeting()) {
            toolsSqlQuery.addParameters(new Parameter.Geotarget());
        }
        if (filter.isDetailedByDay()) {
            return getLocationReportByDay(filter, queryParams);
        }
        return getLocationReport(filter, queryParams);
    }

    private Report getLocationReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        if (filter.isGroupByCategory() && filter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory());
            return toolsSqlQuery.getLocationReportDetailByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        if (filter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory());
            return toolsSqlQuery.getLocationReportDetailByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        if (filter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory());
            return toolsSqlQuery.getLocationInvSourceReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        return toolsSqlQuery.getLocationReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
    }

    private Report getLocationReportByDay(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        if (filter.isGroupByCategory() && filter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory(),
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getLocationReportDetailByDayByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        if (filter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(),
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getLocationReportDetailByDayByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        if (filter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory(),
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getLocationReportDetailByDayByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
        }
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
        return toolsSqlQuery.getLocationReportDetailByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseGeotargeting(), filter.isUseConversionTracking(), false);
    }

}
