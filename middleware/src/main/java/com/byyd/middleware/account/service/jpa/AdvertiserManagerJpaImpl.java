package com.byyd.middleware.account.service.jpa;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserNotificationFlag;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Company;
import com.adfonic.domain.NotificationFlag.Type;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.byyd.middleware.account.dao.AccountDao;
import com.byyd.middleware.account.dao.AdvertiserDao;
import com.byyd.middleware.account.dao.AdvertiserNotificationFlagDao;
import com.byyd.middleware.account.dao.AdvertiserStoppageDao;
import com.byyd.middleware.account.dao.BidSeatDao;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("advertiserManager")
public class AdvertiserManagerJpaImpl extends BaseJpaManagerImpl implements AdvertiserManager {
    
    private static final Logger LOG = Logger.getLogger(AdvertiserManagerJpaImpl.class.getName());

    @Autowired(required = false)
    private AccountDao accountDao;
    
    @Autowired(required = false)
    private AdvertiserDao advertiserDao;
    
    @Autowired(required = false)
    private BidSeatDao bidSeatDao;
    
    @Autowired(required = false)
    private AdvertiserStoppageDao advertiserStoppageDao;
    
    @Autowired(required = false)
    private AdvertiserNotificationFlagDao advertiserNotificationFlagDao;
    
    // ------------------------------------------------------------------------------------------
    // Advertiser
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Advertiser newAdvertiser(Company company, String name, FetchStrategy... fetchStrategy) {
        Advertiser advertiser = new Advertiser(company, name);
        advertiser.setPmpBidSeat(bidSeatDao.getById(BidSeat.DEFAULT_BID_SEAT_ID));
        advertiser.setEnableRtbBidSeat(false);
        
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(advertiser);
        } else {
            advertiser = create(advertiser);
            return getAdvertiserById(advertiser.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Advertiser getAdvertiserById(String id, FetchStrategy... fetchStrategy) {
        return getAdvertiserById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Advertiser getAdvertiserById(Long id, FetchStrategy... fetchStrategy) {
        return advertiserDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Advertiser create(Advertiser advertiser) {
        // An account is created as part of the Advertiser's constructor, and it must be persisted prior to
        // persisting the advertiser. This is the reason why accountDao is injected here, since the create(Account) method
        // is not visible in CompanyManager
        accountDao.create(advertiser.getAccount());
        return advertiserDao.create(advertiser);
    }

    @Override
    @Transactional(readOnly = false)
    public Advertiser update(Advertiser advertiser) {
        return advertiserDao.update(advertiser);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Advertiser advertiser) {
        bidSeatDao.deleteAll(advertiser.getAdvertiserRtbBidSeats());
        advertiserDao.delete(advertiser);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAdvertisers(List<Advertiser> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Advertiser entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Advertiser getAdvertiserByExternalId(String externalID, FetchStrategy... fetchStrategy) {
        return advertiserDao.getByExternalId(externalID, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Advertiser getAdvertiserByName(String name, Company company, FetchStrategy... fetchStrategy) {
        return advertiserDao.getByName(name, company, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllAdvertisersForCompany(Company company) {
        return advertiserDao.countAllForCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisersForCompany(Company company, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAllByCompany(company, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisersForCompany(Company company, Sorting sort, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAllByCompany(company, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisersForCompany(Company company, Pagination page, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAllByCompany(company, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllAdvertisers(AdvertiserFilter filter) {
        return advertiserDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAdvertisers(AdvertiserFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return advertiserDao.findAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<Long, BigDecimal>> getSpendAndBalanceForAdvertisersAndDateIds(List<Advertiser> advertisers, int startDateId, int endDateId) {
        return advertiserDao.getSpendAndBalanceForAdvertisersAndDateIds(advertisers, startDateId, endDateId);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, String statusFilter, FetchStrategy... fetchStrategy) {
        return this.getAllAgencyAdvertisersVisibleForUser(user, statusFilter, null, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(User user, Collection<Advertiser.Status> statuses, FetchStrategy... fetchStrategy) {
        return this.getAllAgencyAdvertisersVisibleForUser(user, statuses, null, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(
            User user,
            String statusFilter,
            String containsName,
            boolean nameWithPreviousSpace,
            FetchStrategy... fetchStrategy) {
        return this.getAllAgencyAdvertisersVisibleForUser(user, statusFilter, containsName, LikeSpec.STARTS_WITH, nameWithPreviousSpace, fetchStrategy);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(
            User user,
            Collection<Advertiser.Status> statuses,
            String containsName,
            boolean nameWithPreviousSpace,
            FetchStrategy... fetchStrategy) {
        return this.getAllAgencyAdvertisersVisibleForUser(user, statuses, containsName, LikeSpec.STARTS_WITH, nameWithPreviousSpace, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(
            User user,
            String statusFilter,
            String containsName,
            LikeSpec containsNameLikeSpec,
            boolean nameWithPreviousSpace,
            FetchStrategy... fetchStrategy) {
        Set<Advertiser.Status> validStatuses = new HashSet<Advertiser.Status>();
        if (StringUtils.isNotEmpty(statusFilter)) {
            validStatuses.add(Advertiser.Status.valueOf(statusFilter));
        } else {
            validStatuses.add(Advertiser.Status.ACTIVE);
            validStatuses.add(Advertiser.Status.INACTIVE);
        }
        return getAllAgencyAdvertisersVisibleForUser(user, validStatuses, containsName, containsNameLikeSpec, nameWithPreviousSpace, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Advertiser> getAllAgencyAdvertisersVisibleForUser(
            User user,
            Collection<Advertiser.Status> statuses,
            String containsName,
            LikeSpec containsNameLikeSpec,
            boolean nameWithPreviousSpace,
            FetchStrategy... fetchStrategy) {
        AdvertiserFilter filter = new AdvertiserFilter();
        filter.setUser(user);
        filter.setStatuses(statuses);
        filter.setContainsName(containsName);
        filter.setContainsNameLikeSpec(containsNameLikeSpec);
        filter.setNameWithPreviousSpace(nameWithPreviousSpace);

        UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        boolean adminCheck = userManager.userHasRole(user, Role.USER_ROLE_ADMINISTRATOR);

        List<Advertiser> advertisers = this.getAllAdvertisers(filter, fetchStrategy);

        if(adminCheck) {
            filter.setUser(null);
            // company is eager on User. If this changes, something will need to handle the lazy loading here
            filter.setCompany(user.getCompany());
            for (Advertiser a : getAllAdvertisers(filter, fetchStrategy)) {
                if (!advertisers.contains(a)) {
                    advertisers.add(a);
                }
            }
        }
        return advertisers;
    }

    // ------------------------------------------------------------------------------------------
    // AdvertiserStoppage
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public AdvertiserStoppage newAdvertiserStoppage(Advertiser advertiser, AdvertiserStoppage.Reason reason, FetchStrategy... fetchStrategy) {
        return this.newAdvertiserStoppage(advertiser, reason, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserStoppage newAdvertiserStoppage(Advertiser advertiser, AdvertiserStoppage.Reason reason, Date reactivateDate, FetchStrategy... fetchStrategy) {
        AdvertiserStoppage stoppage = new AdvertiserStoppage(advertiser, reason, reactivateDate);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(stoppage);
        } else {
            stoppage = create(stoppage);
            return getAdvertiserStoppageById(stoppage.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertiserStoppage getAdvertiserStoppageById(String id,
            FetchStrategy... fetchStrategy) {
        return getAdvertiserStoppageById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertiserStoppage getAdvertiserStoppageById(Long id,
            FetchStrategy... fetchStrategy) {
        return advertiserStoppageDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public AdvertiserStoppage create(AdvertiserStoppage advertiserStoppage) {
        return advertiserStoppageDao.create(advertiserStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserStoppage update(AdvertiserStoppage advertiserStoppage) {
        return advertiserStoppageDao.update(advertiserStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(AdvertiserStoppage advertiserStoppage) {
        advertiserStoppageDao.delete(advertiserStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAdvertiserStoppages(List<AdvertiserStoppage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AdvertiserStoppage entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAdvertiserStoppagesFieldsForNullOrFutureReactivateDate() {
        return advertiserStoppageDao.getFieldsForNullOrFutureReactivateDate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserStoppage> getAdvertiserStoppagesForNullOrFutureReactivateDate(
            FetchStrategy... fetchStrategy) {
        return advertiserStoppageDao
                .getAllForReactivateDateIsNullOrReactivateDateGreaterThan(
                        new Date(), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserStoppage> getAdvertiserStoppagesForAdvertiserAndNullOrFutureReactivateDate(
            Advertiser advertiser, FetchStrategy... fetchStrategy) {
        return advertiserStoppageDao
                .getAllForAdvertiserAndReactivateDateIsNullOrReactivateDateGreaterThan(
                        advertiser, new Date(), fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // AdvertiserNotificationFlag
    // ------------------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    protected Advertiser getAdvertiserObjectFoAdvertiserNotificationFlagCreation(Advertiser advertiser) {
        Advertiser localAdvertiser = advertiser;
        try {
            advertiser.getCompany();
        } catch(Exception e) {
            // Not hydrated right, reload locally
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);
            localAdvertiser = this.getAdvertiserById(advertiser.getId(), fs);
        }
        return localAdvertiser;
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserNotificationFlag newAdvertiserNotificationFlag(Advertiser advertiser, Type type, int ttlSeconds, FetchStrategy... fetchStrategy) {
        Advertiser persistedAdvertiser = getAdvertiserObjectFoAdvertiserNotificationFlagCreation(advertiser);
        AdvertiserNotificationFlag flag = new AdvertiserNotificationFlag(persistedAdvertiser, type, ttlSeconds);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(flag);
        } else {
            flag = create(flag);
            return this.getAdvertiserNotificationFlagById(flag.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserNotificationFlag newAdvertiserNotificationFlag(Advertiser advertiser, Type type, Date expirationDate, FetchStrategy... fetchStrategy) {
        Advertiser persistedAdvertiser = getAdvertiserObjectFoAdvertiserNotificationFlagCreation(advertiser);
        AdvertiserNotificationFlag flag = new AdvertiserNotificationFlag(persistedAdvertiser, type, expirationDate);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(flag);
        } else {
            flag = create(flag);
            return this.getAdvertiserNotificationFlagById(flag.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertiserNotificationFlag getAdvertiserNotificationFlagById(String id, FetchStrategy... fetchStrategy) {
        return getAdvertiserNotificationFlagById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertiserNotificationFlag getAdvertiserNotificationFlagById(Long id, FetchStrategy... fetchStrategy) {
        return advertiserNotificationFlagDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public AdvertiserNotificationFlag create(AdvertiserNotificationFlag advertiserNotificationFlag) {
        return advertiserNotificationFlagDao.create(advertiserNotificationFlag);
    }

    @Transactional(readOnly = false)
    public AdvertiserNotificationFlag update(AdvertiserNotificationFlag advertiserNotificationFlag) {
        return advertiserNotificationFlagDao.update(advertiserNotificationFlag);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(AdvertiserNotificationFlag advertiserNotificationFlag) {
        advertiserNotificationFlagDao.delete(advertiserNotificationFlag);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAdvertiserNotificationFlags(List<AdvertiserNotificationFlag> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AdvertiserNotificationFlag entry : list) {
            delete(entry);
        }
    }

    /**
     * Get all advertisers whose account balance has dropped at or below their
     * notify limit, but still above zero (that's handled separately), and has not
     * yet been notified yet about this OR zero-balance.
     */
    @Override
    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
    public List<Advertiser> getAdvertisersToNotifyForLowBalance(FetchStrategy ... fetchStrategy) {
        // This "query ids then hydrate" is not pretty, but it's more efficient than simply
        // returning a list of objects, and then the caller having to hydrate them one by one.
        List<Number> advertiserIds =
            getTransactionalEntityManager().createNativeQuery("SELECT adv.ID"
                                 + " FROM ADVERTISER adv"
                                 + " INNER JOIN COMPANY co ON co.ID=adv.COMPANY_ID"
                                 + " INNER JOIN ACCOUNT acc ON acc.ID = adv.ACCOUNT_ID"
                                 + " INNER JOIN USER mgr ON mgr.ID = co.ACCOUNT_MANAGER_ID"
                                 // Notify limit was set
                                 + " WHERE adv.NOTIFY_LIMIT IS NOT NULL"
                                 // Only notify if the accountManager is active
                                 + " AND mgr.STATUS != 'DISABLED'"
                                 // Balance is at or below the notify limit
                                 + " AND acc.BALANCE <= adv.NOTIFY_LIMIT"
                                 // Not zero balance...that will be picked up separately
                                 + " AND acc.BALANCE > 0"
                                 // Hasn't been notified yet
                                 + " AND NOT EXISTS ("
                                 + "SELECT 1 FROM NOTIFICATION_FLAG nf"
                                 + " WHERE nf.DISCRIMINATOR='ADVERTISER'"
                                 + " AND nf.ADVERTISER_ID=adv.ID"
                                 // Look not only for a non-expired LOW_BALANCE notification, but
                                 // also for a non-expired ZERO_BALANCE notification.  The zero
                                 // balance notification always takes precedence over this.
                                 + " AND (nf.TYPE = 'LOW_BALANCE' OR nf.TYPE = 'ZERO_BALANCE')"
                                 + " AND (nf.EXPIRATION_DATE IS NULL OR nf.EXPIRATION_DATE > CURRENT_TIMESTAMP)"
                                 + ")")
            .getResultList();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("advertiserIds.size=" + advertiserIds.size());
        }
        if (advertiserIds.isEmpty()) {
            return Collections.<Advertiser> emptyList();
        } else {
            return getAllAdvertisers(new AdvertiserFilter().setAdvertiserIds(toLongs(advertiserIds)), fetchStrategy);
        }
    }
            
    @Override
    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
    public List<Advertiser> getAdvertisersToNotifyForZeroBalance(FetchStrategy ... fetchStrategy) {
        // This "query ids then hydrate" is not pretty, but it's more efficient than simply
        // returning a list of objects, and then the caller having to hydrate them one by one.
        List<Number> advertiserIds =
            getTransactionalEntityManager().createNativeQuery("SELECT adv.ID"
                                 + " FROM ADVERTISER adv"
                                 + " INNER JOIN COMPANY co ON co.ID=adv.COMPANY_ID"
                                 + " INNER JOIN ACCOUNT acc ON acc.ID=adv.ACCOUNT_ID"
                                 + " INNER JOIN USER mgr ON mgr.ID = co.ACCOUNT_MANAGER_ID"
                                 // Notify limit was set
                                 + " WHERE adv.NOTIFY_LIMIT IS NOT NULL"
                                 // Only notify if the accountManager is active
                                 + " AND mgr.STATUS != 'DISABLED'"
                                 // Zero balance
                                 // AF-1103 - Aldrin managed to get his balance to go negative, so we use <= here
                                 + " AND acc.BALANCE <= 0"
                                 // Hasn't been notified yet
                                 + " AND NOT EXISTS ("
                                 + "SELECT 1 FROM NOTIFICATION_FLAG nf"
                                 + " WHERE nf.DISCRIMINATOR='ADVERTISER'"
                                 + " AND nf.ADVERTISER_ID=adv.ID"
                                 + " AND nf.TYPE='ZERO_BALANCE'"
                                 + " AND (nf.EXPIRATION_DATE IS NULL OR nf.EXPIRATION_DATE > CURRENT_TIMESTAMP)"
                                 + ")")
            .getResultList();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("advertiserIds.size=" + advertiserIds.size());
        }
        if (advertiserIds.isEmpty()) {
            return Collections.<Advertiser> emptyList();
        } else {
            return getAllAdvertisers(new AdvertiserFilter().setAdvertiserIds(toLongs(advertiserIds)), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void resetLowOrZeroBalanceNotificationFlagsAsApplicable() {
        getTransactionalEntityManager().createNativeQuery("DELETE NOTIFICATION_FLAG"
                             + " FROM NOTIFICATION_FLAG"
                             + " INNER JOIN ADVERTISER ON ADVERTISER.ID = NOTIFICATION_FLAG.ADVERTISER_ID"
                             + " INNER JOIN ACCOUNT ON ACCOUNT.ID = ADVERTISER.ACCOUNT_ID"
                             + " WHERE ACCOUNT.BALANCE > ADVERTISER.NOTIFY_LIMIT"
                             + " AND NOTIFICATION_FLAG.DISCRIMINATOR='ADVERTISER'"
                             + " AND (NOTIFICATION_FLAG.TYPE='LOW_BALANCE' OR NOTIFICATION_FLAG.TYPE='ZERO_BALANCE')"
                             + " AND (NOTIFICATION_FLAG.EXPIRATION_DATE IS NULL OR NOTIFICATION_FLAG.EXPIRATION_DATE > CURRENT_TIMESTAMP)")
            .executeUpdate();
    }
    
    //------------------------------------------------------------------------------------------
    // RTB BidSeats
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public Set<BidSeat> getAdvertiserRtbBidSeats(long advertiserId){
        Advertiser advertiser = getAdvertiserById(advertiserId);
        Set<BidSeat> result;
        if (advertiser!=null){
            result = new HashSet<>(advertiser.getAdvertiserRtbBidSeats());
        }else{
            result = new HashSet<>(0);
        }
        return result;
    }
    
    @Override
    @Transactional(readOnly=false)
    public Advertiser updateBidSeats(long advertiserId, Set<BidSeat> newRtbBidSeats){
        Advertiser advertiser = getAdvertiserById(advertiserId);
        
        Set<BidSeat> oldBidSeats = new HashSet<BidSeat>(advertiser.getAdvertiserRtbBidSeats());
        advertiser.getAdvertiserRtbBidSeats().clear();
        
        //Create/update bidseats data
        for (BidSeat newRtbBidSeat : newRtbBidSeats){
            BidSeat bidSeat = null;
            
            Pattern p = Pattern.compile(newRtbBidSeat.getTargetPublisher().getRtbSeatIdRegEx());
            Matcher m = p.matcher(newRtbBidSeat.getSeatId());
            if (!m.matches()){
                throw new RuntimeException("Seat id " + newRtbBidSeat.getSeatId() + " does not match with " + 
                                           newRtbBidSeat.getTargetPublisher().getName()  + " seat id regex: " + 
                                           newRtbBidSeat.getTargetPublisher().getRtbSeatIdRegEx());
            }
            
            // Check if data exists
            if (newRtbBidSeat.getId()>0){
                bidSeat = bidSeatDao.getById(newRtbBidSeat.getId());
                bidSeat.setSeatId(newRtbBidSeat.getSeatId());
                bidSeat.setDescription(newRtbBidSeat.getDescription());
                bidSeat.setType(newRtbBidSeat.getType());
                bidSeat.setTargetPublisher(newRtbBidSeat.getTargetPublisher());
                bidSeat = bidSeatDao.update(bidSeat);
                oldBidSeats.remove(bidSeat);
            }else{
                bidSeat = bidSeatDao.create(newRtbBidSeat);
            }
            advertiser.getAdvertiserRtbBidSeats().add(bidSeat);
        }
        
        // Delete not used bidseats
        bidSeatDao.deleteAll(oldBidSeats);
        
        return update(advertiser);
    }
}
