package com.byyd.middleware.account.service.jpa;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Company;
import com.adfonic.domain.Creative;
import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import com.adfonic.domain.TargetPublisher.RtbSeatIdFormat;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherRevShare;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.TargetPublisher;
import com.byyd.middleware.account.dao.BidSeatDao;
import com.byyd.middleware.account.dao.PrivateMarketPlaceDealDao;
import com.byyd.middleware.account.dao.PublisherAuditedCreativeDao;
import com.byyd.middleware.account.dao.PublisherDao;
import com.byyd.middleware.account.dao.PublisherRevShareDao;
import com.byyd.middleware.account.dao.TargetPublisherDao;
import com.byyd.middleware.account.filter.BidSeatFilter;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("publisherManager")
public class PublisherManagerJpaImpl extends BaseJpaManagerImpl implements PublisherManager {
    
    private static final int MAX_SEAT_ID_AUTOGEN_TRIES = 10; 

    @Autowired(required=false)
    private PublisherDao publisherDao;
    
    @Autowired(required=false)
    private PublisherRevShareDao publisherRevShareDao;
    
    @Autowired(required=false)
    private PublisherAuditedCreativeDao publisherAuditedCreativeDao;
    
    @Autowired(required=false)
    private TargetPublisherDao targetPublisherDao; 
    
    @Autowired(required = false)
    private PrivateMarketPlaceDealDao privateMarketPlaceDealDao;
    
    @Autowired(required = false)
    private BidSeatDao bidSeatDao;
    
    //------------------------------------------------------------------------------------------
    // Publisher
    //------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=false)
    public Publisher newPublisher(Company company, String name, BigDecimal revShare, FetchStrategy... fetchStrategy) {
        AccountManager accountsManager = AdfonicBeanDispatcher.getBean(AccountManager.class);

        Publisher publisher = new Publisher(company, name);
        accountsManager.create(publisher.getAccount());
        publisher = create(publisher);
        publisher = setCurrentRevShareForPublisher(publisher, revShare);

        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return publisher;
        } else {
            return getPublisherById(company.getPublisher().getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public Publisher setCurrentRevShareForPublisher(Publisher publisher, BigDecimal revShare) {
        if (revShare == null || (revShare.compareTo(BigDecimal.ZERO) < 0) || (revShare.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException();
        }

        Date now = new Date();

        PublisherRevShare currentRevShare = publisher.getCurrentPublisherRevShare();
        if (currentRevShare != null) {
            // Bail if the rev share isn't changing
            if (revShare.equals(currentRevShare.getRevShare())) {
                return publisher;
            } else {
                // End the previously current rev share
                currentRevShare.setEndDate(now);
                update(currentRevShare);
            }
        }

        // Replace the current rev share and add it to the history
        PublisherRevShare newRevShare = publisher.newPublisherRevShare(revShare, now);
        newRevShare = create(newRevShare);
        publisher.setCurrentRevShare(newRevShare);
        publisher.getRevShareHistory().add(newRevShare);
        return update(publisher);
    }

    @Override
    @Transactional(readOnly=true)
    public Publisher getPublisherById(String id, FetchStrategy... fetchStrategy) {
        return getPublisherById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Publisher getPublisherById(Long id, FetchStrategy... fetchStrategy) {
        return publisherDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public Publisher create(Publisher publisher) {
        return publisherDao.create(publisher);
    }

    @Override
    @Transactional(readOnly=false)
    public Publisher update(Publisher publisher) {
        return publisherDao.update(publisher);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(Publisher publisher) {
        publisher.setCurrentRevShare(null);
        Publisher dbPublisher = update(publisher);
        deletePublisherRevShares(this.getAllPublisherRevShareForPublisher(dbPublisher));
        publisherDao.delete(dbPublisher);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePublishers(List<Publisher> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(Publisher publisher : list) {
            delete(publisher);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Publisher getPublisherByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return publisherDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Publisher getPublisherByName(String name, FetchStrategy... fetchStrategy) {
        return publisherDao.getByName(name, fetchStrategy);
    }

    /*
     * This method exists because combining hydration of
     * Publisher.ecpmTargetRateCard with RateCard.minimumBidMap
     * causes hibernate to NPE. This has something to do with how
     * minimumBidMap is annotated but we haven't found a fix.
     *
     * So to get a hydrated ecpmTargetRate card we access it via
     * publisher and further access a property.
     *
     */
    @Override
    @Transactional(readOnly=true)
    public RateCard getEcpmTargetRateCardForPublisher(Publisher publisher) {
        Publisher dbPublisher = this.getPublisherById(publisher.getId());
        if (dbPublisher.getEcpmTargetRateCard() != null) {
            dbPublisher.getEcpmTargetRateCard().getDefaultMinimum();
        }
        return dbPublisher.getEcpmTargetRateCard();
    }
    
    //------------------------------------------------------------------------------------------
    // PublisherRevShare
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public PublisherRevShare getPublisherRevShareById(String id, FetchStrategy... fetchStrategy) {
        return getPublisherRevShareById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PublisherRevShare getPublisherRevShareById(Long id, FetchStrategy... fetchStrategy) {
        return publisherRevShareDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PublisherRevShare create(PublisherRevShare publisherRevShare) {
        return publisherRevShareDao.create(publisherRevShare);
    }

    @Override
    @Transactional(readOnly=false)
    public PublisherRevShare update(PublisherRevShare publisherRevShare) {
        return publisherRevShareDao.update(publisherRevShare);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PublisherRevShare publisherRevShare) {
        publisherRevShareDao.delete(publisherRevShare);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePublisherRevShares(List<PublisherRevShare> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PublisherRevShare publisherRevShare : list) {
            delete(publisherRevShare);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllPublisherRevShareForPublisher(Publisher publisher) {
        return publisherRevShareDao.countAllForPublisher(publisher);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return publisherRevShareDao.getAllForPublisher(publisher, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return publisherRevShareDao.getAllForPublisher(publisher, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublisherRevShare> getAllPublisherRevShareForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return publisherRevShareDao.getAllForPublisher(publisher, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // PublisherAuditedCreative
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public PublisherAuditedCreative create(PublisherAuditedCreative publisherAuditedCreative) {
        return publisherAuditedCreativeDao.create(publisherAuditedCreative);
    }

    @Override
    @Transactional(readOnly=false)
    public PublisherAuditedCreative update(PublisherAuditedCreative publisherAuditedCreative) {
        return publisherAuditedCreativeDao.update(publisherAuditedCreative);
    }

    @Override
    @Transactional(readOnly=true)
    public PublisherAuditedCreative getPublisherAuditedCreativeByPublisherAndCreative(Publisher publisher, Creative creative, FetchStrategy ... fetchStrategy) {
        return publisherAuditedCreativeDao.getByPublisherAndCreative(publisher, creative, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<PublisherAuditedCreative> getAllPublisherAuditedCreativesForCreative(Creative creative, FetchStrategy... fetchStrategy) {
        return publisherAuditedCreativeDao.getByCreative(creative, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // TargetPublisher
    // ------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly = true)
    public TargetPublisher getTargetPublisherByName(String name, FetchStrategy... fetchStrategy) {
        return targetPublisherDao.getByName(name, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public TargetPublisher getTargetPublisherById(long id, FetchStrategy ... fetchStrategy) {
        return targetPublisherDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public TargetPublisher update(TargetPublisher targetPublisher) {
        return targetPublisherDao.update(targetPublisher);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(TargetPublisher targetPublisher) {
        targetPublisherDao.delete(targetPublisher);
    }

    @Transactional(readOnly = false)
    public TargetPublisher create(TargetPublisher targetPublisher) {
        return targetPublisherDao.create(targetPublisher);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAllTargetPublishers(TargetPublisherFilter filter) {
        return targetPublisherDao.countAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TargetPublisher> getAllTargetPublishers(TargetPublisherFilter filter, FetchStrategy... fetchStrategy) {
        return targetPublisherDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetPublisher> getAllTargetPublishers(TargetPublisherFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return targetPublisherDao.getAll(filter, sort, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TargetPublisher getTargetPublisherByPublisherId(TargetPublisherFilter filter, FetchStrategy... fetchStrategy) {
        List<TargetPublisher> list = targetPublisherDao.getAll(filter, fetchStrategy);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }
    
    @Override
    @Transactional(readOnly = false)
    public boolean isBidSeatAvailabe(BidSeat bidSeat){
        BidSeatFilter filter = new BidSeatFilter();
        filter.setSeatId(bidSeat.getSeatId());
        filter.setTargetPublisher(bidSeat.getTargetPublisher());
        Long count = bidSeatDao.countAll(filter);
        boolean isAvailable = false;
        if (count==0){
            isAvailable = true;
        }
        return isAvailable;
    }
    
    @Override
    @Transactional(readOnly = false)
    public List<BidSeat> generateBidSeats(List<BidSeat> bidSeats){
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(bidSeats)){
            for (BidSeat bidSeat : bidSeats){
                int tries = 0;
                boolean finish = false;
                
                while(!finish && tries<MAX_SEAT_ID_AUTOGEN_TRIES){
                    // Create seat id
                    String autogenSeatId = generateSeatId(bidSeat.getTargetPublisher().getId());
                    bidSeat.setSeatId(autogenSeatId);
                    
                    // Check if seat id already exists
                    if (isBidSeatAvailabe(bidSeat)){
                        finish = true;
                    }else if (bidSeat.getTargetPublisher().getRtbSeatIdAutogenFormat() == RtbSeatIdFormat.ALPHANUMERIC){
                        //only increase number of tries for seat id alphanumeric format. For numeric only we rely on counter
                        tries++;
                    }
                }
                
                if (tries==MAX_SEAT_ID_AUTOGEN_TRIES){
                    throw new RuntimeException("Can not generate an unique seat id for publisher " + bidSeat.getTargetPublisher().getName() + ". It has been tried " + MAX_SEAT_ID_AUTOGEN_TRIES + " times.");
                }
            }
        }
        return bidSeats;
    }
    
    private String generateSeatId(Long targetPublisherId){
        String seatId = null;
        TargetPublisher targetPublisher = targetPublisherDao.getById(targetPublisherId);
        if (targetPublisher!=null){
            Long max = targetPublisher.getRtbSeatIdAutogenMax();
            
            switch (targetPublisher.getRtbSeatIdAutogenFormat()) {
                case ALPHANUMERIC:
                    // Generate UUID without dashes (32 chars)
                    seatId = UUID.randomUUID().toString().replaceAll("-", "");
                    
                    // If max length is less than 32 we apply CRC32 hash in order to reduce the number of chars to 32bits in HEX format
                    if (max!=null && max<32){
                        Checksum checksum = new CRC32();
                        checksum.update(seatId.getBytes(),0,seatId.getBytes().length);
                        seatId = Long.toHexString(checksum.getValue());
                    }
                    break;
        
                default: // NUMERIC case
                    if (max==null){
                        throw new RuntimeException("Max value for numeric seat id is not set in Database for exchange " + targetPublisher.getName() + ". Set a max value for this counter.");
                    }
                    
                    Long counter = targetPublisher.getRtbSeatIdAutogenCounter();
                    if (counter!=null){
                        if (counter<max){
                            targetPublisher.setRtbSeatIdAutogenCounter(counter+1);
                            targetPublisherDao.update(targetPublisher);
                        }else{
                            throw new RuntimeException("Autogeneration counter has reach its end for exchange " + targetPublisher.getName() + ". The end of the world is near, run!");
                        }
                    }
                    
                    // Assign current counter to seat id
                    seatId = String.valueOf(counter);
                    break;
            }
        }
        
        return seatId;
    }
    
    //------------------------------------------------------------------------------------------
    // PrivateMarketPlaceDeal
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=false)
    public PrivateMarketPlaceDeal newPrivateMarketPlaceDeal(Publisher publisher, String dealId, AuctionType auctionType, BigDecimal amount, FetchStrategy... fetchStrategy) {
        PrivateMarketPlaceDeal privateMarketPlaceDeal = new PrivateMarketPlaceDeal(publisher, dealId, auctionType, amount);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(privateMarketPlaceDeal);
        } else {
            privateMarketPlaceDeal = create(privateMarketPlaceDeal);
            return getPrivateMarketPlaceDealById(privateMarketPlaceDeal.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PrivateMarketPlaceDeal getPrivateMarketPlaceDealById(String id, FetchStrategy... fetchStrategy) {
        return getPrivateMarketPlaceDealById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PrivateMarketPlaceDeal getPrivateMarketPlaceDealById(Long id, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PrivateMarketPlaceDeal create(PrivateMarketPlaceDeal privateMarketPlaceDeal) {
        return privateMarketPlaceDealDao.create(privateMarketPlaceDeal);
    }

    @Override
    @Transactional(readOnly=false)
    public PrivateMarketPlaceDeal update(PrivateMarketPlaceDeal privateMarketPlaceDeal) {
        return privateMarketPlaceDealDao.update(privateMarketPlaceDeal);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PrivateMarketPlaceDeal privateMarketPlaceDeal) {
        privateMarketPlaceDealDao.delete(privateMarketPlaceDeal);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePrivateMarketPlaceDeals(List<PrivateMarketPlaceDeal> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PrivateMarketPlaceDeal privateMarketPlaceDeal : list) {
            delete(privateMarketPlaceDeal);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PrivateMarketPlaceDeal getPrivateMarketPlaceDealByPublisherAndDealId(Publisher publisher, String dealId, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getByPublisherAndDealId(publisher, dealId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Number countAllPrivateMarketPlaceDeals() {
        return privateMarketPlaceDealDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(Sorting sort, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDeals(Pagination page, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllPrivateMarketPlaceDealForPublisher(Publisher publisher) {
        return privateMarketPlaceDealDao.countAllForPublisher(publisher);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAllForPublisher(publisher, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAllForPublisher(publisher, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PrivateMarketPlaceDeal> getAllPrivateMarketPlaceDealForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return privateMarketPlaceDealDao.getAllForPublisher(publisher, page, fetchStrategy);
    }

}
