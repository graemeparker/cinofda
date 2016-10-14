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
@Table(name="TARGET_PUBLISHER")
public class TargetPublisher extends BusinessKey implements Named{
    private static final long serialVersionUID = 2L;
    
    public enum RtbSeatIdFormat {ALPHANUMERIC, NUMERIC};   
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID",nullable=false)
    private Publisher publisher;
    
    @Column(name="IS_RTB",nullable=false)
    private boolean rtb;
    
    @Column(name="DISPLAY_NAME",length=255 ,nullable=false)
    private String name;    
    
    @Column(name="DISPLAY_PRIORITY",length=11, nullable=false)
    private int displayPriority;
    
    @Column(name="PMP_AVAILABLE",nullable=false)
    private boolean pmpAvailable;
    
    @Column(name="HIDDEN",nullable=false)
    private boolean hidden;
    
    @Column(name="RTB_SEAT_ID_AVAILABLE",nullable=false)
    private boolean rtbSeatIdAvailable;
    
    @Column(name="RTB_SEAT_ID_REGEX",length=255 ,nullable=false)
    private String rtbSeatIdRegEx;
    
    @Column(name="RTB_SEAT_ID_AUTOGEN_FORMAT")
    @Enumerated(EnumType.STRING)
    private RtbSeatIdFormat rtbSeatIdAutogenFormat;
    
    @Column(name="RTB_SEAT_ID_AUTOGEN_MAX")
    private Long rtbSeatIdAutogenMax;
    
    @Column(name="RTB_SEAT_ID_AUTOGEN_COUNTER")
    private Long rtbSeatIdAutogenCounter;
    
    TargetPublisher(){
    }

    public long getId() { 
        return id; 
    }

	public Publisher getPublisher() {
		return publisher;
	}

    @Override
	public String getName() {
		return name;
	}

	public int getDisplayPriority() {
		return displayPriority;
	}

	public boolean isRtb() {
		return rtb;
	}

    public void setRtb(boolean rtb) {
        this.rtb = rtb;
    }

    public boolean isPmpAvailable() {
        return pmpAvailable;
    }

    public void setPmpAvailable(boolean pmpAvailable) {
        this.pmpAvailable = pmpAvailable;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isRtbSeatIdAvailable() {
        return rtbSeatIdAvailable;
    }

    public void setRtbSeatIdAvailable(boolean rtbSeatIdAvailable) {
        this.rtbSeatIdAvailable = rtbSeatIdAvailable;
    }

    public String getRtbSeatIdRegEx() {
        return rtbSeatIdRegEx;
    }

    public void setRtbSeatIdRegEx(String rtbSeatIdRegEx) {
        this.rtbSeatIdRegEx = rtbSeatIdRegEx;
    }

    public Long getRtbSeatIdAutogenCounter() {
        return rtbSeatIdAutogenCounter;
    }

    public void setRtbSeatIdAutogenCounter(Long rtbSeatIdAutogenCounter) {
        this.rtbSeatIdAutogenCounter = rtbSeatIdAutogenCounter;
    }

    public RtbSeatIdFormat getRtbSeatIdAutogenFormat() {
        return rtbSeatIdAutogenFormat;
    }

    public void setRtbSeatIdAutogenFormat(RtbSeatIdFormat rtbSeatIdAutogenFormat) {
        this.rtbSeatIdAutogenFormat = rtbSeatIdAutogenFormat;
    }

    public Long getRtbSeatIdAutogenMax() {
        return rtbSeatIdAutogenMax;
    }

    public void setRtbSeatIdAutogenMax(Long rtbSeatIdAutogenMax) {
        this.rtbSeatIdAutogenMax = rtbSeatIdAutogenMax;
    }
}
