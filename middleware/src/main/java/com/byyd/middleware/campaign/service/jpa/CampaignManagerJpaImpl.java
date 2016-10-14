package com.byyd.middleware.campaign.service.jpa;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.BudgetSpend;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.CampaignAgencyDiscount;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignInternalLog;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.CampaignTargetCTR;
import com.adfonic.domain.CampaignTargetCVR;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.CampaignTrigger;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative.Status;
import com.adfonic.domain.Language;
import com.adfonic.domain.NotificationFlag.Type;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.campaign.dao.CampaignDao;
import com.byyd.middleware.campaign.dao.CampaignInternalLogDao;
import com.byyd.middleware.campaign.dao.CampaignNotificationFlagDao;
import com.byyd.middleware.campaign.dao.CampaignStoppageDao;
import com.byyd.middleware.campaign.filter.CampaignDataFeeFilter;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.filter.CampaignStateSyncingFilter;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.integrations.service.IntegrationsManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("campaignManager")
public class CampaignManagerJpaImpl extends BaseJpaManagerImpl implements CampaignManager {
    
    @Autowired(required = false)
    private CampaignDao campaignDao;
    
    @Autowired(required = false)
    private CampaignStoppageDao campaignStoppageDao;

    @Autowired(required = false)
    private CampaignNotificationFlagDao campaignNotificationFlagDao;
    
    @Autowired(required = false)
    private CampaignInternalLogDao campaignInternalLogDao;
    
    // ------------------------------------------------------------------------------------------
    // Campaign
    // ------------------------------------------------------------------------------------------
    @Override
    public void syncStates(Campaign source, Campaign destination, CampaignStateSyncingFilter params) {
        if(params.getSyncName()) {
            destination.setName(source.getName());
        }
        destination.setDescription(source.getDescription());
        destination.setReference(source.getReference());
        destination.setOpportunity(source.getOpportunity());

        if(params.getSyncBudgetData()) {
            destination.setDailyBudget(source.getDailyBudget());
            destination.setDailyBudgetWeekday(source.getDailyBudgetWeekday());
            destination.setDailyBudgetWeekend(source.getDailyBudgetWeekend());
            destination.setDailyBudgetAlertEnabled(source.isDailyBudgetAlertEnabled());
            destination.setOverallBudget(source.getOverallBudget());
            destination.setOverallBudgetAlertEnabled(source.isOverallBudgetAlertEnabled());

            destination.setBudgetType(source.getBudgetType());

            destination.setDailyBudgetClicks(source.getDailyBudgetClicks());
            destination.setOverallBudgetClicks(source.getOverallBudgetClicks());

            destination.setDailyBudgetImpressions(source.getDailyBudgetImpressions());
            destination.setOverallBudgetImpressions(source.getOverallBudgetImpressions());

            destination.setDailyBudgetConversions(source.getDailyBudgetConversions());
            destination.setOverallBudgetConversions(source.getOverallBudgetConversions());
        }

        if(params.getSyncLanguageData()) {
            destination.setDefaultLanguage(source.getDefaultLanguage());
            destination.setDisableLanguageMatch(source.getDisableLanguageMatch());
        }

        destination.setInstallTrackingEnabled(source.isInstallTrackingEnabled());
        destination.setInstallTrackingAdXEnabled(source.isInstallTrackingAdXEnabled());
        destination.setConversionTrackingEnabled(source.isConversionTrackingEnabled());
        destination.setApplicationID(source.getApplicationID());

        destination.setHouseAd(source.isHouseAd());
        destination.setThrottle(source.getThrottle());

        destination.setAdvertiserDomain(source.getAdvertiserDomain());

        if(params.getSyncCategory()) {
            destination.setCategory(source.getCategory());
        }

        if(params.getSyncStatus()) {
            destination.setStatus(source.getStatus());
        }

        destination.setCapImpressions(source.getCapImpressions());
        destination.setCapPeriodSeconds(source.getCapPeriodSeconds());
        destination.setCapPerCampaign(source.isCapPerCampaign());

        if(params.getSyncTransparentNetworks()) {
            destination.getTransparentNetworks().clear();
            if(source.getTransparentNetworks() != null) {
                destination.getTransparentNetworks().addAll(source.getTransparentNetworks());
            }
        }
        if(params.getSyncDeviceIdentifierTypes()) {
            destination.getDeviceIdentifierTypes().clear();
            if(source.getDeviceIdentifierTypes() != null) {
                destination.getDeviceIdentifierTypes().addAll(source.getDeviceIdentifierTypes());
            }
        }

        // BL-275: Even distribution
        destination.setEvenDistributionOverallBudget(source.isEvenDistributionOverallBudget());
        destination.setEvenDistributionDailyBudget(source.isEvenDistributionDailyBudget());

        // AT-984
        destination.setInventoryTargetingType(source.getInventoryTargetingType());
        destination.setPublicationList(source.getPublicationList());

        // BL-596
        destination.setPrivateMarketPlaceDeal(source.getPrivateMarketPlaceDeal());

        // AT-1091
        destination.setTargetCPA(source.getTargetCPA());
        
        // MAD-3167
        destination.setCurrencyExchangeRate(source.getCurrencyExchangeRate());
        
        // Set last exchange rate from Autofeed
        destination.setExchangeRate(source.getCurrencyExchangeRate().getCurrentExchangeRate());
        destination.setExchangeRateAdminChange(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getNewCampaignName(Campaign sourceCampaign) {
        Campaign localSourceCampaign = sourceCampaign;
        try {
            localSourceCampaign.getAdvertiser().getName();
        } catch(Exception e) {
            localSourceCampaign = this.getCampaignById(localSourceCampaign.getId(), new FetchStrategyBuilder().addInner(Campaign_.advertiser).build());
        }
        StringBuilder newName = new StringBuilder(COPY_CAMPAIGN_PREFIX);
        String newCampaignName = null;

        newName.append(localSourceCampaign.getName());
        while (newName.length() > MAX_CAMPAIGN_NAME_LEN) {
            newName.deleteCharAt(newName.length() - 1);
        }

        newCampaignName = newName.toString();
        if(isCampaignNameUnique(newCampaignName, localSourceCampaign.getAdvertiser(), localSourceCampaign)) {
            return newCampaignName;
        }else {
            newCampaignName = null;
            for (int i = 1; i < 100; i++) {
                newName.replace(newName.length() - 2, newName.length(), String.valueOf(i));
                newCampaignName = newName.toString();
                if(isCampaignNameUnique(newCampaignName, localSourceCampaign.getAdvertiser(), localSourceCampaign)) {
                    return newCampaignName;
                }
            }
        }
        return newCampaignName;
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign copyCampaignWithTimePeriods(Campaign sourceCampaign,
                                 FetchStrategy... fetchStrategy) {
        return copyCampaign(sourceCampaign, false, true, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign copyCampaign(Campaign sourceCampaign,
                                 FetchStrategy... fetchStrategy) {
        return copyCampaign(sourceCampaign, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign copyCampaign(Campaign sourceCampaign,
                                 boolean copyRemovedPublications,
                                 FetchStrategy... fetchStrategy) {
        return copyCampaign(sourceCampaign, copyRemovedPublications, false,fetchStrategy);
    }

    @Transactional(readOnly = false)
    private Campaign copyCampaign(Campaign sourceCampaign,
                                 boolean copyRemovedPublications,
                                 boolean copyTimePeriods,
                                 FetchStrategy... fetchStrategy) {

        Campaign dbSourceCampaign = this.getCampaignById(sourceCampaign.getId());

        Advertiser advertiser = dbSourceCampaign.getAdvertiser();

        String newCampaignName = this.getNewCampaignName(dbSourceCampaign);
        Campaign newCampaign = newCampaign(newCampaignName,
                                           advertiser,
                                           dbSourceCampaign.getCategory(),
                                           dbSourceCampaign.getDefaultLanguage(),
                                           dbSourceCampaign.getDisableLanguageMatch()/*,
                                           campaignFs*/);
        syncStates(dbSourceCampaign, newCampaign, CampaignStateSyncingFilter.FOR_NEW_INSTANCE);
        newCampaign = update(newCampaign);

        TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        Map<Segment, Segment> oldToNewSegments = copyCampaignSegments(dbSourceCampaign, newCampaign, targetingManager);

        if ((copyTimePeriods) && (dbSourceCampaign.getTimePeriods()!=null)){
            newCampaign = copyCampaignTimePeriods(dbSourceCampaign, newCampaign, targetingManager);
        }

        newCampaign = copyCampaignCreatives(dbSourceCampaign, newCampaign, oldToNewSegments);
        
        copyCampaignBiddingInfo(dbSourceCampaign, newCampaign);

        copyCampaignFees(dbSourceCampaign, newCampaign);

        if (copyRemovedPublications) {
            newCampaign = copyCampaignRemovedPublications(dbSourceCampaign, newCampaign);
        }

        copyCampaignAudiences(dbSourceCampaign, newCampaign);

        copyCampaignTriggers(dbSourceCampaign, newCampaign);
        
        return getCampaignById(newCampaign.getId(), fetchStrategy);
    }

    private Map<Segment, Segment> copyCampaignSegments(Campaign sourceCampaign, Campaign targetCampaign, TargetingManager targetingManager) {
        List<Segment> segments = sourceCampaign.getSegments();
        Map<Segment,Segment> oldToNewSegments = new HashMap<Segment,Segment>();
        targetCampaign.getSegments().clear();
        for(Segment segment : segments) {
            Segment newSegment = targetingManager.copySegment(segment, segment.getName());
            targetCampaign.getSegments().add(newSegment);
            oldToNewSegments.put(segment, newSegment);
        }
        return oldToNewSegments;
    }

    private Campaign copyCampaignTimePeriods(Campaign sourceCampaign, Campaign targetCampaign, TargetingManager targetingManager) {
        Campaign localTargetCampaign = targetCampaign;
        Set<CampaignTimePeriod> set = new HashSet<CampaignTimePeriod>(0);
        Date today = new Date();
        for(CampaignTimePeriod tp : sourceCampaign.getTimePeriods()){
            //if start and end date are null the period is not copied
            if(sourceCampaign.getTimePeriods().size()==1 && tp.getStartDate()==null && tp.getEndDate()==null){
                break;
            }
            if(tp.getEndDate()==null || tp.getEndDate().after(today) || tp.getEndDate().equals(today)){
                CampaignTimePeriod newPeriod = new CampaignTimePeriod();
                if(tp.getStartDate()!=null && tp.getStartDate().before(today)){
                    newPeriod.setStartDate(today);
                }else{
                    newPeriod.setStartDate(tp.getStartDate());
                }
                newPeriod.setEndDate(tp.getEndDate());
                newPeriod.setCampaign(localTargetCampaign);
                if(newPeriod.getStartDate()!=null && newPeriod.getEndDate()!=null){
                    set.add(newPeriod);
                }
            }
        }

        if(!set.isEmpty()){
            targetingManager.deleteCampaignTimePeriods(targetingManager.getAllCampaignTimePeriodsForCampaign(localTargetCampaign));
            localTargetCampaign.clearTimePeriods();
            targetingManager.addTimePeriodsToCampaign(localTargetCampaign, set);
            localTargetCampaign = update(localTargetCampaign);
        }
        return localTargetCampaign;
    }

    private Campaign copyCampaignCreatives(Campaign sourceCampaign, Campaign targetCampaign, Map<Segment, Segment> oldToNewSegments) {
        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
        List<Creative> creatives = creativeManager.getAllCreativesForCampaign(sourceCampaign);
        for(Creative creative : creatives) {
            Segment destinationSegment = oldToNewSegments.get(creative.getSegment());
            Creative newCreative = creativeManager.copyCreative(creative,
                                                     targetCampaign,
                                                     destinationSegment);
            targetCampaign.addCreative(newCreative);
        }
        return update(targetCampaign);
    }

    private void copyCampaignFees(Campaign sourceCampaign, Campaign targetCampaign) {
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);

        if (sourceCampaign.getCurrentRichMediaAdServingFee()!=null){
            feeManager.saveCampaignRichMediaAdServingFee(targetCampaign.getId(), sourceCampaign.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee());
        }
        
        if (sourceCampaign.getCurrentTradingDeskMargin()!=null){
            feeManager.saveCampaignTradingDeskMargin(targetCampaign.getId(), sourceCampaign.getCurrentTradingDeskMargin().getTradingDeskMargin());
        }
        
        CampaignAgencyDiscount oldAgencyDiscount = sourceCampaign.getCurrentAgencyDiscount();
        if (oldAgencyDiscount != null) {
            feeManager.newCampaignAgencyDiscount(targetCampaign, oldAgencyDiscount.getDiscount());
        }
    }

    private Campaign copyCampaignRemovedPublications(Campaign sourceCampaign, Campaign targetCampaign) {
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        for (Map.Entry<Publication,RemovalInfo> entry : sourceCampaign.getRemovedPublications().entrySet()) {
            // We need to "clone" the RemovalInfo values from the other creative
            RemovalInfo removalInfo = new RemovalInfo(entry.getValue());
            removalInfo = commonManager.create(removalInfo);
            targetCampaign.getRemovedPublications().put(entry.getKey(), removalInfo);
        }
        return  update(targetCampaign);
    }

    private void copyCampaignBiddingInfo(Campaign sourceCampaign, Campaign targetCampaign) {
        BiddingManager biddingManager = AdfonicBeanDispatcher.getBean(BiddingManager.class);
        
        CampaignBid oldBid = sourceCampaign.getCurrentBid();
        if (oldBid != null) {
            biddingManager.newCampaignBid(targetCampaign, oldBid.getBidType(), oldBid.getAmount(), oldBid.getBidModelType());
        }
        
        CampaignTargetCTR targetCTR = sourceCampaign.getTargetCTR();
        if(targetCTR != null) {
            biddingManager.copyCampaignTargetCTR(targetCTR, targetCampaign);
        }
        
        CampaignTargetCVR targetCVR = sourceCampaign.getTargetCVR();
        if(targetCVR != null) {
            biddingManager.copyCampaignTargetCVR(targetCVR, targetCampaign);
        }
        
        // MAD-2667: Media cost Optimization
        // MAD-3554: Average Maximum Bid
        Set<BiddingStrategy> biddingStrategies = sourceCampaign.getBiddingStrategies();
        if (biddingStrategies != null && !biddingStrategies.isEmpty()) {
        	biddingManager.copyBiddingStrategies(biddingStrategies, targetCampaign);
        }
        // MAD-3554: Average Maximum Bid threshold
        targetCampaign.setMaxBidThreshold(sourceCampaign.getMaxBidThreshold());
        
        // MAD-2711: Vendor pricing
        Set<BidDeduction> bidDeductions = sourceCampaign.getCurrentBidDeductions();
        if (bidDeductions != null && !bidDeductions.isEmpty()) {
        	biddingManager.copyBidDeductions(bidDeductions, targetCampaign);
        }
    }

    private void copyCampaignAudiences(Campaign sourceCampaign, Campaign targetCampaign) {
        if (!CollectionUtils.isEmpty(sourceCampaign.getCampaignAudiences())) {
            Set<CampaignAudience> audiencesCopy = new HashSet<CampaignAudience>();
            for(CampaignAudience ca: sourceCampaign.getCampaignAudiences()){
                CampaignAudience newCa = new CampaignAudience();
                newCa.setAudience(ca.getAudience());
                newCa.setInclude(ca.isInclude());

                // Audience Recency
                newCa.setRecencyDateFrom(ca.getRecencyDateFrom());
                newCa.setRecencyDateTo(ca.getRecencyDateTo());
                newCa.setRecencyDaysFrom(ca.getRecencyDaysFrom());
                newCa.setRecencyDaysTo(ca.getRecencyDaysTo());

                audiencesCopy.add(newCa);
            }
            AudienceManager audienceManager = AdfonicBeanDispatcher.getBean(AudienceManager.class);
            audienceManager.updateCampaignAudiences(targetCampaign, audiencesCopy);
        }
    }

    private void copyCampaignTriggers(Campaign sourceCampaign, Campaign targetCampaign) {
        if (CollectionUtils.isNotEmpty(sourceCampaign.getCampaignTriggers())){
            Set<CampaignTrigger> triggersCopy = new HashSet<CampaignTrigger>();
            for(CampaignTrigger ct : sourceCampaign.getCampaignTriggers()){
                CampaignTrigger newCt = new CampaignTrigger();
                newCt.setPluginVendor(ct.getPluginVendor());
                newCt.setPluginType(ct.getPluginType());
                triggersCopy.add(newCt);
            }
            IntegrationsManager integrationManager = AdfonicBeanDispatcher.getBean(IntegrationsManager.class);
            integrationManager.updateCampaignTriggers(targetCampaign, triggersCopy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaign(String name,
                                Advertiser advertiser,
                                Category category,
                                Language defaultLanguage,
                                boolean disableLanguageMatch,
                                FetchStrategy... fetchStrategy) {
        AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
        Advertiser dbAdvertiser = advertiserManager.getAdvertiserById(advertiser.getId());
        Campaign campaign = new Campaign(dbAdvertiser, name);
        campaign.setCategory(category);
        campaign.setDefaultLanguage(defaultLanguage);
        campaign.setDisableLanguageMatch(disableLanguageMatch);
        campaign = create(campaign);
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        campaign = feeManager.newCampaignAgencyDiscount(campaign, dbAdvertiser.getCompany().getDiscount());
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return campaign;
        } else {
            return getCampaignById(campaign.getId(), fetchStrategy);
        }
    }


    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaign(Campaign campaign, FetchStrategy... fetchStrategy) {
        Campaign dbCampaign = create(campaign);
        
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        feeManager.newCampaignAgencyDiscount(dbCampaign, dbCampaign.getAdvertiser().getCompany().getDiscount());
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return dbCampaign;
        } else {
            return getCampaignById(dbCampaign.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id, FetchStrategy... fetchStrategy) {
        return campaignDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Campaign create(Campaign campaign) {
        return campaignDao.create(campaign);
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign update(Campaign campaign) {
        // Sanity check
        BudgetType budgetType = campaign.getBudgetType();
        switch(budgetType) {
            case CLICKS:
                campaign.nullifyImpressionsBudgetFields();
                campaign.nullifyMonetaryBudgetFields();
                break;
    
            case IMPRESSIONS:
                campaign.nullifyClicksBudgetFields();
                campaign.nullifyMonetaryBudgetFields();
                break;
    
            case MONETARY:
                campaign.nullifyClicksBudgetFields();
                campaign.nullifyImpressionsBudgetFields();
                break;
            
            default:
                break;
        }
        return campaignDao.update(campaign);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Campaign campaign) {
        this.deleteDailyAndOverallSpend(campaign);
        BiddingManager biddingManager = AdfonicBeanDispatcher.getBean(BiddingManager.class);
        biddingManager.deleteCampaignBids(biddingManager.getAllCampaignBidsForCampaign(campaign));
        CampaignDataFeeFilter campaignDataFeeFilter = new CampaignDataFeeFilter();
        campaignDataFeeFilter.setCampaign(campaign);
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        feeManager.deleteCampaignDataFees(feeManager.getAllCampaignDataFees(campaignDataFeeFilter));
        feeManager.deleteCampaignRichMediaAdServingFees(feeManager.getAllCampaignRichMediaAdServingFeesForCampaign(campaign));
        feeManager.deleteCampaignTradingDeskMargins(feeManager.getAllCampaignTradingDeskMarginsForCampaign(campaign));
        List<CampaignAgencyDiscount> discounts = feeManager.getAllCampaignAgencyDiscountsForCampaign(campaign);
        if(!CollectionUtils.isEmpty(discounts)) {
            feeManager.deleteCampaignAgencyDiscounts(discounts);
        }
        campaignDao.delete(campaign);
    }

    private void deleteDailyAndOverallSpend(Campaign campaign) {
        this.campaignDao.deleteDailyAndOverallSpend(campaign);
    }


    @Override
    @Transactional(readOnly = false)
    public void deleteCampaigns(List<Campaign> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Campaign entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignByExternalId(String externalId,
            FetchStrategy... fetchStrategy) {
        return campaignDao.getByExternalId(externalId, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

     @Override
    @Transactional(readOnly = true)
    public Long countAllCampaigns(CampaignFilter filter) {
        return campaignDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaigns(CampaignFilter filter, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaigns(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaigns(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaigns(CampaignFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(filter, page, sort, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignsForAdvertiser(Advertiser advertiser) {
        return countAllCampaignsForAdvertiser(advertiser, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy) {
        return getAllCampaignsForAdvertiser(advertiser, (Boolean)null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllCampaignsForAdvertiser(advertiser, (Boolean)null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllCampaignsForAdvertiser(advertiser, (Boolean)null, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds) {
        return campaignDao.countAll(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds), page, fetchStrategy);
    }

    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsForAdvertiser(Advertiser advertiser, Boolean houseAds, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds), page, sort, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, FetchStrategy... fetchStrategy) {
        return campaignDao.getAllUsingTwoPhaseLoad(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getAllUsingTwoPhaseLoad(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsUsingTwoPhaseLoad(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignDao.getAllUsingTwoPhaseLoad(filter, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    protected CampaignFilter getCampaignFilter(
            Advertiser advertiser,
            List<Campaign.Status> statuses,
            Range<Date> dateRangeForActive,
            Boolean houseAds,String containsName) {
        return this.getCampaignFilter(advertiser, statuses, dateRangeForActive, houseAds, containsName, false);
    }

    protected CampaignFilter getCampaignFilter(
            Advertiser advertiser,
            List<Campaign.Status> statuses,
            Range<Date> dateRangeForActive,
            Boolean houseAds,String containsName,
            boolean nameWithPreviousSpace) {
        CampaignFilter filter = new CampaignFilter();
        filter.setAdvertiser(advertiser);
        filter.setStatuses(statuses);
        filter.setDateRangeForActive(dateRangeForActive);
        filter.setHouseAds(houseAds);
        filter.setContainsName(containsName);
        filter.setNameWithPreviousSpace(nameWithPreviousSpace);
        return filter;
    }

    public List<Campaign.Status> getCampaignStatusListForEverBeenActive() {
        List<Campaign.Status> list = new ArrayList<Campaign.Status>();
        list.add(Campaign.Status.ACTIVE);
        list.add(Campaign.Status.PAUSED);
        list.add(Campaign.Status.COMPLETED);
        list.add(Campaign.Status.STOPPED);
        return list;
    }
    public List<Campaign.Status> getCampaignStatusActivePaused() {
        List<Campaign.Status> list = new ArrayList<Campaign.Status>();
        list.add(Campaign.Status.ACTIVE);
        list.add(Campaign.Status.PAUSED);
        return list;
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser) {
        return countAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,null), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,null), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,null), page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace) {
        return countAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,containsName, nameWithPreviousSpace));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,containsName, nameWithPreviousSpace), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,containsName, nameWithPreviousSpace), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, String containsName, boolean nameWithPreviousSpace, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, null,containsName, nameWithPreviousSpace), page, fetchStrategy);
    }

   // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds) {
        return countAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, houseAds,null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, houseAds,null), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, houseAds,null), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsThatHaveEverBeenActiveForAdvertiser(Advertiser advertiser, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusListForEverBeenActive(), null, houseAds,null), page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaignsActivePausedForAdvertiser(Advertiser advertiser,String containsName ,FetchStrategy... fetchStrategy) {
        return getAllCampaigns(getCampaignFilter(advertiser, getCampaignStatusActivePaused(), null, null,containsName), fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BudgetSpend getBudgetSpendForCampaign(Campaign campaign, Date date) {
        if ((campaign == null) || (date == null)) {
            return null;
        }

        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "dailySpendMap", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "advertiser", JoinType.INNER);
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);

        Campaign dbCampaign = getCampaignById(campaign.getId(), fs);
        AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
        Advertiser advertiser = advertiserManager.getAdvertiserById(dbCampaign.getAdvertiser().getId(), fs);
        TimeZone timeZone = advertiser.getCompany().getDefaultTimeZone();

        return dbCampaign.getDailySpendMap().get(DateUtils.getTimeID(date, timeZone) / 100);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBudgetSpendAmountForCampaign(Campaign campaign, Date date) {
        BudgetSpend bs = getBudgetSpendForCampaign(campaign, date);
        if (bs == null) {
            return null;
        }
        return bs.getAmount();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean campaignHasDailySpend(Campaign campaign, Date fromDate, Date toDate) {
        if(campaign == null) {
            return false;
        }
        Campaign dbCampaign = getCampaignById(campaign.getId());
        AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
        Advertiser advertiser = advertiserManager.getAdvertiserById(dbCampaign.getAdvertiser().getId());
        TimeZone timeZone = advertiser.getCompany().getDefaultTimeZone();

        int fromTimeId = DateUtils.getTimeID(fromDate, timeZone) / 100;
        int toTimeId = DateUtils.getTimeID(toDate, timeZone) / 100;

        return campaignHasDailySpend(dbCampaign, fromTimeId, toTimeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean campaignHasDailySpend(Campaign campaign, int fromTimeId, int toTimeId) {
        if(campaign == null) {
            return false;
        }
        BigDecimal amount = campaignDao.getTotalDailySpendForCampaign(campaign, fromTimeId, toTimeId);
        return amount.longValue() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getHouseAdCountForPublisher(Publisher publisher) {
        return campaignDao.getHouseAdCountForPublisher(publisher);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign) {
        return campaignDao.countCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, FetchStrategy... fetchStrategy) {
        return campaignDao.getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignDao.getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignDao.getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignNameUnique(String name, Advertiser advertiser, Campaign excludeCampaign) {
        // It's unique if there's no *other* campaign (if excludeCampaign is specified)
        // belonging to the given advertiser with the same name (case-insensitive).
        // Campaigns with status=DELETED are omitted from the search.
        CampaignFilter filter = new CampaignFilter()
            .setAdvertiser(advertiser)
            .setStatuses(EnumSet.complementOf(EnumSet.of(Campaign.Status.DELETED)))
            .setName(name, false); // case-insensitive
        if (excludeCampaign != null) {
            filter.setExcludedIds(Collections.singleton(excludeCampaign.getId()));
        }

        return countAllCampaigns(filter) < 1;
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses) {
        return campaignDao.countAll(this.getCampaignFilter(advertiser, statuses, dateRangeForActive, houseAds,null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, FetchStrategy... fetchStrategy) {
        return campaignDao.getAll(this.getCampaignFilter(advertiser, statuses, dateRangeForActive, houseAds,null), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy) {
          return campaignDao.getAll(this.getCampaignFilter(advertiser, statuses, dateRangeForActive, houseAds,null), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForAdvertiserAndDateRangeAndStatuses(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, List<Campaign.Status> statuses, Pagination page, FetchStrategy... fetchStrategy) {
          return campaignDao.getAll(this.getCampaignFilter(advertiser, statuses, dateRangeForActive, houseAds,null), page, fetchStrategy);
    }


    @Override
    @Transactional(readOnly = true)
    public Long countActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds) {
        return this.countCampaignsForAdvertiserAndDateRangeAndStatuses(advertiser, dateRangeForActive, houseAds, getCampaignStatusListForEverBeenActive());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, FetchStrategy... fetchStrategy) {
        return this.getCampaignsForAdvertiserAndDateRangeAndStatuses(advertiser, dateRangeForActive, houseAds, getCampaignStatusListForEverBeenActive(), new Sorting(asc(Campaign.class, "name")), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getCampaignsForAdvertiserAndDateRangeAndStatuses(advertiser, dateRangeForActive, houseAds, getCampaignStatusListForEverBeenActive(), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaignsForPeriod(Advertiser advertiser, Range<Date> dateRangeForActive, Boolean houseAds, Pagination page, FetchStrategy... fetchStrategy) {
        Pagination localPage = page;
        if(localPage.getSorting() == null) {
            localPage = new Pagination(page, new Sorting(asc(Campaign.class, "name")));
        }
        return this.getCampaignsForAdvertiserAndDateRangeAndStatuses(advertiser, dateRangeForActive, houseAds, getCampaignStatusListForEverBeenActive(), localPage, fetchStrategy);
    }


    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignCurrentlyActive(Campaign campaign) {
        Campaign localCampaign = campaign;
        if(localCampaign.getStatus() != Campaign.Status.ACTIVE) {
            return false;
        }
        try {
            localCampaign.getTimePeriods().size();
        } catch(Exception e) {
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Campaign_.timePeriods)
                                   .build();
            localCampaign = getCampaignById(localCampaign.getId(), fs);
        }
        return localCampaign.isCurrentlyActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignCurrentlyScheduled(Campaign campaign) {
        Campaign localCampaign = campaign;
        if(localCampaign.getStatus() != Campaign.Status.ACTIVE) {
            return false;
        }
        try {
            localCampaign.getTimePeriods().size();
        } catch(Exception e) {
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Campaign_.timePeriods)
                                   .build();
            localCampaign = getCampaignById(localCampaign.getId(), fs);
        }
        return !localCampaign.isCurrentlyActive();
    }

    // ------------------------------------------------------------------------------------------

    protected CampaignFilter getCampaignFilter(PublicationList publicationList) {
        List<Campaign.Status> statuses = new ArrayList<Campaign.Status>();
        statuses.add(Campaign.Status.NEW);
        statuses.add(Campaign.Status.NEW_REVIEW);
        statuses.add(Campaign.Status.PAUSED);
        statuses.add(Campaign.Status.PENDING);
        statuses.add(Campaign.Status.ACTIVE);
        return new CampaignFilter().setPublicationList(publicationList).setStatuses(statuses);
    }

    protected CampaignFilter getCampaignFilter(PublicationList publicationList, List<Campaign.Status> statuses) {
        return new CampaignFilter().setPublicationList(publicationList).setStatuses(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses) {
        return this.countAllCampaigns(getCampaignFilter(publicationList, statuses));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, FetchStrategy... fetchStrategy) {
        return this.getAllCampaigns(getCampaignFilter(publicationList, statuses), new Sorting(asc(Campaign.class, "name")), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllCampaigns(getCampaignFilter(publicationList, statuses), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, List<Campaign.Status> statuses, Pagination page, FetchStrategy... fetchStrategy) {
        Pagination localPage = page;
        if(localPage.getSorting() == null) {
            localPage = new Pagination(localPage, new Sorting(asc(Campaign.class, "name")));
        }
        return this.getAllCampaigns(getCampaignFilter(publicationList, statuses), localPage, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCampaignsForPublicationList(PublicationList publicationList) {
        return this.countAllCampaigns(getCampaignFilter(publicationList));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy) {
        return this.getAllCampaigns(getCampaignFilter(publicationList), new Sorting(asc(Campaign.class, "name")), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllCampaigns(getCampaignFilter(publicationList), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy) {
        Pagination localPage = page;
        if(localPage.getSorting() == null) {
            localPage = new Pagination(localPage, new Sorting(asc(Campaign.class, "name")));
        }
        return this.getAllCampaigns(getCampaignFilter(publicationList), localPage, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> adOpsActivateNewCampaign(
            Campaign campaign,
            String advertiserDomain,
            SegmentSafetyLevel safetyLevel,
            Category campaignIabCategory,
            Set<Category> blackListedPublicationCategories,
            AdfonicUser adfonicUser) {
        Map<String, Object> rcMap = new HashMap<>();

        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
        List<Creative> creatives = creativeManager.getAllCreativesForCampaign(campaign);
        if(CollectionUtils.isEmpty(creatives)) {
            rcMap.put(CAMPAIGN_ACTIVATION_STATUS, false);
            rcMap.put(CAMPAIGN_ACTIVATION_ERROR_MESSAGE, "This campaign has no creatives");
            return rcMap;
        }
        // Each creative will get a Map of statuses and other things, linked to its PK
        Map<Long, Map<String, Object>> creativeActivationRcs = new HashMap<>();
        rcMap.put(CREATIVES_ACTIVATION_STATUSES, creativeActivationRcs);
        boolean atLeastOneCreativeActive = false;
        for(Creative creative : creatives) {
            if(creative.getStatus() == Creative.Status.ACTIVE) {
                // Already activated, skip
                atLeastOneCreativeActive = true;
                continue;
            }
            Map<String, Object> creativeMap = new HashMap<>();
            creativeMap.put(CREATIVE_OLD_STATUS, creative.getStatus());
            creativeActivationRcs.put(creative.getId(), creativeMap);
            boolean mustUpdate = false;
            if (creative.getStatus() == Status.PENDING || creative.getStatus() == Status.REJECTED) {
                creative.setStatus(Status.ACTIVE);
                atLeastOneCreativeActive = true;
                creativeMap.put(CREATIVE_NEW_STATUS, Status.ACTIVE);
                mustUpdate = true;
            } else if (creative.getStatus() == Status.NEW) {
                creative.setStatus(Status.ACTIVE);
                atLeastOneCreativeActive = true;
                creativeMap.put(CREATIVE_NEW_STATUS, Status.ACTIVE);
                mustUpdate = true;
            } else if (creative.getStatus() == Status.PENDING_PAUSED) {
                creative.setStatus(Status.PAUSED);
                creativeMap.put(CREATIVE_NEW_STATUS, Status.PAUSED);
                mustUpdate = true;
            } else {
                creativeMap.put(CreativeManager.CREATIVE_STATUS_UPDATE_STATUS, false);
                creativeMap.put(CreativeManager.CREATIVE_STATUS_UPDATE_ERROR_MESSAGE, "Cannot activate a Creative with status " + creative.getStatus());
            }
            if(mustUpdate) {
                creative = creativeManager.update(creative);

                // Save this change in creative history in JJ
                creativeManager.newCreativeHistory(creative, "Ad Ops Approved new", adfonicUser);

                creativeMap.put(CreativeManager.CREATIVE_STATUS_UPDATE_STATUS, true);
                if(creative.getStatus().equals(Status.ACTIVE)) {
                    atLeastOneCreativeActive = true;
                }
            }
        }

        if(atLeastOneCreativeActive) {
            Segment segment = campaign.getSegments().get(0);
            campaign.setStatus(Campaign.Status.ACTIVE);
            campaign.setAdvertiserDomain(advertiserDomain);
            campaign.setCategory(campaignIabCategory);
            segment.getExcludedCategories().addAll(blackListedPublicationCategories);
            segment.setSafetyLevel(safetyLevel);
            TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
            targetingManager.update(segment);
            update(campaign);
            rcMap.put(CAMPAIGN_ACTIVATION_STATUS, true);
        } else {
            rcMap.put(CAMPAIGN_ACTIVATION_STATUS, false);
            rcMap.put(CAMPAIGN_ACTIVATION_ERROR_MESSAGE, "No creatives from this campaign could be activated");
        }
        return rcMap;
    }

    
    @Override
    @Transactional(readOnly = false)
    public void adOpsUpdateExistingCampaign(
            Campaign campaign,
            String advertiserDomain,
            SegmentSafetyLevel safetyLevel,
            Category campaignIabCategory,
            Set<Category> blackListedPublicationCategories,
            boolean approveAllNewCreatives,
            AdfonicUser adfonicUser) {
        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
        
        if(approveAllNewCreatives) {
            boolean atLeastOneCreativeApproved = false;
            List<Creative> creatives = creativeManager.getAllCreativesForCampaign(campaign);
            for(Creative creative : creatives) {
                if(creative.getStatus().equals(Status.PENDING)) {
                    creative.setStatus(Status.ACTIVE);
                    creative = creativeManager.update(creative);
                    atLeastOneCreativeApproved = true;

                    // Save this change in creative history in JJ
                    creativeManager.newCreativeHistory(creative, "Ad Ops Approved existing", adfonicUser);
                }
            }
            if(atLeastOneCreativeApproved) {
                if(campaign.getStatus().equals(Campaign.Status.PENDING)) {
                    campaign.setStatus(Campaign.Status.ACTIVE);
                } else if (campaign.getStatus().equals(Campaign.Status.PENDING_PAUSED)) {
                    campaign.setStatus(Campaign.Status.PAUSED);
                }
            }
        }

        Segment segment = campaign.getSegments().get(0);
        campaign.setAdvertiserDomain(advertiserDomain);
        campaign.setCategory(campaignIabCategory);
        segment.getExcludedCategories().clear();
        segment.getExcludedCategories().addAll(blackListedPublicationCategories);
        segment.setSafetyLevel(safetyLevel);
        TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        segment = targetingManager.update(segment);
        update(campaign);
    }
    
    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = false)
    public Campaign removePublicationFromCampaign(Campaign campaign, Publication publication, RemovalInfo.RemovalType removalType) {
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        
        RemovalInfo removalInfo = campaign.getRemovedPublications().get(publication);
        if (removalInfo != null) {
            if (removalInfo.getRemovalType().equals(removalType)) {
                // It's already set up as removed (or unremoved) with the given removalType.
                // Silently do nothing...
                return campaign;
            } else {
                // The removal type is changing.
                campaign.getRemovedPublications().remove(publication);
                // ...and fall through to the code below, which will add the replacement.
            }
        }

        removalInfo = new RemovalInfo(removalType);
        removalInfo = commonManager.create(removalInfo);

        campaign.getRemovedPublications().put(publication, removalInfo);

        return update(campaign);
    }

    @Override
    @Transactional(readOnly = false)
    public Campaign unremovePublicationFromCampaign(Campaign campaign, Publication publication) {
        return this.removePublicationFromCampaign(campaign, publication, RemovalInfo.RemovalType.UNREMOVED);
    }
    
    // ------------------------------------------------------------------------------------------
    // CampaignStoppage
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public CampaignStoppage newCampaignStoppage(Campaign campaign, CampaignStoppage.Reason reason, FetchStrategy... fetchStrategy) {
        return newCampaignStoppage(campaign, reason, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignStoppage newCampaignStoppage(Campaign campaign, CampaignStoppage.Reason reason, Date reactivateDate, FetchStrategy... fetchStrategy) {
        CampaignStoppage stoppage = new CampaignStoppage(campaign, reason, reactivateDate);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(stoppage);
        } else {
            stoppage = create(stoppage);
            return getCampaignStoppageById(stoppage.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStoppage getCampaignStoppageById(String id,
            FetchStrategy... fetchStrategy) {
        return getCampaignStoppageById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignStoppage getCampaignStoppageById(Long id,
            FetchStrategy... fetchStrategy) {
        return campaignStoppageDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignStoppage create(CampaignStoppage campaignStoppage) {
        return campaignStoppageDao.create(campaignStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignStoppage update(CampaignStoppage campaignStoppage) {
        return campaignStoppageDao.update(campaignStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(CampaignStoppage campaignStoppage) {
        campaignStoppageDao.delete(campaignStoppage);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCampaignStoppages(List<CampaignStoppage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CampaignStoppage entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getCampaignStoppagesFieldsForNullOrFutureReactivateDate() {
        return campaignStoppageDao.getFieldsForNullOrFutureReactivateDate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignStoppage> getCampaignStoppagesForNullOrFutureReactivateDate(
            FetchStrategy... fetchStrategy) {
        return campaignStoppageDao
                .getAllForReactivateDateIsNullOrReactivateDateGreaterThan(
                        new Date(), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignStoppage> getCampaignStoppagesForCampaignAndNullOrFutureReactivateDate(
            Campaign campaign, FetchStrategy... fetchStrategy) {
        return campaignStoppageDao
                .getAllForCampaignAndReactivateDateIsNullOrReactivateDateGreaterThan(
                        campaign, new Date(), fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // CampaignNotificationFlag
    // ------------------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    protected Campaign getCampaignObjectForCampaignNotificationFlagCreation(Campaign campaign) {
        Campaign localCampaign = campaign;
        try {
            Advertiser advertiser = localCampaign.getAdvertiser();
            advertiser.getCompany();
        } catch(Exception e) {
            // Not hydrated right, reload locally
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(Campaign.class, "advertiser", JoinType.INNER);
            fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);
            localCampaign = this.getCampaignById(localCampaign.getId(), fs);
        }
        return localCampaign;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignNotificationFlag newCampaignNotificationFlag(Campaign campaign, Type type, int ttlSeconds, FetchStrategy... fetchStrategy) {
        Campaign localCampaign = getCampaignObjectForCampaignNotificationFlagCreation(campaign);
        CampaignNotificationFlag flag = new CampaignNotificationFlag(localCampaign, type, ttlSeconds);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return campaignNotificationFlagDao.create(flag);
        } else {
            flag = campaignNotificationFlagDao.create(flag);
            return getCampaignNotificationFlagById(flag.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignNotificationFlag newCampaignNotificationFlag(Campaign campaign, Type type, Date expirationDate, FetchStrategy... fetchStrategy) {
        Campaign localCampaign = getCampaignObjectForCampaignNotificationFlagCreation(campaign);
        CampaignNotificationFlag flag = new CampaignNotificationFlag(localCampaign, type, expirationDate);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return campaignNotificationFlagDao.create(flag);
        } else {
            flag = campaignNotificationFlagDao.create(flag);
            return getCampaignNotificationFlagById(flag.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignNotificationFlag getCampaignNotificationFlagById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignNotificationFlagById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignNotificationFlag getCampaignNotificationFlagById(Long id, FetchStrategy... fetchStrategy) {
        return campaignNotificationFlagDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignNotificationFlag update(CampaignNotificationFlag campaignNotificationFlag) {
        return campaignNotificationFlagDao.update(campaignNotificationFlag);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(CampaignNotificationFlag campaignNotificationFlag) {
        campaignNotificationFlagDao.delete(campaignNotificationFlag);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCampaignNotificationFlags(List<CampaignNotificationFlag> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CampaignNotificationFlag entry : list) {
            delete(entry);
        }
    }
    
    //------------------------------------------------------------------------------------------
    // Campaign Internal Log Level Data (LLD)
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public boolean isCampaignInternalLLDEnabled(Long campaignId){
        boolean isEnabled = false;
        
        Campaign campaign = campaignDao.getById(campaignId);
        if (campaign != null){
            CampaignInternalLog campaignInternalLog = campaignInternalLogDao.getByCampaign(campaign);
            if (campaignInternalLog != null){
                isEnabled = campaignInternalLog.isLldEnabled();
            }
        }
        
        return isEnabled;
    }
    
    @Override
    @Transactional(readOnly = false)
    public CampaignInternalLog enableCampaignInternalLLD(Long campaignId){
        CampaignInternalLog campaignInternalLog = null;
        
        Campaign campaign = campaignDao.getById(campaignId);
        if (campaign != null){
            campaignInternalLog = campaignInternalLogDao.getByCampaign(campaign);
            if (campaignInternalLog==null){
               // Create lld information
               campaignInternalLog = new CampaignInternalLog();
               campaignInternalLog.setCampaign(campaign);
               campaignInternalLog.setCompany(campaign.getAdvertiser().getCompany());
               campaignInternalLog.setLldEnabled(true);
               campaignInternalLog.setCreatedAt(new Date());
               campaignInternalLog = this.campaignInternalLogDao.create(campaignInternalLog);
            }else if(!campaignInternalLog.isLldEnabled()){
               // Update lld information
               campaignInternalLog.setLldEnabled(true);
               campaignInternalLog.setUpdatedAt(new Date());
               campaignInternalLog = this.campaignInternalLogDao.update(campaignInternalLog);
            }
        }
        
        return campaignInternalLog;
    }
    
    @Override
    @Transactional(readOnly = false)
    public CampaignInternalLog disableCampaignInternalLLD(Long campaignId){
        CampaignInternalLog campaignInternalLog = null;
        
        Campaign campaign = campaignDao.getById(campaignId);
        if (campaign != null){
            campaignInternalLog = campaignInternalLogDao.getByCampaign(campaign);
            if ((campaignInternalLog!=null)&&(campaignInternalLog.isLldEnabled())){
               // Update lld information
               campaignInternalLog.setLldEnabled(false);
               campaignInternalLog.setUpdatedAt(new Date());
               campaignInternalLog = this.campaignInternalLogDao.update(campaignInternalLog);
            }
        }
        
        return campaignInternalLog;
    }
}
