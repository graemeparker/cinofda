package com.adfonic.jms;

public class UnStopAdvertiserMessage implements java.io.Serializable {
    public static final long serialVersionUID = 1L;
    
    private final long advertiserId;

    public UnStopAdvertiserMessage(long advertiserId) {
        this.advertiserId = advertiserId;
    }

    public long getAdvertiserId() {
        return advertiserId;
    }

    @Override
    public String toString() {
        return "UnStopAdvertiserMessage[advertiserId=" + advertiserId + "]";
    }
}
