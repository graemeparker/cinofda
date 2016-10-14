package com.byyd.middleware.account.filter;

import com.adfonic.domain.TargetPublisher;


public class BidSeatFilter {

    private String seatId;
    private TargetPublisher targetPublisher;

    public String getSeatId() {
        return seatId;
    }

    public BidSeatFilter setSeatId(String seatId) {
        this.seatId = seatId;
        return this;
    }

    public TargetPublisher getTargetPublisher() {
        return targetPublisher;
    }

    public BidSeatFilter setTargetPublisher(TargetPublisher targetPublisher) {
        this.targetPublisher = targetPublisher;
        return this;
    }
}
