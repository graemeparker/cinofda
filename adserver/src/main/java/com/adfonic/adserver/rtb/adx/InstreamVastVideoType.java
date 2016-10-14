package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum InstreamVastVideoType {

    VPAID_FLASH(30, "Vpaid Flash"),
    SKIPPABLE_INSTREAM_VIDEO(44, "Skippable Instream Video");

    int crAttrId;
    String value;

    InstreamVastVideoType(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId(){
        return crAttrId;
    }
}
