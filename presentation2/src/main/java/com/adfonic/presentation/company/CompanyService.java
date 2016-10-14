package com.adfonic.presentation.company;

import java.util.List;
import java.util.TimeZone;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.user.UserDTO;



public interface CompanyService {
    public CompanyDto getCompanyById(Long companyId);
    public CompanyDto getCompanyForAdvertiser(Long advertiserId);
    public CompanyDto getCompanyForAdvertiser(AdvertiserDto advertiser);
    public TimeZone getTimeZoneForAdvertiser(AdvertiserDto advertiser);
    public List<AdvertiserDto> getAdvertisersForCompany(CompanyDto company);
    public AdvertiserDto getAdvertiserByExternalID(String externalId);
    public AdvertiserDto getAdvertiserById(Long id);
    public List<AdvertiserDto> getAdvertisersById(final String[] advertisersIds);
    public List<AdvertiserDto> searchAdvertiser(String name, CompanyDto company);
    public void changeAdvertiserStatus(List<Long> advertiserIds , AdvertiserStatus status);
    public AdvertiserDto newAdvertiserDto(Long companyId, AdvertiserDto dto) throws Exception;
    public List<AdvertiserDto> doQuery(String search, Long companyId);
    public List<UserDTO> getUsersForCompany(Long companyId);
    public List<AdvertiserDto> getAdvertisersForUser(Long userId);
    
    public PublisherDto getPublisherById(Long id);
    
    public boolean hasTechFee(UserDTO userDto);
    
    public AccountFixedMarginDto getAccountFixedMargin(Long companyId);
    
    // Advertiser default currency
    CurrencyExchangeRateDto getAdvertiserDefaultCurrency(Long advertiserId);
    AdvertiserDto setAdvertiserDefaultCurrency(Long advertiserId, Long currencyExchangeRateId);
    
    // Company default currency
    CurrencyExchangeRateDto getCompanyDefaultCurrency(Long companyId);
    CompanyDto setCompanyDefaultCurrency(Long companyId, Long currencyExchangeRateId);
    
}

