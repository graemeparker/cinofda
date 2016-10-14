package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it. 
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum DestinationUrlType {

    CLICK_TO_APP(31, "ClickToApp");

    int crAttrId;
    String value;

    DestinationUrlType(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
