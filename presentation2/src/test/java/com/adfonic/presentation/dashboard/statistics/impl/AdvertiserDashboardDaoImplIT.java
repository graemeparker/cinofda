package com.adfonic.presentation.dashboard.statistics.impl;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.presentation.dashboard.statistics.AdvertiserDashboardDao;
import com.adfonic.test.AbstractAdfonicTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/adfonic-presentation2-datasource-context.xml" })
public class AdvertiserDashboardDaoImplIT extends AbstractAdfonicTest {

    @Autowired
    ApplicationContext context;

    AdvertiserDashboardDao advertiserDashboardDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        advertiserDashboardDao = new AdvertiserDashboardDaoImpl();
        ((AdvertiserDashboardDaoImpl) advertiserDashboardDao).setDataSource((DataSource) context.getBean("dashBoardDataSource"));
    }

    @After
    public void tearDown() throws Exception {
        advertiserDashboardDao = null;
    }

    @Test
    public void testGetDashboardReportingTableForAdvertiser() {
        int dateRange = 2;
        Long advertiserId = new Long(21557);
        Long numberOfRecords = 10L;
        SortBy sortBy = SortBy.CAMPAIGN_NAME;
        OrderBy orderBy = OrderBy.ASCENDING;
        Long start = 0L;
        CampaignStatus campaignStatus = CampaignStatus.ALL;
        BidType bidType = BidType.ALL;

        List<StatisticsDto> table = advertiserDashboardDao.getDashboardReportingTableForAdvertiser(advertiserId,
                campaignStatus, bidType,dateRange, sortBy, orderBy, start, numberOfRecords,true);

        assertNotNull("The result is not null", table);
        //assertNotNull("Contains atleast one row", table.get(0));
    }

    @Test
    public void testGetDashboardReportingTableForCampaigns() {

        int dateRange = 2;
        Long advertiserId = new Long(21557);
        Long numberOfRecords = 10L;
        SortBy sortBy = SortBy.CAMPAIGN_NAME;
        OrderBy orderBy = OrderBy.ASCENDING;
        Long start = 0L;
        List<Long> campaignIds = new ArrayList<Long>();
        campaignIds.add(44223L);
        campaignIds.add(42441L);
        campaignIds.add(45590L);
        campaignIds.add(42590L);
        campaignIds.add(45720L);
        campaignIds.add(42592L);
        campaignIds.add(46763L);
        campaignIds.add(46764L);
        campaignIds.add(41906L);

        CampaignStatus campaignStatus = CampaignStatus.ALL;
        BidType bidType = BidType.ALL;

        List<StatisticsDto> table = advertiserDashboardDao.getDashboardReportingTableForCampaigns(advertiserId,
                campaignIds, campaignStatus, bidType, dateRange, sortBy, orderBy, start,
                numberOfRecords,true);
        assertNotNull("The result is not null", table);
    }

    @Test
    @Ignore
    public void testGetDashboardReportingTableNumberOfRecordsForCampaigns() {

        int dateRange = 2;
        Long advertiserId = new Long(21557);
        List<Long> campaignIds = new ArrayList<Long>();
        campaignIds.add(44223L);
        campaignIds.add(42441L);
        campaignIds.add(45590L);
        campaignIds.add(42590L);
        campaignIds.add(45720L);
        campaignIds.add(42592L);
        campaignIds.add(46763L);
        campaignIds.add(46764L);
        campaignIds.add(41906L);

        CampaignStatus campaignStatus = CampaignStatus.ALL;
        BidType bidType = BidType.ALL;

        Long numberOfRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForAdvertiser(advertiserId, campaignStatus, bidType, dateRange,true);
        assertNotNull("The result is not null", numberOfRecords);
//        assertTrue("The number of records is greater than 0", numberOfRecords > 0);
        
        
        numberOfRecords = advertiserDashboardDao.getNumberOfRecordsForDashboardReportingTableForCampaigns(advertiserId, campaignIds, campaignStatus, bidType, dateRange,true);
        assertNotNull("The result is not null", numberOfRecords);
//        assertTrue("The number of records is greater than 0", numberOfRecords > 0);
    }
    
    
    @Test
    @Ignore
    public void testGetChartDataForAdvertiser() {
        int dateRange = 2;
        Long advertiserId = new Long(21557);

        List<Long> campaignIds = new ArrayList<Long>();
        campaignIds.add(44223L);
        campaignIds.add(42441L);
        campaignIds.add(45590L);
        campaignIds.add(42590L);
        campaignIds.add(45720L);
        campaignIds.add(42592L);
        campaignIds.add(46763L);
        campaignIds.add(46764L);
        campaignIds.add(41906L);

        Report report = Report.CTR;

        Map<Object, Number> chartData = advertiserDashboardDao.getChartDataForAdvertiser(advertiserId, dateRange, report, CampaignStatus.ACTIVE, BidType.CPC,true);
        assertNotNull("Chart data is not null", chartData);
    }

    @Test
    @Ignore
    public void testGetChartDataForCampaign() {
        int dateRange = 2;
        Long advertiserId = new Long(21557);

        List<Long> campaignIds = new ArrayList<Long>();
        campaignIds.add(44223L);
        campaignIds.add(42441L);
        campaignIds.add(45590L);
        campaignIds.add(42590L);
        campaignIds.add(45720L);
        campaignIds.add(42592L);
        campaignIds.add(46763L);
        campaignIds.add(46764L);
        campaignIds.add(41906L);

        Report report = Report.CONVERSION;

        Map<Object, Number> chartData = advertiserDashboardDao.getChartDataForCampaign(advertiserId, campaignIds, CampaignStatus.ACTIVE, BidType.CPC,
                dateRange, report,true);
        assertNotNull("Chart data is not null", chartData);
    }

    @Test
    @Ignore
    public void testGetHeadlineFiguresForAdvertiser() {
        int dateRange = 2;
        Long advertiserId = new Long(21557);

        AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForAdvertiser(
                advertiserId, CampaignStatus.ACTIVE, BidType.CPC ,dateRange,true);

        assertNotNull(advertiserHeadlineStatsDto.getClicks());
        assertNotNull(advertiserHeadlineStatsDto.getConversions());
        assertNotNull(advertiserHeadlineStatsDto.getCostPerConversion());
        assertNotNull(advertiserHeadlineStatsDto.getImpressions());
    }

    @Test
    @Ignore
    public void testGetHeadlineFiguresForCampaigns() {
        int dateRange = 2;
        Long advertiserId = new Long(21557);

        List<Long> campaignIds = new ArrayList<Long>();
        campaignIds.add(44223L);
        campaignIds.add(42441L);
        campaignIds.add(45590L);
        campaignIds.add(42590L);
        campaignIds.add(45720L);
        campaignIds.add(42592L);
        campaignIds.add(46763L);
        campaignIds.add(46764L);
        campaignIds.add(41906L);

        AdvertiserHeadlineStatsDto advertiserHeadlineStatsDto = advertiserDashboardDao.getHeadlineFiguresForCampaigns(
                advertiserId, campaignIds, CampaignStatus.ACTIVE, BidType.CPC,dateRange,true);

        assertNotNull(advertiserHeadlineStatsDto.getClicks());
        assertNotNull(advertiserHeadlineStatsDto.getConversions());
        assertNotNull(advertiserHeadlineStatsDto.getCostPerConversion());
        assertNotNull(advertiserHeadlineStatsDto.getImpressions());
    }

}
