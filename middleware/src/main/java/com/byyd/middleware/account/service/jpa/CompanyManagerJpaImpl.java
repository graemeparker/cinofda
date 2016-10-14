package com.byyd.middleware.account.service.jpa;

import static com.adfonic.domain.Role.COMPANY_ROLE_PREPAY;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AccountFixedMargin;
import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserMediaCostMargin;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company.AdvertiserCategory;
import com.adfonic.domain.CompanyDirectCost;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.IpAddressRange;
import com.adfonic.domain.MarginShareDSP;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.domain.Publisher;
import com.adfonic.util.IpAddressUtils;
import com.byyd.middleware.account.dao.AccountFixedMarginDao;
import com.byyd.middleware.account.dao.AdvertiserMediaCostMarginDao;
import com.byyd.middleware.account.dao.BidSeatDao;
import com.byyd.middleware.account.dao.CompanyDao;
import com.byyd.middleware.account.dao.CompanyDirectCostDao;
import com.byyd.middleware.account.dao.CompanyMessageDao;
import com.byyd.middleware.account.dao.IpAddressRangeDao;
import com.byyd.middleware.account.dao.MarginShareDSPDao;
import com.byyd.middleware.account.dao.OptimisationReportCompanyPreferencesDao;
import com.byyd.middleware.account.filter.IpAddressRangeFilter;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("companyManager")
public class CompanyManagerJpaImpl extends BaseJpaManagerImpl implements CompanyManager {
    
    @Autowired(required=false)
    private CompanyDao companyDao;
    
    @Autowired(required=false)
    private CompanyMessageDao companyMessageDao;
    
    @Autowired(required=false)
    private MarginShareDSPDao marginShareDSPDao;
    
    @Autowired(required=false)
    private IpAddressRangeDao ipAddressRangeDao;
    
    @Autowired(required=false)
    private AdvertiserMediaCostMarginDao advertiserMediaCostMarginDao;
    
    @Autowired(required=false)
    private OptimisationReportCompanyPreferencesDao optimisationReportCompanyPreferencesDao;
    
    @Autowired(required=false)
    private BidSeatDao bidSeatDao;
    
    @Autowired(required=false)
    private AccountFixedMarginDao accountFixedMarginDao;
    
    @Autowired(required = false)
    private CompanyDirectCostDao companyDirectCostDao;
        
    //------------------------------------------------------------------------------------------
    // Company
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public Company newCompany(String name, Country country, CurrencyExchangeRate defaultCurrencyExchangeRate, FetchStrategy... fetchStrategy) {
        UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        Company company = new Company(name);
        company.setCountry(country);
        // all new companies get the prepay role
        company.getRoles().add(userManager.getRoleByName(COMPANY_ROLE_PREPAY));
        company.setDefaultCurrencyExchangeRate(defaultCurrencyExchangeRate);
        company = create(company);
        this.newMarginShareDSP(company, new BigDecimal(MarginShareDSP.DEFAULT_MARGIN_SHARE_DSP));
        this.newAccountFixedMargin(company, new BigDecimal(AccountFixedMargin.DEFAULT_ACCOUNT_FIXED_MARGIN));
        
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return company;
        } else {
            return getCompanyById(company.getId(), fetchStrategy);
        }
    }
    
    @Override
    @Transactional(readOnly=true)
    public Company getCompanyById(String id, FetchStrategy... fetchStrategy) {
        return getCompanyById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Company getCompanyById(Long id, FetchStrategy... fetchStrategy) {
        return companyDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly=false)
    public Company create(Company company) {
        return companyDao.create(company);
    }

    @Override
    @Transactional(readOnly=false)
    public Company update(Company company) {
        return companyDao.update(company);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(Company company) {
        PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
        UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        
        // Delete company Media Cost Margins
        this.deleteAdvertiserMediaCostMargins(this.getAllAdvertiserMediaCostMarginsForCompany(company));
        
        // Delete publications
        publicationManager.deletePublicationLists(publicationManager.getAllPublicationListsForCompany(company));
        
        // Delete company users
        userManager.deleteUsers(userManager.getAllUsersForCompany(company));
        
        // Delete margin share
        this.deleteMarginShareDSPs(marginShareDSPDao.getAllForCompany(company));
        
        //Delete Account Fixed Margin
        this.deleteAccountFixedMargins(accountFixedMarginDao.getAllForCompany(company));
        
        // Delete Bid Seats
        bidSeatDao.deleteAll(company.getCompanyRtbBidSeats());
        
        // Delete company
        companyDao.delete(company);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteCompanies(List<Company> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(Company entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Company getCompanyByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return companyDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Company getCompanyByName(String name, FetchStrategy... fetchStrategy) {
        return companyDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public BigDecimal getTotalAdvertiserBalance(Company company) {
        return companyDao.getTotalAdvertiserBalance(company);
    }

    @Override
    @Transactional(readOnly=true)
    public BigDecimal getTotalPublisherBalance(Company company) {
        return companyDao.getTotalPublisherBalance(company);
    }

    @Override
    @Transactional(readOnly=true)
    public BigDecimal getSpendForCompany(Company company) {
        return companyDao.getSpendForCompany(company);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean hasSpendForCompany(Company company) {
        return BigDecimal.ZERO.compareTo(getSpendForCompany(company)) != 0;
    }

    //------------------------------------------------------------------------------------------
    // CompanyMessage
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public CompanyMessage newCompanyMessage(Publisher publisher, String systemName, FetchStrategy... fetchStrategy) {
        return this.newCompanyMessage(publisher.getCompany(), null, publisher, systemName, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CompanyMessage newCompanyMessage(Campaign campaign, String systemName, FetchStrategy... fetchStrategy) {
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "advertiser", JoinType.INNER);
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);
        fs.addEagerlyLoadedFieldForClass(Company.class, "publisher", JoinType.LEFT);
        CompanyMessage message = new CompanyMessage(campaignManager.getCampaignById(campaign.getId(), fs), systemName);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(message);
        } else {
            message = create(message);
            return getCompanyMessageById(message.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public CompanyMessage newCompanyMessage(Company company, Advertiser advertiser, Publisher publisher, String systemName, FetchStrategy... fetchStrategy) {
        CompanyMessage message = new CompanyMessage(company, advertiser, publisher, systemName);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(message);
        } else {
            message = create(message);
            return getCompanyMessageById(message.getId(), fetchStrategy);
        }
    }


    @Override
    @Transactional(readOnly=true)
    public CompanyMessage getCompanyMessageById(String id, FetchStrategy... fetchStrategy) {
        return getCompanyMessageById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public CompanyMessage getCompanyMessageById(Long id, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CompanyMessage create(CompanyMessage companyMessage) {
        return companyMessageDao.create(companyMessage);
    }

    @Override
    @Transactional(readOnly=false)
    public CompanyMessage update(CompanyMessage companyMessage) {
        return companyMessageDao.update(companyMessage);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CompanyMessage companyMessage) {
        companyMessageDao.delete(companyMessage);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteCompanyMessages(List<CompanyMessage> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(CompanyMessage entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames) {
        return companyMessageDao.countCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return companyMessageDao.getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames) {
        return companyMessageDao.countCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames);
    }
        
    // ------------------------------------------------------------------------------------------
    // MarginShareDSP
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Company newMarginShareDSP(Company company, BigDecimal margin) {
        Date now = new Date();
        MarginShareDSP marginShareDSP = company.getCurrentMarginShareDSP();
        if(marginShareDSP != null) {
            marginShareDSP.setEndDate(now);
            update(marginShareDSP);
        }

        MarginShareDSP cad = company.createNewMarginShareDSP(margin, now);
        create(cad);
        return update(company);
    }

    @Transactional(readOnly = false)
    public MarginShareDSP create(MarginShareDSP marginShareDSP) {
        return marginShareDSPDao.create(marginShareDSP);
    }

    @Transactional(readOnly = false)
    public MarginShareDSP update(MarginShareDSP marginShareDSP) {
        return marginShareDSPDao.update(marginShareDSP);
    }

    @Transactional(readOnly = false)
    public void delete(MarginShareDSP marginShareDSP) {
        marginShareDSPDao.delete(marginShareDSP);
    }

    @Transactional(readOnly = false)
    public void deleteMarginShareDSPs(List<MarginShareDSP> list) {
        if(!CollectionUtils.isEmpty(list)) {
            for (MarginShareDSP entry : list) {
                delete(entry);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------
    // IpAddressRange
    //------------------------------------------------------------------------------------------ 
    @Override
    @Transactional(readOnly=false)
    public IpAddressRange newIpAddressRange(long startPoint,long endPoint) {
        IpAddressRange ipAddressRange = new IpAddressRange(startPoint, endPoint);
        return create(ipAddressRange);
    }

    @Override
    @Transactional(readOnly=true)
    public IpAddressRange getIpAddressRangeById(Long id) {
        return ipAddressRangeDao.getById(id);
    }

    @Override
    @Transactional(readOnly=false)
    public IpAddressRange create(IpAddressRange ipAddressRange) {
        return ipAddressRangeDao.create(ipAddressRange);
    }

    @Override
    @Transactional(readOnly=false)
    public IpAddressRange update(IpAddressRange ipAddressRange) {
        return ipAddressRangeDao.update(ipAddressRange);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(IpAddressRange ipAddressRange) {
        ipAddressRangeDao.delete(ipAddressRange);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteIpAddressRanges(List<IpAddressRange> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(IpAddressRange entry : list) {
            delete(entry);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, FetchStrategy... fetchStrategy) {
        return getAllIpAddressRange(ipAddressRangeFilter,null,null,fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllIpAddressRange(ipAddressRangeFilter,null,sort,fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllIpAddressRange(ipAddressRangeFilter,page,null,fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return ipAddressRangeDao.getAll(ipAddressRangeFilter,page,sort,fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isIpInWhiteList(long ipAddress,long companyId){
        Company c = getCompanyById(companyId);
        //If no white list, all addresses included
        if(c.getIpAddressRanges() == null || c.getIpAddressRanges().isEmpty()){
            return true;
        }
        for(IpAddressRange iar : c.getIpAddressRanges()){
            if(ipAddress >= iar.getStartPoint() && ipAddress <= iar.getEndPoint()){
                return true;
            }
        }
        return false; 
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isIpInWhiteList(String ipAddress,long companyId){
        try {
            return isIpInWhiteList(IpAddressUtils.ipAddressToLong(ipAddress),companyId);
        } catch (UnknownHostException e) {
            return false;
        }
    }    
    
    //------------------------------------------------------------------------------------------
    // AdvertiserMediaCostMargin
    //------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly = true)
    public AdvertiserMediaCostMargin getAdvertiserMediaCostMarginById(Long id,
            FetchStrategy... fetchStrategy) {
        return advertiserMediaCostMarginDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Company newAdvertiserMediaCostMargin(Company company, BigDecimal amount) {
        Company localCompany = company;
        Date now = new Date();
        AdvertiserMediaCostMargin currentMediaCostMargin = localCompany.getCurrentMediaCostMargin();
        if(currentMediaCostMargin != null) {
            currentMediaCostMargin.setEndDate(now);
            update(currentMediaCostMargin);
        }

        // Bad pattern. The caller should be responsible for the proper hydration of the company object.
        // Since we return an updated company object, any previous hydration pattern would be lost if we did this
        try {
            localCompany.getHistoricalMediaCostMargins().size();
        }catch (Exception e) {
            // hydrate with historical bids
            localCompany = this.getCompanyById(company.getId(),
                    new FetchStrategyBuilder().addLeft(Company_.historicalMediaCostMargins)
                    .build());
        }

        AdvertiserMediaCostMargin cdf = localCompany.createNewMediaCostMargin(amount, now);
        create(cdf);
        return this.update(localCompany);
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserMediaCostMargin create(AdvertiserMediaCostMargin advertiserMediaCostMargin) {
        return advertiserMediaCostMarginDao.create(advertiserMediaCostMargin);
    }

    @Override
    @Transactional(readOnly = false)
    public AdvertiserMediaCostMargin update(AdvertiserMediaCostMargin advertiserMediaCostMargin) {
        return advertiserMediaCostMarginDao.update(advertiserMediaCostMargin);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(AdvertiserMediaCostMargin advertiserMediaCostMargin) {
        advertiserMediaCostMarginDao.delete(advertiserMediaCostMargin);
    }
    
    @Transactional(readOnly = false)
    public void deleteAdvertiserMediaCostMargins(List<AdvertiserMediaCostMargin> list) {
        for(AdvertiserMediaCostMargin entry : list) {
            delete(entry);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAllAdvertiserMediaCostMarginsForCompany(Company company) {
        return advertiserMediaCostMarginDao.countAllForCompany(company);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return advertiserMediaCostMarginDao.getAllForCompany(company, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return advertiserMediaCostMarginDao.getAllForCompany(company, page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return advertiserMediaCostMarginDao.getAllForCompany(company, sort, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // OptimisationReportCompanyPreferences
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public OptimisationReportCompanyPreferences newOptimisationReportCompanyPreferences(Company company, Set<OptimisationReportFields> fields, FetchStrategy... fetchStrategy) {
        OptimisationReportCompanyPreferences optimisationReportCompanyPreferences = new OptimisationReportCompanyPreferences();
        optimisationReportCompanyPreferences.setCompany(company);
        optimisationReportCompanyPreferences.setReportFields(fields);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(optimisationReportCompanyPreferences);
        } else {
            optimisationReportCompanyPreferences = create(optimisationReportCompanyPreferences);
            return getOptimisationReportCompanyPreferencesById(optimisationReportCompanyPreferences.getId(), fetchStrategy);
        }
    }

    
    @Override
    @Transactional(readOnly=true)
    public OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesById(String id, FetchStrategy... fetchStrategy) {
        return getOptimisationReportCompanyPreferencesById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesById(Long id, FetchStrategy... fetchStrategy) {
        return optimisationReportCompanyPreferencesDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public OptimisationReportCompanyPreferences create(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences) {
        return optimisationReportCompanyPreferencesDao.create(optimisationReportCompanyPreferences);
    }

    @Override
    @Transactional(readOnly=false)
    public OptimisationReportCompanyPreferences update(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences) {
        return optimisationReportCompanyPreferencesDao.update(optimisationReportCompanyPreferences);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences) {
        optimisationReportCompanyPreferencesDao.delete(optimisationReportCompanyPreferences);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteOptimisationReportCompanyPreferences(List<OptimisationReportCompanyPreferences> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences : list) {
            delete(optimisationReportCompanyPreferences);
        }
    }
    
    @Override
    @Transactional(readOnly=true)
    public OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesForCompany(Company company, FetchStrategy... fetchStrategy) {
        return optimisationReportCompanyPreferencesDao.getForCompany(company, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // RTB BidSeats
    //------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=false)
    public Company updateBidSeats(long companyId, Set<BidSeat> newRtbBidSeats){
        Company company = getCompanyById(companyId);
        
        Set<BidSeat> oldBidSeats = new HashSet<BidSeat>(company.getCompanyRtbBidSeats());
        company.getCompanyRtbBidSeats().clear();
        
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
            company.getCompanyRtbBidSeats().add(bidSeat);
        }
        
        // Delete not used bidseats
        bidSeatDao.deleteAll(oldBidSeats);
        
        return update(company);
    }

    // ------------------------------------------------------------------------------------------
    // AccountFixedMargin [MAD-3348]
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Company newAccountFixedMargin(Company company, BigDecimal margin) {
        if ((company.getAdvertiserCategory() != AdvertiserCategory.MADISON_LICENCE) && 
            (company.getAdvertiserCategory() != AdvertiserCategory.EXCHANGE) && 
            (company.getAccountTypeFlags()!=AccountType.PUBLISHER.bitValue())){
            // Create new value
            Date now = new Date();
            AccountFixedMargin currentAccountFixedMargin = company.getCurrentAccountFixedMargin();
            if(currentAccountFixedMargin != null) {
                currentAccountFixedMargin.setEndDate(now);
                update(currentAccountFixedMargin);
            }
    
            AccountFixedMargin cad = company.createNewAccountFixedMargin(margin, now);
            create(cad);
            company = update(company);
            
            // Update all new and current campaigns
            CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
            FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
            AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
            
            Set<Status> allowedStatuses = new HashSet<Status>(Arrays.asList(Status.values()));
            allowedStatuses.remove(Status.DELETED);
            List<Advertiser> advertisers = advertiserManager.getAllAdvertisersForCompany(company);
            if (advertisers!=null){
                for (Advertiser advertiser : advertisers){
                    CampaignFilter filter = new CampaignFilter().setAdvertiser(advertiser).setStatuses(allowedStatuses);
                    List<Campaign> campaigns = campaignManager.getAllCampaigns(filter);
                    if(campaigns!=null){
                        for (Campaign campaign : campaigns){
                            if ((campaign.getCurrentTradingDeskMargin()!=null && currentAccountFixedMargin != null) && (campaign.getCurrentTradingDeskMargin().getTradingDeskMargin().compareTo(currentAccountFixedMargin.getMargin())==0)){
                                feeManager.saveCampaignTradingDeskMargin(campaign.getId(), margin);
                            }
                        }
                    }
                }
            }
        }
        return company;
    }

    @Transactional(readOnly = false)
    public AccountFixedMargin create(AccountFixedMargin accountFixedMargin) {
        return accountFixedMarginDao.create(accountFixedMargin);
    }

    @Transactional(readOnly = false)
    public AccountFixedMargin update(AccountFixedMargin accountFixedMargin) {
        return accountFixedMarginDao.update(accountFixedMargin);
    }

    @Transactional(readOnly = false)
    public void delete(AccountFixedMargin accountFixedMargin) {
        accountFixedMarginDao.delete(accountFixedMargin);
    }

    @Transactional(readOnly = false)
    private void deleteAccountFixedMargins(List<AccountFixedMargin> list) {
        if(!CollectionUtils.isEmpty(list)) {
            for (AccountFixedMargin entry : list) {
                delete(entry);
            }
        }
    }

    //------------------------------------------------------------------------------------------
    // CompanyDirectCost
    //------------------------------------------------------------------------------------------
    
    private CompanyDirectCost create(CompanyDirectCost companyDirectCost) {
        return companyDirectCostDao.create(companyDirectCost);
    }

    private CompanyDirectCost update(CompanyDirectCost companyDirectCost) {
        return companyDirectCostDao.update(companyDirectCost);
    }

	@Override
	@Transactional(readOnly = false)
	public CompanyDirectCost newCompanyDirectCost(Company company, BigDecimal newDirectCost) {
		CompanyDirectCost actualCompanyDirectCost = company.getCompanyDirectCost();
		
		// Close actual company direct cost if exists
        if (actualCompanyDirectCost != null) {
        	actualCompanyDirectCost.setEndDate(new Date());
        	update(actualCompanyDirectCost);
        }
        
        // Create new company direct cost
        return create(new CompanyDirectCost(company, newDirectCost));
	}
}
