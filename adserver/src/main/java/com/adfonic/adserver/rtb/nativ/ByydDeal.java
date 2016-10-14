package com.adfonic.adserver.rtb.nativ;

import java.util.List;

public class ByydDeal {

    private String id;
    private Double bidFloor;
    private List<String> seats;

    protected ByydDeal() {
        //marshalling
    }

    /**
     * AdX has only deals without seats
     */
    public ByydDeal(String dealId) {
        this(dealId, null);
    }

    /**
     * Normal OpenRTB PMP has both seat and deal
     */
    public ByydDeal(String dealId, List<String> seats) {
        this.id = dealId;
        this.seats = seats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getBidFloor() {
        return bidFloor;
    }

    public void setBidFloor(Double bidFloor) {
        this.bidFloor = bidFloor;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    @Override
    public String toString() {
        return "ByydDeal {id=" + id + ", bidFloor=" + bidFloor + ", seats=" + seats + "}";
    }

}
