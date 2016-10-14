package com.byyd.middleware.campaign.service.jpa;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.CampaignTargetCTR;
import com.adfonic.domain.CampaignTargetCVR;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Country;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.TransparentNetwork_;
import com.byyd.middleware.campaign.dao.BidDeductionDao;
import com.byyd.middleware.campaign.dao.CampaignBidDao;
import com.byyd.middleware.campaign.dao.CampaignTargetCTRDao;
import com.byyd.middleware.campaign.dao.CampaignTargetCVRDao;
import com.byyd.middleware.campaign.exception.CampaignBidTooLowException;
import com.byyd.middleware.campaign.exception.CampaignBidWrongFormatException;
import com.byyd.middleware.campaign.filter.BidDeductionFilter;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("biddingManager")
public class BiddingManagerJpaImpl extends BaseJpaManagerImpl implements BiddingManager {
    
    private static final Logger LOG = Logger.getLogger(BiddingManagerJpaImpl.class.getName());
    

    private static final FetchStrategy BID_VALIDATION_TRANSPARENT_NETWORK_FS = new FetchStrategyBuilder()
                                                                                .addLeft(TransparentNetwork_.rateCardMap)
                                                                                .build();
    private static final FetchStrategy BID_VALIDATION_CAMPAIGN_FS = new FetchStrategyBuilder()
                                                                    .addLeft(Campaign_.segments)
                                                                    .addLeft(Campaign_.transparentNetworks)
                                                                    .build();
    private static final FetchStrategy BID_VALIDATION_SEGMENT_FS = new FetchStrategyBuilder()
                                                                    .addLeft(Segment_.countries)
                                                                    .build();
    
    @Autowired(required = false)
    private CampaignBidDao campaignBidDao;
    
    @Autowired(required = false)
    private BidDeductionDao bidDeductionDao;
    
    @Autowired(required=false)
    private CampaignTargetCTRDao campaignTargetCTRDao;
    
    @Autowired(required=false)
    private CampaignTargetCVRDao campaignTargetCVRDao;
    
    
    // ------------------------------------------------------------------------------------------
    // CampaignBid
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount) {
        return newCampaignBid(campaign, bidType, amount, BidModelType.NORMAL);
    }

    // Method used with the DSP development
    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount,boolean maximum) {
        BidModelType modelType;
        if(maximum){
            modelType = BidModelType.DSP_LIC;
        }else{
            modelType = BidModelType.NORMAL;
        }
        return newCampaignBid(campaign, bidType, amount, modelType);
    }

    /**
     * NOTE: currentBid MUST be hydrated on Campaign for this method to work
     *
     * @param campaign
     * @param bidType
     * @param amount
     * @param modelType
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaignBid(Campaign campaign, BidType bidType, BigDecimal amount,CampaignBid.BidModelType modelType) {
        Date now = new Date();
        CampaignBid currentBid = campaign.getCurrentBid();
        if(currentBid != null) {
            currentBid.setEndDate(now);
            update(currentBid);
        }

        // this is actually a bad pattern. Since we return the updated campaign, reloading it here just for
        // the purpose of hydrating the historical bids is a bad idea, as it will potentially remove
        // previous hydrations. This shoudl be removed, and the caller should be made responsible for proper
        // hydration
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        Campaign localCampaign = campaign;
        try {
            localCampaign.getHistoricalBids().size();
        }catch (Exception e) {
            // hydrate with historical bids
            localCampaign = campaignManager.getCampaignById(localCampaign.getId(),
                    new FetchStrategyBuilder().addLeft(Campaign_.historicalBids)
                    .build());
        }

        CampaignBid cb = localCampaign.createNewBid(bidType, amount, now, modelType);
        create(cb);
        return campaignManager.update(localCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignBid getCampaignBidById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignBidById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignBid getCampaignBidById(Long id, FetchStrategy... fetchStrategy) {
        return campaignBidDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignBid create(CampaignBid campaignBid) {
        if(campaignBid.getBidModelType()==null){
            if(campaignBid.isMaximum()){
                campaignBid.setBidModelType(BidModelType.DSP_LIC);
            }else{
                campaignBid.setBidModelType(BidModelType.NORMAL);
            }
        }
        return campaignBidDao.create(campaignBid);
    }

    @Transactional(readOnly = false)
    public CampaignBid update(CampaignBid campaignBid) {
        if(campaignBid.getBidModelType()==null){
            if(campaignBid.isMaximum()){
                campaignBid.setBidModelType(BidModelType.DSP_LIC);
            }else{
                campaignBid.setBidModelType(BidModelType.NORMAL);
            }
        }
        return campaignBidDao.update(campaignBid);
    }

    @Transactional(readOnly = false)
    public void delete(CampaignBid campaignBid) {
        campaignBidDao.delete(campaignBid);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteCampaignBids(List<CampaignBid> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CampaignBid entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<BigDecimal> getReferenceBids(Campaign campaign, BidType bidType) {
        // First off, make sure the campaign is hydrated properly
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Campaign_.transparentNetworks)
                           .addLeft(Campaign_.segments)
                           .addLeft(Segment_.countries)
                           .addLeft(Segment_.operators)
                           .addLeft(Segment_.vendors)
                           .addLeft(Segment_.models)
                           .build();
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        Campaign dbCampaign = campaignManager.getCampaignById(campaign.getId(), fs);

        return campaignBidDao.getReferenceBids(dbCampaign, bidType);
    }

    /**
     * Gets an array of counts of the number of bids in each part of the
     * distribution.
     * @param defaultDiff the minimal 'size' to use for the calculation,
     *        e.g. divide between minimumBid and minimumBid + defaultDiff.
     */
    @Override
    public int[] getBidDensity(List<BigDecimal> saneBids, int numSegments, BigDecimal minimumBid, BigDecimal defaultDiff) {
        // Segment this into numSegments buckets
        int[] segments = new int[numSegments];

        // If no bids, bail
        if (saneBids.isEmpty()) {
            return segments;
        }

        BigDecimal diff = saneBids.get(saneBids.size()-1).subtract(minimumBid)
            .max(defaultDiff);

        // Avoid division by zero
        double diffVal = diff.doubleValue();
        if (diffVal == 0.0) {
            return segments;
        }

        for (BigDecimal bidAmount : saneBids) {
            int segment = (int) (numSegments * bidAmount.subtract(minimumBid).doubleValue() / diffVal);
            if (segment >= numSegments) { 
                segment = numSegments - 1; 
            }
            if (segment >= 0) {  // ignore bids below minimum
                segments[segment]++;
            }
        }

        return segments;
    }

    // ------------------------------------------------------------------------------------------
    /*
     * determine min bid for a transparent network respecting segment country targeting
     * getAvailableBidTypes should already exclude bid types that aren't valid for
     * the network
     */
    @Override
    @Transactional(readOnly = true)
    public Map<BidType,BigDecimal> getMinBidMap(Campaign campaign, TransparentNetwork network, Segment currentSegment) {
        PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
        boolean rateCardSet = false;
        TransparentNetwork localNetwork = network;
        try {
            localNetwork.getRateCardMap().size();
            rateCardSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!rateCardSet) {
            localNetwork = publicationManager.getTransparentNetworkById(localNetwork.getId(), BID_VALIDATION_TRANSPARENT_NETWORK_FS);
        }

        Map<BidType,BigDecimal> minBidMap = new HashMap<BidType,BigDecimal>();

        for (BidType bidType : getAvailableBidTypesForCampaign(campaign)) {
            RateCard rateCard = null;

            if (!localNetwork.isDefaultRateCard() && localNetwork.getRateCard(bidType) != null) {
                rateCard = localNetwork.getRateCard(bidType);
            }else {
                rateCard = publicationManager.getRateCardByBidType(bidType);
            }

            BigDecimal minBid = rateCard.getDefaultMinimum();

            for (Country c : currentSegment.getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }

            minBidMap.put(bidType, minBid);
        }
        return minBidMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidType> getAvailableBidTypesForCampaign(Campaign campaign) {
        List<BidType> allowed = java.util.Arrays.asList(BidType.values());

        if (!campaign.isTransparent()) {
            return allowed;
        } else {
            PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
            // Transparent networks may not allow all bid types
            // All targeted networks must support the bid type
            for (TransparentNetwork p : campaign.getTransparentNetworks()) {
                p = publicationManager.getTransparentNetworkById(p.getId(), BID_VALIDATION_TRANSPARENT_NETWORK_FS);
                // for default don't remove any of the bid types
                if (!p.isDefaultRateCard()) {
                    allowed.retainAll(p.getRateCardMap().keySet());
                }
            }
            return allowed;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMinimumBidForCampaign(Campaign campaign, BidType bidType) {
        Campaign localCampaign = campaign;
        try {
            localCampaign.getSegments().size();
            localCampaign.getTransparentNetworks().size();
        } catch(Exception e) {
            CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
            localCampaign = campaignManager.getCampaignById(localCampaign.getId(), BID_VALIDATION_CAMPAIGN_FS);
        }
        return getMinimumBidForCampaign(localCampaign, localCampaign.getSegments().get(0), bidType);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMinimumBidForCampaign(Campaign campaign, Segment segment, BidType bidType) {
         if (bidType != null) {
            Campaign localCampaign = campaign;
            try {
                localCampaign.getTransparentNetworks().size();
            } catch(Exception e) {
                CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
                localCampaign = campaignManager.getCampaignById(localCampaign.getId(), BID_VALIDATION_CAMPAIGN_FS);
            }

            // Based on the code in CampaignBean, a campaign is premium if and only if it has no transparent networks
            boolean isPremiumCampaign = false;
            if (!localCampaign.getTransparentNetworks().isEmpty()) {
                isPremiumCampaign = true;
            }

            PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
            if (isPremiumCampaign) {
                return getMinimumBidForPremiumCampaign(bidType, localCampaign, publicationManager);
            } else {
                // Otherwise minimums are dictated by geographical targeting
                Segment localSegment = segment;
                try {
                    localSegment.getCountries().size();
                } catch(Exception e) {
                    TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
                    localSegment = targetingManager.getSegmentById(localSegment.getId(), BID_VALIDATION_SEGMENT_FS);
                }

                RateCard rateCard = publicationManager.getRateCardByBidType(bidType);
                BigDecimal minBid = rateCard.getDefaultMinimum();
                for (Country c : localSegment.getCountries()) {
                    minBid = minBid.max(rateCard.getMinimumBid(c));
                }
                return minBid;
            }
        } else {
            //this should not happen
            return new BigDecimal(0);
        }
    }

    private BigDecimal getMinimumBidForPremiumCampaign(BidType bidType, Campaign localCampaign, PublicationManager publicationManager) {
        BigDecimal result = new BigDecimal(0);
        Set<TransparentNetwork> trNet = localCampaign.getTransparentNetworks();
        if(!CollectionUtils.isEmpty(trNet)) {
            result = getMinimunForTransparentNetworks(trNet, bidType, publicationManager, result);
        }
        return result;
    }

    private BigDecimal getMinimunForTransparentNetworks(Set<TransparentNetwork> trNet, BidType bidType, PublicationManager publicationManager, BigDecimal result) {
        Iterator<TransparentNetwork> itNet = trNet.iterator();
        boolean goOn = true;
        while(itNet.hasNext() && goOn) {
            TransparentNetwork trNetWork = publicationManager.getTransparentNetworkById(itNet.next().getId(), BID_VALIDATION_TRANSPARENT_NETWORK_FS);
            if(TransparentNetwork.PERFORMANCE_NETWORK_NAME.equals(trNetWork.getName())) {
                BigDecimal bg = trNetWork.getRateCard(bidType).getDefaultMinimum();
                goOn = false;
                if(bg != null){
                    result = bg;
                }
            }
        }
        return result;
    }

    /***
     * Given a  bid amount returns true if the bid amount is valid, ie.according to the targeting and countries.
     *
     * */
    @Override
    @Transactional(readOnly = true)
    public boolean validateMinimumBidForCampaign(Campaign campaign, BidType bidType, BigDecimal newAmount) {
        return validateMinimumBidForCampaign(campaign, campaign.getSegments().get(0), bidType, newAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateMinimumBidForCampaign(Campaign campaign, Segment segment, BidType bidType, BigDecimal newAmount) {
        BigDecimal minBid = getMinimumBidForCampaign(campaign, segment, bidType);
        if (newAmount == null || newAmount.compareTo(minBid) < 0) {
            throw new CampaignBidTooLowException(campaign, newAmount, minBid);
        }
        // no fractional cents pricing changes allowed in campaign workflow
        if (!campaign.isPriceOverridden() && (((newAmount.multiply(new BigDecimal(1000)).intValue()) % 10) != 0)) {
                throw new CampaignBidWrongFormatException(campaign, newAmount);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignBidsForCampaign(Campaign campaign) {
        return campaignBidDao.countAllForCampaign(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return campaignBidDao.getAllForCampaign(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return campaignBidDao.getAllForCampaign(campaign, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignBid> getAllCampaignBidsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return campaignBidDao.getAllForCampaign(campaign, sort, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // BidDeduction
    // ------------------------------------------------------------------------------------------

	@Override
	@Transactional(readOnly = true)
	public BidDeduction getBidDeductionById(Long id, FetchStrategy... fetchStrategy) {
		return bidDeductionDao.getById(id, fetchStrategy);
	}
	
	@Override
    @Transactional(readOnly = false)
    public BidDeduction create(BidDeduction bidDeduction) {
		return bidDeductionDao.create(bidDeduction);
    }
	
	@Override
    @Transactional(readOnly = false)
    public BidDeduction update(BidDeduction bidDeduction) {
		return bidDeductionDao.update(bidDeduction);
    }
	
    @Override
    @Transactional(readOnly=true)
    public List<BidDeduction> getBidDeductions(BidDeductionFilter filter, FetchStrategy... fetchStrategy) {
        return bidDeductionDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<BidDeduction> getBidDeductions(BidDeductionFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return bidDeductionDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<BidDeduction> getBidDeductions(BidDeductionFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return bidDeductionDao.getAll(filter, page, fetchStrategy);
    }
	
	/**
	 * Update the bid Deductions only the campaign should be also saved separately.
	 */
	@Override
	@Transactional(readOnly = false)
	public Campaign updateBidDeductions(Campaign campaign, Set<BidDeduction> newBidDeductions) {
		
		// Storing current campaign bid deductions
		Set<BidDeduction> currentBidDeductions = new HashSet<BidDeduction>(campaign.getCurrentBidDeductions());
		
		// Bid deductions which was removed
		Set<BidDeduction> bidDeductionsToUnlinkAndClose = new HashSet<BidDeduction>(currentBidDeductions);
		
		for (BidDeduction newBidDeduction : newBidDeductions) {

			// Look up the bid deduction
			BidDeduction currentBidDeduction = searchBidDeduction(currentBidDeductions, newBidDeduction);
			
			// Exists 
			if (currentBidDeduction != null) {
				
				// Field has changed (Re-creation)
				if (currentBidDeduction.hasFieldChanged(newBidDeduction)) {
					
					// Unlink and close (the current bid deduction from campaign bid deductions)
					unlinkAndClose(campaign, currentBidDeduction);
					
					// Re-Create (based on the new one)
					campaign.getCurrentBidDeductions().add(reCreate(newBidDeduction));
					
				// Field has not changed (keep)
				} else {
					// Keep it (leave open the current one) 
					bidDeductionsToUnlinkAndClose.remove(currentBidDeduction);
				}
			
			// Does not Exist (Creation)
			} else {
				
				// Create (based on the new one)
				campaign.getCurrentBidDeductions().add(create(newBidDeduction));
			}
		}
		
		// Unlink and close (all the closable current bid deductions from campaign)
		bidDeductionsToUnlinkAndClose.forEach(currentBidDeduction -> unlinkAndClose(campaign, currentBidDeduction));
		
		return campaign;
	}

	private BidDeduction reCreate(BidDeduction newBidDeduction) {
		BidDeduction bd = new BidDeduction();
		bd.setAmount(newBidDeduction.getAmount());
		bd.setCampaign(newBidDeduction.getCampaign());
		bd.setPayerIsByyd(newBidDeduction.getPayerIsByyd());
		bd.setThirdPartyVendor(newBidDeduction.getThirdPartyVendor());
		bd.setThirdPartyVendorFreeText(newBidDeduction.getThirdPartyVendorFreeText());
		return create(bd);
	}
	
	private void unlinkAndClose(Campaign campaign, BidDeduction bidDeduction) {
		campaign.getCurrentBidDeductions().remove(bidDeduction);
		
		bidDeduction.setEndDate(new Date());
		update(bidDeduction);
	}

	private BidDeduction searchBidDeduction(Set<BidDeduction> bidDeductions, BidDeduction cbd) {
		BidDeduction result = null;
		for (BidDeduction element : bidDeductions) {
			if (element.equals(cbd)) {
				result = element;
				break;
			}
		}
		return result;
	}

    //------------------------------------------------------------------------------------------
    // CampaignTargetCTR
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public CampaignTargetCTR getCampaignTargetCTRById(Long id, FetchStrategy... fetchStrategy) {
        return campaignTargetCTRDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCTR newCampaignTargetCTR(Campaign campaign, BigDecimal targetCTR, FetchStrategy... fetchStrategy) {
        CampaignTargetCTR campaignTargetCTR = new CampaignTargetCTR();
        campaignTargetCTR.setCampaign(campaign);
        campaignTargetCTR.setTargetCTR(targetCTR);
        campaignTargetCTR = create(campaignTargetCTR);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return campaignTargetCTR;
        } else {
            return this.getCampaignTargetCTRById(campaignTargetCTR.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCTR create(CampaignTargetCTR campaignTargetCTR) {
        return campaignTargetCTRDao.create(campaignTargetCTR);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCTR update(CampaignTargetCTR campaignTargetCTR) {
        return campaignTargetCTRDao.update(campaignTargetCTR);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CampaignTargetCTR campaignTargetCTR) {
        campaignTargetCTRDao.delete(campaignTargetCTR);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCTR copyCampaignTargetCTR(CampaignTargetCTR campaignTargetCTR, Campaign newCampaign, FetchStrategy... fetchStrategy) {
        return newCampaignTargetCTR(newCampaign,
                                      campaignTargetCTR.getTargetCTR(),
                                      fetchStrategy);

    }

    
    //------------------------------------------------------------------------------------------
    // CampaignTargetCVR
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public CampaignTargetCVR getCampaignTargetCVRById(Long id, FetchStrategy... fetchStrategy) {
        return campaignTargetCVRDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCVR newCampaignTargetCVR(Campaign campaign, BigDecimal targetCVR, FetchStrategy... fetchStrategy) {
        CampaignTargetCVR campaignTargetCVR = new CampaignTargetCVR();
        campaignTargetCVR.setCampaign(campaign);
        campaignTargetCVR.setTargetCVR(targetCVR);
        campaignTargetCVR = create(campaignTargetCVR);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return campaignTargetCVR;
        } else {
            return this.getCampaignTargetCVRById(campaignTargetCVR.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCVR create(CampaignTargetCVR campaignTargetCVR) {
        return campaignTargetCVRDao.create(campaignTargetCVR);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCVR update(CampaignTargetCVR campaignTargetCVR) {
        return campaignTargetCVRDao.update(campaignTargetCVR);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CampaignTargetCVR campaignTargetCVR) {
        campaignTargetCVRDao.delete(campaignTargetCVR);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTargetCVR copyCampaignTargetCVR(CampaignTargetCVR campaignTargetCVR, Campaign newCampaign, FetchStrategy... fetchStrategy) {
        return newCampaignTargetCVR(newCampaign,
                                      campaignTargetCVR.getTargetCVR(),
                                      fetchStrategy);

    }
   
    //------------------------------------------------------------------------------------------
    // BiddingStrategy
    //------------------------------------------------------------------------------------------
    
	@Override
	public void copyBiddingStrategies(Set<BiddingStrategy> biddingStrategies, Campaign targetCampaign, FetchStrategy... fetchStrategy) {
		if (targetCampaign.getBiddingStrategies() == null) {
			targetCampaign.setBiddingStrategies(new HashSet<>());
		}

		// Add the new copied bidding strategies
		targetCampaign.getBiddingStrategies().addAll(biddingStrategies);
	}
	
    //------------------------------------------------------------------------------------------
    // BidDeduction
    //------------------------------------------------------------------------------------------
    
	@Override
	public void copyBidDeductions(Set<BidDeduction> bidDeductions, Campaign targetCampaign, FetchStrategy... fetchStrategy) {
		for(BidDeduction bd : bidDeductions) {
			newBidDeduction(targetCampaign, bd, fetchStrategy);
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public BidDeduction newBidDeduction(Campaign campaign, BidDeduction bidDeduction, FetchStrategy... fetchStrategy) {
		BidDeduction newBidDeduction = new BidDeduction();

		// Copy Bid deduction fields
		newBidDeduction.setAmount(bidDeduction.getAmount());
		newBidDeduction.setCampaign(campaign);
		newBidDeduction.setEndDate(bidDeduction.getEndDate());
		newBidDeduction.setPayerIsByyd(bidDeduction.getPayerIsByyd());
		newBidDeduction.setStartDate(bidDeduction.getStartDate());
		newBidDeduction.setThirdPartyVendor(bidDeduction.getThirdPartyVendor());
		newBidDeduction.setThirdPartyVendorFreeText(bidDeduction.getThirdPartyVendorFreeText());
		newBidDeduction = create(newBidDeduction);

		if (campaign.getCurrentBidDeductions() == null) {
			campaign.setCurrentBidDeductions(new HashSet<>());
		}

		// Add the new copied bid deduction
		campaign.getCurrentBidDeductions().add(newBidDeduction);

		if (fetchStrategy == null || fetchStrategy.length == 0) {
			return newBidDeduction;
		} else {
			return this.getBidDeductionById(newBidDeduction.getId(), fetchStrategy);
		}
	}

    //------------------------------------------------------------------------------------------
    // Balance And Budget
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
    public List<Campaign> getCampaignsToNotifyForDailyBudget(FetchStrategy ... fetchStrategy) {
        // This "query ids then hydrate" is not pretty, but it's more efficient than simply
        // returning a list of objects, and then the caller having to hydrate them one by one.
        List<Number> campaignIds = 
          getTransactionalEntityManager().createNativeQuery("SELECT c.ID FROM CAMPAIGN c"
                                 + " INNER JOIN CAMPAIGN_DAILY_SPEND spend ON spend.CAMPAIGN_ID = c.ID"
                                 + " INNER JOIN ADVERTISER adv ON adv.ID = c.ADVERTISER_ID"
                                 + " INNER JOIN COMPANY co ON co.ID = adv.COMPANY_ID"
                                 + " INNER JOIN USER mgr ON mgr.ID = co.ACCOUNT_MANAGER_ID"
                                 + " WHERE spend.BUDGET IS NOT NULL"
                                 + " AND c.DAILY_BUDGET_ALERT_ENABLED = TRUE"
                                 + " AND c.STATUS = 'ACTIVE'"
                                 // Only notify if the accountManager is active
                                 + " AND mgr.STATUS != 'DISABLED'"
                                 // "Today" in the advertiser's time zone
                                 + " AND spend.DATE_ID = DATE_FORMAT(CONVERT_TZ(NOW(), @@global.time_zone, co.DEFAULT_TIME_ZONE), '%Y%m%d')"
                                 // Today's spend exceeds 80% of the daily budget
                                 + " AND (spend.AMOUNT / spend.BUDGET) >= 0.8"
                                 // Hasn't been notified yet today for this campaign
                                 + " AND NOT EXISTS ("
                                 + "SELECT 1 FROM NOTIFICATION_FLAG"
                                 + " WHERE DISCRIMINATOR='CAMPAIGN'"
                                 + " AND CAMPAIGN_ID=c.ID"
                                 + " AND TYPE='DAILY_BUDGET'"
                                 + " AND (EXPIRATION_DATE IS NULL OR EXPIRATION_DATE > CURRENT_TIMESTAMP)"
                                 + ")")
            .getResultList();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("campaignIds.size=" + campaignIds.size());
        }
        if (campaignIds.isEmpty()) {
            return Collections.<Campaign> emptyList();
        } else {
            CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
            return campaignManager.getAllCampaigns(new CampaignFilter().setCampaignIds(toLongs(campaignIds)), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void resetDailyBudgetNotificationFlagsAsApplicable() {
        getTransactionalEntityManager().createNativeQuery("DELETE nf"
                             + " FROM NOTIFICATION_FLAG nf"
                             + " INNER JOIN CAMPAIGN c ON c.ID = nf.CAMPAIGN_ID"
                             + " INNER JOIN CAMPAIGN_DAILY_SPEND spend ON spend.CAMPAIGN_ID = c.ID"
                             + " INNER JOIN ADVERTISER adv ON adv.ID = c.ADVERTISER_ID"
                             + " INNER JOIN COMPANY co ON co.ID = adv.COMPANY_ID"
                             // "Today" in the advertiser's time zone
                             + " WHERE spend.DATE_ID = DATE_FORMAT(CONVERT_TZ(NOW(), @@global.time_zone, co.DEFAULT_TIME_ZONE), '%Y%m%d')"
                             + " AND ("
                             // Spend is below 80% of daily budget
                             + "(c.DAILY_BUDGET_ALERT_ENABLED=TRUE AND spend.BUDGET IS NOT NULL AND (spend.AMOUNT / spend.BUDGET) < 0.8)"
                             + " OR "
                             // The budget has been removed (AF-1107 & AF-1121)
                             + "(spend.BUDGET IS NULL)"
                             + ")"
                             + " AND nf.DISCRIMINATOR='CAMPAIGN'"
                             + " AND nf.TYPE='DAILY_BUDGET'"
                             + " AND (nf.EXPIRATION_DATE IS NULL OR nf.EXPIRATION_DATE > CURRENT_TIMESTAMP)")
            .executeUpdate();
    }
        
    @Override
    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
    public List<Campaign> getCampaignsToNotifyForOverallBudget(FetchStrategy ... fetchStrategy) {
        // This "query ids then hydrate" is not pretty, but it's more efficient than simply
        // returning a list of objects, and then the caller having to hydrate them one by one.
        List<Number> campaignIds =
            getTransactionalEntityManager().createNativeQuery("SELECT c.ID FROM CAMPAIGN c"
                                 + " INNER JOIN CAMPAIGN_OVERALL_SPEND spend ON spend.CAMPAIGN_ID=c.ID"
                                 + " INNER JOIN ADVERTISER adv ON adv.ID=c.ADVERTISER_ID"
                                 + " INNER JOIN COMPANY co ON co.ID=adv.COMPANY_ID"
                                 + " INNER JOIN USER mgr ON mgr.ID=co.ACCOUNT_MANAGER_ID"
                                 + " WHERE c.OVERALL_BUDGET IS NOT NULL"
                                 + " AND c.OVERALL_BUDGET_ALERT_ENABLED = TRUE"
                                 + " AND c.STATUS = 'ACTIVE'"
                                 // Only notify if the accountManager is active
                                 + " AND mgr.STATUS != 'DISABLED'"
                                 // Overall spend exceeds 80% of the overall budget
                                 + " AND (spend.AMOUNT / c.OVERALL_BUDGET) >= 0.8"
                                 // Hasn't been notified yet for this campaign
                                 + " AND NOT EXISTS ("
                                 + "SELECT 1 FROM NOTIFICATION_FLAG"
                                 + " WHERE DISCRIMINATOR='CAMPAIGN'"
                                 + " AND CAMPAIGN_ID=c.ID"
                                 + " AND TYPE='OVERALL_BUDGET'"
                                 + " AND (EXPIRATION_DATE IS NULL OR EXPIRATION_DATE > CURRENT_TIMESTAMP)"
                                 + ")")
            .getResultList();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("campaignIds.size=" + campaignIds.size());
        }
        if (campaignIds.isEmpty()) {
            return Collections.<Campaign> emptyList();
        } else {
            CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
            return campaignManager.getAllCampaigns(new CampaignFilter().setCampaignIds(toLongs(campaignIds)), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void resetOverallBudgetNotificationFlagsAsApplicable() {
        getTransactionalEntityManager().createNativeQuery("DELETE nf"
                             + " FROM NOTIFICATION_FLAG nf"
                             + " INNER JOIN CAMPAIGN c ON c.ID = nf.CAMPAIGN_ID"
                             + " INNER JOIN CAMPAIGN_OVERALL_SPEND spend ON spend.CAMPAIGN_ID = c.ID"
                             + " WHERE ("
                             // Spend is below 80% of overall budget
                             + "(c.OVERALL_BUDGET_ALERT_ENABLED=TRUE AND c.OVERALL_BUDGET IS NOT NULL AND (spend.AMOUNT / c.OVERALL_BUDGET) < 0.8)"
                             + " OR "
                             // The budget has been removed (AF-1107 & AF-1121)
                             + "(c.OVERALL_BUDGET IS NULL)"
                             + ")"
                             + " AND nf.DISCRIMINATOR='CAMPAIGN'"
                             + " AND nf.TYPE='OVERALL_BUDGET'"
                             + " AND (nf.EXPIRATION_DATE IS NULL OR nf.EXPIRATION_DATE > CURRENT_TIMESTAMP)")
            .executeUpdate();
    }
}
