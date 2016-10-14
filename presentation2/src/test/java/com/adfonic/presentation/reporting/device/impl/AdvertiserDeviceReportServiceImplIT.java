package com.adfonic.presentation.reporting.device.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
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
import com.adfonic.presentation.reporting.device.AdvertiserDeviceQueryParameters;
import com.adfonic.presentation.reporting.device.AdvertiserDeviceReportService;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown;
import com.adfonic.reporting.Report;
import com.adfonic.util.Range;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserDeviceReportServiceImplIT {

    @Autowired
    @Qualifier("deviceReportService")
    private AdvertiserDeviceReportService deviceReportService;
    
    @Before
    public void setUp() {
        deviceReportService.init(new Locale("en", "UK"), TimeZone.getTimeZone("Europe/London"));
    }
    
    @Test
    public void testAutowiringStatics() {
        assertNotNull(AdvertiserDeviceReportServiceImpl.toolsSqlQuery);
    }
    
    @Test
    public void shouldReturnDeviceReportByCountryAndVendor() {
        AdvertiserDeviceQueryParameters queryParams = new AdvertiserDeviceQueryParameters.Builder()
                                                          .advertiserId(403)
                                                          .dateRange(new Range<Date>(new Date(), new Date())).build();
        Report report = deviceReportService.getReport(LocationBreakdown.COUNTRY_BY_VENDOR, new AdvertiserReportFilter.Builder().build(), queryParams);
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()),
                equalTo("[Country, Vendor, IMPRESSIONS, CLICKS, CTR, ECPM_AD, ECPC_AD, COST]"));
    }

}
