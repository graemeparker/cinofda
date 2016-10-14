package com.byyd.middleware.account.service;

import static com.adfonic.domain.Role.COMPANY_ROLE_PREPAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserMediaCostMargin;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.IpAddressRange;
import com.adfonic.domain.MarginShareDSP;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.OptimisationReportCompanyPreferences_;
import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class CompanyManagerIT {
    
    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private CommonManager commonManager;

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetCompanyByIdWithInvalidId() {
        assertNull(companyManager.getCompanyById(0L));
    }

    @Test
    @Transactional
    public void testCompany() {
        Country country = commonManager.getCountryByIsoCode("US");
        Company company = null;
        try {
            // Get default currency exchange rate
            CurrencyExchangeRatesFilter currencyExchangeRatesFilter = new CurrencyExchangeRatesFilter().setDefaultConversion(true);
            List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getCurrencyExchangeRates(currencyExchangeRatesFilter);
            
            company = companyManager.newCompany("Middleware Test Company " + System.currentTimeMillis(), country, currencyExchangeRates.get(0));
            assertNotNull(company);
            long id = company.getId();
            assertTrue(id > 0);
            assertEquals(country, company.getCountry());

            company = companyManager.getCompanyById(company.getId());
            assertNotNull(company);
            assertEquals(id, company.getId());

            company = companyManager.getCompanyById(Long.toString(company.getId()));
            assertNotNull(company);
            assertEquals(id, company.getId());

            String newName = company.getName() + " modified";
            company.setName(newName);
            companyManager.update(company);
            company = companyManager.getCompanyById(company.getId());
            assertEquals(newName, company.getName());

            assertEquals(company, companyManager.getCompanyByExternalId(company.getExternalID()));
            assertEquals(company, companyManager.getCompanyByName(company.getName()));

            company = companyManager.getCompanyById(company.getId(), 
                    new FetchStrategyBuilder()
                        .addInner(Company_.roles)
                        .build());
            assertTrue(company.getRoles().contains(userManager.getRoleByName(COMPANY_ROLE_PREPAY)));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            companyManager.delete(company);
            assertNull(companyManager.getCompanyById(company.getId()));
        }
    }

    @Test
    public void testGetCompanyMessageByIdWithInvalidId() {
        assertNull(companyManager.getCompanyMessageById(0L));
    }

    @Test
    @Transactional
    public void testCompanyMessage() {
        String systemName = "Testing";
        Company company = companyManager.getCompanyById(2L);
        Publisher publisher = publisherManager.getPublisherById(1L);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        Campaign campaign = campaignManager.getCampaignById(1L);
        List<String> systemNames = new ArrayList<String>();
        systemNames.add(systemName);

        CompanyMessage message = null;
        try {
            message = companyManager.newCompanyMessage(company, advertiser, publisher, systemName);
            assertNotNull(message);
            long id = message.getId();
            assertTrue(id > 0);
            assertEquals(company, message.getCompany());
            assertEquals(publisher, message.getPublisher());
            assertEquals(advertiser, message.getAdvertiser());
            assertEquals(systemName, message.getSystemName());

            message = companyManager.getCompanyMessageById(id);
            assertNotNull(message);
            assertEquals(id, message.getId());

            message = companyManager.getCompanyMessageById(Long.toString(id));
            assertNotNull(message);
            assertEquals(id, message.getId());

            List<CompanyMessage> advertiserMessages = companyManager.getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames);
            assertTrue(CollectionUtils.isNotEmpty(advertiserMessages) && advertiserMessages.contains(message));
            assertTrue(companyManager.countCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames) > 0);

            List<CompanyMessage> publisherMessages = companyManager.getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames);
            assertTrue(CollectionUtils.isNotEmpty(publisherMessages) && publisherMessages.contains(message));
            assertTrue(companyManager.countCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames) > 0);

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            // No setters on CompanyMessage. Cant test any updates.
            companyManager.delete(message);
            assertNull(companyManager.getCompanyMessageById(message.getId()));
        }

        try {
            message = companyManager.newCompanyMessage(campaign, systemName);
            assertNotNull(message);
            long id = message.getId();
            assertTrue(id > 0);

            message = companyManager.getCompanyMessageById(id);
            assertNotNull(message);
            assertEquals(id, message.getId());

            message = companyManager.getCompanyMessageById(Long.toString(id));
            assertNotNull(message);
            assertEquals(id, message.getId());

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            // No setters on CompanyMessage. Cant test any updates.
            companyManager.delete(message);
            assertNull(companyManager.getCompanyMessageById(message.getId()));
        }
    }

    @Test
    @Transactional
    public void testCompanyCreation() {
       FetchStrategy fs = new FetchStrategyBuilder()
                          .addLeft(Company_.currentMarginShareDSP)
                          .build();
       Country country = commonManager.getCountryByIsoCode("US");
       Company company = null;
       try {
           // Get default currency exchange rate
           CurrencyExchangeRatesFilter currencyExchangeRatesFilter = new CurrencyExchangeRatesFilter().setDefaultConversion(true);
           List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getCurrencyExchangeRates(currencyExchangeRatesFilter);
           
           company = companyManager.newCompany("Middleware Test Company " + System.currentTimeMillis(), country, currencyExchangeRates.get(0));
           assertNotNull(company);
           long id = company.getId();
           assertTrue(id > 0);
           assertEquals(country, company.getCountry());

           company = companyManager.getCompanyById(company.getId(), fs);
           assertNotNull(company);
           assertEquals(id, company.getId());
           assertEquals(new Double(company.getCurrentMarginShareDSP().getMargin().doubleValue()), new Double(MarginShareDSP.DEFAULT_MARGIN_SHARE_DSP)); 
          } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    //----------------------------------------------------------------------------------------------------------

    @Test
    @Transactional
    public void testMarginShareDSP() {
        FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Company_.currentMarginShareDSP)
                               .addLeft(Company_.historicalMarginShareDSPs)
                               .build();
        Company company = companyManager.getCompanyById(2L, fs);
        if(company != null) {
            double margin = 0.33;
            MarginShareDSP marginShareDSP = null;
            try {
                company = companyManager.newMarginShareDSP(company, BigDecimal.valueOf(margin));
                marginShareDSP = company.getCurrentMarginShareDSP();
                assertNotNull(marginShareDSP);
                long id = marginShareDSP.getId();
                assertTrue(id > 0L);
   
//                marginShareDSP = companyManager.getMarginShareDSPById(id);
//                assertNotNull(marginShareDSP);
//                assertEquals(id, marginShareDSP.getId());
   
//                marginShareDSP = companyManager.getMarginShareDSPById(Long.toString(id));
//                assertNotNull(marginShareDSP);
//                assertEquals(id, marginShareDSP.getId());
   
                company = companyManager.getCompanyById(2L, fs);
                assertTrue(company.getHistoricalMarginShareDSPs().contains(marginShareDSP));
                
//                List<MarginShareDSP> list = companyManager.getAllMarginShareDSPsForCompany(company);
//                assertTrue(list.contains(marginShareDSP));
   
            } catch(Exception e) {
                String stackTrace = ExceptionUtils.getStackTrace(e);
                System.out.println(stackTrace);
                fail(stackTrace);
            } finally {
            }
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    @Transactional
    public void testIpAddressRange() {
        long ipStartPoint = 3397140480L;
        long ipEndPoint = 3397148671L;
        IpAddressRange ipAddressRange = null;
        try {
            ipAddressRange = new IpAddressRange(ipStartPoint, ipEndPoint);
            ipAddressRange = companyManager.create(ipAddressRange);
            
            assertNotNull(ipAddressRange);
            long id = ipAddressRange.getId();
            assertTrue(id > 0);
                         
            assertEquals(ipAddressRange, companyManager.getIpAddressRangeById(id));

            long newStartPoint = 1370045440L;
            long newEndPoint = 1370046463L;
            long ipContained = 1370045454L;
            long ipNotContained = 1370079580L;
            ipAddressRange.setStartPoint(newStartPoint);
            ipAddressRange.setEndPoint(newEndPoint);
            ipAddressRange = companyManager.update(ipAddressRange);
            ipAddressRange = companyManager.getIpAddressRangeById(id);
            assertEquals(newStartPoint, ipAddressRange.getStartPoint());
            assertEquals(newEndPoint, ipAddressRange.getEndPoint());
            
            Company company = companyManager.getCompanyById(2L);
            company.getIpAddressRanges().add(ipAddressRange);
            company = companyManager.update(company);
            
            company = companyManager.getCompanyById(2L);
            assertFalse(company.getIpAddressRanges().isEmpty());
            boolean isContained = false;
            for(IpAddressRange iar : company.getIpAddressRanges()){
                if(iar.getId()==ipAddressRange.getId()){
                    isContained=true;
                }
            }
            assertTrue(isContained);
            
            assertTrue(companyManager.isIpInWhiteList(ipContained, 2L));
            assertFalse(companyManager.isIpInWhiteList(ipNotContained, 2L));
            
            company.getIpAddressRanges().remove(ipAddressRange);
            company = companyManager.update(company);
            
            company = companyManager.getCompanyById(2L);
            isContained = false;
            for(IpAddressRange iar : company.getIpAddressRanges()){
                if(iar.getId()==ipAddressRange.getId()){
                    isContained=true;
                }
            }
            assertFalse(isContained);
            
            company.getIpAddressRanges().clear();
            company = companyManager.update(company);
            //With no ranges all ips should be accepted
            assertTrue(companyManager.isIpInWhiteList(ipContained, 2L));
            assertTrue(companyManager.isIpInWhiteList(ipNotContained, 2L));
            
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            companyManager.delete(ipAddressRange);
            assertNull(companyManager.getIpAddressRangeById(ipAddressRange.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    @Transactional
    public void testAdvertiserMediaCostMargin() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Company_.currentMediaCostMargin)
                           .addLeft(Company_.historicalMediaCostMargins)
                           .build();
        Company company = companyManager.getCompanyById(2L, fs);
        double amount = 1.00;
        AdvertiserMediaCostMargin margin = null;
        try {
            company = companyManager.newAdvertiserMediaCostMargin(company, BigDecimal.valueOf(amount));
            margin = company.getCurrentMediaCostMargin();
            assertNotNull(margin);
            long id = margin.getId();
            assertTrue(id > 0L);

            margin = companyManager.getAdvertiserMediaCostMarginById(id);
            assertNotNull(margin);
            assertEquals(id, margin.getId());

            company = companyManager.getCompanyById(2L, fs);
            assertTrue(company.getHistoricalMediaCostMargins().contains(margin));
            
            List<AdvertiserMediaCostMargin> list = companyManager.getAllAdvertiserMediaCostMarginsForCompany(company);
            assertTrue(list.contains(margin));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    @Transactional
    public void testOptimisationReportCompanyPreferences() {
        OptimisationReportCompanyPreferences prefs = null;
        List<OptimisationReportFields> targetFields = new ArrayList<>();
        targetFields.add(OptimisationReportFields.PID);
        targetFields.add(OptimisationReportFields.IAB_CATEGORY);
        targetFields.add(OptimisationReportFields.ECPC);
        try {
            Company company = companyManager.getCompanyById(2L);
            Set<OptimisationReportFields> fields = new HashSet<>();
            fields.addAll(targetFields);
            prefs = companyManager.newOptimisationReportCompanyPreferences(company, fields);
            assertNotNull(prefs);
            assertTrue(prefs.getId() > 0);
            prefs = companyManager.getOptimisationReportCompanyPreferencesById(prefs.getId(), new FetchStrategyBuilder().addInner(OptimisationReportCompanyPreferences_.company).build());
            assertEquals(prefs.getCompany(), company);
            for(OptimisationReportFields field : targetFields) {
                assertTrue(prefs.getReportFields().contains(field));
            }
            OptimisationReportFields newField = OptimisationReportFields.INVENTORY_SOURCE;
            prefs.getReportFields().add(newField);
            prefs = companyManager.update(prefs);
            prefs = companyManager.getOptimisationReportCompanyPreferencesById(prefs.getId());
            assertTrue(prefs.getReportFields().contains(newField));
            prefs.getReportFields().clear();
            prefs.getReportFields().add(newField);
            prefs = companyManager.update(prefs);
            prefs = companyManager.getOptimisationReportCompanyPreferencesById(prefs.getId());
            assertTrue(prefs.getReportFields().contains(newField));
            for(OptimisationReportFields field : targetFields) {
                assertFalse(prefs.getReportFields().contains(field));
            }
            
            assertEquals(prefs, companyManager.getOptimisationReportCompanyPreferencesForCompany(company));
            
            try {
                companyManager.newOptimisationReportCompanyPreferences(company, fields);
                fail("We were able to create a second OptimisationReportCompanyPreferences for the same company");
            } catch(Exception e) {
            }
            
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            companyManager.delete(prefs);
            assertNull(companyManager.getOptimisationReportCompanyPreferencesById(prefs.getId()));
        }
    }
    

}
