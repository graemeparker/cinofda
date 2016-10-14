package com.adfonic.adserver.rtb.nativ;

/**
 * Enumeration of API frameworks defined for the "api" attribute of the "banner" object in OpenRTB.
 * See OpenRTB API Specification Version 2.2, section 6.4.
 *
 * The ordinal values of the enumeration correspond to the integer values in the spec, as per the inline comments. 
 */
public enum APIFramework {
    _,         /* 0, not used */
    VPAID_1_0, /* 1 */
    VPAID_2_0, /* 2 */
    MRAID,     /* 3, aka MRAID_1 */
    ORMMA,     /* 4 */
    MRAID_2    /* 5 */
}
