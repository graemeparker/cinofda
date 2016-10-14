package com.adfonic.jms;

public class UnStopCampaignMessage implements java.io.Serializable {
    public static final long serialVersionUID = 1L;
    
    private final long campaignId;

    public UnStopCampaignMessage(long campaignId) {
        this.campaignId = campaignId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    @Override
    public String toString() {
        return "UnStopCampaignMessage[campaignId=" + campaignId + "]";
    }
}
