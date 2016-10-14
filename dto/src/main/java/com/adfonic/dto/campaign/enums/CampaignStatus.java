package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Campaign;

public enum CampaignStatus {

    ALL("page.dashboard.labels.table.filter.status.options.all", null), 
    
    // Brand spanking NEW campaign
    NEW("page.dashboard.labels.table.filter.status.options.new", Campaign.Status.NEW),
    
    // Edited campaign needs a second NEW_REVIEW
    NEW_REVIEW("page.dashboard.labels.table.filter.status.options.newreview", Campaign.Status.NEW_REVIEW), 
    
    // PENDING Adops approval using Admin
    PENDING("page.dashboard.labels.table.filter.status.options.pending", Campaign.Status.PENDING),  
    
    // PENDING approval, but set to PAUSED after approval by Adops in Admin...not ACTIVE so it doesn't spend yet
    PENDING_PAUSED("page.dashboard.labels.table.filter.status.options.pendingpaused", Campaign.Status.PENDING_PAUSED), 
    
    // Live (ACTIVE) and making $$$$$. I'm relying on this to put my kid through college!
    ACTIVE("page.dashboard.labels.table.filter.status.options.active", Campaign.Status.ACTIVE), 
    
    // Live campaign but PAUSED by the user (UI or API) not making money, MAY start again.
    PAUSED("page.dashboard.labels.table.filter.status.options.paused", Campaign.Status.PAUSED),     
    
    // Tasks sets the status to complete when the CAMPAIGN.END_DATE is reached. 
    COMPLETED("page.dashboard.labels.table.filter.status.options.completed", Campaign.Status.COMPLETED), 
    
    // Campaign has been DELETED by the user to removed from the UI
    DELETED("page.dashboard.labels.table.filter.status.options.deleted", Campaign.Status.DELETED), 
    
    // Campaign has been STOPPED by the user and doens't wish to restart. We have opened this up a little and is no longer permanent. 
    STOPPED("page.dashboard.labels.table.filter.status.options.stopped", Campaign.Status.STOPPED);

    private String campaignStatusStr;
    private Campaign.Status status;

    // Private to stop people making up their own statuses.
    private CampaignStatus(String campaignStatusStr, Campaign.Status status) {
        this.campaignStatusStr = campaignStatusStr;
        this.status = status;
    }

    public String getCampaignStatusStr() {
        return campaignStatusStr;
    }

    public Campaign.Status getStatus() {
        return status;
    }

    /**
     * Helper method. Return enumeration from a String.
     * @param text
     * @return
     */
    public static CampaignStatus fromString(String text) {
        if (text != null) {
            for (CampaignStatus b : CampaignStatus.values()) {
                if (text.equalsIgnoreCase(b.campaignStatusStr)) {
                    return b;
                }
            }
        }
        return null;
    }
}
