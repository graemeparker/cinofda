package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.ArrayList;
import java.util.List;

public class MopubBidResponse extends com.adfonic.adserver.rtb.open.v1.BidResponse<MopubSeatBid>{

    public MopubBidResponse(){
        seatbid = new ArrayList<>(1);
        seatbid.add(new MopubSeatBid());
    }
    
    private List<MopubSeatBid> seatbid;

    @Override
    public List<MopubSeatBid> getSeatbid() {
        return seatbid;
    }

    @Override
    public void setSeatbid(List<MopubSeatBid> seatbid) {
        this.seatbid = seatbid;
    }

}
