package com.adfonic.domain;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="ADSERVER_STATUS")
public class AdserverStatus extends BusinessKey {
    private static final long serialVersionUID = 2L;

    public enum Status {
    	
    	NOT_STARTED ("NOTSTARTED","Check Adserver task not started"),
    	BAD_URL ("BADURL","Malformed URL for Adserver Address"),
    	TIMEDOUT ("TIMEDOUT","No Response for Adserver in allowed table"),
    	OK("OK","Adserver servering correcly"),
    	INVALID_SHARD("INVALID_SHARD","Adserver belongs to invalid shard"),
    	FAILED("FAILED","Adserver responded Incorrectly"),
    	DNS_FAILED("DNSFAILED","Address not found for Adserver");
    	
    	private String status;
    	private String description;
    	
    	Status(String s,String desc){
    		status = s;
    		description=desc;
    	}
    	
    	public String getStatus(){
    		return status;
    	}
    	
    	public String getDescription(){
    		return description;
    	}
    }
    
    {
    	this.status = Status.TIMEDOUT;
    	this.description = Status.TIMEDOUT.getDescription();
    	this.lastUpdated = new Date();
    	this.shard = new AdserverShard();
    }
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME", length=100,nullable=false)
    private String name;
    
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(name="DESCRIPTION", length=255,nullable=false)
    private String description;
    
    @Column(name="LAST_UPDATED",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="ADSERVER_SHARD_ID",nullable=false)
    private AdserverShard shard;
    
    AdserverStatus() {}
    
    public AdserverStatus(AdserverShard shard) {
    	this.shard = shard;
    }

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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public AdserverShard getShard() {
		return shard;
	}

	public void setShard(AdserverShard shard) {
		this.shard = shard;
	}
}
