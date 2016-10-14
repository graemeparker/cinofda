package com.adfonic.presentation.reporting.location.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.location.AdvertiserLocationReportService;
import com.adfonic.reporting.Report;
import com.adfonic.util.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserLocationReportServiceImplIT {

    @Autowired
    @Qualifier("locationReportService")
    private AdvertiserLocationReportService locationReportService;
    
    @Before
    public void setUp() {
        locationReportService.init(new Locale("en", "US"), TimeZone.getTimeZone("America/Los_Angeles"));
    }
    
    @Test
    public void shouldReturnLocationReportByInventorySourceAndUseGeotargeting() {
        AdvertiserQueryParameters queryParams = new AdvertiserQueryParameters.Builder()
                                                    .advertiserId(403)
                                                    .campaignIds("4096")
                                                    .dateRange(new Range<Date>(new Date(), new Date()))
                                                    .build();
        Report report = locationReportService.getReport(new AdvertiserReportFilter.Builder()
                                                                   .useGeotargeting(true)
                                                                   .detailedByDay(false)
                                                                   .groupByInventorySource(true).build(), queryParams);
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()),
                equalTo("[Country, Geotarget, Inventory, LOCATION_PERCENT_IMPRESSIONS, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST]"));
    }

    @Test
    public void shouldReturnVanillaLocationReport() {
        Report report = locationReportService.getReport(new AdvertiserReportFilter.Builder().detailedByDay(false).build(),
                new AdvertiserQueryParameters.Builder().advertiserId(403).dateRange(new Range<Date>(new Date(), new Date())).build());
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()),
                equalTo("[Country, LOCATION_PERCENT_IMPRESSIONS, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST]"));
    }
}
