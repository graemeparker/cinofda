package com.adfonic.dto.campaign.enums;

public enum PluginType {

    FINANCE_EVENT("page.campaign.scheduling.plugintype.financeevent.label", com.adfonic.domain.CampaignTrigger.PluginType.FINANCE_EVENT), SPORT_EVENT(
            "page.campaign.scheduling.plugintype.sportevent.label", com.adfonic.domain.CampaignTrigger.PluginType.SPORT_EVENT), TIMING_EVENT(
            "page.campaign.scheduling.plugintype.timingevent.label", com.adfonic.domain.CampaignTrigger.PluginType.TIMING_EVENT), TV_AD_EVENT(
            "page.campaign.scheduling.plugintype.tvadevent.label", com.adfonic.domain.CampaignTrigger.PluginType.TV_AD_EVENT), TV_GUIDE_EVENT(
            "page.campaign.scheduling.plugintype.tvguideevent.label", com.adfonic.domain.CampaignTrigger.PluginType.TV_GUIDE_EVENT), WEATHER_EVENT(
            "page.campaign.scheduling.plugintype.weatherevent.label", com.adfonic.domain.CampaignTrigger.PluginType.WEATHER_EVENT);

    private String displayName;
    private com.adfonic.domain.CampaignTrigger.PluginType pluginType;

    private PluginType(String displayName, com.adfonic.domain.CampaignTrigger.PluginType pluginType) {
        this.displayName = displayName;
        this.pluginType = pluginType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public com.adfonic.domain.CampaignTrigger.PluginType getPluginType() {
        return pluginType;
    }
}
