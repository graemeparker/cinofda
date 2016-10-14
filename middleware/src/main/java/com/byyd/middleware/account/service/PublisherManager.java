package com.byyd.middleware.account.service;

import java.math.BigDecimal;
import java.util.List;

import com.adfonic.domain.Company;
import com.adfonic.domain.Creative;
import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherRevShare;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.TargetPublisher;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;


public interface PublisherManager extends BaseManager {

    //------------------------------------------------------------------------------------------
    // Publisher
    //------------------------------------------------------------------------------------------
    Publisher newPublisher(Company company, String name, BigDecimal revShare, FetchStrategy... fetchStrategy);
    Publisher setCurrentRevShareForPublisher(Publisher publisher, BigDecimal revShare);

    Publisher getPublisherById(String id, FetchStrategy... fetchStrategy);
    Publisher getPublisherById(Long id, FetchStrategy... fetchStrategy);
    Publisher create(Publisher publisher);
    Publisher update(Publisher publisher);
    void delete(Publisher publisher);
    void deletePublishers(List<Publisher> list);

    Publisher getPublisherByExternalId(String externalId, FetchStrategy... fetchStrategy);
    Publisher getPublisherByName(String name, FetchStrategy... fetchStrategy);

    RateCard getEcpmTargetRateCardForPublisher(Publisher publisher);

    //------------------------------------------------------------------------------------------
    // PublisherRevShare
    //------------------------------------------------------------------------------------------
    PublisherRevShare getPublisherRevShareById(String id, FetchStrategy... fetchStrategy);
    PublisherRevShare getPublisherRevShareById(Long id, FetchStrategy... fetchStrategy);
    PublisherRevShare create(PublisherRevShare publisherRevShare);
    PublisherRevShare update(PublisherRevShare publisherRevShare);
    void delete(PublisherRevShare publisherRevShare);
    void deletePublisherRevShares(List<PublisherRevShare> list);

    Long countAllPublisherRevShareForPublisher(Publisher publisher);
    List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // PublisherAuditedCreative
    //------------------------------------------------------------------------------------------
    PublisherAuditedCreative create(PublisherAuditedCreative publisherAuditedCreative);
    PublisherAuditedCreative update(PublisherAuditedCreative publisherAuditedCreative);
    PublisherAuditedCreative getPublisherAuditedCreativeByPublisherAndCreative(Publisher publisher, Creative creative, FetchStrategy ... fetchStrategy);
    List<PublisherAuditedCreative> getAllPublisherAuditedCreativesForCreative(Creative creative, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // TargetPublisher
    //------------------------------------------------------------------------------------------
    TargetPublisher getTargetPublisherByName(String name, FetchStrategy... fetchStrategy);
    TargetPublisher getTargetPublisherById(long id, FetchStrategy ... fetchStrategy);
    TargetPublisher update(TargetPublisher targetPublisher);
    void delete(TargetPublisher targetPublisher);
    Long countAllTargetPublishers(TargetPublisherFilter filter);
    List<TargetPublisher> getAllTargetPublishers(TargetPublisherFilter filter, FetchStrategy... fetchStrategy);
    List<TargetPublisher> getAllTargetPublishers(TargetPublisherFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    TargetPublisher getTargetPublisherByPublisherId(TargetPublisherFilter filter, FetchStrategy... fetchStrategy);
    boolean isBidSeatAvailabe(BidSeat bidSeat);
    List<BidSeat> generateBidSeats(List<BidSeat> bidSeats);
    
    //------------------------------------------------------------------------------------------
    // PrivateMarketPlaceDeal
    //------------------------------------------------------------------------------------------

    PrivateMarketPlaceDeal newPrivateMarketPlaceDeal(Publisher publisher, String dealId, AuctionType auctionType, BigDecimal amount, FetchStrategy... fetchStrategy);
    
    PrivateMarketPlaceDeal getPrivateMarketPlaceDealById(String id, FetchStrategy... fetchStrategy);
    PrivateMarketPlaceDeal getPrivateMarketPlaceDealById(Long id, FetchStrategy... fetchStrategy);
    PrivateMarketPlaceDeal create(PrivateMarketPlaceDeal privateMarketPlaceDeal);
    PrivateMarketPlaceDeal update(PrivateMarketPlaceDeal privateMarketPlaceDeal);
    void delete(PrivateMarketPlaceDeal privateMarketPlaceDeal);
    void deletePrivateMarketPlaceDeals(List<PrivateMarketPlaceDeal> list);
    
    PrivateMarketPlaceDeal getPrivateMarketPlaceDealByPublisherAndDealId(Publisher publisher, String dealId, FetchStrategy... fetchStrategy);
    
    Number countAllPrivateMarketPlaceDeals();
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(Sorting sort, FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(Pagination page, FetchStrategy... fetchStrategy);

    Long countAllPrivateMarketPlaceDealForPublisher(Publisher publisher);
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);

 
}
