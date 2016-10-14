package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum VideoType {

    ADOBE_FLASH_FLV(3, "Adobe Flash FLV"), 
    REAL_PLAYER(4, "RealPlayer"), 
    QUICK_TIME(5, "QuickTime"), 
    WINDOWS_MEDIA(6, "Windows Media");
    
    int crAttrId;
    String value;

    VideoType(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId(){
        return crAttrId;
    }
}
