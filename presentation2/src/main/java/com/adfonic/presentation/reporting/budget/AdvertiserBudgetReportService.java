package com.adfonic.presentation.reporting.budget;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.presentation.reporting.budget.impl.BudgetBreakdown;
import com.adfonic.reporting.Report;

public interface AdvertiserBudgetReportService extends AdvertiserReportService {
    
    public abstract Report getReport(BudgetBreakdown option, AdvertiserQueryParameters queryParams);

}
