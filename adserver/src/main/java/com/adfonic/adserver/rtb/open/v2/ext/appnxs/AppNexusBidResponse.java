package com.adfonic.adserver.rtb.open.v2.ext.appnxs;

import java.util.ArrayList;
import java.util.List;

public class AppNexusBidResponse extends com.adfonic.adserver.rtb.open.v1.BidResponse<AppNexusSeatBid> {

    public AppNexusBidResponse() {
        seatbid = new ArrayList<>(1);
        seatbid.add(new AppNexusSeatBid());
    }

    private List<AppNexusSeatBid> seatbid;


    @Override
    public List<AppNexusSeatBid> getSeatbid() {
        return seatbid;
    }


    @Override
    public void setSeatbid(List<AppNexusSeatBid> seatbid) {
        this.seatbid = seatbid;
    }

}
