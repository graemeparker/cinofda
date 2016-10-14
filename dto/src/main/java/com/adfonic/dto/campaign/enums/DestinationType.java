package com.adfonic.dto.campaign.enums;

public enum DestinationType {
    URL("page.campaign.creative.destinationtype.options.url.label", com.adfonic.domain.DestinationType.URL),
    CALL("page.campaign.creative.destinationtype.options.call.label", com.adfonic.domain.DestinationType.CALL),
    AUDIO("page.campaign.creative.destinationtype.options.audio.label", com.adfonic.domain.DestinationType.AUDIO),
    ITUNES_STORE("page.campaign.creative.destinationtype.options.itunesstore.label", com.adfonic.domain.DestinationType.ITUNES_STORE),
    IPHONE_APP_STORE("page.campaign.creative.destinationtype.options.iphoneappstore.label", com.adfonic.domain.DestinationType.IPHONE_APP_STORE),
    ANDROID("page.campaign.creative.destinationtype.options.android.label", com.adfonic.domain.DestinationType.ANDROID),
    VIDEO("page.campaign.creative.destinationtype.options.video.label", com.adfonic.domain.DestinationType.VIDEO);
    
    private String name;
    private com.adfonic.domain.DestinationType destinationType;

    private DestinationType(String name, com.adfonic.domain.DestinationType destinationType) {
        this.name = name;
        this.destinationType = destinationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public com.adfonic.domain.DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(com.adfonic.domain.DestinationType destinationType) {
        this.destinationType = destinationType;
    }

}
