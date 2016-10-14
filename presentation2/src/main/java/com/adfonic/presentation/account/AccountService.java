package com.adfonic.presentation.account;

import java.math.BigDecimal;

import com.adfonic.dto.account.AccountDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.publisher.PublisherDto;

public interface AccountService {

    public BigDecimal getAccountBalance(PublisherDto publisher);
    public BigDecimal getAccountBalance(AdvertiserDto advertiser);
    public Double getAccountDailyBudget(AdvertiserDto advertiser);
    public Double getAgencyAccountBalance(long companyId);
    
    AccountDto getAccountById(Long id);
}

