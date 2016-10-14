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

import com.adfonic.domain.RemovalInfo.RemovalType;

@Entity
@Table(name="CREATIVE_REMOVED_PUBLICATION_HISTORY")
public class CreativeRemovedPublicationHistory extends BusinessKey {

    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=false)
    private Creative creative;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_ID",nullable=false)
    private Publication publication;
    
    @Column(name="REMOVAL_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private RemovalType removalType;
    
    @Column(name="REMOVAL_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removalTime;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID",nullable=true)
    private User user;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADFONIC_USER_ID",nullable=true)
    private AdfonicUser adfonicUser;

    public long getId() { return id; }

	public Creative getCreative() {
		return creative;
	}

	public void setCreative(Creative creative) {
		this.creative = creative;
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	public RemovalType getRemovalType() {
		return removalType;
	}

	public void setRemovalType(RemovalType removalType) {
		this.removalType = removalType;
	}

	public Date getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(Date removalTime) {
		this.removalTime = removalTime;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public AdfonicUser getAdfonicUser() {
		return adfonicUser;
	}

	public void setAdfonicUser(AdfonicUser adfonicUser) {
		this.adfonicUser = adfonicUser;
	};


}
