package com.adfonic.adserver;

import java.math.BigDecimal;
import java.util.Date;

import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

/**
 * Backup logger (formerly CSV logging in AdEventLogger)
 * @see https://tickets.adfonic.com/browse/SC-19
 */
public interface BackupLogger {
    /**
     * Start timing the request from filter
     */
    void startFilterRequest();

    /**
     * Start timing the request from interceptor
     */
    void startControllerRequest();

    /**
     * The request has ended, so stop timing and flush values to the log file
     */
    void endFilterRequest();

    /**
     * Log that an ad request resulted in an rtb bid being served
     * @param bidRequest 
     */
    void logBidServed(Impression impression, Date eventTime, TargetingContext context, ByydRequest bidRequest);

    /**
     * Log that an ad request resulted in an ad being served
     */
    void logAdServed(Impression impression, Date eventTime, TargetingContext context);
    
    /**
     * Log that an impression, used to splid AD_SERVED_AND_IMPRESSION in two different events
     */
    void logImpression(Impression impression, Date eventTime, TargetingContext context);

    /**
     * Log that an ad request resulted in an unfilled request
     */
    void logUnfilledRequest(UnfilledReason unfilledReason, Date eventTime, TargetingContext context);

    /**
     * Log the failure of an ad request
     */
    void logAdRequestFailure(String reason, TargetingContext context, String... extraValues);

    /**
     * Log the successful completion of an RTB bid (win/loss outcome still unknown)
     */
    void logRtbBidSuccess(Impression impression, BigDecimal price, Date eventTime, TargetingContext context);

    /**
     * Log the failure of an RTB bid request (not to be confused with BID_FAILED)
     */
    void logRtbBidFailure(String reason, TargetingContext context, ByydRequest req, String... extraValues);

    /**
     * Log when an RTB bid didn't win the auction (this one is BID_FAILED)
     */
    void logRtbLoss(Impression impression, Date eventTime, TargetingContext context, String... extraValues);

    /**
     * Log when an RTB bid won the auction and we served it successfully
     */
    void logRtbWinSuccess(Impression impression, BigDecimal settlementPrice, Date eventTime, TargetingContext context);

    /**
     * Log the failure of an RTB win
     */
    void logRtbWinFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues);

    /**
     * Log the successful result of a beacon request
     */
    void logBeaconSuccess(Impression impression, Date eventTime, TargetingContext context);

    /**
     * Log the failure of a beacon request.  This version of the method should
     * be used only when an Impression object has not been established.  If
     * the Impression is handy, call the other variant (see below).
     */
    void logBeaconFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues);

    /**
     * Log the failure of a beacon request.  This version of the method should
     * be used any time the Impression object has been established.  This
     * enables us to log more detail.
     */
    void logBeaconFailure(Impression impression, String reason, TargetingContext context, String... extraValues);

    /**
     * Log the successful result of a clickthrough
     */
    void logClickSuccess(Impression impression, AdSpaceDto adspace, Date eventTime, Long campaignId, TargetingContext context);

    /**
     * Log the failure of a clickthrough.  This version of the method should
     * be used only when an Impression object has not been established.  If
     * the Impression is handy, call the other variant (see below).
     */
    void logClickFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues);

    /**
     * Log the failure of a clickthrough.  This version of the method should
     * be used any time the Impression object has been established.  This
     * enables us to log more detail.
     */
    void logClickFailure(Impression impression, String reason, TargetingContext context, String... extraValues);
}
