package com.adfonic.presentation.reporting.creative.impl;

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

import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeQueryParameters;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeReportService;
import com.adfonic.reporting.Report;
import com.adfonic.util.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserCreativeReportServiceImplIT {

    @Autowired
    @Qualifier("creativeReportService")
    private AdvertiserCreativeReportService creativeReportService;
    
    @Before
    public void setUp() {
        creativeReportService.init(new Locale("en", "UK"), TimeZone.getTimeZone("Europe/London"));
    }
    
    @Test
    public void shouldReturnCreativeReportDetailByDay() {
        AdvertiserCreativeQueryParameters queryParams = new AdvertiserCreativeQueryParameters.Builder()
                .advertiserId(403)
                .dateRange(new Range<Date>(new Date(), new Date())).build();
        
        AdvertiserReportFilter reportFilter = new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .detailedByDay(true)
                .useConversionTracking(true).build();
        Report report = creativeReportService.getReport(reportFilter, queryParams);
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()), equalTo("[Campaign, Creative, Format, Category, Date, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST, CONVERSIONS, CONVERSION_PERCENT, COST_PER_CONVERSION]"));
    }

}
