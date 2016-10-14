package com.adfonic.dto.campaign.enums;

public enum CreativeTrackedProperty {
    // Trigger re-approval
    TAG("tag", true), VENDOR("vendor", true), DESTINATION_TYPE("destination type", true),
    DESTINATION("url", true), AD_TEXT("text", true), BEACON("beacon", true),
    REPRESENTATIVE_ICON("representation icon", true), REPRESENTATIVE_IMAGE("representation image", true),
    // Copy to all functionality
    LANGUAGE_COPY_TO_ALL("language (triggered)", true), DESTINATION_COPY_TO_ALL("destination (triggered)", true),
    
    // NOT trigger re-approval
    STATUS("status"), NAME("name"), LANGUAGE("language"), TRANSLATION("translation"), ATTRIBUTE("attribute"),
    NATIVE_AD_TITLE("title"), NATIVE_AD_DESCRIPTION("description"), NATIVE_AD_CLICK_TO_ACTION("clickToAction"),
    IS_FINAL_DESTINATION("url is final destination"), FINAL_DESTINATION("final destination"),
    // Copy to all functionality
    FINAL_DESTINATION_COPY_TO_ALL("final destination (triggered)");
    
    private String name;
    private boolean reApprovalNeeded;
    
    private CreativeTrackedProperty(String name, boolean reApprovalNeeded) {
        this.name = name;
        this.reApprovalNeeded = reApprovalNeeded;
    }
    
    private CreativeTrackedProperty(String name) {
        this.name = name;
        this.reApprovalNeeded = false;
    }

    public boolean isReApprovalNeeded() {
        return reApprovalNeeded;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
