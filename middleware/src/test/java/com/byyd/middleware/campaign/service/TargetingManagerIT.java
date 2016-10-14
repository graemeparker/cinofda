package com.byyd.middleware.campaign.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.GeotargetType_;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class TargetingManagerIT extends AbstractAdfonicTest {

    @Autowired
    TargetingManager targetingManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    CommonManager commonManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private PublicationManager publicationManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetSegmentWithInvalidId() {
        assertNull(targetingManager.getSegmentById(0L));
    }

    @Test
    public void testSegment() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "segments", JoinType.LEFT);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L, fs);
        String name = "Testing";
        Segment segment = null;
        try {
            segment = targetingManager.newSegment(advertiser, name);
            assertNotNull(segment);
            long id = segment.getId();
            assertTrue(id > 0L);

            segment = targetingManager.getSegmentById(id);
            assertNotNull(segment);
            assertEquals(id, segment.getId());

            segment = targetingManager.getSegmentById(Long.toString(id));
            assertNotNull(segment);
            assertEquals(id, segment.getId());

            advertiser = advertiserManager.getAdvertiserById(1L, fs);
            Set<Segment> segments = advertiser.getSegments();
            assertTrue(segments.contains(segment));

            String newName = name + " Changed";
            segment.setName(newName);
            segment = targetingManager.update(segment);
            segment = targetingManager.getSegmentById(segment.getId());
            assertEquals(newName, segment.getName());


        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            targetingManager.delete(segment);
            assertNull(targetingManager.getSegmentById(segment.getId()));
        }
    }

    @Test
    public void testSegment2() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "segments", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Segment.class, "adSpaces", JoinType.LEFT);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L, fs);
        String name = "Testing";
        Segment segment = null;
        try {
            segment = targetingManager.newSegment(advertiser, name, fs);
            assertNotNull(segment);
            long id = segment.getId();
            assertTrue(id > 0L);

            for(long i = 1;i <= 10;i++) {
                AdSpace adSpace = publicationManager.getAdSpaceById(i);
                if(adSpace != null) {
                    segment.getAdSpaces().add(adSpace);
                }
            }

            segment = targetingManager.update(segment);
            segment = targetingManager.getSegmentById(id, fs);
            assertTrue(segment.getAdSpaces().size() > 0);

            segment.getAdSpaces().clear();
            segment = targetingManager.update(segment);
            segment = targetingManager.getSegmentById(id, fs);
            assertFalse(segment.getAdSpaces().size() > 0);

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            targetingManager.delete(segment);
            assertNull(targetingManager.getSegmentById(segment.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCampaignTimePeriods() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        FetchStrategyImpl fs = new FetchStrategyImpl();
        //fs.addEagerlyLoadedFieldForClass(CampaignTimePeriod.class, "campaign");
        CampaignTimePeriod ctp = null;
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(new Date());
        Date endDate = DateUtils.addDays(startDate, 5);
        String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(dateFormatPattern);
        try {
            ctp = targetingManager.newCampaignTimePeriod(campaign, startDate, endDate, fs);
            assertNotNull(ctp);
            long id = ctp.getId();
            assertTrue(id > 0);

            CampaignTimePeriod ctp2 = targetingManager.getCampaignTimePeriodById(id, fs);
            assertEquals(startDate, ctp2.getStartDate());
            assertEquals(ctp.getStartDate(), ctp2.getStartDate());
            assertEquals(ctp, ctp2);
            assertEquals(ctp2, ctp);

            /*
            //assertEquals(format.format(ctp.getStartDate()), format.format(startDate));
            //assertEquals(format.format(ctp.getEndDate()), format.format(endDate));
            CampaignTimePeriod ctp2 = targetingManager.getCampaignTimePeriodById(id, fs);
            //assertEquals(format.format(ctp2.getStartDate()), format.format(startDate));
            //assertEquals(format.format(ctp2.getEndDate()), format.format(endDate));
            System.out.println("startDate: " + startDate);
            System.out.println("[F] startDate: " + format.format(startDate));
            System.out.println("endDate: " + endDate);
            System.out.println("[F] endDate: " + format.format(endDate));
            System.out.println("ctp start date: " + ctp.getStartDate());
            System.out.println("ctp2 start date: " + ctp2.getStartDate());
            System.out.println("ctp end date: " + ctp.getEndDate());
            System.out.println("ctp2 end date: " + ctp2.getEndDate());
            System.out.println("[F] ctp start date: " + format.format(ctp.getStartDate()));
            System.out.println("[F] ctp2 start date: " + format.format(ctp2.getStartDate()));
            System.out.println("[F] ctp end date: " + format.format(ctp.getEndDate()));
            System.out.println("[F] ctp2 end date: " + format.format(ctp2.getEndDate()));
            */

            assertEquals(ctp, targetingManager.getCampaignTimePeriodById(id, fs));
            assertEquals(ctp, targetingManager.getCampaignTimePeriodById(Long.toString(id), fs));
            assertEquals(ctp.getStartDate(), startDate);
            assertEquals(ctp.getEndDate(), endDate);

            Date newEndDate = DateUtils.addDays(endDate, 5);
            ctp.setEndDate(newEndDate);
            ctp = targetingManager.update(ctp);

            ctp = targetingManager.getCampaignTimePeriodById(id, fs);
            assertEquals(newEndDate, ctp.getEndDate());


        } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if(ctp != null) {
                targetingManager.delete(ctp);
                assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
            }
        }
    }

    @Test
    public void testCampaignTimePeriodsJon() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        FetchStrategyImpl fs = new FetchStrategyImpl();
        //fs.addEagerlyLoadedFieldForClass(CampaignTimePeriod.class, "campaign");
        CampaignTimePeriod ctp = null;
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(new Date());
        Date endDate = DateUtils.addDays(startDate, 5);
        String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(dateFormatPattern);
        try {
            ctp = targetingManager.newCampaignTimePeriod(campaign, startDate, endDate, fs);
            assertNotNull(ctp);
            long id = ctp.getId();
            assertTrue(id > 0);
            CampaignTimePeriod ctp2 = targetingManager.getCampaignTimePeriodById(id, fs);
            assertEquals(startDate, ctp2.getStartDate());
            assertEquals(ctp.getStartDate(), ctp2.getStartDate());
            assertEquals(ctp,ctp2);
        } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if(ctp != null) {
                targetingManager.delete(ctp);
                assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
            }
        }
    }

    @Test
    @Transactional
    public void addTimePeriodToCampaign() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "timePeriods", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        CampaignTimePeriod ctp = null;
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(DateUtils.addDays(new Date(), 365));
        Date endDate = DateUtils.addDays(startDate, 5);
        try {
            ctp = targetingManager.newCampaignTimePeriod(campaign, startDate, endDate);
            assertNotNull(ctp);
            campaign = targetingManager.addTimePeriodToCampaign(campaign, ctp);
            campaign = campaignManager.getCampaignById(campaign.getId(), fs);
            assertTrue(campaign.getTimePeriods().contains(ctp));
         } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if(ctp != null) {
                targetingManager.delete(ctp);
                assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
            }
        }

    }

    @Test
    public void addUnpersistedTimePeriodToCampaign() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "timePeriods", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        CampaignTimePeriod ctp = null;
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(DateUtils.addDays(new Date(), 365));
        Date endDate = DateUtils.addDays(startDate, 5);
        try {
            ctp = new CampaignTimePeriod(campaign, startDate, endDate);
            campaign = targetingManager.addTimePeriodToCampaign(campaign, ctp);
            campaign = campaignManager.getCampaignById(campaign.getId(), fs);
            boolean found = false;
            for(CampaignTimePeriod tp : campaign.getTimePeriods()) {
                if(tp.getStartDate().getTime() == ctp.getStartDate().getTime() &&
                   tp.getEndDate().getTime() == ctp.getEndDate().getTime()) {
                    found = true;
                }
            }
            if(!found) {
                fail();
            }
         } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if(ctp != null) {
                targetingManager.delete(ctp);
                assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
            }
        }

    }

    @Test
    public void addTimePeriodsToCampaign() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "timePeriods", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        List<CampaignTimePeriod> ctps = new ArrayList<CampaignTimePeriod>();
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(DateUtils.addDays(new Date(), 365));
        Date endDate = DateUtils.addDays(startDate, 5);
        int nbTimePeriods = 10;
        try {
            Date thisStartDate = startDate;
            Date thisEndDate = endDate;
            for(int i = 0;i < nbTimePeriods;i++) {
                CampaignTimePeriod ctp = targetingManager.newCampaignTimePeriod(campaign, thisStartDate, thisEndDate);
                assertNotNull(ctp);
                ctps.add(ctp);
                thisStartDate = DateUtils.addDays(thisStartDate, 30);
                thisEndDate = DateUtils.addDays(thisEndDate, 30);
            }
            // Creating a time period using the method above persists it and links it to the campaign,
            // so trying to add them again will fail
            //campaign = targetingManager.addTimePeriodsToCampaign(campaign, new HashSet<CampaignTimePeriod>(ctps));
            campaign = campaignManager.getCampaignById(campaign.getId(), fs);
            for(CampaignTimePeriod ctp : ctps) {
                assertTrue(campaign.getTimePeriods().contains(ctp));
            }
         } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            for(CampaignTimePeriod ctp : ctps) {
                if(ctp != null) {
                    targetingManager.delete(ctp);
                    assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
                }
            }
        }

    }
    
    @Test
    @Transactional
    public void addUnpersistedTimePeriodsToCampaign() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "timePeriods", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        targetingManager.deleteCampaignTimePeriods(targetingManager.getAllCampaignTimePeriodsForCampaign(campaign));
        List<CampaignTimePeriod> ctps = new ArrayList<CampaignTimePeriod>();
        Date startDate = com.adfonic.util.DateUtils.sanitizeDate(DateUtils.addDays(new Date(), 365));
        Date endDate = DateUtils.addDays(startDate, 5);
        int nbTimePeriods = 10;
        try {
            Date thisStartDate = startDate;
            Date thisEndDate = endDate;
            for(int i = 0;i < nbTimePeriods;i++) {
                CampaignTimePeriod ctp = new CampaignTimePeriod(campaign, thisStartDate, thisEndDate);
                assertNotNull(ctp);
                ctps.add(ctp);
                thisStartDate = DateUtils.addDays(thisStartDate, 30);
                thisEndDate = DateUtils.addDays(thisStartDate, 5);
            }
            campaign = targetingManager.addTimePeriodsToCampaign(campaign, new HashSet<CampaignTimePeriod>(ctps));
            campaign = campaignManager.getCampaignById(campaign.getId(), fs);
            assertEquals(campaign.getTimePeriods().size(), nbTimePeriods);
         } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            for(CampaignTimePeriod ctp : ctps) {
                if(ctp != null) {
                    targetingManager.delete(ctp);
                    assertNull(targetingManager.getCampaignTimePeriodById(ctp.getId()));
                }
            }
        }

    }

   
    @Test
    public void testCrossTargetAdvertisers() {
        FetchStrategy fs = new FetchStrategyBuilder()
            .addLeft(Advertiser_.crossTargetAdvertisers)
            .nonRecursive(Advertiser_.crossTargetAdvertisers)
            .build();
        Advertiser a1 = advertiserManager.getAdvertiserById(1L, fs);
        showCrossTargetAdvertisers(a1);

        a1.getCrossTargetAdvertisers().clear();
        advertiserManager.update(a1);
        a1 = advertiserManager.getAdvertiserById(1L, fs);
        showCrossTargetAdvertisers(a1);

        a1.getCrossTargetAdvertisers().add(advertiserManager.getAdvertiserById(2L));
        a1.getCrossTargetAdvertisers().add(advertiserManager.getAdvertiserById(4L));
        a1.getCrossTargetAdvertisers().add(advertiserManager.getAdvertiserById(6L));
        advertiserManager.update(a1);
        a1 = advertiserManager.getAdvertiserById(1L, fs);
        showCrossTargetAdvertisers(a1);
    }
    
   
    
    void showCampaigns(List<Campaign> campaigns) {
        System.out.println("=========================================================================");
        for (Campaign c : campaigns) {
            c = campaignManager.getCampaignById(c.getId(), new FetchStrategyBuilder().addInner(Campaign_.advertiser).build());
            System.out.println(c.getName() + " for advertiser " + c.getAdvertiser().getId());
        }
    }

    static void showCrossTargetAdvertisers(Advertiser advertiser) {
        System.out.println("=========================================================================");
        System.out.println("Advertiser id=" + advertiser.getId() + " has cross target permission on:");
        for (Advertiser a2 : advertiser.getCrossTargetAdvertisers()) {
            System.out.println("- Advertiser id=" + a2.getId() + ", name=" + a2.getName());
        }
    }
  
    @Test
    public void testLoadSegmentFetchMode() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Segment_.excludedModels)
                               .build();
            
            Segment segment = targetingManager.getSegmentById(1L);
            System.out.println(segment.getName());
            segment = targetingManager.getSegmentById(1L, fs);
            System.out.println(segment.getName());
           } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    @Transactional(readOnly=true)
    public void testLoadSegmentFetchModeTransactional() {
        try {
            Segment segment = targetingManager.getSegmentById(1L);
            segment.getExcludedModels().size();
           } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    @Transactional(readOnly=true)
    public void testLoadSegmentThroughCreativeFetchModeTransactional() {
        try {
            Creative creative = creativeManager.getCreativeById(1L);
            creative.getSegment().getName();
            creative.getSegment().getExcludedModels().size();
           } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testLocationTargets() {
        LocationTarget locationTarget = null;
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addInner(Campaign_.advertiser)
                               .addInner(Campaign_.segments)
                               .addLeft(Segment_.locationTargets)
                               .build();
            Campaign campaign = campaignManager.getCampaignById(1L, fs);
            Advertiser advertiser = campaign.getAdvertiser();
            Segment segment = campaign.getSegments().get(0);
            String name1 = "Name1";
            String name2 = "Name2";
            String name3 = "Name3";
            String name = name1 + name2 + name3;
            BigDecimal latitude = BigDecimal.valueOf(1.1);
            BigDecimal longitude = BigDecimal.valueOf(2.2);
            BigDecimal radiusMiles = BigDecimal.valueOf(3.3);
            
            locationTarget = targetingManager.newLocationTarget(advertiser, name, latitude, longitude, radiusMiles);
            assertTrue(locationTarget.getId() > 0);
            assertEquals(locationTarget.getLatitude(), latitude);
               assertEquals(locationTarget.getLongitude(), longitude);
               assertEquals(locationTarget.getRadiusMiles(), radiusMiles);
               
             
               assertTrue(targetingManager.getAllLocationTargetsForAdvertiser(advertiser).contains(locationTarget));
               assertTrue(targetingManager.getAllLocationTargetsForAdvertiserAndPartialName(advertiser, name1, LikeSpec.STARTS_WITH, false).contains(locationTarget));
              assertTrue(targetingManager.getAllLocationTargetsForAdvertiserAndPartialName(advertiser, name2, LikeSpec.CONTAINS, false).contains(locationTarget));
              assertTrue(targetingManager.getAllLocationTargetsForAdvertiserAndPartialName(advertiser, name3, LikeSpec.ENDS_WITH, false).contains(locationTarget));
              
              segment.getLocationTargets().add(locationTarget);
              segment = targetingManager.update(segment);
              campaign = campaignManager.getCampaignById(1L, fs);
               segment = campaign.getSegments().get(0);
              
               assertTrue(segment.getLocationTargets().contains(locationTarget));
               
               try {
                   targetingManager.delete(locationTarget);
                   fail("We were able to delete a LocationTarget still linked to a segment");
               } catch(Exception e) {
               }
               
               segment.getLocationTargets().clear();
               segment = targetingManager.update(segment);
               
         } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(locationTarget != null) {
                targetingManager.delete(locationTarget);
                assertNull(targetingManager.getLocationTargetById(locationTarget.getId()));
            }
        }
    }


    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGeotarget() {
        Geotarget gt = null;
        try {
            String name = "Testing" + System.currentTimeMillis();
            Country country = commonManager.getCountryByIsoCode("US");
            GeotargetType type = targetingManager.getGeotargetTypeByNameAndType("STATE", "STATE");
            double displayLatitude = 10.5;
            double displayLongitude = -13.56;

            gt = targetingManager.newGeotarget(name, country, type, displayLatitude, displayLongitude);
            assertNotNull(gt);
            long id = gt.getId();
            assertTrue(id > 0);

            assertEquals(gt, targetingManager.getGeotargetById(id));
            assertEquals(gt, targetingManager.getGeotargetById(Long.toString(id)));

            String newName = name + "Changed";
            gt.setName(newName);
            gt = targetingManager.update(gt);

            gt = targetingManager.getGeotargetById(id);
            assertEquals(newName, gt.getName());

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            targetingManager.delete(gt);
            assertNull(targetingManager.getGeotargetById(gt.getId()));
        }
    }


    @Test
    public void testGeotargetDeux() {
        try {
            Country country = commonManager.getCountryByIsoCode("GB");
            GeotargetType type = targetingManager.getGeotargetTypeByNameAndType("STATE", "STATE");
            String name = "br";
            LikeSpec like = LikeSpec.CONTAINS;
            boolean caseSensitive = false;

            long count = targetingManager.countGeotargetsByNameAndTypeAndIsoCode(country.getIsoCode(), type, name, caseSensitive, like);
            assertTrue(count > 0);

            List<Geotarget> list = targetingManager.getGeotargetsByNameAndTypeAndIsoCode(country.getIsoCode(), type, name, caseSensitive, like, new Sorting(asc(Geotarget.class, "name")));
            assertNotNull(list);
            assertTrue(list.size() > 0);

            for(Geotarget gt : list) {
                System.out.println(gt.getName());
            }
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testGeotargetTrois() {
        try {
            String isoCode = "GB";

            long count = targetingManager.countAllGeotargetTypesForCountryIsoCode(isoCode);
            assertTrue(count > 0);
            System.out.println(count);

            List<GeotargetType> types = targetingManager.getAllGeotargetTypesForCountryIsoCode(isoCode, new Sorting(asc("type")));
            assertNotNull(types);
            assertTrue(types.size() > 0);

            for(GeotargetType type : types) {
                System.out.println(type.toString());
            }
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------
    
    @Test
    public void testGeotargetType() {

        List<GeotargetType> types = targetingManager.getAllGeotargetTypes();
        assertNotNull(types);
        assertTrue(types.size() > 0);
        Long id = null;
        for (GeotargetType type : types) {
            System.out.println(type.getName());
            id = type.getId();
        }
        assertNotNull(targetingManager.getGeotargetTypeById(id));
    }
    
    @Test
    public void testGeotargetTypeCountries() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(GeotargetType_.countries)
                           .build();
                           
        List<GeotargetType> types = targetingManager.getAllGeotargetTypes(fs);
        assertNotNull(types);
        assertTrue(types.size() > 0);
        
        for (GeotargetType type : types) {
            System.out.println(type.getName());
            for(Country country : type.getCountries()) {
                System.out.println("    " + country.getName());
            }
        }
    }
    
    @Test
    public void testGetAllGeoTargetingCountries() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(GeotargetType_.countries)
                           .build();
        List<Country> targetCountries = new ArrayList<Country>(0);
        List<GeotargetType> geotargetTypes =targetingManager.getAllGeotargetTypes(fs);
        for(GeotargetType type : geotargetTypes) {
            if(type.getCountries() != null) {
                for(Country country : type.getCountries()) {
                    if(!targetCountries.contains(country)) {
                        targetCountries.add(country);
                    }
                }
            }
        }
        for(Country country : targetCountries) {
            System.out.println(country.getName());
        }
    }
    
    @Test
    public void testGetGeotargetingTypesForCountry() {
        List<String> isoCodes = new ArrayList<>();
        isoCodes.add("US");
        isoCodes.add("GB");
        for(String isoCode : isoCodes) {
            System.out.println("For " + isoCode + ": ");
            List<GeotargetType> types = targetingManager.getAllGeotargetTypesForCountryIsoCode(isoCode);
            for(GeotargetType type : types) {
                System.out.println("--> " + type.getName() + " - " + type.getType());
            }

        }
    }
}
