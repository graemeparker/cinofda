package com.adfonic.adserver.rtb.adx;

/**
 * Generated from creative-attributes.txt. To be kept in sync with it.
 * @deprecated Use AdxCreativeAttribute instead
 */
@Deprecated
public enum CreativeType {

    TEXT(1, "Text"), IMAGE_RICHMEDIA(2, "Image/Rich Media"), HTML(21, "Html"), VASTVIDEO(22, "VastVideo");

    int crAttrId;
    String value;

    CreativeType(int crAttrId, String value) {
        this.crAttrId = crAttrId;
        this.value = value;
    }

    public int creativeAttributeId() {
        return crAttrId;
    }
}
