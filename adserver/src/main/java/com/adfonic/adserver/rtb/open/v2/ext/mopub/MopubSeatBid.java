package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.List;

public class MopubSeatBid extends com.adfonic.adserver.rtb.open.v1.SeatBid<MopubBid> {

    private List<MopubBid> bid;

    @Override
    public List<MopubBid> getBid() {
        return bid;
    }

    @Override
    public void setBid(List<MopubBid> bid) {
        this.bid = bid;
    }

}
