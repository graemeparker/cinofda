package com.adfonic.jms;

public class AdSpaceVerifiedMessage implements java.io.Serializable {
    public static final long serialVersionUID = 1L;

    private long adSpaceId;

    public AdSpaceVerifiedMessage() {
    }
    
    public AdSpaceVerifiedMessage(long adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public long getAdSpaceId() {
        return adSpaceId;
    }
    public void setAdSpaceId(long adSpaceId) {
        this.adSpaceId = adSpaceId;
    }
    
    @Override
    public String toString() {
        return "AdSpaceVerifiedMessage{adSpaceId=" + adSpaceId + "}";
    }
}
