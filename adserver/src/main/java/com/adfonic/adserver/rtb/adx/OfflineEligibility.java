package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 *  @deprecated Use AdxCreativeAttribute instead
 */
public enum OfflineEligibility {

    NOT_ONLINE_ELIGIBLE(45, "Not Online Eligible"),
    OFFLINE_ELIGIBLE(46, "Offline Eligible");

    int crAttrId;
    String value;

    OfflineEligibility(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId(){
        return crAttrId;
    }
}
