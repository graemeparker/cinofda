package com.adfonic.domain;

import java.math.BigDecimal;

public enum AdAction {
    UNFILLED_REQUEST(BigDecimal.ZERO),
    IMPRESSION(BigDecimal.ONE),
    CLICK(new BigDecimal("0.0055")),
    INSTALL(new BigDecimal("0.00001875")),
    // This is an unconfirmed (beacon will confirm) impression
    AD_SERVED(BigDecimal.ONE),
    // Conversion tracking event
    CONVERSION(new BigDecimal("0.00001875")), // arbitrary for now
    BID_FAILED(BigDecimal.ZERO),
    // Special enum value to indicate to datacollector that we need
    // to log *both* an AD_SERVED and IMPRESSION event.  NOTE that
    // this enum value doesn't exist in the db, it's just for use
    // in communications between adserver and datacollector.
    AD_SERVED_AND_IMPRESSION(BigDecimal.ONE),
    // Video view event types
    COMPLETED_VIEW(BigDecimal.ZERO),
    VIEW_Q1(BigDecimal.ZERO),
    VIEW_Q2(BigDecimal.ZERO),
    VIEW_Q3(BigDecimal.ZERO),
    VIEW_Q4(BigDecimal.ZERO),
    // NOTE: If you add a new value here, you must also add it in MySQL
    ;

    // What is the typical conversion rate for an impression to this
    // action?  Used for converting CPC to eCPM and CPM to eCPC
    private BigDecimal estimatedConversionRate;

    AdAction(BigDecimal estimatedConversionRate) {
	this.estimatedConversionRate = estimatedConversionRate;
    }

    public BigDecimal getEstimatedConversionRate() {
	return estimatedConversionRate;
    }
}














