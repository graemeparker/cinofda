package com.adfonic.presentation.reporting.budget.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.budget.AdvertiserBudgetReportService;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("budgetReportService")
public class AdvertiserBudgetReportServiceImpl implements AdvertiserBudgetReportService {

    @Autowired
    private ToolsSQLQuery toolsSqlQuery;
    
    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        toolsSqlQuery.init(userLocale, companyTimeZone);
    }

    @Override
    public Report getReport(BudgetBreakdown option, AdvertiserQueryParameters queryParams) {
        if (option.equals(BudgetBreakdown.DAILY)) {
            return toolsSqlQuery.getBudgetReportCampaignDaily(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                    queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd());
        }
        return toolsSqlQuery.getBudgetReportCampaignOverall(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), 
                queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd());
    }
}
