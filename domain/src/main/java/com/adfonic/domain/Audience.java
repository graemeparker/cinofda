package com.adfonic.domain;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "AUDIENCE")
@SQLDelete(sql = "UPDATE AUDIENCE SET STATUS = 'DELETED' WHERE id = ?")
public class Audience extends BusinessKey implements Named, HasExternalID {

	private static final long serialVersionUID = 1L;

	public enum Status {
		NEW, NEW_REVIEW, ACTIVE, DELETED
	}

    public enum AudienceType {
        CAMPAIGN_EVENT, DEVICE, SITE_APP, DMP, LOCATION;
    }

	@Id @GeneratedValue @Column(name="ID")
	private long id;
	@Column(name = "NAME", length = 255, nullable = false)
	private String name;
	@Column(name="CREATION_TIME", nullable=false)
	private Date creationTime;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADVERTISER_ID", nullable = false)
	private Advertiser advertiser;
    @OneToOne(mappedBy="audience",fetch=FetchType.LAZY, optional=true)
    private DMPAudience dmpAudience;
    @OneToOne(mappedBy="audience",fetch=FetchType.LAZY, optional=true)
    private FirstPartyAudience firstPartyAudience;
    @Column(name="STATUS", length=64, nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;

    {
    	externalID = UUID.randomUUID().toString();
    	status = Status.NEW;
    }

    @Override
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Advertiser getAdvertiser() {
		return advertiser;
	}
	public void setAdvertiser(Advertiser advertiser) {
		this.advertiser = advertiser;
	}
	public DMPAudience getDmpAudience() {
		return dmpAudience;
	}
	public void setDmpAudience(DMPAudience dmpAudience) {
		this.dmpAudience = dmpAudience;
	}

    @Override
    public long getId() {
		return id;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public FirstPartyAudience getFirstPartyAudience() {
		return firstPartyAudience;
	}
	public void setFirstPartyAudience(FirstPartyAudience firstPartyAudience) {
		this.firstPartyAudience = firstPartyAudience;
	}

    @Override
    public String getExternalID() {
		return externalID;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
}
