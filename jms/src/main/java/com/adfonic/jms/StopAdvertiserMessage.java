package com.adfonic.jms;

import java.util.Date;

public class StopAdvertiserMessage implements java.io.Serializable {
    public static final long serialVersionUID = 3L;
    
    private final long advertiserId;
    private final String reason;
    private final Date timestamp;
    private final Date reactivateDate;

    public StopAdvertiserMessage(long advertiserId, String reason, Date timestamp, Date reactivateDate) {
        this.advertiserId = advertiserId;
        this.reason = reason;
        this.timestamp = (timestamp!=null ? new Date(timestamp.getTime()) : null);
        this.reactivateDate = (reactivateDate!=null ? new Date(reactivateDate.getTime()) : null);
    }

    public long getAdvertiserId() {
        return advertiserId;
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
        return "StopAdvertiserMessage[advertiserId=" + advertiserId
            + ",reason=" + reason
            + ",timestamp=" + timestamp
            + ",reactivateDate=" + reactivateDate
            + "]";
    }
}
