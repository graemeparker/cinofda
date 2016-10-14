package com.adfonic.tools.beans.reporting.location;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.primefaces.model.chart.DonutChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.DonutChart;
import com.adfonic.presentation.reporting.GeoChart;
import com.adfonic.presentation.reporting.location.AdvertiserLocationReportService;
import com.adfonic.presentation.reporting.location.AdvertiserLocationSnapshotService;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserLocationReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = -5415048741433565123L;

    private static final int MAX_HEIGHT = 220;
    private static final int MAX_WIDTH = 440;

    @Autowired
    AdvertiserLocationReportService service;
    @Autowired
    AdvertiserLocationSnapshotService locationSnapshotService;
    private GeoChart geoChart;
    private DonutChart donutChart;
    private DonutChartModel donutModel;
    private Long advertiserId;

    @Override
    protected void initReport() {
        // no specific initialisation for this report
    }

    @Override
    protected String getReportName() {
        return Constants.REPORT_LOCATIONS;
    }

    @Override
    protected void generateReport() {
        advertiserId = getUser().getAdvertiserDto().getId();
        Range<Date> range = new Range<Date>(getStartDate(), getEndDate());

        locationSnapshotService.init(getUserLocale(), getCompanyTimeZone());
        Map<String, Long> locationData = getLocationChartData(range);
        this.geoChart = new GeoChart(locationData, MAX_WIDTH, MAX_HEIGHT);
        this.donutChart = new DonutChart(locationData, MAX_WIDTH, MAX_HEIGHT);

        service.init(getUserLocale(), getCompanyTimeZone());
        setReport(service.getReport(
                new AdvertiserReportFilter.Builder().detailedByDay(isDailyStatistics()).groupByCategory(isGroupByCategory())
                        .groupByInventorySource(isGroupByInventory()).useConversionTracking(isConversionTrackingUsed())
                        .useGeotargeting(isUseGeotargeting()).build(), new AdvertiserQueryParameters.Builder().advertiserId(advertiserId)
                        .campaignIds(getSelectedCampaignIdsForProc()).dateRange(range).build()));
    }

    private Map<String, Long> getLocationChartData(Range<Date> range) {
        return locationSnapshotService.getImpressionsPerCountry(
                new AdvertiserReportFilter.Builder().build(),
                new AdvertiserQueryParameters.Builder().advertiserId(advertiserId).campaignIds(getSelectedCampaignIdsForProc())
                        .dateRange(range).build());
    }

    private boolean isUseGeotargeting() {
        return campaignService.isGeotargetingUsed(isAllCampaignsSelected() ? null : Arrays.asList(getSelectedCampaignIds()), advertiserId);
    }

    public GeoChart getGeoChart() {
        return this.geoChart;
    }

    public DonutChart getDonutChart() {
        return donutChart;
    }

    public DonutChartModel getDonutModel() {
        return donutModel;
    }
}
