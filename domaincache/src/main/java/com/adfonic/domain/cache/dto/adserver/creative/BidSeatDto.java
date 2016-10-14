package com.adfonic.domain.cache.dto.adserver.creative;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class BidSeatDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private String seatId;

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    @Override
    public String toString() {
        return "BidSeatDto {" + getId() + ", seatId=" + seatId + "}";
    }

}
