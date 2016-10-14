package com.byyd.middleware.campaign.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.adfonic.domain.Audience;
import com.adfonic.domain.AudiencePrices;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAgencyDiscount;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignDataFee;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.byyd.middleware.campaign.filter.CampaignDataFeeFilter;
import com.byyd.middleware.campaign.service.jpa.FeeManagerJpaImpl.DataFeeCalculationResult;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface FeeManager extends BaseManager {

    //------------------------------------------------------------------------------------------
    // CampaignTradingDeskMargin
    //------------------------------------------------------------------------------------------
    
    CampaignTradingDeskMargin getCampaignTradingDeskMarginById(Long id, FetchStrategy... fetchStrategy);
    CampaignTradingDeskMargin getCampaignTradingDeskMarginById(String id, FetchStrategy... fetchStrategy);
    
    void deleteCampaignTradingDeskMargins(List<CampaignTradingDeskMargin> list) ;
    
    Long countAllCampaignTradingDeskMarginsForCampaign(Campaign campaign);
    List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
    
    Campaign saveCampaignTradingDeskMargin(Long campaignId, BigDecimal newCampaignTradingDeskMargin);

    //------------------------------------------------------------------------------------------
    // Rich Media Serving Fee
    //------------------------------------------------------------------------------------------
    
    CampaignRichMediaAdServingFee getCampaignRichMediaAdServingFeeById(Long id, FetchStrategy... fetchStrategy);
    CampaignRichMediaAdServingFee getCampaignRichMediaAdServingFeeById(String id, FetchStrategy... fetchStrategy);
    
    void deleteCampaignRichMediaAdServingFees(List<CampaignRichMediaAdServingFee> list);
    
    Long countAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign);
    List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
    
    Campaign saveCampaignRichMediaAdServingFee(Long campaignId, BigDecimal newCampaignRichMediaAdServingFee);
    
    //------------------------------------------------------------------------------------------
    // CampaignDataFee
    //------------------------------------------------------------------------------------------
    
    CampaignDataFee getCampaignDataFeeById(Long id, FetchStrategy... fetchStrategy);
    CampaignDataFee getCampaignDataFeeById(String id, FetchStrategy... fetchStrategy);
    CampaignDataFee create(CampaignDataFee campaignDataFee);
    CampaignDataFee update(CampaignDataFee campaignDataFee);
    void delete(CampaignDataFee campaignDataFee);
    void deleteCampaignDataFees(List<CampaignDataFee> list); 
    
    Long countAllCampaignDataFees(CampaignDataFeeFilter filter);
    List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, FetchStrategy ... fetchStrategy);
    List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    
    BigDecimal calculateCampaignDataFee(Campaign campaign);
    BigDecimal calculateCampaignAudienceDataFee(Set<CampaignAudience> campaignAudiences);
    BigDecimal calculateAudicencesDataFee(Set<Audience> audiences);
    DataFeeCalculationResult calculateCampaignAudiencesDataFee(Set<CampaignAudience> campaignAudiences);
    AudiencePrices calculateAudienceDataFee(Audience audience);
    
    //------------------------------------------------------------------------------------------
    // CampaignAgencyDiscount
    //------------------------------------------------------------------------------------------
    Campaign newCampaignAgencyDiscount(Campaign campaign, BigDecimal amount);

    CampaignAgencyDiscount getCampaignAgencyDiscountById(String id, FetchStrategy... fetchStrategy);
    CampaignAgencyDiscount getCampaignAgencyDiscountById(Long id, FetchStrategy... fetchStrategy);
    
    void deleteCampaignAgencyDiscounts(List<CampaignAgencyDiscount> list);

    Long countAllCampaignAgencyDiscountsForCampaign(Campaign campaign);
    List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
}
