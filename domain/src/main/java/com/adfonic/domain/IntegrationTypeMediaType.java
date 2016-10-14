package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

/**
 * This object exists solely because ORM doesn't deal well with nested
 * collections.  In this case, if IntegrationType could simply support
 * <code>Map<MediaType,Set<ContentForm>></code>, then we wouldn't need
 * this first-class object.  Tricky.  So screw it, we use this.
 */
@Entity
@Table(name="INTEGRATION_TYPE_MEDIA_TYPE")
public class IntegrationTypeMediaType extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="INTEGRATION_TYPE_ID",nullable=false)
    private IntegrationType integrationType;
    @Column(name="MEDIA_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="INTEGRATION_TYPE_MEDIA_FORM_MAP",joinColumns=@JoinColumn(name="INTEGRATION_TYPE_MEDIA_TYPE_ID",referencedColumnName="ID"))
    @Column(name="CONTENT_FORM",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<ContentForm> contentForms;

    {
        contentForms = new HashSet<ContentForm>();
    }
    
    IntegrationTypeMediaType() {}

    IntegrationTypeMediaType(IntegrationType integrationType, MediaType mediaType) {
        this.integrationType = integrationType;
        this.mediaType = mediaType;
    }

    public long getId() {
        return id;
    }
    
    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Set<ContentForm> getContentForms() {
        return contentForms;
    }
}
