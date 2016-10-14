package com.adfonic.adserver.rtb.open.v2.ext.appnxs;

import java.util.List;

public class AppNexusSeatBid extends com.adfonic.adserver.rtb.open.v1.SeatBid<AppNexusBid> {

    private List<AppNexusBid> bid;

    @Override
    public List<AppNexusBid> getBid() {
        return bid;
    }

    @Override
    public void setBid(List<AppNexusBid> bid) {
        this.bid = bid;
    }

}
