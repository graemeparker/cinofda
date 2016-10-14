package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PRIVATE_MARKET_PLACE_DEAL")
public class PrivateMarketPlaceDeal extends BusinessKey {
    private static final long serialVersionUID = 1L;
    
    public enum AuctionType {
        NOT_AN_AUCTION, FIRST_PRICE_AUCTION, SECOND_PRICE_ACTION;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    PrivateMarketPlaceDeal() {}
    
    public PrivateMarketPlaceDeal(Publisher publisher, String dealId, AuctionType auctionType, BigDecimal amount) {
    	this.publisher = publisher;
    	this.dealId = dealId;
    	this.auctionType = auctionType;
    	this.amount = amount;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID",nullable=false)
    private Publisher publisher;

    @Column(name="DEAL_ID",length=128,nullable=false)
    private String dealId;
    
    @Column(name="AUCTION_TYPE",nullable=true)
    @Enumerated(EnumType.ORDINAL)
    private AuctionType auctionType;

    @Column(name="FLOOR",nullable=true)
    private BigDecimal amount;

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public long getId() {
		return id;
	}

    
}
