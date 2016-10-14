package com.adfonic.adserver.rtb.open.v1;

import java.util.List;

import com.adfonic.adserver.rtb.nativ.BaseResponse;

public class BidResponse<T extends SeatBid<? extends Bid>> implements BaseResponse{
    
    private String id;

    private String bidid;
    
    private List<T> seatbid;
    
    private String cur;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBidid() {
        return bidid;
    }

    public void setBidid(String bidid) {
        this.bidid = bidid;
    }

    public List<T> getSeatbid() {
        return seatbid;
    }

    public void setSeatbid(List<T> seatbid) {
        this.seatbid = seatbid;
    }

    public String getCur() {
        return cur;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }

}
