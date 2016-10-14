package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 *  @deprecated Use AdxCreativeAttribute instead
 */
public enum CookieTargeting {

    IS_COOKIE_TARGETED(8, "IsCookieTargeted");

    int crAttrId;
    String value;

    CookieTargeting(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
