package com.adfonic.domain;

import java.util.HashSet;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "FIRST_PARTY_AUDIENCE")
public class FirstPartyAudience extends BusinessKey {

    private static final long serialVersionUID = 1L;
    
    public enum Type {
    	CLICK, INSTALL, CONVERSION, UPLOAD, COLLECT, LOCATION
    }
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="AUDIENCE_ID",nullable=false)
    private Audience audience;
    // Note, this boolean does not apply to types UPLOAD
    @Column(name="ACTIVE", nullable=false)
    private boolean active;
 	@Column(name="MUID_SEGMENT_ID")
    private Long muidSegmentId;
    @Column(name="TYPE",length=64,nullable=false)
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="FIRST_PARTY_AUDIENCE_CAMPAIGN",joinColumns=@JoinColumn(name="FIRST_PARTY_AUDIENCE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="CAMPAIGN_ID",referencedColumnName="ID"))
    private Set<Campaign> campaigns; 
    @OneToMany(mappedBy="firstPartyAudience",fetch=FetchType.LAZY)
    private Set<FirstPartyAudienceDeviceIdsUploadHistory> deviceIdsUploadHistory;

    {
    	campaigns = new HashSet<Campaign>();
    	active = true;
    	deviceIdsUploadHistory = new HashSet<FirstPartyAudienceDeviceIdsUploadHistory>();
    }
    
    public boolean isActive() {
 		return active;
 	}
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 	public Type getType() {
 		return type;
 	}
 	public void setType(Type type) {
 		this.type = type;
 	}
 	public Set<Campaign> getCampaigns() {
 		return campaigns;
 	}
 	public void setCampaigns(Set<Campaign> campaigns) {
 		this.campaigns = campaigns;
 	}
 	public long getId() {
 		return id;
 	}
	public Audience getAudience() {
		return audience;
	}
	public void setAudience(Audience audience) {
		this.audience = audience;
	}
	public Long getMuidSegmentId() {
		return muidSegmentId;
	}
	public void setMuidSegmentId(Long muidSegmentId) {
		this.muidSegmentId = muidSegmentId;
	}

	public String getCampaignsAsString() {
        return NamedUtils.namedCollectionToString(campaigns);
	}
	
	public Set<FirstPartyAudienceDeviceIdsUploadHistory> getDeviceIdsUploadHistory() {
		return deviceIdsUploadHistory;
	}
	public void setDeviceIdsUploadHistory(
			Set<FirstPartyAudienceDeviceIdsUploadHistory> deviceIdsUploadHistory) {
		this.deviceIdsUploadHistory = deviceIdsUploadHistory;
	}

}
