package com.adfonic.datacollector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import javax.jms.Topic;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.time.DateUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.datacollector.dao.ToolsDao;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StopAdvertiserMessage;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.test.AbstractAdfonicTest;

public class TestStoppageManager extends AbstractAdfonicTest {
    private ToolsDao toolsDao;
    private Ehcache campaignStoppageCache;
    private Ehcache advertiserStoppageCache;
    private JmsUtils jmsUtils;
    private JmsTemplate centralJmsTemplate;
    private Topic stopCampaignTopic;
    private Topic stopAdvertiserTopic;
    private StoppageManager stoppageManager;

    @Before
    public void runBeforeEachTest() throws java.io.IOException {
        toolsDao = mock(ToolsDao.class);
        campaignStoppageCache = mock(Ehcache.class, "campaignStoppageCache");
        advertiserStoppageCache = mock(Ehcache.class, "advertiserStoppageCache");
        jmsUtils = mock(JmsUtils.class);
        centralJmsTemplate = mock(JmsTemplate.class, "centralJmsTemplate");
        stopCampaignTopic = mock(Topic.class, "stopCampaignTopic");
        stopAdvertiserTopic = mock(Topic.class, "stopAdvertiserTopic");

        stoppageManager = new StoppageManager();
        
        inject(stoppageManager, "toolsDao", toolsDao);
        inject(stoppageManager, "campaignStoppageCache", campaignStoppageCache);
        inject(stoppageManager, "advertiserStoppageCache", advertiserStoppageCache);
        inject(stoppageManager, "jmsUtils", jmsUtils);
        inject(stoppageManager, "centralJmsTemplate", centralJmsTemplate);
        inject(stoppageManager, "stopCampaignTopic", stopCampaignTopic);
        inject(stoppageManager, "stopAdvertiserTopic", stopAdvertiserTopic);
    }

    @Test
    public void testStopCampaign01_reactivateDate_passed() throws java.sql.SQLException {
        final Date eventTime = DateUtils.addDays(new Date(), -2); // 2 days ago
        final CampaignDto campaign = mock(CampaignDto.class);
        final long campaignId = randomLong();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignStoppage.Reason reason = CampaignStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();

        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
        }});

        stoppageManager.stopCampaign(eventTime, campaign, reason);
    }

    @Test
    public void testStopCampaign02_duplicate() throws java.sql.SQLException {
        final Date eventTime = new Date();
        final CampaignDto campaign = mock(CampaignDto.class);
        final long campaignId = randomLong();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignStoppage.Reason reason = CampaignStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();
        final Date reactivateDate = com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone);
        final Element element = new Element(campaignId, reactivateDate);

        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
            oneOf (campaignStoppageCache).get(campaignId); will(returnValue(element));
        }});

        stoppageManager.stopCampaign(eventTime, campaign, reason);
    }

    @Test
    public void testStopCampaign03_new() throws java.sql.SQLException {
        final Date eventTime = new Date();
        final CampaignDto campaign = mock(CampaignDto.class);
        final long campaignId = randomLong();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignStoppage.Reason reason = CampaignStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();
        final Date reactivateDate = com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone);

        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
            oneOf (campaignStoppageCache).get(campaignId); will(returnValue(null));
            oneOf (campaignStoppageCache).put(with(any(Element.class)));
            oneOf (toolsDao).createCampaignStoppage(with(campaignId), with(reason), with(any(Date.class)), with(any(Date.class)));
            oneOf (jmsUtils).sendObject(with(centralJmsTemplate), with(stopCampaignTopic), with(any(StopCampaignMessage.class)));
        }});

        stoppageManager.stopCampaign(eventTime, campaign, reason);
    }

    @Test
    public void testStopAdvertiser01_reactivateDate_passed() throws java.sql.SQLException {
        final Date eventTime = DateUtils.addDays(new Date(), -2); // 2 days ago
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final long advertiserId = randomLong();
        final CompanyDto company = mock(CompanyDto.class);
        final AdvertiserStoppage.Reason reason = AdvertiserStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();

        expect(new Expectations() {{
            allowing (advertiser).getId(); will(returnValue(advertiserId));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
        }});

        stoppageManager.stopAdvertiser(eventTime, advertiser, reason);
    }

    @Test
    public void testStopAdvertiser02_duplicate() throws java.sql.SQLException {
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final long advertiserId = randomLong();
        final CompanyDto company = mock(CompanyDto.class);
        final AdvertiserStoppage.Reason reason = AdvertiserStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();
        final Date reactivateDate = com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone);
        final Element element = new Element(advertiserId, reactivateDate);

        expect(new Expectations() {{
            allowing (advertiser).getId(); will(returnValue(advertiserId));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
            oneOf (advertiserStoppageCache).get(advertiserId); will(returnValue(element));
        }});

        stoppageManager.stopAdvertiser(eventTime, advertiser, reason);
    }

    @Test
    public void testStopAdvertiser03_new() throws java.sql.SQLException {
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final long advertiserId = randomLong();
        final CompanyDto company = mock(CompanyDto.class);
        final AdvertiserStoppage.Reason reason = AdvertiserStoppage.Reason.DAILY_BUDGET;
        final TimeZone timeZone = TimeZone.getDefault();
        final Date reactivateDate = com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone);

        expect(new Expectations() {{
            allowing (advertiser).getId(); will(returnValue(advertiserId));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(timeZone));
            oneOf (advertiserStoppageCache).get(advertiserId); will(returnValue(null));
            oneOf (advertiserStoppageCache).put(with(any(Element.class)));
            oneOf (toolsDao).createAdvertiserStoppage(with(advertiserId), with(reason), with(any(Date.class)), with(any(Date.class)));
            oneOf (jmsUtils).sendObject(with(centralJmsTemplate), with(stopAdvertiserTopic), with(any(StopAdvertiserMessage.class)));
        }});

        stoppageManager.stopAdvertiser(eventTime, advertiser, reason);
    }

    @Test
    public void testCalculateReactivateDate_advertiser() {
        final Date eventTime = new Date();
        final TimeZone timeZone = TimeZone.getDefault();
        
        assertNull(StoppageManager.calculateReactivateDate(AdvertiserStoppage.Reason.ZERO_BALANCE, eventTime, timeZone));
        assertEquals(com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone), StoppageManager.calculateReactivateDate(AdvertiserStoppage.Reason.DAILY_BUDGET, eventTime, timeZone));
    }

    @Test
    public void testCalculateReactivateDate_campaign() {
        final Date eventTime = new Date();
        final TimeZone timeZone = TimeZone.getDefault();
        
        assertNull(StoppageManager.calculateReactivateDate(CampaignStoppage.Reason.OVERALL_BUDGET, eventTime, timeZone));
        assertEquals(com.adfonic.util.DateUtils.getStartOfDayTomorrow(eventTime, timeZone), StoppageManager.calculateReactivateDate(CampaignStoppage.Reason.DAILY_BUDGET, eventTime, timeZone));
    }

    @Test
    public void testHasReactivateDatePassed() {
        assertFalse(StoppageManager.hasReactivateDatePassed(null));
        assertTrue(StoppageManager.hasReactivateDatePassed(new Date()));
        assertTrue(StoppageManager.hasReactivateDatePassed(DateUtils.addDays(new Date(), -1)));
        assertFalse(StoppageManager.hasReactivateDatePassed(DateUtils.addDays(new Date(), 1)));
    }

    @Test
    public void testDeDup01_new() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = DateUtils.addDays(new Date(), 1);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(null));
            oneOf (cache).put(with(any(Element.class)));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.NEW, StoppageManager.deDup(id, reactivateDate, cache));
    }

    @Test
    public void testDeDup02_duplicate_with_reactivateDate_match1() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = DateUtils.addDays(new Date(), 1);
        final Date cachedReactivateDate = reactivateDate;
        final Element element = new Element(id, cachedReactivateDate);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(element));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.DUPLICATE, StoppageManager.deDup(id, reactivateDate, cache));
    }

    @Test
    public void testDeDup03_duplicate_with_reactivateDate_match2() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = null;
        final Date cachedReactivateDate = reactivateDate;
        final Element element = new Element(id, cachedReactivateDate);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(element));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.DUPLICATE, StoppageManager.deDup(id, reactivateDate, cache));
    }

    @Test
    public void testDeDup04_duplicate_with_reactivateDate_mismatch1() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = null;
        final Date cachedReactivateDate = DateUtils.addDays(new Date(), 1);
        final Element element = new Element(id, cachedReactivateDate);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(element));
            oneOf (cache).put(with(any(Element.class)));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.NEW, StoppageManager.deDup(id, reactivateDate, cache));
    }

    @Test
    public void testDeDup05_duplicate_with_reactivateDate_mismatch2() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = DateUtils.addDays(new Date(), 1);
        final Date cachedReactivateDate = null;
        final Element element = new Element(id, cachedReactivateDate);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(element));
            oneOf (cache).put(with(any(Element.class)));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.NEW, StoppageManager.deDup(id, reactivateDate, cache));
    }

    @Test
    public void testDeDup06_duplicate_with_reactivateDate_mismatch3() {
        final Ehcache cache = mock(Ehcache.class, "cache");
        final long id = randomLong();
        final Date reactivateDate = DateUtils.addDays(new Date(), 1);
        final Date cachedReactivateDate = DateUtils.addDays(reactivateDate, 1);
        final Element element = new Element(id, cachedReactivateDate);
        
        expect(new Expectations() {{
            oneOf (cache).get(id); will(returnValue(element));
            oneOf (cache).put(with(any(Element.class)));
        }});

        assertEquals(StoppageManager.DuplicateOrNew.NEW, StoppageManager.deDup(id, reactivateDate, cache));
    }
}
