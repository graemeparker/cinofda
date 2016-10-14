package com.adfonic.adserver.rtb;

/**
 * 5.19 No-Bid Reason Codes
 * The following table lists the options for a bidder to signal the exchange as to why it did not offer a bid for the impression.
 * 
 * 0 - Unknown Error
 * 1 - Technical Error
 * 2 - Invalid Request
 * 3 - Known Web Spider
 * 4 - Suspected Non-Human Traffic
 * 5 - Cloud, Data center, or Proxy IP
 * 6 - Unsupported Device
 * 7 - Blocked Publisher or Site
 * ￼8 - Unmatched User
 */
public enum NoBidReason {

    @Deprecated
    REASON_UNKNOWN(0), //Not used anymore (and never was)
    @Deprecated
    IMPRESSION_NOT_NEEDED(1), // Not used anymore (NOTHING_TO_BID)
    @Deprecated
    IMPRESSION_VIOLATES_FILTER(2), // Not used anymore (REQUEST_INVALID / KNOWN_IGNORED)

    TEST_REQUEST(0), // Some exchanges has testing requests 
    NOTHING_TO_BID(1), // Found no suitable creative
    REQUEST_INVALID(2), // Something missing/fishy in bid request
    REQUEST_DROPPED(1), // Timeout or something missing/fishy on our side 
    // NOBID_MODE(1), GLOBAL_THROTTLE(1), PUBLICATION_THROTTLE(1), PUBLICATION_NOTFOUND(1), // 
    KNOWN_IGNORED(1), //Well known REQUEST_INVALID, long standing, non fixable 
    TECHNICAL_ERROR(1); // NullPointerException and other joyful things

    private final int v1nbr;

    private NoBidReason(int v1nbr) {
        this.v1nbr = v1nbr;
    }

    public int getV1nbr() {
        return v1nbr;
    }
}
