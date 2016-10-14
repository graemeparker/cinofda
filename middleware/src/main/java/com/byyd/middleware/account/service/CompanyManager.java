package com.byyd.middleware.account.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserMediaCostMargin;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.CompanyDirectCost;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.IpAddressRange;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.account.filter.IpAddressRangeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface CompanyManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // Company
    //------------------------------------------------------------------------------------------
    Company newCompany(String name, Country country, CurrencyExchangeRate defaultCurrencyExchangeRate, FetchStrategy... fetchStrategy);

    Company getCompanyById(String id, FetchStrategy... fetchStrategy);
    Company getCompanyById(Long id, FetchStrategy... fetchStrategy);
    Company update(Company company);
    void delete(Company company);
    void deleteCompanies(List<Company> list);

    Company getCompanyByExternalId(String externalId, FetchStrategy... fetchStrategy);
    Company getCompanyByName(String name, FetchStrategy... fetchStrategy);

    BigDecimal getTotalAdvertiserBalance(Company company);
    BigDecimal getTotalPublisherBalance(Company company);
    BigDecimal getSpendForCompany(Company company);
    boolean hasSpendForCompany(Company company);

    //------------------------------------------------------------------------------------------
    // CompanyMessage
    //------------------------------------------------------------------------------------------
    CompanyMessage newCompanyMessage(Publisher publisher, String systemName, FetchStrategy... fetchStrategy);
    CompanyMessage newCompanyMessage(Campaign campaign, String systemName, FetchStrategy... fetchStrategy);
    CompanyMessage newCompanyMessage(Company company, Advertiser advertiser, Publisher publisher, String systemName, FetchStrategy... fetchStrategy);

    CompanyMessage getCompanyMessageById(String id, FetchStrategy... fetchStrategy);
    CompanyMessage getCompanyMessageById(Long id, FetchStrategy... fetchStrategy);
    CompanyMessage create(CompanyMessage companyMessage);
    CompanyMessage update(CompanyMessage companyMessage);
    void delete(CompanyMessage companyMessage);
    void deleteCompanyMessages(List<CompanyMessage> list);

    List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, FetchStrategy... fetchStrategy);
    List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);
    Long countCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames);

    List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, FetchStrategy... fetchStrategy);
    List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);
    Long countCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames);
    
    //------------------------------------------------------------------------------------------
    // MarginShareDSP
    //------------------------------------------------------------------------------------------
    Company newMarginShareDSP(Company company, BigDecimal margin);
    
    //------------------------------------------------------------------------------------------
    // IpAddressRange
    //------------------------------------------------------------------------------------------
    
    IpAddressRange newIpAddressRange(long startPoint,long endPoint);
    IpAddressRange getIpAddressRangeById(Long id);
    IpAddressRange create(IpAddressRange ipAddressRange);
    IpAddressRange update(IpAddressRange ipAddressRange);
    void delete(IpAddressRange ipAddressRange);
    void deleteIpAddressRanges(List<IpAddressRange> list);
    List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, FetchStrategy... fetchStrategy);
    List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Sorting sort, FetchStrategy... fetchStrategy);
    List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Pagination page, FetchStrategy... fetchStrategy);
    List<IpAddressRange> getAllIpAddressRange(IpAddressRangeFilter ipAddressRangeFilter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
    
    boolean isIpInWhiteList(long ipAddress,long companyId);
    boolean isIpInWhiteList(String ipAddress,long companyId);
    
    //------------------------------------------------------------------------------------------
    // AdvertiserMediaCostMargin
    //------------------------------------------------------------------------------------------
    AdvertiserMediaCostMargin getAdvertiserMediaCostMarginById(Long id, FetchStrategy... fetchStrategy);
    Company newAdvertiserMediaCostMargin(Company company, BigDecimal amount);
    AdvertiserMediaCostMargin create(AdvertiserMediaCostMargin advertiserMediaCostMargin);
    AdvertiserMediaCostMargin update(AdvertiserMediaCostMargin advertiserMediaCostMargin);
    void delete(AdvertiserMediaCostMargin advertiserMediaCostMargin);
    
    Long countAllAdvertiserMediaCostMarginsForCompany(Company company);
    List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<AdvertiserMediaCostMargin> getAllAdvertiserMediaCostMarginsForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // OptimisationReportCompanyPreferences
    //------------------------------------------------------------------------------------------
    OptimisationReportCompanyPreferences newOptimisationReportCompanyPreferences(Company company, Set<OptimisationReportFields> fields, FetchStrategy... fetchStrategy);
    
    OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesById(String id, FetchStrategy... fetchStrategy);
    OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesById(Long id, FetchStrategy... fetchStrategy);
    OptimisationReportCompanyPreferences create(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences);
    OptimisationReportCompanyPreferences update(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences);
    void delete(OptimisationReportCompanyPreferences optimisationReportCompanyPreferences);
    void deleteOptimisationReportCompanyPreferences(List<OptimisationReportCompanyPreferences> list);

    OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferencesForCompany(Company company, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // RTB BidSeats
    //------------------------------------------------------------------------------------------
    Company updateBidSeats(long companyId, Set<BidSeat> companyRtbBidSeats);
    
    //------------------------------------------------------------------------------------------
    // AccountFixedMargin
    //------------------------------------------------------------------------------------------
    Company newAccountFixedMargin(Company company, BigDecimal margin);
    
    //------------------------------------------------------------------------------------------
    // CompanyDirectCost
    //------------------------------------------------------------------------------------------
    CompanyDirectCost newCompanyDirectCost(Company company, BigDecimal newDirectCost);
}
