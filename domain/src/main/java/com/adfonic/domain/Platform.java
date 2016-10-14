package com.adfonic.domain;

import javax.persistence.*;

import com.adfonic.util.Constrained;

/**
 * This area needs a little more thought, but the idea is that the
 * advertiser can target only devices with certain capabilities.
 * Ideally, the system should know for a given capability whether a
 * device can support it or not.  But I'm not sure how specific we
 * want to get with this...
 */
@Entity
@Table(name="PLATFORM")
public class Platform extends BusinessKey implements Named, Constrained {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;
    @Column(name="DESCRIPTION",length=255,nullable=true)
    private String description;

    // The constraints describes how the engine should test whether the
    // platform is present, e.g. "osRim" (boolean valued property)
    @Column(name="CONSTRAINTS",length=255,nullable=true)
    private String constraints;

    Platform() {}

    public Platform(String name, String systemName) {
        this.name = name;
        this.systemName = systemName;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }
}
