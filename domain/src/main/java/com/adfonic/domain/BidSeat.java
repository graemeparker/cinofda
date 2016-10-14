package com.adfonic.domain;

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
@Table(name="BID_SEAT")
public class BidSeat extends BusinessKey {
    
    public enum BidSeatType {ADVERTISER, COMPANY, PMP} 
    
    private static final long serialVersionUID = 1L;
    
    public static final Long DEFAULT_BID_SEAT_ID = 1L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @Column(name="SEAT_ID",length=128,nullable=false)
    private String seatId;
    
    @Column(name="DESCRIPTION",length=255,nullable=true)
    private String description;
    
    @Column(name="TYPE", nullable=false)
    @Enumerated(EnumType.STRING)
    private BidSeatType type;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="TARGET_PUBLISHER_ID",nullable=true)
    private TargetPublisher targetPublisher;
    
    BidSeat() {
    }
    
    public BidSeat(String seatId, String description, BidSeatType type, TargetPublisher targetPublisher) {
    	this.seatId = seatId;
    	this.description = description;
    	this.type = type;
    	this.targetPublisher = targetPublisher;
    }
    
    public long getId() {
        return id;
    }

	public String getSeatId() {
		return seatId;
	}

	public void setSeatId(String seatId) {
		this.seatId = seatId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public BidSeatType getType() {
        return type;
    }

    public void setType(BidSeatType type) {
        this.type = type;
    }

    public TargetPublisher getTargetPublisher() {
        return targetPublisher;
    }

    public void setTargetPublisher(TargetPublisher targetPublisher) {
        this.targetPublisher = targetPublisher;
    }
}
