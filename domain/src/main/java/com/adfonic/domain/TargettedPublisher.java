package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TARGET_PUBLISHER")
public class TargettedPublisher extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLISHER_ID",nullable=false)
    private Publisher publisher;
    @Column(name="IS_RTB",nullable=false)
    private boolean rtb;
    @Column(name="DISPLAY_NAME",nullable=false)
    private String displayName;
    @Column(name="DISPLAY_PRIORITY",nullable=false)
    private int displayPriority;
    
    
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Publisher getPublisher() {
		return publisher;
	}
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	public boolean isRtb() {
		return rtb;
	}
	public void setRtb(boolean rtb) {
		this.rtb = rtb;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public int getDisplayPriority() {
		return displayPriority;
	}
	public void setDisplayPriority(int displayPriority) {
		this.displayPriority = displayPriority;
	}

}
