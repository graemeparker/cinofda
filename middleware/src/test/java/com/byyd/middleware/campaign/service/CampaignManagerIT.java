package com.byyd.middleware.campaign.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Language;
import com.adfonic.domain.NotificationFlag;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.User;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.Range;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class CampaignManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private TargetingManager targetingManager;
    
    @Autowired
    private FeeManager feeManager;
    
    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testNewCampaign() {
        try {
            Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
            Category category = commonManager.getCategoryByName("Automotive");
            Language language = commonManager.getLanguageByIsoCode("en");
            String name = UUID.randomUUID().toString();
            
            campaignManager.newCampaign(name, advertiser, category, language, false);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    @Transactional
    public void testHydrationAfterUpdate() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                                .addLeft(Company_.advertisers)
                                .addLeft(Company_.accountManager)
                                .addLeft(Company_.publisher)
                                .build();
            Company company = companyManager.getCompanyById(152L, fs);
            // update our company
            String newName = "Name" + System.currentTimeMillis();
            company.setName(newName);
            System.out.println("Advertisers size: " + company.getAdvertisers().size());
            System.out.println("Publisher: " + company.getPublisher().getExternalID());
            System.out.println("Account Manager: " + company.getAccountManager().getFullName());
            System.out.println("Updating...");
            company = companyManager.update(company);
            System.out.println("Updated...");
            // if we lost advertiser hydration this would throw
            System.out.println("Advertisers size: " + company.getAdvertisers().size());
            System.out.println("Publisher: " + company.getPublisher().getExternalID());
            System.out.println("Account Manager: " + company.getAccountManager().getFullName());



        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetCampaignStoppageWithInvalidId() {
        assertNull(campaignManager.getCampaignStoppageById(0L));
    }

    @Test
    public void testCampaignStoppage() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        CampaignStoppage.Reason reason = CampaignStoppage.Reason.OVERALL_BUDGET;
        Date now = new Date();
        Date tomorrow = DateUtils.addDays(now, 1);
        CampaignStoppage stoppageWithReactivateDate = null;
        CampaignStoppage stoppageWithoutReactivateDate = null;
        try {
            stoppageWithReactivateDate = campaignManager.newCampaignStoppage(campaign, reason, tomorrow);
            assertNotNull(stoppageWithReactivateDate);
            long id = stoppageWithReactivateDate.getId();
            assertTrue(id > 0);
            assertEquals(reason, stoppageWithReactivateDate.getReason());

            stoppageWithReactivateDate = campaignManager.getCampaignStoppageById(id);
            assertNotNull(stoppageWithReactivateDate);
            assertEquals(id, stoppageWithReactivateDate.getId());

            stoppageWithReactivateDate = campaignManager.getCampaignStoppageById(Long.toString(id));
            assertNotNull(stoppageWithReactivateDate);
            assertEquals(id, stoppageWithReactivateDate.getId());

            List<CampaignStoppage> stoppages = campaignManager.getCampaignStoppagesForNullOrFutureReactivateDate();
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithReactivateDate));

            stoppages = campaignManager.getCampaignStoppagesForCampaignAndNullOrFutureReactivateDate(campaign);
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithReactivateDate));

            stoppageWithoutReactivateDate = campaignManager.newCampaignStoppage(campaign, reason);
            assertTrue(stoppageWithoutReactivateDate.getId() > 0);
            assertEquals(reason, stoppageWithoutReactivateDate.getReason());

            stoppages = campaignManager.getCampaignStoppagesForNullOrFutureReactivateDate();
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithoutReactivateDate));

            stoppages = campaignManager.getCampaignStoppagesForCampaignAndNullOrFutureReactivateDate(campaign);
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithoutReactivateDate));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            campaignManager.delete(stoppageWithReactivateDate);
            assertNull(campaignManager.getCampaignStoppageById(stoppageWithReactivateDate.getId()));

            campaignManager.delete(stoppageWithoutReactivateDate);
            assertNull(campaignManager.getCampaignStoppageById(stoppageWithoutReactivateDate.getId()));
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetCampaignWithInvalidId() {
        assertNull(campaignManager.getCampaignById(0L));
    }

    @Test
    @Transactional
    public void testCampaign() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        Language language = commonManager.getLanguageByIsoCode("EN");
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        String name = "Testing" + UUID.randomUUID().toString();
        Campaign campaign = null;
        try {
            assertTrue(campaignManager.isCampaignNameUnique(name, advertiser, null));
            assertTrue(campaignManager.isCampaignNameUnique(name.toUpperCase(), advertiser, null));
            assertTrue(campaignManager.isCampaignNameUnique(name.toLowerCase(), advertiser, null));

            campaign = new Campaign(advertiser, name);
            campaign.setDefaultLanguage(language);
            campaign.setCategory(category);
            campaign = campaignManager.newCampaign(campaign);
            assertNotNull(campaign);
            long id = campaign.getId();
            assertTrue(id > 0);
            assertEquals(name, campaign.getName());

            assertFalse(campaignManager.isCampaignNameUnique(name, advertiser, null));
            assertFalse(campaignManager.isCampaignNameUnique(name.toUpperCase(), advertiser, null));
            assertFalse(campaignManager.isCampaignNameUnique(name.toLowerCase(), advertiser, null));

            assertTrue(campaignManager.isCampaignNameUnique(name, advertiser, campaign));
            assertTrue(campaignManager.isCampaignNameUnique(name.toUpperCase(), advertiser, campaign));
            assertTrue(campaignManager.isCampaignNameUnique(name.toLowerCase(), advertiser, campaign));

            campaign = campaignManager.getCampaignById(id);
            assertNotNull(campaign);
            assertEquals(id, campaign.getId());

            campaign = campaignManager.getCampaignById(Long.toString(id));
            assertNotNull(campaign);
            assertEquals(id, campaign.getId());

            Campaign c = campaignManager.getCampaignByExternalId(campaign.getExternalID());
            assertNotNull(c);
            assertEquals(c.getId(), campaign.getId());

            String newName = name + " Changed";
            campaign.setName(newName);
            campaign = campaignManager.update(campaign);
            campaign = campaignManager.getCampaignById(campaign.getId());
            assertEquals(newName, campaign.getName());

            CampaignFilter filter = new CampaignFilter();
            filter.setAdvertiser(advertiser);
            Long count = campaignManager.countAllCampaigns(filter);
            assertTrue(count > 0);
            int previousCount = count.intValue();

            // Test excluding campaign
            filter = new CampaignFilter();
            filter.setAdvertiser(advertiser);
            filter.setExcludedIds(Collections.singleton(campaign.getId()));
            assertEquals(previousCount - 1, campaignManager.countAllCampaigns(filter).intValue());

            // Test name
            filter = new CampaignFilter();
            filter.setAdvertiser(advertiser);
            filter.setName(campaign.getName(), true); // case-sensitive
            assertEquals(1, campaignManager.countAllCampaigns(filter).intValue());

            List<Campaign> campaigns = campaignManager.getAllCampaigns(filter);
            assertTrue(campaigns.size() > 0);
            assertTrue(campaigns.contains(campaign));

            // Test the campaignIds filter functionality
            filter.setCampaignIds(Collections.singleton(0L));
            campaigns = campaignManager.getAllCampaigns(filter);
            assertTrue(campaigns.isEmpty());

            filter.setCampaignIds(Collections.singleton(campaign.getId()));
            campaigns = campaignManager.getAllCampaigns(filter);
            assertEquals(1, campaigns.size());
            assertTrue(campaigns.contains(campaign));

            filter.setCampaignIds(null); // undo the campaignIds filter

            campaigns = campaignManager.getAllCampaignsThatHaveEverBeenActiveForAdvertiser(advertiser);
            assertFalse(campaigns.contains(campaign));

            Campaign.Status newStatus = Campaign.Status.ACTIVE;
            campaign.setStatus(newStatus);
            campaign = campaignManager.update(campaign);
            assertEquals(newStatus, campaign.getStatus());

            count = campaignManager.countAllCampaignsThatHaveEverBeenActiveForAdvertiser(advertiser);
            assertTrue(count > 0);
            campaigns = campaignManager.getAllCampaignsThatHaveEverBeenActiveForAdvertiser(advertiser);
            assertTrue(campaigns.contains(campaign));

            campaign.setStatus(Campaign.Status.DELETED);
            campaign = campaignManager.update(campaign);

            // Now that the Campaign has been marked DELETED, its name should again
            // be eligible for use.
            assertTrue(campaignManager.isCampaignNameUnique(name, advertiser, null));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            //campaignManager.delete(campaign);
            //assertNull(campaignManager.getCampaignBidById(campaign.getId()));
        }
    }

    @Test
    public void testCampaignDeux() {
        try {
            Advertiser advertiser = advertiserManager.getAdvertiserById(5290L);
            Range<Date> dateRangeForActive = new Range<Date>(null, null); //new Range<Date>(DateUtils.parseDate("2010-01-01", parsePatterns), DateUtils.parseDate("2011-01-01", parsePatterns));
            Boolean houseAds = null;
            long count = campaignManager.countActiveCampaignsForPeriod(advertiser, dateRangeForActive, houseAds);
            assertTrue(count > 0);
            System.out.println(count);

            List<Campaign> list = campaignManager.getActiveCampaignsForPeriod(advertiser, dateRangeForActive, houseAds);
            assertTrue(list.size() > 0);
            for(Campaign campaign : list) {
                System.out.println(campaign.getName() + " - " + campaign.getStartDate() + " - " + campaign.getEndDate());
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    @Transactional
    public void testCampaignTrois() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        Language language = commonManager.getLanguageByIsoCode("EN");
        String name = "Testing" + UUID.randomUUID().toString();
        Campaign campaign = null;
        try {
            campaign = new Campaign(advertiser);
            campaign.setName(name);
            campaign.setDefaultLanguage(language);
            campaign.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));
            campaign = campaignManager.newCampaign(campaign);
            assertNotNull(campaign);
            assertTrue(campaign.getId() > 0);

            FetchStrategyImpl campaignFs = new FetchStrategyImpl();
            campaignFs.addEagerlyLoadedFieldForClass(Campaign.class, "advertiser", JoinType.LEFT);
            campaign = campaignManager.getCampaignById(campaign.getId(), campaignFs);
            assertEquals(campaign.getAdvertiser(), advertiser);

            FetchStrategyImpl advertiserFs = new FetchStrategyImpl();
            advertiserFs.addEagerlyLoadedFieldForClass(Advertiser.class, "campaigns", JoinType.LEFT);
            advertiser = advertiserManager.getAdvertiserById(advertiser.getId(), advertiserFs);
            assertTrue(advertiser.getCampaigns().contains(campaign));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    @Transactional
    public void testCampaignTwoPhaseLoad() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Campaign_.creatives)
                               .build();

            User user = userManager.getUserById(2L);
            Advertiser advertiser = advertiserManager.getAdvertiserById(1l);
            com.adfonic.util.DateUtils.Period period = com.adfonic.util.DateUtils.Period.TODAY;
            CampaignFilter filter = new CampaignFilter();
            filter.setAdvertiser(advertiser);
            filter.setDateRangeForActive(period.getRange(user.getCompany().getDefaultTimeZone()));
            filter.setHouseAds(false);

            List<Campaign> campaigns = campaignManager.getAllCampaigns(filter);
            System.out.println("One Phase Load with no fs there are " + campaigns.size()
                    + " campaigns<br/>");

            List<Campaign> campaignsNoFs = campaignManager.getAllCampaignsUsingTwoPhaseLoad(filter);
            System.out.println("Two Phase Load with no fs there are " + campaignsNoFs.size()
                    + " campaigns<br/>");

            List<Campaign> campaignsFs = campaignManager.getAllCampaignsUsingTwoPhaseLoad(filter, fs);
            System.out.println("Two Phase Load with fs there are " + campaignsFs.size()
                    + " campaigns<br/>");
            for(Campaign campaign : campaignsFs) {
                System.out.println("Campaign \"" + campaign.getName() + "\" has " + campaign.getCreatives().size() + " creatives");
            }

            assertEquals(campaigns.size(), campaignsNoFs.size());
            assertEquals(campaignsNoFs.size(), campaignsFs.size());
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testHalfBakedCampaign() {
        try {
            String name = "Testing" + System.currentTimeMillis();
            FetchStrategy advertiserFs = new FetchStrategyBuilder()
                                         .addLeft(Advertiser_.segments)
                                         .build();
            Advertiser advertiser = advertiserManager.getAdvertiserById(1L, advertiserFs);
            Language language = commonManager.getLanguageByIsoCode("en");
            Category category = commonManager.getCategoryByName("Automotive");

            Campaign campaign = new Campaign(advertiser);

            // Make a segment
            campaign.getSegments().add(advertiser.newSegment());

            // Set all the required fields
            campaign.setDefaultLanguage(language);
            campaign.setDisableLanguageMatch(true);
            campaign.setName(name);
            campaign.setCategory(category);


            FetchStrategy campaignFs = new FetchStrategyBuilder()
                                       .addLeft(Campaign_.segments)
                                       .build();
            Campaign newCampaign = campaignManager.newCampaign(
                                        campaign.getName(),
                                        campaign.getAdvertiser(),
                                        campaign.getCategory(),
                                        campaign.getDefaultLanguage(),
                                        campaign.getDisableLanguageMatch(),
                                        campaignFs);
            boolean mustUpdateCampaign = false;

            if(!org.apache.commons.collections.CollectionUtils.isEmpty(campaign.getSegments())) {
                for(Segment segment : campaign.getSegments()) {
                    Segment newSegment = targetingManager.newSegment(newCampaign.getAdvertiser(), segment.getName());
                    newCampaign.getSegments().add(newSegment);
                }
                mustUpdateCampaign = true;
            }

            if(mustUpdateCampaign) {
                newCampaign = campaignManager.update(newCampaign);
            }

            newCampaign = campaignManager.getCampaignById(newCampaign.getId(), campaignFs);
            System.out.println("Campaign Name: " + campaign.getName());
            for(Segment segment : newCampaign.getSegments()) {
                System.out.println("Segment Name: " + segment.getName());
            }
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }

    }
    
    @Test
    public void testSumCampaignDailySpend() {
        try {
            Campaign campaign = campaignManager.getCampaignById(1L);
            boolean rc = campaignManager.campaignHasDailySpend(campaign, 20090701, 20090731);
            assertTrue(rc);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testSumCampaignDailySpendDeux() {
        try {
            String[] patterns = { "yyyy-MM-dd" };
            Date fromDate = org.apache.commons.lang.time.DateUtils.parseDate("2009-07-01", patterns);
            Date toDate = org.apache.commons.lang.time.DateUtils.parseDate("2009-07-31", patterns);
            Campaign campaign = campaignManager.getCampaignById(1L);
            boolean rc = campaignManager.campaignHasDailySpend(campaign, fromDate, toDate);
            assertTrue(rc);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testCampaignBudgetType() {
           try {
               BigDecimal dailyBudget = new BigDecimal(1000L);
               BigDecimal overallBudget = new BigDecimal(2000L);
               Campaign campaign = campaignManager.getCampaignById(1L);
               
               campaign.setBudgetType(BudgetType.CLICKS);
               campaign.setDailyBudget(dailyBudget);
               campaign.setOverallBudget(overallBudget);
               campaign.setDailyBudgetClicks(dailyBudget);
               campaign.setOverallBudgetClicks(overallBudget);
               campaign.setDailyBudgetImpressions(dailyBudget);
               campaign.setOverallBudgetImpressions(overallBudget);
               campaign = campaignManager.update(campaign);
               assertNull(campaign.getDailyBudget());
               assertNull(campaign.getOverallBudget());
               assertNull(campaign.getDailyBudgetImpressions());
               assertNull(campaign.getOverallBudgetImpressions());
               assertEquals(campaign.getDailyBudgetClicks(), dailyBudget);
               assertEquals(campaign.getOverallBudgetClicks(), overallBudget);
               campaign.setBudgetType(null);
               assertEquals(campaign.inferBudgetType(), BudgetType.CLICKS);
               
               campaign.setBudgetType(BudgetType.IMPRESSIONS);
               campaign.setDailyBudget(dailyBudget);
               campaign.setOverallBudget(overallBudget);
               campaign.setDailyBudgetClicks(dailyBudget);
               campaign.setOverallBudgetClicks(overallBudget);
               campaign.setDailyBudgetImpressions(dailyBudget);
               campaign.setOverallBudgetImpressions(overallBudget);
               campaign = campaignManager.update(campaign);
               assertNull(campaign.getDailyBudget());
               assertNull(campaign.getOverallBudget());
               assertNull(campaign.getDailyBudgetClicks());
               assertNull(campaign.getOverallBudgetClicks());
               assertEquals(campaign.getDailyBudgetImpressions(), dailyBudget);
               assertEquals(campaign.getOverallBudgetImpressions(), overallBudget);
               campaign.setBudgetType(null);
               assertEquals(campaign.inferBudgetType(), BudgetType.IMPRESSIONS);

              campaign.setBudgetType(BudgetType.MONETARY);
               campaign.setDailyBudget(dailyBudget);
               campaign.setOverallBudget(overallBudget);
               campaign.setDailyBudgetClicks(dailyBudget);
               campaign.setOverallBudgetClicks(overallBudget);
               campaign.setDailyBudgetImpressions(dailyBudget);
               campaign.setOverallBudgetImpressions(overallBudget);
               campaign = campaignManager.update(campaign);
               assertNull(campaign.getDailyBudgetImpressions());
               assertNull(campaign.getOverallBudgetImpressions());
               assertNull(campaign.getDailyBudgetClicks());
               assertNull(campaign.getOverallBudgetClicks());
               assertEquals(campaign.getDailyBudget(), dailyBudget);
               assertEquals(campaign.getOverallBudget(), overallBudget);
               campaign.setBudgetType(null);
               assertEquals(campaign.inferBudgetType(), BudgetType.MONETARY);

           } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    /*
     * Tests whatever needs to be automatically done when creating a campaign. Note that if
     * the unit test is marked transactional, fetch strategies are not required. But for consistency
     * and meaningful testing for apps that do not deal with DTOs, the FS-based version is active
     */
    @Test
    //@Transactional(readOnly=false)
    public void testCampaignCreation() {
        FetchStrategy advertiserFs = new FetchStrategyBuilder()
                                     .addInner(Advertiser_.company)
                                     .build();
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L, advertiserFs);
        Language language = commonManager.getLanguageByIsoCode("EN");
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        String name = "Testing" + UUID.randomUUID().toString();
        Campaign campaign = null;
        try {
            campaign = new Campaign(advertiser, name);
            campaign.setDefaultLanguage(language);
            campaign.setCategory(category);
            campaign = campaignManager.newCampaign(campaign);
            assertNotNull(campaign);
            long id = campaign.getId();
            assertTrue(id > 0);
            assertEquals(name, campaign.getName());
            assertNotNull(campaign.getCurrentAgencyDiscount());
            assertEquals(new Double(campaign.getCurrentAgencyDiscount().getDiscount().doubleValue()), new Double(advertiser.getCompany().getDiscount().doubleValue())); 
          } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
 
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetHouseAdCountForPublisher() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        try {
            Long count = campaignManager.getHouseAdCountForPublisher(publisher);
            System.out.println(count);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCampaignNotificationFlag() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        NotificationFlag.Type type = NotificationFlag.Type.WENT_LIVE;
        int ttlSeconds = 15;
        CampaignNotificationFlag flag = null;
        CampaignNotificationFlag flagDeux = null;
        Date expirationDate = DateUtils.addDays(new Date(), 1);
        try {
            flag = campaignManager.newCampaignNotificationFlag(campaign, type, ttlSeconds);
            assertNotNull(flag);
            assertTrue(flag.getId() > 0);

            assertEquals(flag, campaignManager.getCampaignNotificationFlagById(flag.getId()));
            assertEquals(flag, campaignManager.getCampaignNotificationFlagById(Long.toString(flag.getId())));

            flagDeux = campaignManager.newCampaignNotificationFlag(campaign, type, expirationDate);
            assertNotNull(flagDeux);
            assertTrue(flagDeux.getId() > 0);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            campaignManager.delete(flag);
            assertNull(campaignManager.getCampaignNotificationFlagById(flag.getId()));
            campaignManager.delete(flagDeux);
            assertNull(campaignManager.getCampaignNotificationFlagById(flagDeux.getId()));
        }
    }

    @Test
    public void testCampaignWatchers() {
        FetchStrategy fs = new FetchStrategyBuilder().addLeft(Campaign_.watchers).build();
        Campaign campaign = campaignManager.getCampaignById(1L, fs);

        campaign.getWatchers().clear();
        campaignManager.update(campaign);
        campaign = campaignManager.getCampaignById(1L, fs);
        assertTrue(campaign.getWatchers().isEmpty());
        
        AdfonicUser watcher1 = userManager.getAdfonicUserById(9L);
        campaign.getWatchers().add(watcher1);
        campaignManager.update(campaign);
        campaign = campaignManager.getCampaignById(1L, fs);
        assertEquals(1, campaign.getWatchers().size());
        assertTrue(campaign.getWatchers().contains(watcher1));
        
        AdfonicUser watcher2 = userManager.getAdfonicUserById(4L);
        campaign.getWatchers().add(watcher2);
        campaignManager.update(campaign);
        campaign = campaignManager.getCampaignById(1L, fs);
        assertEquals(2, campaign.getWatchers().size());
        assertTrue(campaign.getWatchers().contains(watcher1));
        assertTrue(campaign.getWatchers().contains(watcher2));
        
        campaign.getWatchers().clear();
        campaignManager.update(campaign);
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCopyCampaignOne() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Campaign_.currentDataFee)
                           .addLeft(Campaign_.historicalDataFees)
                           .addLeft(Campaign_.currentRichMediaAdServingFee)
                           .addLeft(Campaign_.historicalRMAdServingFees)
                           .addLeft(Campaign_.currentTradingDeskMargin)
                           .addLeft(Campaign_.historicalTDMarginFees)
                           .build();
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        CampaignTradingDeskMargin campaignTradingDeskMargin = null;
        double campaignTradingDeskMarginAmount = 1.00;
        CampaignRichMediaAdServingFee campaignRichMediaAdServingFee = null;
        double campaignRichMediaAdServingFeeAmount = 1000.00;
        try {
            campaign = feeManager.saveCampaignRichMediaAdServingFee(campaign.getId(), BigDecimal.valueOf(campaignRichMediaAdServingFeeAmount));
            campaign = feeManager.saveCampaignTradingDeskMargin(campaign.getId(), BigDecimal.valueOf(campaignTradingDeskMarginAmount));
            campaignTradingDeskMargin = campaign.getCurrentTradingDeskMargin();
            campaignRichMediaAdServingFee = campaign.getCurrentRichMediaAdServingFee();
            
            Campaign copiedCampaign = campaignManager.copyCampaign(campaign, fs);
            assertEquals(new Double(copiedCampaign.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee().doubleValue()), new Double(campaignRichMediaAdServingFee.getRichMediaAdServingFee().doubleValue()));
            assertEquals(new Double(copiedCampaign.getCurrentTradingDeskMargin().getTradingDeskMargin().doubleValue()), new Double(campaignTradingDeskMargin.getTradingDeskMargin().doubleValue()));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testCopyCampaignBidAT933() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Campaign_.currentBid)
                                   .build();
            Campaign campaign = campaignManager.getCampaignById(2111L, fs);
            Campaign campaign2 = campaignManager.copyCampaign(campaign, fs); 
            assertEquals(campaign.getCurrentBid().getBidModelType(), campaign2.getCurrentBid().getBidModelType());
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testCopyCampaignAT935() {
           try {
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Campaign_.segments)
                                   .addLeft(Segment_.targettedPublishers)
                                   .build();
            Campaign campaign = campaignManager.getCampaignById(2259L, fs);
            Campaign campaign2 = campaignManager.copyCampaign(campaign, fs); 
            for(Publisher publisher : campaign.getSegments().get(0).getTargettedPublishers()) {
                System.out.println("Testing for " + publisher.getName());
                assertTrue(campaign2.getSegments().get(0).getTargettedPublishers().contains(publisher));
            }
            
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

}
