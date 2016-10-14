package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 *  @deprecated Use AdxCreativeAttribute instead
 */
public enum ExpandingDirection {

    EXPANDING_UP(13, "ExpandingUp"), 
    EXPANDING_DOWN(14, "ExpandingDown"), 
    EXPANDING_LEFT(15, "ExpandingLeft"), 
    EXPANDING_RIGHT(16, "ExpandingRight"), 
    EXPANDING_UP_LEFT(17, "ExpandingUpLeft"), 
    EXPANDING_UP_RIGHT(18, "ExpandingUpRight"), 
    EXPANDING_DOWN_LEFT(19, "ExpandingDownLeft"), 
    EXPANDING_DOWN_RIGHT(20, "ExpandingDownRight"), 
    EXPANDING_UP_OR_DOWN(25, "ExpandingUpOrDown"), 
    EXPANDING_LEFT_OR_RIGHT(26, "ExpandingLeftOrRight"), 
    EXPANDING_ANY_DIAGONAL(27, "ExpandingAnyDiagonal");
    
    int crAttrId;
    String value;

    ExpandingDirection(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId(){
        return crAttrId;
    }
}
