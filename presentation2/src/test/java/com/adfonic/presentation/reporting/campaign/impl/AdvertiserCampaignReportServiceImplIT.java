package com.adfonic.presentation.reporting.campaign.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignReportService;
import com.adfonic.presentation.reporting.campaign.impl.AdvertiserCampaignReportServiceImpl.CampaignDetailedReportOption;
import com.adfonic.reporting.Report;
import com.adfonic.util.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/spring/adfonic-presentation2-test-context.xml", "classpath:/spring/adfonic-presentation2-datasource-context.xml"})
public class AdvertiserCampaignReportServiceImplIT {

    @Autowired
    @Qualifier("campaignReportService")
    private AdvertiserCampaignReportService campaignReportService;
        
    private static Map<String, String> COLUMN_NAME_MAP;
    
    // Report table column names
    private static String CAMPAIGN = "Campaign";
    private static String CATEGORY = "Category";
    private static String INVENTORY = "Inventory";
    private static String DATE = "Date";
    private static String CTR = "CTR";
    
    private static String IMPRESSIONS = "Impressions";
    private static String CLICKS = "Clicks";
    private static String ECPM = "eCPM";
    private static String ECPC = "eCPC";
    private static String SPEND = "Spend";
    private static String CONVERSIONS = "Conversions";
    private static String CLICK_CONVERSIONS = "Click Conversions";
    private static String ECPA = "eCPA";
    private static String TOTAL_IMPRESSIONS = "% Total Impressions";
    private static String TIME = "Time";
    
    /** Mapping between proc column names and the displayed column names */
    @BeforeClass
    public static void beforeClass() {
        COLUMN_NAME_MAP = new HashMap<>();
        COLUMN_NAME_MAP.put(IMPRESSIONS, "IMPRESSIONS");
        COLUMN_NAME_MAP.put(CLICKS, "CLICKS");
        COLUMN_NAME_MAP.put(ECPM, "ECPM_AD");
        COLUMN_NAME_MAP.put(ECPC, "ECPC_AD");
        COLUMN_NAME_MAP.put(SPEND, "COST");
        COLUMN_NAME_MAP.put(CONVERSIONS, "CONVERSIONS");
        COLUMN_NAME_MAP.put(CLICK_CONVERSIONS, "CONVERSION_PERCENT");
        COLUMN_NAME_MAP.put(ECPA, "COST_PER_CONVERSION");
        COLUMN_NAME_MAP.put(TOTAL_IMPRESSIONS, "LOCATION_PERCENT_IMPRESSIONS");
        COLUMN_NAME_MAP.put(TIME, "Hour");
    }
    
    @Before
    public void setUp() {
        campaignReportService.init(new Locale("en", "UK"), TimeZone.getTimeZone("Europe/London"));
    }
    
    @Test
    public void testApplicationContext() {
        assertNotNull(AdvertiserCampaignReportServiceImpl.toolsSqlQuery);
    }
    
    /** Daily */
    @Test
    public void testDailyReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }

    /** Daily + category */
    @Test
    public void testDailyCategoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Daily + inventory */
    @Test
    public void testDailyInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Daily + conversion */
    @Test
    public void testDailyConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Daily + category + inventory */
    @Test
    public void testDailyCategoryInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Daily + category + conversion */
    @Test
    public void testDailyCategoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Daily + inventory + conversion */
    @Test
    public void testDailyInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.DAILY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }

    /** Hourly */
    @Test
    public void testHourlyReportForCampaigns() {     
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Hourly + category */
    @Test
    public void testHourlyCategoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    
    /** Hourly + inventory */
    @Test
    public void testHourlyInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Hourly + conversion */
    @Test
    public void testHourlyConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Hourly + category + inventory */
    @Test
    public void testHourlyCategoryInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Hourly + category + conversion */
    @Test
    public void testHourlyCategoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Hourly + inventory + conversion */
    @Test
    public void testHourlyInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Hourly + category + inventory + conversion */
    @Test
    public void testHourlyCategoryInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.HOURLY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, DATE, TIME, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Summary */
    @Test
    public void testSummaryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Summary + category */
    @Test
    public void testSummaryCategoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Summary + inventory */
    @Test
    public void testSummaryInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Summary + conversion */
    @Test
    public void testSummaryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Summary + category + inventory */
    @Test
    public void testSummaryCategoryInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Summary + category + conversion */
    @Test
    public void testSummaryCategoryConversionForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Summary + inventory + conversion */
    @Test
    public void testSummaryInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Summary + category + inventory + conversion */
    @Test
    public void testSummaryCategoryInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.SUMMARY, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Total */
    @Test
    public void testTotalReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .build(),
                createQueryParam(),
                Arrays.asList(DATE, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    /** Total + category */
    @Test
    public void testTotalCategoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByCategory(true).build(),
                createQueryParam(),
                Arrays.asList(/*CAMPAIGN,*/ /*TODO: why no campaign*/ CATEGORY, DATE/*TODO: why date*/, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Total + inventory */
    public void testTotalInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Total + conversion */
    public void testTotalConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Total + category + inventory */
    @Test
    public void testTotalCategoryInventoryReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true).build(),
                createQueryParam(),
                Arrays.asList(/*CAMPAIGN,*/ /*TODO: why no campaign*/ CATEGORY, INVENTORY, DATE,/* TODO why date*/ IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND));
    }
    
    /** Total + category + conversion */
    public void testTotalCategoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Total + inventory + conversion */
    public void testTotalInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }
    
    /** Total + category + inventory + conversion */
    public void testTotalCategoryInventoryConversionReportForCampaigns() {
        testCampaignReportHeaders(
                CampaignDetailedReportOption.TOTAL, new AdvertiserReportFilter.Builder()
                .groupByCategory(true)
                .groupByInventorySource(true)
                .useConversionTracking(true).build(),
                createQueryParam(),
                Arrays.asList(CAMPAIGN, CATEGORY, INVENTORY, IMPRESSIONS, CLICKS, CTR, ECPM, ECPC, SPEND, CONVERSIONS, CLICK_CONVERSIONS, ECPA));
    }

    // Private methods
    
    private void testCampaignReportHeaders(CampaignDetailedReportOption option, AdvertiserReportFilter filter, AdvertiserQueryParameters query,
            List<String> expectedColumnHeaders) {
        Report report = campaignReportService.getReport(option, filter, query);
        assertThat(report.getRows().size(), greaterThan(0));
        assertThat(Arrays.toString(report.getHeaders().getCells()), equalTo(getReportHeaderString(expectedColumnHeaders)));
    }
    
    private AdvertiserQueryParameters createQueryParam() {
        return new AdvertiserQueryParameters.Builder()
                    .advertiserId(403)
                    .dateRange(new Range<Date>(new Date(), new Date()))
                    .campaignIds("215").build();
    }
    
    private String getReportHeaderString(List<String> columnNames) {
        String sep = "";
        StringBuilder sb = new StringBuilder("[");
        for(String columName : columnNames) {
            if (COLUMN_NAME_MAP.containsKey(columName)) {
                sb.append(sep).append(COLUMN_NAME_MAP.get(columName));
            } else {               
                sb.append(sep).append(columName);
            }
            sep = ", ";
        }
        return sb.append("]").toString();
    }
}
