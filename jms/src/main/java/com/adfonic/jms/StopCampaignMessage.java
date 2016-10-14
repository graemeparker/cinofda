package com.adfonic.jms;

import java.util.Date;

public class StopCampaignMessage implements java.io.Serializable {
    public static final long serialVersionUID = 2L;
    
    private final long campaignId;
    private final String reason;
    private final Date timestamp;
    private final Date reactivateDate;

    public StopCampaignMessage(long campaignId, String reason, Date timestamp, Date reactivateDate) {
        this.campaignId = campaignId;
        this.reason = reason;
        this.timestamp = (timestamp!=null ? new Date(timestamp.getTime()) : null);
        this.reactivateDate = (reactivateDate!=null ? new Date(reactivateDate.getTime()) : null);
    }

    public long getCampaignId() {
        return campaignId;
    }

    public String getReason() {
        return reason;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getReactivateDate() {
        return reactivateDate;
    }

    @Override
    public String toString() {
        return "StopCampaignMessage[campaignId=" + campaignId
            + ",reason=" + reason
            + ",timestamp=" + timestamp
            + ",reactivateDate=" + reactivateDate
            + "]";
    }
}
