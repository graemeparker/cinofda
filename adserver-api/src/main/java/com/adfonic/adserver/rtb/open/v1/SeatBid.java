package com.adfonic.adserver.rtb.open.v1;

import java.util.List;

public class SeatBid<T extends Bid> {
    
    private List<T> bid;
    private String seat;

    public List<T> getBid() {
        return bid;
    }

    public void setBid(List<T> bid) {
        this.bid = bid;
    }

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}
}
