package com.adfonic.adserver.rtb.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.RtbBidManager;
import com.adfonic.adserver.rtb.RtbCacheService;
import com.adfonic.adserver.rtb.dec.SecurityAlias;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.RtbConfig.AdmProfile;
import com.adfonic.domain.RtbConfig.DecryptionScheme;
import com.adfonic.domain.RtbConfig.RtbAdMode;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig.RtbImpTrackMode;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

@Component
public class RtbWinLogicImpl {

    private static final Logger LOG = LoggerFactory.getLogger(RtbWinLogicImpl.class);

    static final String WIN_RESPONSE_BID_NOT_FOUND = "<!-- Bid not found -->";
    static final String WIN_RESPONSE_AD_SPACE_NOT_FOUND = "<!-- AdSpace not found -->";
    static final String WIN_RESPONSE_CREATIVE_NOT_FOUND = "<!-- Creative not found -->";

    @Autowired
    private RtbBidManager rtbBidManager;

    @Autowired
    private AdEventFactory adEventFactory;

    @Autowired
    private AdServerStats stats;

    @Autowired
    private BackupLogger backupLogger;

    @Autowired
    private AdMarkupRenderer adMarkupRenderer;

    @Autowired
    private ImpressionService impressionService;

    /**
     * Exchanges with win notifications delivered by beacon (impression tracker)
     * RTB_CONFIG.WIN_NOTICE_MODE='BEACON'
     */
    public void winOnImpression(String priceString, TargetingContext context, Impression impression, CreativeDto creative, RtbConfigDto rtbConfig) {
        stats.winStarted(context.getAdSpace(), creative, AsCounter.WinOnImpression);

        rtbBidManager.removeBidDetails(impression.getExternalID());

        winAdEvent(priceString, impression, context, creative, rtbConfig);
    }

    /**
     * Exchanges with win notifications delivered by nurl notification
     * RTB_CONFIG.WIN_NOTICE_MODE='OPEN_RTB'
     * 
     * In case of RTB_CONFIG.AD_MODE='WIN_NOTICE' also return Ad markup must be rendered and returned
     */
    public String[] winOnRtbNurl(String impressionId, String settlementPrice, HttpServletRequest httpRequest) throws java.io.IOException {

        RtbBidDetails bidDetails = rtbBidManager.removeBidDetails(impressionId);
        if (bidDetails == null) {
            // Details timed out from impression cache or possibly repeated win notification...
            LOG.warn("RtbBidDetails not found in impression cache for win notice. Impression " + impressionId);
            backupLogger.logRtbWinFailure(impressionId, "RtbBidDetails not found", null);
            stats.increment(AsCounter.WinRtbDetailsNotFound);
            return null;
        }
        Impression impression = bidDetails.getImpression();
        TargetingContext tctx = rtbBidManager.getTargetingContextFromBidDetails(bidDetails);

        AdserverDomainCache adCache = tctx.getAdserverDomainCache();
        AdSpaceDto adSpace = adCache.getAdSpaceById(impression.getAdSpaceId());
        if (adSpace == null) {
            // AdCache changed since we sent bid and AdSpace was kicked out of it for some reason or another
            LOG.warn("AdSpace " + impression.getAdSpaceId() + " not found in Ad cache for win notice. Impression " + impressionId);
            backupLogger.logRtbWinFailure(impressionId, "AdSpace not found", tctx, String.valueOf(impression.getAdSpaceId()));
            stats.increment(AsCounter.WinAdSpaceNotFound);
            return new String[] { WIN_RESPONSE_AD_SPACE_NOT_FOUND, Constant.TEXT_HTML };
        }
        tctx.setAdSpace(adSpace);

        CreativeDto creative = adCache.getCreativeById(impression.getCreativeId());
        // AdCache changed since we sent bid
        if (creative == null) {
            // Second chance club for creative...
            creative = adCache.getRecentlyStoppedCreativeById(impression.getCreativeId());
            if (creative == null) {
                LOG.warn("Creative " + impression.getCreativeId() + " not found in Ad cache for win notice. Impression " + impressionId);
                stats.increment(adSpace, AsCounter.WinCreativeNotFound);
                backupLogger.logRtbWinFailure(impressionId, "Creative not found", tctx, String.valueOf(impression.getCreativeId()));
                return new String[] { WIN_RESPONSE_CREATIVE_NOT_FOUND, Constant.TEXT_HTML };
            }
        }

        stats.winStarted(adSpace, creative, AsCounter.WinOnRtbNurl);

        PublisherDto publisher = adSpace.getPublication().getPublisher();
        RtbConfigDto rtbConfig = publisher.getRtbConfig();
        if (publisher.getId().equals(RtbExchange.Rubicon.getPublisherId())) {
            // Use "special" RtbConfigDto for Rubicon video - http://kb.rubiconproject.com/index.php/RTB/OpenRTB#Use_of_adm_vs_nurl
            // Normally Rubicon uses "markup on bid" and "beacon win notification" but only for video (VAST) Rubicon hacked it into "markup on rtb win notification" 
            rtbConfig = new RubiconVastRtbConfig(rtbConfig);
        }
        String[] rendered = null;
        if (rtbConfig.getAdMode() == RtbAdMode.WIN_NOTICE) {
            FormatDto format = tctx.getDomainCache().getFormatById(creative.getFormatId());

            // Add the required stuff back into the TargetingContext.  This is stuff
            // that we derived at bid time and is required now for ad generation, but
            // since we don't have the full User-Agent header at win notice time, we
            // can't re-derive it now.  Propagate...
            DisplayTypeDto displayType = tctx.getDomainCache().getDisplayTypeBySystemName(bidDetails.getDisplayTypeSystemName());
            rendered = adMarkupRenderer.createMarkup(tctx, impression, creative, format, adSpace, displayType, httpRequest, bidDetails.getProxiedDestination(), rtbConfig);
            // Save the Impression object for subsequent clickthrough calls
            impressionService.saveImpression(impression);
        }

        if (rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.OPEN_RTB) {
            winAdEvent(settlementPrice, impression, tctx, creative, rtbConfig);
        } else if (rendered != null) {// markup being generated, put back bidDetails, beacon will come to get it
            // rtbBidManager.saveBidDetails(bidDetails);
            //  no need, will have a race situation here that way. 
            // it is of no use anyway.
            //  its won (so should not time out) and rendered (no further use of the object) 
        } else {
            // This should never happen unless content of RTB_CONFIG table is messed up
            throw new RuntimeException("Neither RtbAdMode.WIN_NOTICE nor Ad markup generated");
        }

        return rendered;
    }

    /**
     * Create and publish win AdEvent  
     */
    private void winAdEvent(String priceString, Impression impression, TargetingContext context, CreativeDto creative, RtbConfigDto rtbConfig) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("win price: " + priceString + ", impression: " + impression.getExternalID() + ", creative: " + creative.getId() + ", adspace: " + impression.getAdSpaceId());
        }
        // SC-483 create upfront; since we don't want to change the time semantics
        AdEvent event = adEventFactory.newInstance(AdAction.AD_SERVED);

        // AF-1342 - ensure that if we were passed an RTB settlement price
        // that it gets set on the Impression.  This is done for RTB when
        // the win notice mode is BEACON.
        BigDecimal settlementPrice = getPrice(priceString, impression, context, creative, rtbConfig, event.getEventTime());

        // Log the AD_SERVED event...IMPRESSION will get logged at beacon load time
        //        context.populateAdEvent(event, impression, creative);
        //        adEventLogger.logAdEvent(event, context);
        impression.setRtbSettlementPrice(settlementPrice);

        backupLogger.logRtbWinSuccess(impression, impression.getRtbSettlementPrice(), event.getEventTime(), context);
        stats.winCompleted(context.getAdSpace(), creative, settlementPrice);
    }

    /**
     * Can return null when price
     */
    private BigDecimal getPrice(String priceString, Impression impression, TargetingContext context, CreativeDto creative, RtbConfigDto rtbConfig, Date eventTime) {
        BigDecimal settlementPrice = null;
        if (StringUtils.isNotBlank(priceString)) {
            try {
                DecryptionScheme decryptionScheme = rtbConfig.getDecryptionScheme();
                if (decryptionScheme != null) {
                    // Some exchanges send settlement price encrypted
                    SecurityAlias alias = SecurityAlias.valueOfCached(rtbConfig.getSecurityAlias());
                    if (alias == null) {
                        throw new IllegalStateException("No SecurityAlias found for: " + rtbConfig.getSecurityAlias() + " Check properties");
                    }
                    settlementPrice = alias.getPriceCrypter(decryptionScheme).decodePrice(priceString);
                } else {
                    // settlement price is simple plain text 
                    settlementPrice = new BigDecimal(priceString);
                }
                // SC-483
                if (!Constant.USD.equals(rtbConfig.getBidCurrency())) {
                    // dig in to get the adSpace - I'm not going to pollute all the methods down the stack, already moved this conversion from settleImpressionAt()
                    //   *edit - apparently in both routes currently adSpace is set on context.
                    settlementPrice = context.getAdserverDomainCache().convertFromBidCurrencyToUsd(context.getAdSpace(), RtbBidLogic.gmtTimeYMDHFormat.format(eventTime),
                            settlementPrice);
                }
                // We have insane precision for bid price while Smaato sends rounded value
                BigDecimal bidPrice = impression.getRtbBidPrice().round(MathContext.DECIMAL64);
                BigDecimal diffPrice = settlementPrice.subtract(bidPrice);
                if (diffPrice.signum() == 1) { // if sp > bp
                    BigDecimal d1000 = diffPrice.divide(bidPrice, RoundingMode.HALF_UP).movePointRight(3);
                    if (d1000.compareTo(BigDecimal.ONE) == 1) { // diff > 0.1% of bp
                        LOG.warn("Probable bad settlement from adspace[" + impression.getAdSpaceId() + "]. Settling at [" + settlementPrice + "] for impression["
                                + impression.getExternalID() + "] originally bid at [" + bidPrice + "]. Creative - [" + impression.getCreativeId() + "]");
                    }
                }

            } catch (Exception x) {
                stats.increment(context.getAdSpace(), AsCounter.WinPriceError);
                LOG.error("Failed to settle price " + priceString + " AdSpace: " + impression.getAdSpaceId() + ", Creative: " + creative, x);
            }
        } else {
            //MAX-93 temporary counter.
            stats.increment(context.getAdSpace(), AsCounter.WinPriceMissing);
            LOG.warn("No settlement price present for adspace " + impression.getAdSpaceId() + " and creative " + creative.getId() + " and impression " + impression.getExternalID()
                    + " with RTBCONFIG winotice as " + rtbConfig.getWinNoticeMode().name());
        }
        return settlementPrice;
    }

    @Autowired
    RtbCacheService rtbCacheService;

    @Autowired
    AdServerStats astats;

    /**
     * Usually our loss is simply timeout based, but some exchanges have loss notification service and using it we can handle our rtb losses more quickly
     */
    public RtbBidDetails bidLoss(String impressionId, String reason) {

        /**
         * It looks that Mopub sends loss notification for bid, but later displays as if it wins
         * Removing from impression from cache makes our beacon invocation fail later
         * 
         * We need to find intersection between impression external ids from loss and win
         */
        RtbBidDetails bidDetails = rtbCacheService.getBidDetails(impressionId);
        Impression impression = bidDetails.getImpression();
        astats.loss(impression, reason);
        return bidDetails;

        /*
        RtbBidDetails bidDetails = rtbBidManager.removeBidDetails(impressionId);
        if (bidDetails != null) {
            rtbBidManager.logFailedBid(bidDetails, reason);
        }
        return bidDetails;
        */

    }

    public static class RubiconVastRtbConfig extends RtbConfigDto {

        private static final long serialVersionUID = 1L;

        private final RtbConfigDto delegate;

        public RubiconVastRtbConfig(RtbConfigDto delegate) {
            this.delegate = delegate;
        }

        @Override
        public RtbAdMode getAdMode() {
            return RtbAdMode.WIN_NOTICE;
        }

        @Override
        public RtbWinNoticeMode getWinNoticeMode() {
            return RtbWinNoticeMode.OPEN_RTB;
        }

        @Override
        public Long getId() {
            return delegate.getId();
        }

        @Override
        public void setId(Long id) {
            delegate.setId(id);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public DecryptionScheme getDecryptionScheme() {
            return delegate.getDecryptionScheme();
        }

        @Override
        public void setDecryptionScheme(DecryptionScheme decryptionScheme) {
            delegate.setDecryptionScheme(decryptionScheme);
        }

        @Override
        public void setAdMode(RtbAdMode adMode) {
            delegate.setAdMode(adMode);
        }

        @Override
        public void setWinNoticeMode(RtbWinNoticeMode winNoticeMode) {
            delegate.setWinNoticeMode(winNoticeMode);
        }

        @Override
        public AdmProfile getAdmProfile() {
            return delegate.getAdmProfile();
        }

        @Override
        public void setAdmProfile(AdmProfile admProfile) {
            delegate.setAdmProfile(admProfile);
        }

        @Override
        public String getSpMacro() {
            return delegate.getSpMacro();
        }

        @Override
        public void setSpMacro(String spMacro) {
            delegate.setSpMacro(spMacro);
        }

        @Override
        public String getEscapedClickForwardURL() {
            return delegate.getEscapedClickForwardURL();
        }

        @Override
        public void setEscapedClickForwardURL(String escapedClickForwardURL) {
            delegate.setEscapedClickForwardURL(escapedClickForwardURL);
        }

        @Override
        public String getClickForwardValidationPattern() {
            return delegate.getClickForwardValidationPattern();
        }

        @Override
        public void setClickForwardValidationPattern(String clickForwardValidationPattern) {
            delegate.setClickForwardValidationPattern(clickForwardValidationPattern);
        }

        @Override
        public String getDpidFallback() {
            return delegate.getDpidFallback();
        }

        @Override
        public void setDpidFallback(String dpidFallback) {
            delegate.setDpidFallback(dpidFallback);
        }

        @Override
        public String getPrefixonEscapedURLs() {
            return delegate.getPrefixonEscapedURLs();
        }

        @Override
        public void setPrefixonEscapedURLs(String prefixonEscapedURLs) {
            delegate.setPrefixonEscapedURLs(prefixonEscapedURLs);
        }

        @Override
        public String getIntegrationTypePrefix() {
            return delegate.getIntegrationTypePrefix();
        }

        @Override
        public void setIntegrationTypePrefix(String integrationTypePrefix) {
            delegate.setIntegrationTypePrefix(integrationTypePrefix);
        }

        @Override
        public String getBidCurrency() {
            return delegate.getBidCurrency();
        }

        @Override
        public void setBidCurrency(String bidCurrency) {
            delegate.setBidCurrency(bidCurrency);
        }

        @Override
        public RtbAuctionType getAuctionType() {
            return delegate.getAuctionType();
        }

        @Override
        public void setAuctionType(RtbAuctionType auctionType) {
            delegate.setAuctionType(auctionType);
        }

        @Override
        public long getRtbLostTimeDuration() {
            return delegate.getRtbLostTimeDuration();
        }

        @Override
        public void setRtbLostTimeDuration(long rtbLostTimeDuration) {
            delegate.setRtbLostTimeDuration(rtbLostTimeDuration);
        }

        @Override
        public boolean isSslRequired() {
            return delegate.isSslRequired();
        }

        @Override
        public void setSslRequired(boolean sslRequired) {
            delegate.setSslRequired(sslRequired);
        }

        @Override
        public String getSecurityAlias() {
            return delegate.getSecurityAlias();
        }

        @Override
        public void setSecurityAlias(String securityAlias) {
            delegate.setSecurityAlias(securityAlias);
        }

        @Override
        public void setImpTrackMode(RtbImpTrackMode impTrackMode) {
            delegate.setImpTrackMode(impTrackMode);
        }

        @Override
        public RtbImpTrackMode getImpTrackMode() {
            return delegate.getImpTrackMode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

    }
}
