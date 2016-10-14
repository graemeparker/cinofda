package com.adfonic.presentation.reporting.operator.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.operator.AdvertiserOperatorReportService;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("operatorReportService")
public class AdvertiserOperatorReportServiceImpl implements AdvertiserOperatorReportService {

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
        toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.Operators());
        ReportUtil.addReportMetrics(toolsSqlQuery, false, filter.isUseConversionTracking());
        
        if (filter.isDetailedByDay()) {
            toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
            return getOperatorReportByDay(filter, queryParams);
        }
        return getOperatorReport(filter, queryParams);
    }

    private Report getOperatorReportByDay(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        return toolsSqlQuery.getOperatorReportDetailByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getDateRange().getStart(),
                queryParams.getDateRange().getEnd(), filter.isUseConversionTracking());
    }

    private Report getOperatorReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        return toolsSqlQuery.getOperatorReportDetail(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getDateRange().getStart(),
                queryParams.getDateRange().getEnd(), filter.isUseConversionTracking());
    }

}
