package com.adfonic.presentation.account.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Account;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.dto.account.AccountDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("accountService")
public class AccountServiceImpl extends GenericServiceImpl implements AccountService {
    
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private AccountManager accountManager;
    
    private static final FetchStrategy accountFs = new FetchStrategyBuilder()
    .addLeft(Advertiser_.account)
    .addLeft(Publisher_.account)
    .build();
    
    public BigDecimal getAccountBalance(PublisherDto publisher){
        Publisher p = publisherManager.getPublisherById(publisher.getId(),accountFs);
        Account a = accountManager.getAccountById(p.getAccount().getId());
        
        return a.getBalance();
    }
    public BigDecimal getAccountBalance(AdvertiserDto advertiser){
        Advertiser ad = advertiserManager.getAdvertiserById(advertiser.getId(),accountFs);
        Account a = accountManager.getAccountById(ad.getAccount().getId());
        
        return a.getBalance();
    }
    
    public Double getAccountDailyBudget(AdvertiserDto advertiser){
        Advertiser ad = advertiserManager.getAdvertiserById(advertiser.getId(),accountFs);
        if(ad.getDailyBudget()==null){
            return null;
        }
        return ad.getDailyBudget().doubleValue();
    }
    
    @Transactional(readOnly=true)
    public Double getAgencyAccountBalance(long companyId){
        return accountManager.getAccountAdvertisersBalanceForCompany(companyId);
    }
    
    @Transactional(readOnly=true)
    public AccountDto getAccountById(Long id) {
    	Account account = accountManager.getAccountById(id);
    	return this.getObjectDto(AccountDto.class, account);
    }
}
