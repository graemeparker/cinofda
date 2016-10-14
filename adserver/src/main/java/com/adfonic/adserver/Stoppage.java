package com.adfonic.adserver;

import java.util.Date;

public final class Stoppage {

    private final long timestamp;
    private final Long reactivateDate;

    public Stoppage(long timestamp, Long reactivateDate) {
        this.timestamp = timestamp;
        this.reactivateDate = reactivateDate;
    }

    public Stoppage(Date timestamp, Date reactivateDate) {
        this.timestamp = timestamp.getTime();
        this.reactivateDate = reactivateDate == null ? null : reactivateDate.getTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Long getReactivateDate() {
        return reactivateDate;
    }

    public boolean isStillInEffect() {
        return reactivateDate == null || reactivateDate > System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Stoppage[timestamp=" + timestamp + ", reactivateDate=" + reactivateDate + "]";
    }
}
