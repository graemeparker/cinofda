package com.adfonic.dto.campaign.enums;

public enum ExtendedCreativeDataEnum {
    NA_TITLE("title"), 
    NA_DESCRIPTION("description"), 
    NA_CLICK_TO_ACTION("click_to_action"),
    VAST_DURATION("duration");

    private String key = "";

    ExtendedCreativeDataEnum(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
