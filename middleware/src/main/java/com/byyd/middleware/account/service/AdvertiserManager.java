package com.byyd.middleware.account.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserNotificationFlag;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Company;
import com.adfonic.domain.NotificationFlag.Type;
import com.adfonic.domain.User;
import com.byyd.middleware.account.dao.AdvertiserDao;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;


public interface AdvertiserManager extends BaseManager {
    
    static final String SPEND_BY_ADVERTISER_ID = AdvertiserDao.SPEND_BY_ADVERTISER_ID;
    static final String BALANCE_BY_ADVERTISER_ID = AdvertiserDao.BALANCE_BY_ADVERTISER_ID;

    //------------------------------------------------------------------------------------------
    // Advertiser
    //------------------------------------------------------------------------------------------
    Advertiser newAdvertiser(Company company, String name, FetchStrategy... fetchStrategy);

    Advertiser getAdvertiserById(String id, FetchStrategy... fetchStrategy);
    Advertiser getAdvertiserById(Long id, FetchStrategy... fetchStrategy);
    //public Advertiser create(Advertiser advertiser);
    Advertiser update(Advertiser advertiser);
    void delete(Advertiser advertiser);
    void deleteAdvertisers(List<Advertiser> list);

    Advertiser getAdvertiserByExternalId(String externalID, FetchStrategy... fetchStrategy);
    Advertiser getAdvertiserByName(String name, Company company, FetchStrategy... fetchStrategy);

    Long countAllAdvertisersForCompany(Company company);
    List<Advertiser> getAllAdvertisersForCompany(Company company, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAdvertisersForCompany(Company company, Sorting sort, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAdvertisersForCompany(Company company, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllAdvertisers(AdvertiserFilter filter);
    List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Map<String, Map<Long, BigDecimal>> getSpendAndBalanceForAdvertisersAndDateIds(List<Advertiser> advertisers, int startDateId, int endDateId);
    
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, String statusFilter, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, Collection<Advertiser.Status> statuses, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, String statusFilter, String containsName, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, Collection<Advertiser.Status> statuses, String containsName, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, String statusFilter, String containsName, LikeSpec containsNameLikeSpec, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy);
    List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, Collection<Advertiser.Status> statuses, String containsName, LikeSpec containsNameLikeSpec, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // AdvertiserStoppage
    //------------------------------------------------------------------------------------------
    AdvertiserStoppage newAdvertiserStoppage(Advertiser advertiser, AdvertiserStoppage.Reason reason, FetchStrategy... fetchStrategy);
    AdvertiserStoppage newAdvertiserStoppage(Advertiser advertiser, AdvertiserStoppage.Reason reason, Date reactivateDate, FetchStrategy... fetchStrategy);

    AdvertiserStoppage getAdvertiserStoppageById(String id, FetchStrategy... fetchStrategy);
    AdvertiserStoppage getAdvertiserStoppageById(Long id, FetchStrategy... fetchStrategy);
    AdvertiserStoppage update(AdvertiserStoppage advertiserStoppage);
    void delete(AdvertiserStoppage advertiserStoppage);
    void deleteAdvertiserStoppages(List<AdvertiserStoppage> list);

    List<Object[]> getAdvertiserStoppagesFieldsForNullOrFutureReactivateDate();
    List<AdvertiserStoppage> getAdvertiserStoppagesForNullOrFutureReactivateDate(FetchStrategy... fetchStrategy);
    List<AdvertiserStoppage> getAdvertiserStoppagesForAdvertiserAndNullOrFutureReactivateDate(Advertiser advertiser, FetchStrategy... fetchStrategy);


    //------------------------------------------------------------------------------------------
    // AdvertiserNotificationFlag
    //------------------------------------------------------------------------------------------
    AdvertiserNotificationFlag newAdvertiserNotificationFlag(Advertiser advertiser, Type type, int ttlSeconds, FetchStrategy... fetchStrategy);
    AdvertiserNotificationFlag newAdvertiserNotificationFlag(Advertiser advertiser, Type type, Date expirationDate, FetchStrategy... fetchStrategy);
    AdvertiserNotificationFlag getAdvertiserNotificationFlagById(String id, FetchStrategy... fetchStrategy);
    AdvertiserNotificationFlag getAdvertiserNotificationFlagById(Long id, FetchStrategy... fetchStrategy);
    void delete(AdvertiserNotificationFlag advertiserNotificationFlag);
    void deleteAdvertiserNotificationFlags(List<AdvertiserNotificationFlag> list);
    /**
     * Get all advertisers whose account balance has dropped at or below their
     * notify limit, but still above zero (that's handled separately), and has not
     * yet been notified yet about this OR zero-balance.
     */
    List<Advertiser> getAdvertisersToNotifyForLowBalance(FetchStrategy ... fetchStrategy);

    /**
     * Get all advertisers whose account balance has dropped to zero, where the
     * notify limit has been set, and they have not yet been notified about this.
     */
    List<Advertiser> getAdvertisersToNotifyForZeroBalance(FetchStrategy ... fetchStrategy);
        
    /**
     * Delete notification flags for low or zero balance conditions
     * that no longer apply due to balance having been restored.
     */
    void resetLowOrZeroBalanceNotificationFlagsAsApplicable();

    //------------------------------------------------------------------------------------------
    // RTB BidSeats
    //------------------------------------------------------------------------------------------
    Set<BidSeat> getAdvertiserRtbBidSeats(long advertiserId);
    Advertiser updateBidSeats(long advertiserId, Set<BidSeat> advertiserRtbBidSeats);
}
