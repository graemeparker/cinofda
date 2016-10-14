package com.adfonic.datacollector.kafka;

import com.adfonic.adserver.AdEvent;
import com.adfonic.datacollector.AdEventAccounting;

public class AdEventData{
    
    
    private AdEvent adEvent;
    private AdEventAccounting accounting;  
    private Long userAgentId;
    private Integer advertiserTimeId;
    private int publisherTimeId;
    private byte[] originalMessage;
    
    public AdEventData(AdEvent adEvent, AdEventAccounting accounting, Long userAgentId,
            Integer advertiserTimeId, int publisherTimeId, byte[] originalMessage){
        this.adEvent = adEvent;
        this.accounting = accounting;
        this.userAgentId = userAgentId;
        this.advertiserTimeId = advertiserTimeId;
        this.publisherTimeId = publisherTimeId;
        this.originalMessage = originalMessage;
    }
    
    
    public AdEvent getAdEvent() {
        return adEvent;
    }
    public void setAdEvent(AdEvent adEvent) {
        this.adEvent = adEvent;
    }
    public AdEventAccounting getAccounting() {
        return accounting;
    }
    public void setAccounting(AdEventAccounting accounting) {
        this.accounting = accounting;
    }
    public Long getUserAgentId() {
        return userAgentId;
    }
    public void setUserAgentId(Long userAgentId) {
        this.userAgentId = userAgentId;
    }
    public Integer getAdvertiserTimeId() {
        return advertiserTimeId;
    }
    public void setAdvertiserTimeId(Integer advertiserTimeId) {
        this.advertiserTimeId = advertiserTimeId;
    }
    public int getPublisherTimeId() {
        return publisherTimeId;
    }
    public void setPublisherTimeId(int publisherTimeId) {
        this.publisherTimeId = publisherTimeId;
    }
    public byte[] getOriginalMessage() {
        return originalMessage;
    }
    public void setOriginalMessage(byte[] originalMessage) {
        this.originalMessage = originalMessage;
    }
}