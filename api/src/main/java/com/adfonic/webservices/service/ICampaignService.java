package com.adfonic.webservices.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Segment;
import com.adfonic.domain.User;
import com.adfonic.webservices.dto.CampaignBidDTO;

public interface ICampaignService extends IOwnedEntityService<Campaign> {

    public Campaign createMinimalCampaign(Advertiser advertiser, Collection<Segment> segments, String name, String defaultLanguage);

    public void submit(Campaign campaign);

    public void delete(Campaign campaign);

    public void setDailyBudgets(Campaign campaign, BudgetType budgetType, BigDecimal dailyBudget, BigDecimal dailyBudgetWeekday, BigDecimal dailyBudgetWeekend);

    public void setInstallTracking(Campaign campaign, Boolean installTrackingEnabled, String applicationID);

    public void setTimePeriods(Campaign campaign, Collection<CampaignTimePeriod> timePeriods);

    void validateNewBid(CampaignBidDTO bid, Campaign campaign);

    public void createNewBid(Campaign campaign, BidType bidType, BigDecimal amount);
    
    void resubmitAdXCreatives(Campaign campaign);

    public List<Creative> getAllCreativesForCampaign(User user, String externalID);
    
    public void setInventoryTargeting(Campaign campaign, String pubList, boolean hasSegmentParams);
}
