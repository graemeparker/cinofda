package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum UserInterestTargeting {

    IS_USER_INTEREST_TARGETED(9, "IsUserInterestTargeted");

    int crAttrId;
    String value;

    UserInterestTargeting(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
