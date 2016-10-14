package com.adfonic.adserver.impl;

import java.util.Objects;

/**
 * For RTB response, we need to track if creative is bidding using PMP Deal and Seat or not for public PMP
 * 
 * @author mvanek
 *
 */
public class CreativeBidDeal {

    private final String dealId;

    private final String seatId; //optional

    public CreativeBidDeal(String dealId) {
        this(dealId, null);
    }

    public CreativeBidDeal(String dealId, String seatId) {
        Objects.requireNonNull(dealId);
        this.dealId = dealId;
        this.seatId = seatId;
    }

    public String getDealId() {
        return dealId;
    }

    public String getSeatId() {
        return seatId;
    }

    @Override
    public String toString() {
        return "CreativeBidDeal {dealId=" + dealId + ", seatId=" + seatId + "}";
    }

}
