package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * 
 * @deprecated Use AdxCreativeAttribute instead
 */
public enum RichMediaCapabilityType {

    RICH_MEDIA_CAPABILITY_HTML4(33, "RichMediaCapabilityHTML4"), 
    RICH_MEDIA_CAPABILITY_FLASH(34, "RichMediaCapabilityFlash"), 
    RICH_MEDIA_CAPABILITY_HTML5(39, "RichMediaCapabilityHTML5"), 
    RICH_MEDIA_CAPABILITY_HTML5_BASIC_SVG(40, "RichMediaCapabilityHTML5BasicSVG"), 
    RICH_MEDIA_CAPABILITY_HTML5_SVG_FILTERS(41, "RichMediaCapabilityHTML5SVGFilters"), 
    RICH_MEDIA_CAPABILITY_HTML5_SVG_FONTS(42, "RichMediaCapabilityHTML5SVGFonts"), 
    RICH_MEDIA_CAPABILITY_LARGE_EXPANDABLE(43, "RichMediaCapabilityLargeExpandable"),
    RICH_MEDIA_CAPABILITY_SSL(47, "RichMediaCapabilitySSL"),
    RICH_MEDIA_CAPABILITY_NON_SSL(48, "RichMediaCapabilityNonSSL"),
    RICH_MEDIA_CAPABILITY_NON_FLASH(50, "RichMediaCapabilityNonFlash"),
    RICH_MEDIA_CAPABILITY_INTERSTITIAL(51, "RichMediaCapabilityInterstitial");
    
    int crAttrId;
    String value;

    RichMediaCapabilityType(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId(){
        return crAttrId;
    }
}
