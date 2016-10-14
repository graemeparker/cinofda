package com.adfonic.presentation.reporting.operator.impl;

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
import com.adfonic.presentation.reporting.operator.AdvertiserOperatorReportService;
import com.adfonic.reporting.Report;
import com.adfonic.util.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserOperatorReportServiceImplIT {

    @Autowired
    @Qualifier("operatorReportService")
    private AdvertiserOperatorReportService operatorReportService;
    
    @Before
    public void setUp() {
        operatorReportService.init(new Locale("en", "US"), TimeZone.getTimeZone("America/Los_Angeles"));
    }
    
    @Test
    public void shouldReturnOperatorConnectionsReport() {
        Report report = operatorReportService.getReport(new AdvertiserReportFilter.Builder().detailedByDay(false).build(), 
                                                        new AdvertiserQueryParameters.Builder()
                                                            .advertiserId(403)
                                                            .campaignIds(null)
                                                            .dateRange(new Range<Date>(new Date(), new Date()))
                                                            .build());
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()), equalTo("[Country, Operator, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST]"));
    }
    
    @Test
    public void shouldReturnOperatorConnectionsReportDetailedByDay() {
        Report report = operatorReportService.getReport(new AdvertiserReportFilter.Builder().detailedByDay(true).build(), 
                                                        new AdvertiserQueryParameters.Builder()
                                                            .advertiserId(403)
                                                            .campaignIds(null)
                                                            .dateRange(new Range<Date>(new Date(), new Date()))
                                                            .build());
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()), equalTo("[Country, Operator, Date, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST]"));
    }

}
