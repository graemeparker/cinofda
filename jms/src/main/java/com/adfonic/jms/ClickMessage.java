package com.adfonic.jms;

import java.util.Date;

public class ClickMessage implements java.io.Serializable {
    public static final long serialVersionUID = 2L;

    private final Date creationTime;
    private final byte[] serializedImpression;
    private final long campaignId;
    private final long publicationId;
    private final String ipAddress;
    private final String userAgentHeader;

    public ClickMessage(byte[] serializedImpression, long campaignId, long publicationId, String ipAddress, String userAgentHeader) {
        this.creationTime = new Date();
        if (serializedImpression!=null){
            this.serializedImpression = serializedImpression.clone();
        }else{
            this.serializedImpression = null;
        }
        this.campaignId = campaignId;
        this.publicationId = publicationId;
        this.ipAddress = ipAddress;
        this.userAgentHeader = userAgentHeader;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public byte[] getSerializedImpression() {
        return serializedImpression;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public long getPublicationId() {
        return publicationId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }
    
    @Override
    public String toString() {
        return "ClickMessage[creationTime=" + creationTime
            + ",serializedImpression=(" + serializedImpression.length + " bytes)"
            + ",campaignId=" + campaignId
            + ",publicationId=" + publicationId
            + ",ipAddress=" + ipAddress
            + ",userAgentHeader=" + userAgentHeader
            + "]";
    }
}
