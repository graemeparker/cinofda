package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="DMP_VENDOR")
public class DMPVendor extends BusinessKey implements Named {
	
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="RESTRICTED",nullable=false)
    private Boolean restricted;
    @Column(name="ADMIN_ONLY",nullable=false)
    private Boolean adminOnly;
    @OneToMany(mappedBy="dmpVendor",fetch=FetchType.LAZY)
    private Set<DMPAttribute> dmpAttributes;
    @Embedded
    @AttributeOverrides({
      @AttributeOverride(name="dataRetail", column=@Column(name="DEFAULT_DATA_RETAIL")),
      @AttributeOverride(name="dataWholesale", column=@Column(name="DEFAULT_DATA_WHOLESALE"))
    })
    private AudiencePrices defaultAudiencePrices;
    
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="DMP_VENDOR_PUBLISHER",joinColumns=@JoinColumn(name="DMP_VENDOR_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PUBLISHER_ID",referencedColumnName="ID"))
    private Set<Publisher> publishers;
    
    {
    	dmpAttributes = new HashSet<DMPAttribute>();
    	publishers = new HashSet<>();
    }
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getRestricted() {
		return restricted;
	}
	public void setRestricted(Boolean restricted) {
		this.restricted = restricted;
	}
	public Boolean getAdminOnly() {
		return adminOnly;
	}
	public void setAdminOnly(Boolean adminOnly) {
		this.adminOnly = adminOnly;
	}
	public long getId() {
		return id;
	}
	public Set<DMPAttribute> getDmpAttributes() {
		return dmpAttributes;
	}
	public void setDmpAttributes(Set<DMPAttribute> dmpAttributes) {
		this.dmpAttributes = dmpAttributes;
	}
	public AudiencePrices getDefaultAudiencePrices() {
		return defaultAudiencePrices;
	}
	public void setDefaultAudiencePrices(AudiencePrices defaultAudiencePrices) {
		this.defaultAudiencePrices = defaultAudiencePrices;
	}
	public Set<Publisher> getPublishers() {
		return publishers;
	}
	public void setPublishers(Set<Publisher> publishers) {
		this.publishers = publishers;
	}
}
