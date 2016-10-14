package com.adfonic.tools.beans.reporting.budget;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.budget.AdvertiserBudgetReportService;
import com.adfonic.presentation.reporting.budget.impl.BudgetBreakdown;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserBudgetReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = 1L;

    // ////////////////////
    // Option fields
    // ////////////////////

    private BudgetBreakdown reportView;

    // ////////////////////
    // Services
    // ////////////////////

    @Autowired
    private AdvertiserBudgetReportService budgetReportService;

    // ////////////////////
    // Overrides
    // ////////////////////

    @Override
    protected String getReportName() {
        return Constants.REPORT_BUDGETS;
    }

    @Override
    protected void initReport() {
        setReportView(BudgetBreakdown.DAILY.name());
    }

    @Override
    protected void generateReport() {
        AdvertiserQueryParameters queryParams = new AdvertiserQueryParameters.Builder()
        .advertiserId(getUser().getAdvertiserDto().getId().longValue()).campaignIds(getSelectedCampaignIdsForProc())
        .dateRange(new Range<Date>(getStartDate(), getEndDate())).build();

        budgetReportService.init(getUserLocale(), getCompanyTimeZone());
        setReport(budgetReportService.getReport(reportView, queryParams));
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public String getReportView() {
        return reportView.name();
    }

    public void setReportView(String reportView) {
        this.reportView = BudgetBreakdown.valueOf(reportView);
    }

}
