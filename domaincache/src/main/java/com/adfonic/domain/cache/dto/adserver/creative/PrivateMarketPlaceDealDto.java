package com.adfonic.domain.cache.dto.adserver.creative;

import java.math.BigDecimal;

import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PrivateMarketPlaceDealDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private Long publisherId;
    private String dealId;
    private AuctionType auctionType;
    private BigDecimal floor;

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public BigDecimal getFloor() {
        return floor;
    }

    public void setFloor(BigDecimal floor) {
        this.floor = floor;
    }

    @Override
    public String toString() {
        return "PrivateMarketPlaceDealDto {" + getId() + ", publisherId=" + publisherId + ", dealId=" + dealId + ", auctionType=" + auctionType + ", floor=" + floor + "}";
    }

}
