package com.byyd.middleware.account.filter;

import com.adfonic.domain.Publisher;

public class TargetPublisherFilter {
    
    private Boolean rtb;
    
    private Boolean pmpAvailable;
    
    private String name;
    
    private Publisher publisher;
    
    private boolean hidden;
    
    private Boolean rtbSeatIdAvailable; 
    
    public TargetPublisherFilter(){
        
    }
    
    public TargetPublisherFilter(Boolean rtb, boolean hidden){
        this.rtb = rtb;
        this.hidden = hidden;
    }
    
    public TargetPublisherFilter(Boolean rtb, Boolean pmpAvailable, boolean hidden){
        this.rtb = rtb;
        this.pmpAvailable = pmpAvailable;
        this.hidden = hidden;
    }
    
    public Boolean isRtb() {
        return rtb;
    }
    
    public Boolean isPmpAvailable() {
        return pmpAvailable;
    }

    public void setRtb(Boolean rtb) {
        this.rtb = rtb;
    }

    public void setPmpAvailable(Boolean pmpAvailable) {
        this.pmpAvailable = pmpAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getRtbSeatIdAvailable() {
        return rtbSeatIdAvailable;
    }

    public TargetPublisherFilter setRtbSeatIdAvailable(Boolean rtbSeatIdAvailable) {
        this.rtbSeatIdAvailable = rtbSeatIdAvailable;
        return this;
    }
    
}
