package com.adfonic.presentation.reporting.campaign.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters.Builder;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignChartService;
import com.adfonic.util.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserCampaignChartServiceImplIT {

    @Autowired
    private AdvertiserCampaignChartService campaignChartService;
    
    @Before
    public void setUp() {
        campaignChartService.init(new Locale("en", "UK"), TimeZone.getTimeZone("Europe/London"));
    }
    
    @Test
    public void shouldReturnSeriesForAllColumnsInHourlyReport() {
       Date today = new Date();
       String campaignHourlyStatistics = campaignChartService.getCampaignHourlyStatistics(new Builder()
                                                                                       .advertiserId(403)
                                                                                       .campaignIds(null)
                                                                                       .dateRange(new Range<Date>(today, today)) // end date not read
                                                                                       .build());
       assertThat(campaignHourlyStatistics, equalTo("[[{v:01, f:'01:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:02, f:'02:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:03, f:'03:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:04, f:'04:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:05, f:'05:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:06, f:'06:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:07, f:'07:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:08, f:'08:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:09, f:'09:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:10, f:'10:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:11, f:'11:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:12, f:'12:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:13, f:'13:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:14, f:'14:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:15, f:'15:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:16, f:'16:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:17, f:'17:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:18, f:'18:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:19, f:'19:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:20, f:'20:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:21, f:'21:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:22, f:'22:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],[{v:23, f:'23:00'}, 0, 0, 0.0000, 0.00, 0, 0.00],"
               + "[{v:24, f:'00:00'}, 0, 0, 0.0000, 0.00, 0, 0.00]]"));
    }
    
}
