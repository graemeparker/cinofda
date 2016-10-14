package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it. 
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum ExpandingAction {

    ROLLOVER_TO_EXPAND(28, "RolloverToExpand");

    int crAttrId;
    String value;

    ExpandingAction(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
