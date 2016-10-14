package com.adfonic.adserver;

public class TimeLimit {

    private final long duration;
    private final long expireTime;

    public TimeLimit(long startedTimestamp, long duration) {
        this.duration = duration;
        this.expireTime = startedTimestamp + duration;
    }

    public long getDuration() {
        return duration;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getTimeLeft() {
        return Math.max(expireTime - System.currentTimeMillis(), 0);
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() >= expireTime;
    }

    @Override
    public String toString() {
        return "TimeLimit[duration=" + duration + ",expireTime=" + expireTime + "]";
    }
}