package com.adfonic.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.collections.MapUtils;

/**
 * Extended creative type support
 */
@Entity
@Table(name="EXTENDED_CREATIVE_TYPE")
public class ExtendedCreativeType extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="EXTENDED_CREATIVE_TYPE_FEATURE",joinColumns=@JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID",referencedColumnName="ID"))
    @Column(name="FEATURE",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<Feature> features;
    @Column(name="MEDIA_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    @ElementCollection(fetch=FetchType.LAZY,targetClass=String.class)
    @CollectionTable(name="EXTENDED_CREATIVE_TYPE_TEMPLATE_MAP",joinColumns=@JoinColumn(name="EXTENDED_CREATIVE_TYPE_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="CONTENT_FORM",length=32,nullable=false)
    @MapKeyClass(ContentForm.class)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name="TEMPLATE",length=255,nullable=false)
    private Map<ContentForm,String> templateMap;
    @Column(name="CLICK_REDIRECT_REQUIRED",nullable=false)
    private boolean clickRedirectRequired;
    
    @Column(name="HIDDEN",nullable=false)
    private boolean hidden;

    @Column(name="USE_DYNAMIC_TEMPLATES", nullable=false)
    private boolean useDynamicTemplates;
    
    @OneToMany(mappedBy="extendedCreativeType",fetch=FetchType.LAZY)
    private Set<ExtendedCreativeTypeMacro> macros;

    
    {
        features = new HashSet<Feature>();
        templateMap = new HashMap<ContentForm,String>();
        macros = new HashSet<ExtendedCreativeTypeMacro>();
    }

    ExtendedCreativeType() {}

    public ExtendedCreativeType(String name, MediaType mediaType) {
        this.name = name;
        this.mediaType = mediaType;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public Map<ContentForm,String> getTemplateMap() {
        return templateMap;
    }

    public String getTemplate(ContentForm contentForm) {
        return templateMap.get(contentForm);
    }

    public void setTemplate(ContentForm contentForm, String template) {
        templateMap.put(contentForm, template);
    }

    public boolean isClickRedirectRequired() {
        return clickRedirectRequired;
    }
    public void setClickRedirectRequired(boolean clickRedirectRequired) {
        this.clickRedirectRequired = clickRedirectRequired;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    public boolean isUseDynamicTemplates() {
        return useDynamicTemplates;
    }

    public void setUseDynamicTemplates(boolean useDynamicTemplates) {
        this.useDynamicTemplates = useDynamicTemplates;
    }
    
    public List<ContentForm> getContentForms() {
        List<ContentForm> contentForms = new ArrayList<ContentForm>(0);
        if (MapUtils.isNotEmpty(templateMap)) {
            contentForms.addAll(templateMap.keySet());
            ContentForm.sortByDescription(contentForms);
        }
        return contentForms;
    }

	public Set<ExtendedCreativeTypeMacro> getMacros() {
		return macros;
	}

	public void setMacros(Set<ExtendedCreativeTypeMacro> macros) {
		this.macros = macros;
	}
    
    
}
