package com.byyd.middleware.campaign.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignTargetCTR;
import com.adfonic.domain.CampaignTargetCVR;
import com.adfonic.domain.Segment;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.campaign.filter.BidDeductionFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface BiddingManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // CampaignBid
    //------------------------------------------------------------------------------------------
    Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount);
    //Used with the dsp development. If user is dps the maximum = true
    Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount,boolean maximum);
    Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount,CampaignBid.BidModelType modelType);

    CampaignBid getCampaignBidById(String id, FetchStrategy... fetchStrategy);
    CampaignBid getCampaignBidById(Long id, FetchStrategy... fetchStrategy);
    
    void deleteCampaignBids(List<CampaignBid> list);
    
    List<BigDecimal> getReferenceBids(Campaign campaign, BidType bidType);
    int[] getBidDensity(List<BigDecimal> saneBids, int numSegments, BigDecimal minimumBid, BigDecimal defaultDiff);

    Map<BidType,BigDecimal> getMinBidMap(Campaign campaign, TransparentNetwork network, Segment currentSegment);
    List<BidType> getAvailableBidTypesForCampaign(Campaign campaign);
    BigDecimal getMinimumBidForCampaign(Campaign campaign, BidType bidType);
    BigDecimal getMinimumBidForCampaign(Campaign campaign, Segment segment, BidType bidType);
    boolean validateMinimumBidForCampaign(Campaign campaign, BidType bidType, BigDecimal newAmount);
    boolean validateMinimumBidForCampaign(Campaign campaign, Segment segment, BidType bidType, BigDecimal newAmount);

    Long countAllCampaignBidsForCampaign(Campaign campaign);
    List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy);
    List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy);
    List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // BidDeduction
    //------------------------------------------------------------------------------------------
    
    BidDeduction getBidDeductionById(Long id, FetchStrategy... fetchStrategy);
    BidDeduction create(BidDeduction bidDeduction);
    BidDeduction update(BidDeduction bidDeduction);
    Campaign updateBidDeductions(Campaign campaign, Set<BidDeduction> newBidDeductions);
    List<BidDeduction> getBidDeductions(BidDeductionFilter filter, FetchStrategy ... fetchStrategy);
    List<BidDeduction> getBidDeductions(BidDeductionFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<BidDeduction> getBidDeductions(BidDeductionFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
        
    //------------------------------------------------------------------------------------------
    // CampaignTargetCTR
    //------------------------------------------------------------------------------------------

    CampaignTargetCTR getCampaignTargetCTRById(Long id, FetchStrategy... fetchStrategy);
    CampaignTargetCTR newCampaignTargetCTR(Campaign campaign, BigDecimal targetCTR, FetchStrategy... fetchStrategy);
    CampaignTargetCTR create(CampaignTargetCTR campaignTargetCTR);
    CampaignTargetCTR update(CampaignTargetCTR campaignTargetCTR);
    void delete(CampaignTargetCTR campaignTargetCTR);
    CampaignTargetCTR copyCampaignTargetCTR(CampaignTargetCTR campaignTargetCTR, Campaign newCampaign, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // CampaignTargetCVR
    //------------------------------------------------------------------------------------------

    CampaignTargetCVR getCampaignTargetCVRById(Long id, FetchStrategy... fetchStrategy);
    CampaignTargetCVR newCampaignTargetCVR(Campaign campaign, BigDecimal targetCVR, FetchStrategy... fetchStrategy);
    CampaignTargetCVR create(CampaignTargetCVR campaignTargetCVR);
    CampaignTargetCVR update(CampaignTargetCVR campaignTargetCVR);
    void delete(CampaignTargetCVR campaignTargetCVR);
    CampaignTargetCVR copyCampaignTargetCVR(CampaignTargetCVR campaignTargetCVR, Campaign newCampaign, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // BiddingStrategy
    //------------------------------------------------------------------------------------------
    
    void copyBiddingStrategies(Set<BiddingStrategy> biddingStrategy, Campaign targetCampaign, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // BidDeduction
    //------------------------------------------------------------------------------------------
    
    void copyBidDeductions(Set<BidDeduction> bidDeductions, Campaign targetCampaign, FetchStrategy... fetchStrategy);
    BidDeduction newBidDeduction(Campaign campaign, BidDeduction bidDeduction, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Balance And Budget
    //------------------------------------------------------------------------------------------
    
    /**
     * Get all active campaigns whose daily budget has been set, whose daily
     * budget alert is enabled, whose spending today has hit at least 80% of
     * the daily budget, and has not yet been notified today.
     */
    List<Campaign> getCampaignsToNotifyForDailyBudget(FetchStrategy ... fetchStrategy);

    /**
     * Delete campaign daily budget notification flags that no longer apply
     * due to the budget having been increased (or removed).
     */
    void resetDailyBudgetNotificationFlagsAsApplicable();
    
    /**
     * Get all active campaigns whose overall budget has been set, whose overall
     * budget alert is enabled, whose total spending has hit at least 80% of the
     * overall budget, and has not yet been notified.
     */
    List<Campaign> getCampaignsToNotifyForOverallBudget(FetchStrategy ... fetchStrategy);

    /**
     * Delete campaign overall budget notification flags that no longer apply
     * due to the budget having been increased (or removed).
     */
    void resetOverallBudgetNotificationFlagsAsApplicable();

}
