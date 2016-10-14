package com.adfonic.adserver.rtb.yieldlab;

public class YieldlabBidResponse {

    private YieldlabBid bid = new YieldlabBid();

    public YieldlabBidResponse(String tid) {
        bid.setTid(tid);
    }

    YieldlabBidResponse() {
        // marshalling
    }

    public YieldlabBid getBid() {
        return bid;
    }

}
