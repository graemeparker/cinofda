package com.byyd.middleware.account.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
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

import com.adfonic.domain.Account;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserNotificationFlag;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company;
import com.adfonic.domain.NotificationFlag;
import com.adfonic.domain.User;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.Range;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AdvertiserManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private AccountManager accountsManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAdvertiserWithInvalidId() {
        assertNull(advertiserManager.getAdvertiserById(0L));
    }

    @Test
    @Transactional
    public void testAdvertiserUsers() {
        final long advertiserId = 1l;
        final long userId = 2l;
        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserId);
        User user = userManager.getUserById(userId);

        // clear any users out
        advertiser.getUsers().clear();
        advertiser = advertiserManager.update(advertiser);
        assertEquals(0, advertiser.getUsers().size());

        // add one
        advertiser.getUsers().add(user);
        advertiser = advertiserManager.update(advertiser);
        assertEquals(1, advertiser.getUsers().size());

        // re-fetch
        advertiser = advertiserManager.getAdvertiserById(advertiserId);
        assertEquals(1, advertiser.getUsers().size());
    }

    @Test
    public void testAdvertiser() {
        Company company = companyManager.getCompanyById(2L);
        String name = "Testing" + UUID.randomUUID().toString();
        Advertiser advertiser = null;
        try {
            advertiser = advertiserManager.newAdvertiser(company, name);
            assertNotNull(advertiser);
            long id = advertiser.getId();
            assertTrue(id > 0L);

            assertEquals(advertiser, advertiserManager.getAdvertiserById(id));
            assertEquals(advertiser, advertiserManager.getAdvertiserById(Long.toString(id)));
            assertEquals(advertiser, advertiserManager.getAdvertiserByExternalId(advertiser.getExternalID()));
            assertEquals(advertiser, advertiserManager.getAdvertiserByName(advertiser.getName(), company));

            String newName = name + " Changed";
            advertiser.setName(newName);
            advertiser = advertiserManager.update(advertiser);
            assertEquals(newName, advertiser.getName());

            List<Advertiser> advertisers = advertiserManager.getAllAdvertisersForCompany(company);
            assertTrue(advertisers.contains(advertiser));

            AdvertiserFilter filter = new AdvertiserFilter()
                .setCompany(company);
            assertTrue(advertiserManager.getAllAdvertisers(filter).contains(advertiser));
            assertTrue(advertiserManager.countAllAdvertisers(filter) > 0);

            filter.setAdvertiserIds(Collections.singleton(0L));
            assertTrue(advertiserManager.getAllAdvertisers(filter).isEmpty());
            assertEquals((Object)0L, advertiserManager.countAllAdvertisers(filter));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(advertiser != null) {
                advertiserManager.delete(advertiser);
                assertNull(advertiserManager.getAdvertiserById(advertiser.getId()));
            }
        }
    }

    @Test
    public void testGetSpendAndBalanceForAdvertisersAndDateIds() {
        try {
            List<Advertiser> advertisers = new ArrayList<Advertiser>();
            advertisers.add(advertiserManager.getAdvertiserById(1L));
            advertisers.add(advertiserManager.getAdvertiserById(17L));
            TimeZone timeZone = TimeZone.getDefault();
            String[] parsePatterns = { "yyyy-MM-dd" };
            Range<Date> range = new Range<Date>(DateUtils.parseDate("2009-08-05", parsePatterns), DateUtils.parseDate("2010-02-10", parsePatterns));

            // The SQL query will need "date ids" for the date range.  Those are just
            // yyyymmddhh / 100 = yyyymmdd
            int startDateId = com.adfonic.util.DateUtils.getTimeID(range.getStart(), timeZone) / 100;
            int endDateId = com.adfonic.util.DateUtils.getTimeID(range.getEnd(), timeZone) / 100;

            Map<String, Map<Long, BigDecimal>> map = advertiserManager.getSpendAndBalanceForAdvertisersAndDateIds(advertisers, startDateId, endDateId);
            Map<Long, BigDecimal> spend = map.get(AdvertiserManager.SPEND_BY_ADVERTISER_ID);
            Map<Long, BigDecimal> balance = map.get(AdvertiserManager.BALANCE_BY_ADVERTISER_ID);

            System.out.println("Spend:");
            for(Entry<Long, BigDecimal> entry : spend.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
            System.out.println("Balance:");
            for(Entry<Long, BigDecimal> entry : balance.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    protected void testGetAllAgencyAdvertisersVisibleForUser(User user) {
        FetchStrategy advertiserFs = new FetchStrategyBuilder()
                                     .addInner(Advertiser_.company)
                                     .build();
        List<Advertiser> advertisers = null;
        String name = null;

        // Not passing any status will default to Active+Inactive
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user,(String) null, null, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
        }
        System.out.println("----------------------------------------------------------");
        // A status can be passed as a string, it will be converted to an actual Status object in the service layer
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, "ACTIVE", null, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
        }
        System.out.println("----------------------------------------------------------");
        // Actual status objects can be passed explicitely in a Collection
        List<Advertiser.Status> statuses = new ArrayList<Advertiser.Status>();
        statuses.add(Advertiser.Status.ACTIVE);
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, statuses, null, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
        }
        System.out.println("----------------------------------------------------------");
        // Testing a "contains" for the advertiser name
        name = "test";
        System.out.println("Contains \"" + name + "\"");
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, "ACTIVE", name, LikeSpec.CONTAINS, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
            assertTrue(advertiser.getName().toLowerCase().indexOf(name) != -1);
        }
        System.out.println("----------------------------------------------------------");
        // Testing a "starts with" for the advertiser name
        name = "aldrin";
        System.out.println("Starts with \"" + name + "\"");
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, "ACTIVE", name, LikeSpec.STARTS_WITH, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
            assertTrue(advertiser.getName().toLowerCase().startsWith(name));
        }
        System.out.println("----------------------------------------------------------");
        // Testing an "ends with" for the advertiser name
        name = "testing";
        System.out.println("Ends with \"" + name + "\"");
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user,
                "ACTIVE", name, LikeSpec.ENDS_WITH, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
            assertTrue(advertiser.getName().toLowerCase().endsWith(name));
        }
        System.out.println("----------------------------------------------------------");
        // If no LikeSpec is passed, the service layer will default to "starts with"
        name = "aldrin";
        System.out.println("Starts with \"" + name + "\"");
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, "ACTIVE", name, false, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
            assertTrue(advertiser.getName().toLowerCase().startsWith(name));
        }
        System.out.println("----------------------------------------------------------");
        // Testing a "name with space before" configuration. That is in addition to the containsName, so the results in this case
        // should translate to "starts with 'Testing' or contains ' Testing' 
        name = "testing";
        System.out.println("Name with space before \"" + name + "\"");
        advertisers = advertiserManager.getAllAgencyAdvertisersVisibleForUser(user, "ACTIVE", name, true, advertiserFs);
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + ", "
                    + advertiser.getCompany().getName() + ", "
                    + advertiser.getStatus().toString());
            assertEquals(user.getCompany(), advertiser.getCompany());
            assertEquals(advertiser.getStatus(), Advertiser.Status.ACTIVE);
            assertTrue(advertiser.getName().toLowerCase().startsWith(name) || advertiser.getName().toLowerCase().indexOf(" " + name) != -1);
        }
        System.out.println("----------------------------------------------------------");
    }
    
    @Test
    @Transactional
    public void testGetAllAgencyAdvertisersVisibleForUser() {
        try {
            // Admin user
            User user105 = userManager.getUserById(105L);
            if(user105 != null) {
                testGetAllAgencyAdvertisersVisibleForUser(user105);
            }
            System.out.println("==========================================================");
            User user261 = userManager.getUserById(261L);
            if(user261 != null) {
                testGetAllAgencyAdvertisersVisibleForUser(user261);
            }
            
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAdvertiserStoppageWithInvalidId() {
        assertNull(advertiserManager.getAdvertiserStoppageById(0L));
    }

    @Test
    public void testAdvertiserStoppage() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        AdvertiserStoppage.Reason reason = AdvertiserStoppage.Reason.ZERO_BALANCE;
        Date now = new Date();
        Date tomorrow = DateUtils.addDays(now, 1);
        AdvertiserStoppage stoppageWithReactivateDate = null;
        AdvertiserStoppage stoppageWithoutReactivateDate = null;
        try {
            stoppageWithReactivateDate = advertiserManager.newAdvertiserStoppage(advertiser, reason, tomorrow);
            assertNotNull(stoppageWithReactivateDate);
            long id = stoppageWithReactivateDate.getId();
            assertTrue(id > 0);
            assertEquals(reason, stoppageWithReactivateDate.getReason());

            stoppageWithReactivateDate = advertiserManager.getAdvertiserStoppageById(id);
            assertNotNull(stoppageWithReactivateDate);
            assertEquals(id, stoppageWithReactivateDate.getId());

            stoppageWithReactivateDate = advertiserManager.getAdvertiserStoppageById(Long.toString(id));
            assertNotNull(stoppageWithReactivateDate);
            assertEquals(id, stoppageWithReactivateDate.getId());

            List<AdvertiserStoppage> stoppages = advertiserManager.getAdvertiserStoppagesForNullOrFutureReactivateDate();
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithReactivateDate));

            stoppages = advertiserManager.getAdvertiserStoppagesForAdvertiserAndNullOrFutureReactivateDate(advertiser);
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithReactivateDate));

            stoppageWithoutReactivateDate = advertiserManager.newAdvertiserStoppage(advertiser, reason);
            assertTrue(stoppageWithoutReactivateDate.getId() > 0);
            assertEquals(reason, stoppageWithoutReactivateDate.getReason());

            stoppages = advertiserManager.getAdvertiserStoppagesForNullOrFutureReactivateDate();
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithoutReactivateDate));

            stoppages = advertiserManager.getAdvertiserStoppagesForAdvertiserAndNullOrFutureReactivateDate(advertiser);
            assertTrue(stoppages.size() > 0);
            assertTrue(stoppages.contains(stoppageWithoutReactivateDate));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            advertiserManager.delete(stoppageWithReactivateDate);
            assertNull(advertiserManager.getAdvertiserStoppageById(stoppageWithReactivateDate.getId()));

            advertiserManager.delete(stoppageWithoutReactivateDate);
            assertNull(advertiserManager.getAdvertiserStoppageById(stoppageWithoutReactivateDate.getId()));
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testAdvertiserNotificationFlag() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        NotificationFlag.Type type = NotificationFlag.Type.WENT_LIVE;
        int ttlSeconds = 15;
        AdvertiserNotificationFlag flag = null;
        AdvertiserNotificationFlag flagDeux = null;
        Date expirationDate = DateUtils.addDays(new Date(), 1);
        try {
            flag = advertiserManager.newAdvertiserNotificationFlag(advertiser, type, ttlSeconds);
            assertNotNull(flag);
            assertTrue(flag.getId() > 0);

            assertEquals(flag, advertiserManager.getAdvertiserNotificationFlagById(flag.getId()));
            assertEquals(flag, advertiserManager.getAdvertiserNotificationFlagById(Long.toString(flag.getId())));

            flagDeux = advertiserManager.newAdvertiserNotificationFlag(advertiser, type, expirationDate);
            assertNotNull(flagDeux);
            assertTrue(flagDeux.getId() > 0);
        } catch(Exception e) {
            fail("Failure detected: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (flag != null) {
                advertiserManager.delete(flag);
                assertNull(advertiserManager.getAdvertiserNotificationFlagById(flag.getId()));
            }
            if (flagDeux != null) {
                advertiserManager.delete(flagDeux);
                assertNull(advertiserManager.getAdvertiserNotificationFlagById(flagDeux.getId()));
            }
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------
    
    public void testAdvertiserLoad() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addInner(Advertiser_.account)
                               .build();
            
            Long companyId = 12276L;
            Company company = companyManager.getCompanyById(companyId);
            if(company == null) {
                System.out.println("Cannot load company");
                fail();
                return;
            }
            Iterator<Advertiser> iter = advertiserManager.getAllAdvertisersForCompany(company, fs).iterator();
            Advertiser advertiser = null;
            if (iter.hasNext()) {
                advertiser = iter.next();
                if(advertiser == null) {
                    System.out.println("Cannot load advertiser");
                    fail();
                    return;
                }
                if(advertiser.getAccount() == null) {
                    System.out.println("Account is null for advertiser " + advertiser.getId());
                    fail();
                    return;
                }
            }

            Account advertiserAccount = accountsManager.getAccountById(advertiser.getAccount().getId());
            if(advertiserAccount == null) {
                System.out.println("Cannot load account " + advertiser.getAccount().getId());
                fail();
                return;
            }
            System.out.println("TestAdvertiserLoad ran successfully for company " + companyId);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        }
    }
}
