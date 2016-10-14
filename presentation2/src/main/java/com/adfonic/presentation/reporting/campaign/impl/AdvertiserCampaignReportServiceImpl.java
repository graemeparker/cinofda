package com.adfonic.presentation.reporting.campaign.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Company;
import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignReportService;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("campaignReportService")
public class AdvertiserCampaignReportServiceImpl implements AdvertiserCampaignReportService {

    protected static ToolsSQLQuery toolsSqlQuery;
    private static TimeZone companyTimeZone;

    @Autowired
    public void setToolsSQLQuery(ToolsSQLQuery toolsSqlQuery) {
        AdvertiserCampaignReportServiceImpl.toolsSqlQuery = toolsSqlQuery;
    }
    
    @Override
    public void init(Locale userLocale, Company company) {
        TimeZone defaultTimeZone = company.getDefaultTimeZone();
        toolsSqlQuery.init(userLocale, defaultTimeZone);
        AdvertiserCampaignReportServiceImpl.companyTimeZone = defaultTimeZone;
    }
    
    public enum CampaignDetailedReportOption {
        HOURLY {
            @Override
            protected Report getCampaignReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
                return generateCampaignDetailedHourlyReport(reportFilter, queryParams);
            }
        }, 
        DAILY {
            @Override
            protected Report getCampaignReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
                return generateCampaignDetailedDailyReport(reportFilter, queryParams);
            }
        }, 
        SUMMARY {
            @Override
            protected Report getCampaignReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
                return generateCampaignTotalSummaryReport(reportFilter, queryParams);
            }
        }, 
        TOTAL {
            @Override
            protected Report getCampaignReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
                return generateCampaignDetailedTotalReport(reportFilter, queryParams);
            }
        };
        
        protected abstract Report getCampaignReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams);
    }
    
    @Override
    public Report getReport(CampaignDetailedReportOption option, AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
        ReportUtil.addReportMetrics(toolsSqlQuery, false, reportFilter.isUseConversionTracking());
        return option.getCampaignReport(reportFilter, queryParams);
    }
    
    private static Report generateCampaignDetailedTotalReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
        // extra false parameter for video metrics as it's not required for any report at this stage
        if(reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportTotalByDayByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportTotalByDayByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportTotalByDayByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
        return toolsSqlQuery.getCampaignReportTotalByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
    }
    
    private static Report generateCampaignTotalSummaryReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
        // extra false parameter for video metrics as it's not required for any report at this stage
        if(reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory());
            return toolsSqlQuery.getCampaignReportDetailByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory());
            return toolsSqlQuery.getCampaignReportDetailByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory());
            return toolsSqlQuery.getCampaignInvSourceReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        return toolsSqlQuery.getCampaignReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
    }
    
    private static Report generateCampaignDetailedDailyReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
        // extra false parameter for video metrics as it's not required for any report at this stage
        if(reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportDetailByDayByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportDetailByDayByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory(), 
                                        new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignInvSourceReportDetailByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
        } 
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
        return toolsSqlQuery.getCampaignReportDetailByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), reportFilter.isUseConversionTracking(), false);
    }
    
    private static Report generateCampaignDetailedHourlyReport(AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams) {
        // extra false parameter for video metrics as it's not required for any report at this stage
        if(reportFilter.isGroupByCategory() && reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), new Parameter.GroupByInventory(), 
                    new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()), 
                    new Parameter.AdvertiserTimeByHour(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportDetailByHourByCategoryByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByCategory()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByCategory(), 
                    new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()), 
                    new Parameter.AdvertiserTimeByHour(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportDetailByHourByCategory(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), reportFilter.isUseConversionTracking(), false);
        } 
        if (reportFilter.isGroupByInventorySource()) {
            toolsSqlQuery.addParameters(new Parameter.GroupByInventory(), 
                    new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()), 
                    new Parameter.AdvertiserTimeByHour(companyTimeZone, queryParams.getDateRange()));
            return toolsSqlQuery.getCampaignReportDetailByHourByInvSource(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), reportFilter.isUseConversionTracking(), false);
        }
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()), 
                                    new Parameter.AdvertiserTimeByHour(companyTimeZone, queryParams.getDateRange()));
        return toolsSqlQuery.getCampaignReportDetailByHour(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), reportFilter.isUseConversionTracking(), false);
    }

    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        // TODO Auto-generated method stub
        
    }
    
}
