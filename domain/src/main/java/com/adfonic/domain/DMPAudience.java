package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DMP_AUDIENCE")
public class DMPAudience extends BusinessKey {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue @Column(name="ID")
	private long id;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="AUDIENCE_ID",nullable=false)
    private Audience audience;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DMP_VENDOR_ID",nullable=false)
    private DMPVendor dmpVendor;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="DMP_AUDIENCE_DMP_SELECTOR",joinColumns=@JoinColumn(name="DMP_AUDIENCE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DMP_SELECTOR_ID",referencedColumnName="ID"))
    private Set<DMPSelector> dmpSelectors; 
    @Column(name="USER_ENTERED_DMP_SELECTOR_EXTERNAL_ID", length=255, nullable=true)
    private String userEnteredDMPSelectorExternalId;

    {
    	dmpSelectors = new HashSet<DMPSelector>();
    }
    
	public Audience getAudience() {
		return audience;
	}
	public void setAudience(Audience audience) {
		this.audience = audience;
	}
	public DMPVendor getDmpVendor() {
		return dmpVendor;
	}
	public void setDmpVendor(DMPVendor dmpVendor) {
		this.dmpVendor = dmpVendor;
	}
	public long getId() {
		return id;
	}
	public Set<DMPSelector> getDmpSelectors() {
		return dmpSelectors;
	}
	public void setDmpSelectors(Set<DMPSelector> dmpSelectors) {
		this.dmpSelectors = dmpSelectors;
	}
	public String getUserEnteredDMPSelectorExternalId() {
		return userEnteredDMPSelectorExternalId;
	}
	public void setUserEnteredDMPSelectorExternalId(
			String userEnteredDMPSelectorExternalId) {
		this.userEnteredDMPSelectorExternalId = userEnteredDMPSelectorExternalId;
	}

    public String getDmpSelectorsAsString() {
        return NamedUtils.namedCollectionToString(dmpSelectors);
    }

}
