package com.adfonic.presentation.company.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AccountFixedMargin;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("companyService")
public class CompanyServiceImpl extends GenericServiceImpl implements CompanyService {
    
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private CompanyManager companyManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private CommonManager commonManager;
    
    private static final FetchStrategy advertiserFs = new FetchStrategyBuilder()
    .addLeft(Advertiser_.company)
    .build();
    
    private static final FetchStrategy usersFs = new FetchStrategyBuilder()
    .addLeft(Company_.users)
    .addLeft(User_.roles)
    .build();
    
    private static final FetchStrategy companyFs = new FetchStrategyBuilder()
    .addLeft(Company_.advertisers)
    .addLeft(Advertiser_.users)
    .build();
    
    @Transactional(readOnly=true)
    public CompanyDto getCompanyById(Long companyId){
        Company company = companyManager.getCompanyById(companyId);
        return getDtoObject(CompanyDto.class, company);
    }
    
    @Transactional(readOnly=true)
    public CompanyDto getCompanyForAdvertiser(Long advertiserId){
        Advertiser adv = advertiserManager.getAdvertiserById(advertiserId,advertiserFs);
        CompanyDto dto = getDtoObject(CompanyDto.class, adv.getCompany());
        dto.setTimeZone(adv.getCompany().getDefaultTimeZone());
        return dto;
    }
    
    @Transactional(readOnly=true)
    public CompanyDto getCompanyForAdvertiser(AdvertiserDto advertiser){
        return getCompanyForAdvertiser(advertiser.getId());
    }
    
    @Transactional(readOnly=true)
    public TimeZone getTimeZoneForAdvertiser(AdvertiserDto advertiser){
        Advertiser adv = advertiserManager.getAdvertiserById(advertiser.getId(),advertiserFs);
        return adv.getCompany().getDefaultTimeZone();
    }
    
    @Transactional(readOnly=true)
    public List<AdvertiserDto> getAdvertisersForCompany(CompanyDto company){
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(User.class, "company", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Company.class, "advertisers", JoinType.LEFT);
        Company comp = companyManager.getCompanyById(company.getId());
        AdvertiserFilter advertiserFilter = new AdvertiserFilter().setCompany(comp);
        List<Advertiser> advertisers = advertiserManager.getAllAdvertisers(advertiserFilter, fs);
        if (!CollectionUtils.isEmpty(advertisers)) {
            List<AdvertiserDto> advertisersListDto = (List<AdvertiserDto>)getList(AdvertiserDto.class, advertisers);
            return advertisersListDto;
        }
        return null;
    }
    
    @Transactional(readOnly=true)
    public AdvertiserDto getAdvertiserByExternalID(String externalId){
        Advertiser advertiser = advertiserManager.getAdvertiserByExternalId(externalId);
        return getDtoObject(AdvertiserDto.class, advertiser);
    }
    
    @Transactional(readOnly=true)
    public AdvertiserDto getAdvertiserById(Long id){
        Advertiser advertiser = advertiserManager.getAdvertiserById(id,advertiserFs);
        return getDtoObject(AdvertiserDto.class, advertiser);
    }
    
    @Transactional(readOnly=true)
    public List<AdvertiserDto> getAdvertisersById(final String[] advertisersIds){
        List<AdvertiserDto> result = new ArrayList<AdvertiserDto>(0);
        for (String adv : advertisersIds) {
            Advertiser advertiser = advertiserManager.getAdvertiserById(Long.parseLong(adv.trim()));
            if (advertiser != null)
                result.add(getDtoObject(AdvertiserDto.class, advertiser));
        }
        return result;
    }
    
    @Transactional(readOnly=true)
    public List<AdvertiserDto> searchAdvertiser(String name, CompanyDto company){
        AdvertiserFilter filter = new AdvertiserFilter();
        if (!StringUtils.isEmpty(name)) {
            filter.setName(name, false);
            filter.setCompany(companyManager.getCompanyById(company.getId()));
            Collection<Advertiser> advs = advertiserManager.getAllAdvertisers(filter);
            return (List<AdvertiserDto>)getList(AdvertiserDto.class, advs);
        }
        
        return new ArrayList<AdvertiserDto>();
        
    }
    
    @Transactional(readOnly=false)
    public void changeAdvertiserStatus(List<Long> advertiserIds , AdvertiserStatus status) {
        if(!CollectionUtils.isEmpty(advertiserIds)){
            for( Long id:advertiserIds ) {
                Advertiser adv = advertiserManager.getAdvertiserById(id);
                adv.setStatus(status.getStatus());
                advertiserManager.update(adv);
            }
        }
    }
    
    @Transactional(readOnly=false)
    public AdvertiserDto newAdvertiserDto(Long companyId, AdvertiserDto dto) throws Exception{
        Company company = companyManager.getCompanyById(companyId);
        Advertiser advertiser = advertiserManager.newAdvertiser(company, dto.getName());
        advertiser = advertiserManager.getAdvertiserById(advertiser.getId(),advertiserFs);
        advertiser.setStatus(dto.getStatus());
        advertiser = advertiserManager.update(advertiser);
        advertiser = advertiserManager.getAdvertiserById(advertiser.getId(),advertiserFs);
        return getDtoObject(AdvertiserDto.class, advertiser);
    } 
    
    @Transactional(readOnly=true)
    public List<AdvertiserDto> doQuery(String search, Long companyId){
        AdvertiserFilter filter = new AdvertiserFilter();
        Company company = companyManager.getCompanyById(companyId);
        filter.setContainsName(search);
        filter.setCompany(company);
        List<Advertiser> advertisers = advertiserManager.getAllAdvertisers(filter, advertiserFs);
        List<AdvertiserDto> dtos = new ArrayList<>();
        for(Advertiser advertiser : advertisers){
            dtos.add(getDtoObject(AdvertiserDto.class, advertiser));
        }
        return dtos;
    }
    
    @Transactional(readOnly=true)
    public List<UserDTO> getUsersForCompany(Long companyId){
        Company company = companyManager.getCompanyById(companyId, usersFs);
        List<UserDTO> result = new ArrayList<UserDTO>();
        for(User user : company.getUsers()){
            result.add(getDtoObject(UserDTO.class, user));
        }
        return result;
    }
    
    @Transactional(readOnly=true)
    public List<AdvertiserDto> getAdvertisersForUser(Long userId){
        List<AdvertiserDto> result = new ArrayList<AdvertiserDto>();
        User user = userManager.getUserById(userId);
        Company company = companyManager.getCompanyById(user.getCompany().getId(), companyFs);
        
        for(Advertiser advertiser : company.getAdvertisers()){
            if(advertiser.getUsers().contains(user)){
                result.add(getDtoObject(AdvertiserDto.class, advertiser));
            }
        }
        return result;
    }
    
    @Transactional(readOnly=true)
    public PublisherDto getPublisherById(Long id){
        Publisher publisher = publisherManager.getPublisherById(id);
        return getDtoObject(PublisherDto.class, publisher);
    }
    
    @Transactional(readOnly=true)
    public boolean hasTechFee(UserDTO userDto){
        if(userDto.getCompany() == null) {
            return false;
        }
        Long id = userDto.getCompany().getId();
        Company company = companyManager.getCompanyById(id);
        return company.hasTechFee();
    }
    
    @Transactional(readOnly=true)
    public AccountFixedMarginDto getAccountFixedMargin(Long companyId){
        AccountFixedMarginDto accountFixedMarginDto = null;
        
        Company company = companyManager.getCompanyById(companyId);
        if (company!=null){
            AccountFixedMargin accountFixedMargin = company.getCurrentAccountFixedMargin();
            if(accountFixedMargin!=null){
                accountFixedMarginDto = getDtoObject(AccountFixedMarginDto.class, accountFixedMargin);
            }
        }
        
        return accountFixedMarginDto;
    }
    
    //
    // Advertiser default currency
    //
    @Override
    @Transactional(readOnly=true)
    public CurrencyExchangeRateDto getAdvertiserDefaultCurrency(Long advertiserId){
        // Get advertiser
        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserId);
        
        CurrencyExchangeRate currencyExchangeRate = advertiser.getDefaultCurrencyExchangeRate();
        if(currencyExchangeRate==null){
            Company company = advertiser.getCompany();
            currencyExchangeRate = company.getDefaultCurrencyExchangeRate();
        }
        return getObjectDto(CurrencyExchangeRateDto.class, currencyExchangeRate);
    }
    
    @Override
    @Transactional(readOnly=false)
    public AdvertiserDto setAdvertiserDefaultCurrency(Long advertiserId, Long currencyExchangeRateId){
        // Get advertiser
        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserId);
        
        CurrencyExchangeRate selectedCurrency = null;
        if (currencyExchangeRateId!=null){
            selectedCurrency = commonManager.getCurrencyExchangeRateById(currencyExchangeRateId);
        }
        
        advertiser.setDefaultCurrencyExchangeRate(selectedCurrency);
        advertiser = advertiserManager.update(advertiser);
        
        return getDtoObject(AdvertiserDto.class, advertiser);
    }
    
    //
    // Company default currency
    //
    @Override
    @Transactional(readOnly=true)
    public CurrencyExchangeRateDto getCompanyDefaultCurrency(Long companyId){
        // Get company
        Company company = companyManager.getCompanyById(companyId);
        return getObjectDto(CurrencyExchangeRateDto.class, company.getDefaultCurrencyExchangeRate());
    }
    
    @Override
    @Transactional(readOnly=false)
    public CompanyDto setCompanyDefaultCurrency(Long companyId, Long currencyExchangeRateId){
        // Get company
        Company company = companyManager.getCompanyById(companyId);
        
        CurrencyExchangeRate selectedCurrency = commonManager.getCurrencyExchangeRateById(currencyExchangeRateId);
        
        company.setDefaultCurrencyExchangeRate(selectedCurrency);
        company = companyManager.update(company);
        
        return getDtoObject(CompanyDto.class, company);
    }
}
