package com.byyd.middleware.account.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Company;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import com.adfonic.domain.PrivateMarketPlaceDeal_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherRevShare;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class PublisherManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetPublisherWithInvalidId() {
        assertNull(publisherManager.getPublisherById(0L));
    }

    @Test
    public void testPublisher() {
        Country country = commonManager.getCountryByIsoCode("US");
        Company company = null;
        String companyName = "Company Testing";
        Publisher publisher = null;
        String publisherName = "Publisher Testing";
        BigDecimal revShare = BigDecimal.valueOf(0.3);
        try {
            company = companyManager.newCompany(companyName, country, null);
            assertNotNull(company);
            publisher = publisherManager.newPublisher(company, publisherName, revShare);
            assertNotNull(publisher);
            long id = publisher.getId();
            assertTrue(id > 0L);
            assertEquals(publisher.getCurrentPublisherRevShare().getRevShare().doubleValue(), revShare.doubleValue(), 0);

            assertEquals(publisher, publisherManager.getPublisherById(id));
            assertEquals(publisher, publisherManager.getPublisherById(Long.toString(id)));
            assertEquals(publisher, publisherManager.getPublisherByExternalId(publisher.getExternalID()));

            BigDecimal newRevShare = BigDecimal.valueOf(0.5);
            publisher = publisherManager.setCurrentRevShareForPublisher(publisher, newRevShare);
            publisher = publisherManager.getPublisherById(publisher.getId());
            assertEquals(publisher.getCurrentPublisherRevShare().getRevShare().doubleValue(), newRevShare.doubleValue(), 0);

            List<PublisherRevShare> revShares = publisherManager.getAllPublisherRevShareForPublisher(publisher, new Sorting(asc("id")));
            assertTrue(revShares.size() == 2);
            assertEquals(revShares.get(0).getRevShare().doubleValue(), revShare.doubleValue(), 0);
            assertEquals(revShares.get(1).getRevShare().doubleValue(), newRevShare.doubleValue(), 0);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            //if(publisher != null) {
            //    publisher = publisherManager.getPublisherById(publisher.getId());
            //   publisherManager.delete(publisher);
            //    assertNull(publisherManager.getPublisherById(publisher.getId()));
            //}
            //if(company != null) {
            //    accountsManager.delete(accountsManager.getCompanyById(company.getId()));
            //    assertNull(accountsManager.getCompanyById(company.getId()));
            //}
         }
    }

    @Test
    public void testPublisherApprovedCreatives() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Publisher.class, "approvedCreatives", JoinType.LEFT);
        Publisher publisher = publisherManager.getPublisherById(1L, fs);
        List<Long> creativeIds = new ArrayList<Long>();
        for(long i = 1L;i <= 10L;i++) {
            Creative creative = creativeManager.getCreativeById(i);
            if(creative != null) {
                publisher.getApprovedCreatives().add(creative);
                creativeIds.add(i);
            }
        }
        publisher = publisherManager.update(publisher);
        publisher = publisherManager.getPublisherById(publisher.getId(), fs);
        for(Long id : creativeIds) {
            Creative creative = creativeManager.getCreativeById(id);
            assertNotNull(creative);
            assertTrue(publisher.getApprovedCreatives().contains(creative));
        }

        Long id = creativeIds.get(0);
        Creative creative = creativeManager.getCreativeById(id);
        publisher.getApprovedCreatives().remove(creative);
        publisher = publisherManager.update(publisher);

        publisher = publisherManager.getPublisherById(publisher.getId(), fs);
        assertFalse(publisher.getApprovedCreatives().contains(creative));

    }

    @Test
    public void testPublisherApprovedCreativesDeux() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Publisher.class, "approvedCreatives", JoinType.LEFT);
        Publisher publisher = publisherManager.getPublisherById(1L, fs);
        List<Long> creativeIds = new ArrayList<Long>();
        for(long i = 1L;i <= 10L;i++) {
            Creative creative = creativeManager.getCreativeById(i);
            if(creative != null) {
                publisher.getApprovedCreatives().add(creative);
                creativeIds.add(i);
            }
        }
        publisher = publisherManager.update(publisher);
        publisher = publisherManager.getPublisherById(publisher.getId(), fs);

        for(long i = 1L;i <= 10L;i++) {
            Creative creative = creativeManager.getCreativeById(i);
            if(creative != null) {
                publisher.getApprovedCreatives().add(creative);
                break;
            }
        }
        publisher = publisherManager.update(publisher);
        publisher = publisherManager.getPublisherById(publisher.getId(), fs);

        for(Long id : creativeIds) {
            Creative creative = creativeManager.getCreativeById(id);
            assertNotNull(creative);
            assertTrue(publisher.getApprovedCreatives().contains(creative));
        }

        Long id = creativeIds.get(0);
        Creative creative = creativeManager.getCreativeById(id);
        publisher.getApprovedCreatives().remove(creative);
        publisher = publisherManager.update(publisher);

        publisher = publisherManager.getPublisherById(publisher.getId(), fs);
        assertFalse(publisher.getApprovedCreatives().contains(creative));

    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testPrivateMarketPlaceDeals() {
        PrivateMarketPlaceDeal deal = null;
        FetchStrategy fs = new FetchStrategyBuilder()
                               .addInner(PrivateMarketPlaceDeal_.publisher)
                               .build();
        try {
            Publisher publisher = publisherManager.getPublisherById(1L);
            String dealId = UUID.randomUUID().toString();
            AuctionType auctionType = AuctionType.FIRST_PRICE_AUCTION;
            BigDecimal amount = new BigDecimal(0.5);
            
            deal = publisherManager.newPrivateMarketPlaceDeal(publisher, dealId, auctionType, amount);
            deal = publisherManager.getPrivateMarketPlaceDealById(deal.getId(), fs);
            assertEquals(deal.getPublisher(), publisher);
            assertEquals(deal.getDealId(), dealId);
            assertEquals(deal.getAuctionType(), auctionType);
            assertEquals(new Double(deal.getAmount().doubleValue()), new Double(amount.doubleValue()));
            
            assertEquals(deal, publisherManager.getPrivateMarketPlaceDealByPublisherAndDealId(publisher, dealId));
            
            deal.setAuctionType(AuctionType.SECOND_PRICE_ACTION);
            publisherManager.update(deal);
            deal = publisherManager.getPrivateMarketPlaceDealById(deal.getId(), fs);
            assertEquals(deal.getAuctionType(), AuctionType.SECOND_PRICE_ACTION);
            
            assertTrue(publisherManager.getAllPrivateMarketPlaceDealForPublisher(publisher).contains(deal));
          } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(deal != null) {
                publisherManager.delete(deal);
                assertNull(publisherManager.getPrivateMarketPlaceDealById(deal.getId()));
            }
        }
    }
}
