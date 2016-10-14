package com.adfonic.dto.campaign.enums;

public enum ContentSpecKeyEnum {
    CS_NATIVE_AD_ICON("App Icon for NA"), CS_NATIVE_AD_IMAGE("NA Main Image 1200x627");
    String key;

    ContentSpecKeyEnum(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
