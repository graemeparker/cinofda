package com.adfonic.domain;

import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="PUBLICATION_TYPE")
public class PublicationType extends BusinessKey implements Named {
    private static final long serialVersionUID = 6L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="MEDIUM",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Medium medium;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_TYPE_PLATFORM",joinColumns=@JoinColumn(name="PUBLICATION_TYPE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PLATFORM_ID",referencedColumnName="ID"))
    private Set<Platform> platforms;
    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;
    @Column(name="DEFAULT_TRACKING_IDENTIFIER_TYPE",nullable=false)
    @Enumerated(EnumType.STRING)
    private TrackingIdentifierType defaultTrackingIdentifierType;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DEFAULT_INTEGRATION_TYPE_ID",nullable=false)
    private IntegrationType defaultIntegrationType;

    PublicationType() {}

    public PublicationType(String name, Medium medium) {
	this.name = name;
	this.medium = medium;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Medium getMedium() { return medium; }
    public void setMedium(Medium medium) { this.medium = medium; }

    public Set<Platform> getPlatforms() {
	return platforms;
    }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) {
	this.systemName = systemName;
    }

    public TrackingIdentifierType getDefaultTrackingIdentifierType() { return defaultTrackingIdentifierType; }
    public void setDefaultTrackingIdentifierType(TrackingIdentifierType defaultTrackingIdentifierType) {
        this.defaultTrackingIdentifierType = defaultTrackingIdentifierType;
    }

    public IntegrationType getDefaultIntegrationType() {
        return defaultIntegrationType;
    }
    public void setDefaultIntegrationType(IntegrationType defaultIntegrationType) {
        this.defaultIntegrationType = defaultIntegrationType;
    }
}
