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
@Table(name="CAPABILITY")
public class Capability extends BusinessKey implements Named, Constrained {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="DESCRIPTION",length=255,nullable=true)
    private String description;

    // The constraints describes how the engine should test whether the
    // capability is fulfilled, e.g. "umts" (boolean valued property
    // or "midp >= 1.0" (string properties)
    private String constraints;

    Capability() {}

    public Capability(String name) {
	this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
	this.description = description;
    }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }
}
