package com.adfonic.tools.beans.reporting.campaign;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignChartService;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignReportService;
import com.adfonic.presentation.reporting.campaign.impl.AdvertiserCampaignReportServiceImpl.CampaignDetailedReportOption;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserCampaignReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = 1L;

    private static final String FORMATTER_PATTERN = "EEEE, d MMMM yyyy";
    private static final int METRIC_IMPRESSIONS = 1;
    private static final int METRIC_CLICKS = 2;

    // ////////////////////
    // Option fields
    // ////////////////////

    private CampaignDetailedReportOption reportView;
    private int[] selectedMetrics = { METRIC_IMPRESSIONS, METRIC_CLICKS };

    // ////////////////////
    // Services
    // ////////////////////

    @Autowired
    private AdvertiserCampaignReportService campaignReportService;
    @Autowired
    private AdvertiserCampaignChartService campaignChartService;

    private String statistics;
    private String xAxisTicks;

    // ////////////////////
    // Overrides
    // ////////////////////

    @Override
    protected String getReportName() {
        return Constants.REPORT_CAMPAIGNS;
    }

    @Override
    protected void initReport() {
        setReportView(CampaignDetailedReportOption.TOTAL.name());
    }

    @Override
    protected void generateReport() {
        AdvertiserQueryParameters queryParams = new AdvertiserQueryParameters.Builder()
        .advertiserId(getUser().getAdvertiserDto().getId().longValue()).campaignIds(getSelectedCampaignIdsForProc())
        .dateRange(new Range<Date>(getStartDate(), getEndDate())).build();
        AdvertiserReportFilter reportFilter = new AdvertiserReportFilter.Builder().groupByCategory(isGroupByCategory())
        .groupByInventorySource(isGroupByInventory()).useConversionTracking(isConversionTrackingUsed()).build();

        campaignReportService.init(getUserLocale(), getUser().getUser().getCompany());
        setReport(campaignReportService.getReport(reportView, reportFilter, queryParams));

        if (CampaignDetailedReportOption.HOURLY.equals(reportView)) {
            campaignChartService.init(getUserLocale(), getUser().getUser().getCompany());
            this.statistics = campaignChartService.getCampaignHourlyStatistics(queryParams);
            this.xAxisTicks = campaignChartService.getxAxisTicks();
        }
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public void setEndDateToday() {
        if (CampaignDetailedReportOption.HOURLY.equals(reportView)) {
            setEndDate(returnNow());
        }
    }
    
    public String getReportView() {
        return reportView.name();
    }

    public void setReportView(String reportView) {
        this.reportView = CampaignDetailedReportOption.valueOf(reportView);
    }

    public String getStatistics() {
        return statistics;
    }

    public String getxAxisTicks() {
        return xAxisTicks;
    }

    public int[] getSelectedMetrics() {
        return selectedMetrics;
    }

    public void setSelectedMetrics(int[] selectedMetrics) {
        this.selectedMetrics = (selectedMetrics == null ? null : selectedMetrics.clone());
    }

    public String getFormattedStartDate() {
        return new SimpleDateFormat(FORMATTER_PATTERN).format(getStartDate());
    }
}
