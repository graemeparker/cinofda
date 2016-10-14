package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum Tagging {

    IS_TAGGED(7, "IsTagged");

    int crAttrId;
    String value;

    Tagging(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
