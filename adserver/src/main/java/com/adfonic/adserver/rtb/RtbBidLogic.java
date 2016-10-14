package com.adfonic.adserver.rtb;

import java.text.Format;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

public interface RtbBidLogic {

    public static final Format gmtTimeYMDHFormat = FastDateFormat.getInstance("yyyyMMddHH", TimeZone.getTimeZone("GMT"));

    ByydResponse bid(RtbExecutionContext<?, ?> rtbContext, RtbBidEventListener bidListener, TargetingEventListener targetListener) throws NoBidException;

    /**
     * Handle an RTB win notice and possibly produce an ad response.  An RTB exchange is
     * notifying us that our bid won.
     * We might be using the "Ad Served on the Win Notice" method of delivering the ad, which
     * means now is the time to write the ad content to the response.
     * 
     * @param request
     * @param impressionExternalID the key we use to look up the respective RtbBidDetails in cache
     * @param settlementPrice the actual settlement price for the win
     * @param response
     * @param contentGenerator ContentGenerator used to generate client response from markup if any generated
     */
    /*
    void handleWinNoticeAndWriteResponse(String impressionExternalID, String settlementPrice, HttpServletRequest request, HttpServletResponse response,
            ContentGenerator contentGenerator) throws java.io.IOException;
    */
    /**
     * Handle an RTB win on an already served ad
     * 
     * @param rtbSettlementPrice
     * @param context
     * @param impression
     * @param creative
     * @param rtbConfig TODO
     * @param adXEncodedRtbSettlementPrice
     * @param openXEncryptedRtbSettlementPrice
     * @param rubiconEncodedRtbSettlementPrice
     */
    /*
    void handleWinOnServedAd(String rtbSettlementPrice, TargetingContext context, Impression impression, CreativeDto creative, RtbConfigDto rtbConfig);
    */
}