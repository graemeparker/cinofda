package com.adfonic.domain;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;

@Entity
@Table(name="COMPONENT")
public class Component extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="FORMAT_ID", insertable=true, updatable=false, nullable=true)
    private Format format;

    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;

    @Column(name="NAME",length=255,nullable=true)
    private String name;
    @OneToMany(fetch=FetchType.LAZY)
    @JoinTable(name="COMPONENT_CONTENT_SPEC_MAP",joinColumns=@JoinColumn(name="COMPONENT_ID"),inverseJoinColumns=@JoinColumn(name="CONTENT_SPEC_ID"))
    @MapKeyJoinColumn(name="DISPLAY_TYPE_ID",referencedColumnName="ID")
    private Map<DisplayType, ContentSpec> contentSpecMap;
    @Column(name="REQUIRED",nullable=false)
    private boolean required;

    {
    this.contentSpecMap = new HashMap<DisplayType, ContentSpec>();
    }

    Component() {}

    /** Use factory method on Format to construct. */
    Component(Format format, String systemName, String name) {
    this.format = format;
    this.systemName = systemName;
    this.name = name;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<DisplayType, ContentSpec> getContentSpecMap() {
    return contentSpecMap;
    }

    public ContentSpec getContentSpec(DisplayType displayType) {
    return contentSpecMap.get(displayType);
    }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }
}
