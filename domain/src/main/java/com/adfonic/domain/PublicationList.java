package com.adfonic.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PUBLICATION_LIST")
public class PublicationList extends  BusinessKey {
	
    private static final long serialVersionUID = 1L;
    
    public enum PublicationListLevel { COMPANY_LEVEL, ADVERTISER_LEVEL }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=64, nullable=false)
    private String name;
    
    @Column(name="PUBLICATION_LIST_LEVEL",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private PublicationListLevel publicationListLevel;
	
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=true)
    private Company company;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ADVERTISER_ID",nullable=true)
    private Advertiser advertiser;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_LIST_PUBLICATION",joinColumns=@JoinColumn(name="PUBLICATION_LIST_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PUBLICATION_ID",referencedColumnName="ID"))
    private Set<Publication> publications;

    @Column(name="WHITE_LIST",nullable=false)
    private Boolean whiteList;
    
    @Column(name="SNAPSHOT_DATE_TIME",nullable=false)
    private Date snapshotDateTime;

	public PublicationListLevel getPublicationListLevel() {
		return publicationListLevel;
	}

	public void setPublicationListLevel(PublicationListLevel publicationListLevel) {
		this.publicationListLevel = publicationListLevel;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Advertiser getAdvertiser() {
		return advertiser;
	}

	public void setAdvertiser(Advertiser advertiser) {
		this.advertiser = advertiser;
	}

	public Set<Publication> getPublications() {
		return publications;
	}

	public void setPublications(Set<Publication> publications) {
		this.publications = publications;
	}

	public Boolean getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(Boolean whiteList) {
		this.whiteList = whiteList;
	}

	public long getId() {
		return id;
	}

	public Date getSnapshotDateTime() {
		return snapshotDateTime;
	}

	public void setSnapshotDateTime(Date snapshotDateTime) {
		this.snapshotDateTime = snapshotDateTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
    
}
