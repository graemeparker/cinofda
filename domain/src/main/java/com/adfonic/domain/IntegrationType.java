package com.adfonic.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

/**
 * An IntegrationType represents the method with which a publisher
 * is integrating with our API.  It specifies capabilities such as
 * supported features (i.e. rich media, beacon, sms), supported
 * beacon rendering modes, etc.
 */
@Entity
@Table(name="INTEGRATION_TYPE")
public class IntegrationType extends BusinessKey implements Named {
    private static final long serialVersionUID = 6L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    
    @Column(name="SYSTEM_NAME",length=64,nullable=false)
    private String systemName;

    @Column(name="PREFIX",length=64,nullable=true)
    private String prefix;

    @Column(name="VERSION_RANGE_START",nullable=true)
    private Integer versionRangeStart;

    @Column(name="VERSION_RANGE_END",nullable=true)
    private Integer versionRangeEnd;
    
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="INTEGRATION_TYPE_FEATURE",joinColumns=@JoinColumn(name="INTEGRATION_TYPE_ID",referencedColumnName="ID"))
    @Column(name="FEATURE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<Feature> supportedFeatures;
    
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="INTEGRATION_TYPE_BEACON_MODE",joinColumns=@JoinColumn(name="INTEGRATION_TYPE_ID",referencedColumnName="ID"))
    @Column(name="BEACON_MODE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<BeaconMode> supportedBeaconModes;
    
    // JPA
    @OneToMany(mappedBy="integrationType",fetch=FetchType.LAZY)
    @MapKeyColumn(name="MEDIA_TYPE",length=16,nullable=false)
    @MapKeyClass(MediaType.class)
    @MapKeyEnumerated(EnumType.STRING)
    // JDO
    private Map<MediaType,IntegrationTypeMediaType> mediaTypeMap;

    {
        this.supportedFeatures = new HashSet<Feature>();
        this.supportedBeaconModes = new HashSet<BeaconMode>();
        this.mediaTypeMap = new HashMap<MediaType,IntegrationTypeMediaType>();
    }
    
    IntegrationType() {}

    public IntegrationType(String name, String systemName) {
        this.name = name;
        this.systemName = systemName;
    }

    public long getId() { return id; };
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getVersionRangeStart() {
        return versionRangeStart;
    }
    public void setVersionRangeStart(Integer versionRangeStart) {
        this.versionRangeStart = versionRangeStart;
    }

    public Integer getVersionRangeEnd() {
        return versionRangeEnd;
    }
    public void setVersionRangeEnd(Integer versionRangeEnd) {
        this.versionRangeEnd = versionRangeEnd;
    }

    public Set<Feature> getSupportedFeatures() {
        return supportedFeatures;
    }
    
    public Set<BeaconMode> getSupportedBeaconModes() {
        return supportedBeaconModes;
    }

    public Map<MediaType,IntegrationTypeMediaType> getMediaTypeMap() {
        return mediaTypeMap;
    }

    /**
     * Is a given MediaType supported by this IntegrationType?
     * @return true if there is at least one supported ContentForm for the given MediaType
     */
    public boolean isMediaTypeSupported(MediaType mediaType) {
        return mediaTypeMap.containsKey(mediaType);
    }

    /**
     * Convenience method so callers don't need to know about IntegrationTypeMediaType
     */
    public Set<ContentForm> getSupportedContentForms(MediaType mediaType) {
        IntegrationTypeMediaType itmt = mediaTypeMap.get(mediaType);
        return itmt == null ? Collections.<ContentForm>emptySet() : itmt.getContentForms();
    }

    /**
     * Convenience method so callers don't need to know about IntegrationTypeMediaType
     */
    public void setSupportedContentForms(MediaType mediaType, Set<ContentForm> contentForms) {
        if (contentForms == null || contentForms.isEmpty()) {
            mediaTypeMap.remove(mediaType);
        } else {
            IntegrationTypeMediaType itmt = mediaTypeMap.get(mediaType);
            if (itmt == null) {
                itmt = new IntegrationTypeMediaType(this, mediaType);
                mediaTypeMap.put(mediaType, itmt);
            } else {
                itmt.getContentForms().clear();
            }
            itmt.getContentForms().addAll(contentForms);
        }
    }
}
