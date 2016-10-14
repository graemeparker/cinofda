package com.adfonic.adserver;

/**
 * String constants for various x.SYSTEM_NAME columns used in code
 * 
 * @author mvanek
 * 
 */
public interface SystemName {

    // FORMAT.SYSTEM_NAME
    public static final String FORMAT_TEXT = "text";
    public static final String FORMAT_BANNER = "banner";
    public static final String FORMAT_NATIVE = "native_app_install";
    public static final String FORMAT_VIDEO_PREFIX = "video";

    // COMPONENT.SYSTEM_NAME
    public static final String COMPONENT_IMAGE = "image";
    public static final String COMPONENT_VIDEO = "video";
    // text format components
    public static final String COMPONENT_ADM = "adm";
    public static final String COMPONENT_TEXT = "text";
    public static final String COMPONENT_ICON = "icon";
    // native app format components
    public static final String COMPONENT_APP_ICON = "app_icon";
    public static final String COMPONENT_TITLE = "title";
    public static final String COMPONENT_DESCRIPTION = "description";

    // Parsed from CONTENT_SPEC.MANIFEST string
    public static final String CONTENT_SPEC_WIDTH = "width";
    public static final String CONTENT_SPEC_HEIGHT = "height";

    // DISPLAY_TYPE.SYSTEM_NAME
    public static final String DISPLAY_TYPE_GENERIC = "generic";
    // XXL Image Banner: 320 x 50 (10KB still image, 15KB animated)
    public static final String DISPLAY_TYPE_XXL = "xxl";
    // XL Image Banner: 300 x 50 (10KB still image, 15KB animated)
    public static final String DISPLAY_TYPE_XL = "xl";
    // Promobox / MMA Banner 2:1 (300 x 150) - German invention
    public static final String DISPLAY_TYPE_XLP = DISPLAY_TYPE_GENERIC; // Using "generic" DISPLAY_TYPE for banner variant is not very nice...
    // L Image Banner: 216 x 36
    public static final String DISPLAY_TYPE_L = "l";
    // M Image Banner: 168 x 28 (4KB still image, 6KB animated)
    public static final String DISPLAY_TYPE_M = "m";
    // S Image Banner: 120 x 20 (2KB still image, 3KB animated)
    public static final String DISPLAY_TYPE_S = "s";

    // PUBLICATION_TYPE.SYSTEM_NAME
    public static final String IPHONE_APP = "IPHONE_APP";
    public static final String IPAD_APP = "IPAD_APP";
    public static final String ANDROID_APP = "ANDROID_APP";
    public static final String OTHER_APP = "OTHER_APP";
    public static final String MOBILE_SITE = "MOBILE_SITE";

}
