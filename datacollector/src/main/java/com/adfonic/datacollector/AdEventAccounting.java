package com.adfonic.datacollector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.adserver.AdEvent;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.TaxUtils;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignBidDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDataFeeDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherRevShareDto;

/** This wraps accounting logic around an AdEvent */
public class AdEventAccounting {

    private static final transient Logger LOG = Logger.getLogger(AdEventAccounting.class.getName());

    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000.0);

    private final AdEvent adEvent;
    private final CampaignDto campaign;
    private final AdvertiserDto advertiser;
    private final PublisherDto publisher;
    private final BigDecimal publisherRevShare;

    private BigDecimal cost;
    private BigDecimal payout;
    private BigDecimal advertiserVat;
    private BigDecimal publisherVat;

    private BigDecimal buyerPremium;
    private BigDecimal directCost;
    private BigDecimal techFee;
    private BigDecimal dataRetail;
    private BigDecimal dataWholesale;
    private BigDecimal dspMargin;
    private BigDecimal campaignDiscount;

    private Long dataFeeId;

    // These values are lazily calculated on demand
    private BigDecimal publisherCreditMultiplier;

    public AdEventAccounting(AdEvent adEvent, CampaignDto campaign, PublisherDto publisher) {
        this.adEvent = adEvent;
        this.campaign = campaign;
        this.advertiser = campaign.getAdvertiser();
        this.publisher = publisher;

        // 99.99% of the time, the campaign's current bid will be applicable to the
        // time of the ad event.  But just in case there was an ad event queue backlog,
        // we need to double check to make sure the current bid applies to the time of
        // the event.  We incur one extra date comparison hit here as a barrier to any
        // iteration.
        CampaignBidDto bidForTheAdAtAdServeTime = campaign.getCurrentBid();
        if (bidForTheAdAtAdServeTime != null && bidForTheAdAtAdServeTime.getStartDate().after(adEvent.getEventTime())) {
            bidForTheAdAtAdServeTime = campaign.getBidForDate(adEvent.getEventTime());
        }

        // Same deal with publisherRevShare as campaign bid above.  We assume that
        // the current rev share applies, but we incur one extra date comparison as
        // a barrier to needing to dive deeper by date.
        PublisherRevShareDto currentRevShare = publisher.getCurrentPublisherRevShare();
        if (currentRevShare.getStartDate().after(adEvent.getEventTime())) {
            this.publisherRevShare = publisher.getRevShareForDate(adEvent.getEventTime());
        } else {
            this.publisherRevShare = currentRevShare.getRevShare();
        }

        // Same with data fee, if campaign is not audience targeting
        // data fee will be null 
        CampaignDataFeeDto dataFee = campaign.getCurrentDataFee();
        Long eventDataFeeId = adEvent.getCampaignHistoryDataFeeId();
        if ((dataFee == null && eventDataFeeId != null) || ((dataFee != null) && !dataFee.getId().equals(eventDataFeeId))) {
            dataFee = campaign.getDataFeeForDate(adEvent.getEventTime());
        }
        //Set data fee id for AD_EVENT_LOG
        if (dataFee != null) {
            this.dataFeeId = dataFee.getId();
        } else {
            this.dataFeeId = null;
        }

        BigDecimal settlementPrice = adEvent.getRtbSettlementPrice();
        if (settlementPrice == null) {
            //MAX-93
            settlementPrice = BigDecimal.ZERO;
        }

        if (adEvent.isRtb() && AdAction.AD_SERVED.equals(adEvent.getAdAction()) && adEvent.getRtbSettlementPrice() != null) {
            // At AD_SERVED time, we only set payout, and it gets set to the RTB settlement
            // price -- which already has advertiser discount and publisher rev share factored in.
            // All other accounting fields (cost, advertiserVat) should be left null.  The
            // RTB settlement price is given in CPM, so payout for this event is 1/1000 of that.
            payout = settlementPrice.divide(ONE_THOUSAND);
            publisherVat = TaxUtils.calculatePublisherVat(payout, adEvent.getEventTime(), publisher.getCompany().isTaxablePublisher());
        }

        if (bidForTheAdAtAdServeTime != null) { // bid will be null for house ads
            BigDecimal cost = bidForTheAdAtAdServeTime.getAmount().divide(new BigDecimal(bidForTheAdAtAdServeTime.getBidType().getQuantity()));
            //SC-404
            // Special cases for RTB
            boolean assignCostForDspLicAtImpressionTime = false;
            if (adEvent.isRtb() && AdAction.AD_SERVED.equals(adEvent.getAdAction())) {
                BigDecimal mediaCostMargin = campaign.getAdvertiser().getCompany().getMediaCostMargin();
                if (mediaCostMargin.doubleValue() > 0.0) {
                    //COST = PAYOUT * (1 + BUYER_PREMIUM) * (1 + DIRECT_COST) / (1-COMPANY.ADVERTISER_MEDIA_COST_MARKUP) + DATA_FEE + RM_AD_SERVING_FEE
                    cost = settlementPrice;
                    LOG.fine("RTB settlement price(" + campaign.getId() + ") = " + cost);

                    // Apply buyer premium if the campaign is not using a PMP
                    if (!campaign.isPMP()) {
                        cost = cost.multiply(BigDecimal.ONE.add(publisher.getBuyerPremium()));
                    }

                    // Apply direct cost if the campaign is not using a PMP

                    
					if (!campaign.isPMP()) {
						BigDecimal directCostAtAdServeTime = getDirectCostAtAdServeTimeOrZero();
						if (directCostAtAdServeTime.signum() > 0) {
							cost = cost.multiply(BigDecimal.ONE.add(directCostAtAdServeTime));
						}
					}
                    // Divide by (1-COMPANY.ADVERTISER_MEDIA_COST_MARKUP)
                    cost = cost.divide(BigDecimal.ONE.subtract(mediaCostMargin), 6, RoundingMode.HALF_UP);
                    //ADD data fee if applies
                    if (dataFee != null) {
                        cost = cost.add(dataFee.getAmount()); // ADD DATA_FEE
                        techFee = dataFee.getAmount();
                    }
                    cost = cost.add(campaign.getRmAdServingFee()); // ADD RM_AD_SERVING_FEE
                    LOG.fine("Cost before discount for campaign id(" + campaign.getId() + ") = " + cost);
                    if (campaign.getAgencyDiscount() == null) {
                        LOG.fine("Campaign Agency discount for campaign id (" + campaign.getId() + ") is null");
                    } else {
                        LOG.fine("Campaign Agency discount for campaign id (" + campaign.getId() + ") = " + campaign.getAgencyDiscount().doubleValue());
                    }
                    BigDecimal agencyDiscount = campaign.getAgencyDiscount();
                    if (agencyDiscount != null && agencyDiscount.doubleValue() > 0) {
                        cost = cost.divide(BigDecimal.ONE.subtract(agencyDiscount), 6, RoundingMode.HALF_UP);
                        LOG.fine("Cost after discount for campaign id(" + campaign.getId() + ") = " + cost);
                    }
                    // AD-293 For CPM campaigns
                    if (bidForTheAdAtAdServeTime.getBidType().equals(BidType.CPM)) {
                        //In case of CPM we first compare the calculated cost to bid amount and THEN calculate the cost / impression by dividing by 1000.
                        //when we make a bid in case of CPM when the BID is $2, then one impression cost MUST NOT BE MORE THEN .002 as we have bid 1000 impressions
                        cost = compareCost(cost, bidForTheAdAtAdServeTime); // compare cost to the bid amount
                        cost = cost.divide(new BigDecimal(bidForTheAdAtAdServeTime.getBidType().getQuantity()), 6, RoundingMode.HALF_UP); // Divide by 1000
                    } else {
                        // AD-293 For CPC/CPA/CPI campaigns we want to treat CPA/CPI campaigns same as CPC campaigns
                        // In case of CPC/CPA/CPI we first calculate the cost / impression by dividing by 1000 and then compare the calculated cost to the bid amount.
                        // in case of CPC if when the bid is $2, then one impression cost MUST NOT BE MORE then $2.
                        // SC-423 Instead of Dividing by bidForTheAdAtAdServeTime.getBidType().getQuantity() we need to divide by BidType.CPM.getQuantity()
                        //because bidForTheAdAtAdServeTime.getBidType() is CPC now and getQuantity will return 1, but we need to divide by 1000.
                        cost = cost.divide(new BigDecimal(BidType.CPM.getQuantity()), 6, RoundingMode.HALF_UP); // Divide by 1000
                        // compare cost to the bid amount
                        cost = compareCost(cost, bidForTheAdAtAdServeTime);
                        assignCostForDspLicAtImpressionTime = true;
                        LOG.fine("Cost post CPC for campaign id(" + campaign.getId() + ") = " + cost);

                    }

                    LOG.fine("Final Cost for campaign id(" + campaign.getId() + ") = " + cost);
                }
            }

            // Only assign cost if the BidType's AdAction matches that of the AdEvent
            // or SC-404, assign cost for impression of CPC campaign for DSP Lic and AD-293, assign cost for impression of CPA/CPI campaign for DSP Lic
            if (bidForTheAdAtAdServeTime.getBidType().getAdAction().equals(adEvent.getAdAction()) || assignCostForDspLicAtImpressionTime) {
                // Pass false for RTB events so payout and publisherVat are left untouched
                //SC-415 / SC-404 We shud not assign the cost if user is DSP Lic and bid Type is CPC and event is Click as during impression only we have assigned the cost.
                if ((campaign.getAdvertiser().getCompany().getMediaCostMargin().doubleValue() > 0.0) && !bidForTheAdAtAdServeTime.getBidType().equals(BidType.CPM)
                        && !adEvent.getAdAction().equals(BidType.CPM.getAdAction())) {
                    //then dont assign the cost for CPC, CPA, CPI
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Not assigning cost for " + adEvent.getAdAction() + ", Campaign id=" + campaign.getId()
                                + ". Payout and publisherVat are untouched. This is DSP_LIC user with bid.bidType=" + bidForTheAdAtAdServeTime.getBidType());
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Assigning cost for " + adEvent.getAdAction() + ", Campaign id=" + campaign.getId() + " bid.bidType=" + bidForTheAdAtAdServeTime.getBidType());
                    }
                    assignCost(cost, !adEvent.isRtb(), campaign.getAgencyDiscount());
                }
            } else if (LOG.isLoggable(Level.FINE)) {
                //AdAction doesn't match that of the Advent
                LOG.fine("Not assigning cost for " + adEvent.getAdAction() + ", Campaign id=" + campaign.getId() + " bid.bidType=" + bidForTheAdAtAdServeTime.getBidType());
            }
        } else if (LOG.isLoggable(Level.FINE)) {
            // bid is null, must be a house ad
            LOG.fine("Not assigning cost for " + adEvent.getAdAction() + ", Campaign id=" + campaign.getId() + " has no bid");
        }
    }

	BigDecimal getDirectCostAtAdServeTimeOrZero() {
		CompanyDirectCostDto directCostAtAdServeTimeDto = advertiser.getCompany().getDirectCost();
		if (directCostAtAdServeTimeDto == null) {
			CompanyDirectCostDto historical = advertiser.getCompany().getDirectCostForDate(adEvent.getEventTime());
			return historical == null ? BigDecimal.ZERO : historical.getDirectCost();
		}

		if (directCostAtAdServeTimeDto.getStartDate().after(adEvent.getEventTime())) {
			CompanyDirectCostDto historical = advertiser.getCompany().getDirectCostForDate(adEvent.getEventTime());
			return historical == null ? BigDecimal.ZERO : historical.getDirectCost();
		}

		return directCostAtAdServeTimeDto.getDirectCost();
	}
    
    public AdEvent getAdEvent() {
        return adEvent;
    }

    public CampaignDto getCampaign() {
        return campaign;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public BigDecimal getAdvertiserVat() {
        return advertiserVat;
    }

    public BigDecimal getPublisherVat() {
        return publisherVat;
    }

    public Long getDataFeeId() {
        return dataFeeId;
    }

    public void setDataFeeId(Long dataFeeId) {
        this.dataFeeId = dataFeeId;
    }

    /**
     * 	if we have calculated the cost more then the actual bid amount then we can not charge
     *  advertiser more then the bid amount , we need to fallback on the bid amount
     *  and also print a warning message as it should not be happening and consult the powers that be!!
     * @param bidForTheAdAtAdServeTime
     */
    final BigDecimal compareCost(BigDecimal cost, CampaignBidDto bidForTheAdAtAdServeTime) {
        if (cost.compareTo(bidForTheAdAtAdServeTime.getAmount()) > 0) {
            cost = bidForTheAdAtAdServeTime.getAmount();
            LOG.log(Level.WARNING, "Calculated cost for " + adEvent.getAdAction() + ", Campaign id=" + campaign.getId() + " bid.bidType=" + bidForTheAdAtAdServeTime.getBidType()
                    + " is more then the actual bid amount " + bidForTheAdAtAdServeTime.getAmount() + ". It should not happen as we are paying more then what we getting.");
        }
        return cost;
    }

    /**
     * Set the value of cost, and calculate advertiser VAT if the advertiser
     * is taxable.  When assignPayout is true, we also [re]caclulate the value
     * of payout and the publisher VAT.  In certain scenarios (i.e. RTB), we
     * don't always want to recalculate payout -- i.e. it gets left null for
     * impression/click events despite the advertiser cost being adjusted.
     */
    final void assignCost(BigDecimal cost, boolean assignPayout, BigDecimal discount) {
        this.cost = cost;
        if (cost == null) {
            advertiserVat = null;

            if (assignPayout) {
                payout = null;
                publisherVat = null;
            }
        } else {
            advertiserVat = TaxUtils.calculateAdvertiserVat(cost, adEvent.getEventTime(), advertiser.getCompany().isTaxableAdvertiser());

            if (assignPayout) {
                if (discount != null) {
                    // Apply the advertiser discount like this:
                    //AD-369
                    // N = COST * (1 - campaign.agencydiscount), i.e. net revenue
                    // m = Company.currentMarginShareDSP
                    // r = Publisher.currentRevShare
                    // PAYOUT = N * r * m / (1 + r * m - r)
                    BigDecimal N = cost.multiply(BigDecimal.ONE.subtract(discount)); // N = COST * (1 - campaign.agencyDiscount)
                    BigDecimal r = publisherRevShare;
                    BigDecimal m = advertiser.getCompany().getMarginShareDSP();
                    BigDecimal num = (N.multiply(r).multiply(m)); // N * r * m 
                    LOG.log(Level.FINE, "N * r * m: " + N + "*" + r + "*" + m + "m = " + num);
                    BigDecimal denom = BigDecimal.ONE.add(r.multiply(m).subtract(r)); // (1 + r * m - r)
                    LOG.log(Level.FINE, "(1 + r * m - r): " + BigDecimal.ONE + "+" + r + "*" + m + "-" + r + "= " + denom);
                    payout = num.divide(denom, RoundingMode.HALF_UP); // N * r * m / (1 + r * m - r)
                    LOG.log(Level.FINE, "Payout:N * r * m / (1 + r * m - r) = " + payout);
                    //payout = (N.multiply(r).multiply(m)).divide((BigDecimal.ONE.add(r.multiply(m).subtract(r)))); // N * r * m / (1 + r * m - r)
                } else {
                    payout = cost.multiply(publisherRevShare);
                }
                publisherVat = TaxUtils.calculatePublisherVat(payout, adEvent.getEventTime(), publisher.getCompany().isTaxablePublisher());
                LOG.log(Level.FINE, "Publisher VAT = " + publisherVat);
            }
        }
    }

    /** Advertiser spend equals cost plus VAT on the advertiser. */
    public BigDecimal getAdvertiserSpend() {
        if (cost == null) {
            return null;
        }

        // The value we pass into UPDATE_BUDGETS for depends on whether the
        // advertiser is tax-postpaid or not.  Normally, we pass in a "spend"
        // value that includes VAT.  For tax-postpaid advertisers, we need to
        // pass in just the "cost" alone.
        if (advertiser.getCompany().isPostPay()) {
            return cost;
        } else if (advertiserVat == null) {
            return cost;
        } else {
            return cost.add(advertiserVat);
        }
    }

    /** 
     * Redistributes spend to cost and advertiserVat fields, and recalculates
     * publisher payout and publisherVat.  This method gets called after we
     * have updated budgets...so the spend value coming back in may have been
     * adjusted for budget constraints.  That's why everything needs to get
     * redistributed/recalculated.
     */
    public void setAdjustedAdvertiserSpend(BigDecimal advertiserSpend, BigDecimal discount) {
        // In all of the following cases we want to make sure we pass false
        // as the second arg to assignCost for RTB events.  We never want
        // payout to be affected by cost adjustments for RTB events.
        boolean assignPayout = !adEvent.isRtb();
        if (advertiserSpend == null) {
            assignCost(null, assignPayout, discount);
        } else if (advertiser.getCompany().isTaxableAdvertiser()) {
            if (advertiser.getCompany().isPostPay()) {
                // See getAdvertiserSpend().  The adjusted value represents
                // cost only (not including VAT).
                assignCost(advertiserSpend, assignPayout, discount);
            } else {
                // See getAdvertiserSpend().  We need to factor VAT back out of
                // spend in order to arrive at the true cost.  I say "true cost"
                // because the spend value may have been adjusted (lowered) in
                // the updateBudgets phase due to budget restrictions.  In that
                // case, the true cost ends up being lower than the original
                // bid-determined cost.
                assignCost(advertiserSpend.divide(BigDecimal.ONE.add(TaxUtils.getTaxRate(adEvent.getEventTime())), RoundingMode.HALF_UP), assignPayout, discount);
            }
        } else {
            // There wasn't any VAT factored in, it's just the cost
            assignCost(advertiserSpend, assignPayout, discount);
        }
    }

    public BigDecimal getPublisherCreditMultiplier(BigDecimal discount) {
        // We allow the value to be lazily calculated
        if (publisherCreditMultiplier == null) {
            publisherCreditMultiplier = calculatePublisherCreditMultiplier(advertiser, publisher, publisherRevShare, discount, adEvent.getEventTime());
        }
        return publisherCreditMultiplier;
    }

    static BigDecimal calculatePublisherCreditMultiplier(AdvertiserDto advertiser, PublisherDto publisher, BigDecimal publisherRevShare, BigDecimal discount, Date eventTime) {
        // Calculate the publisher's "credit multiplier" -- which as per Wes
        // is computed like this:
        // publisherRevShare * (1 + publisherTaxRate) / (1 + effectiveTaxRateOnSpend)
        BigDecimal publisherTaxRate = getPublisherTaxRate(publisher, eventTime);
        // The effective tax rate on spend depends on whether or not the
        // advertiser is postpaid or not.
        BigDecimal effectiveTaxRateOnSpend = getEffectiveTaxRateOnSpend(advertiser, eventTime);
        BigDecimal publisherCreditMultiplier = publisherRevShare.multiply(BigDecimal.ONE.add(publisherTaxRate)).divide(BigDecimal.ONE.add(effectiveTaxRateOnSpend), 5,
                RoundingMode.HALF_UP);
        if (discount != null) {
            publisherCreditMultiplier = publisherCreditMultiplier.multiply(BigDecimal.ONE.subtract(discount));
        }
        return publisherCreditMultiplier;
    }

    static BigDecimal getPublisherTaxRate(PublisherDto publisher, Date eventTime) {
        return publisher.getCompany().isTaxablePublisher() ? TaxUtils.getTaxRate(eventTime) : BigDecimal.ZERO;
    }

    static BigDecimal getEffectiveTaxRateOnSpend(AdvertiserDto advertiser, Date eventTime) {
        // We have to be careful here...see getAdvertiserSpend().  We factor
        // out advertiser VAT if the advertiser is taxable, but NOT postpaid.
        return advertiser.getCompany().isTaxableAdvertiser() && !advertiser.getCompany().isPostPay() ? TaxUtils.getTaxRate(eventTime) : BigDecimal.ZERO;
    }

    public BigDecimal getDirectCost() {
        return directCost;
    }

    public BigDecimal getTechFee() {
        return techFee;
    }

    public BigDecimal getDataRetail() {
        return dataRetail;
    }

    public BigDecimal getDataWholesale() {
        return dataWholesale;
    }

    public BigDecimal getDspMargin() {
        return dspMargin;
    }

    public BigDecimal getBuyerPremium() {
        return buyerPremium;
    }

    public BigDecimal getCampaignDiscount() {
        return campaignDiscount;
    }
}
