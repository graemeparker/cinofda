package com.adfonic.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="ADSERVER_SHARD")
public class AdserverShard extends BusinessKey implements Named {
	
	public enum Mode { INCLUDE, EXCLUDE }
	
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME", length=100,nullable=false)
    private String name;
    
    @Column(name="LAST_UPDATED",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    
    @Column(name="MODE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Mode mode;
    
    @Column(name="RTB",nullable=false)
    private boolean rtb;
    
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public boolean isRtb() {
		return rtb;
	}

	public void setRtb(boolean rtb) {
		this.rtb = rtb;
	}
}
