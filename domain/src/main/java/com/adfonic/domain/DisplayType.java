package com.adfonic.domain;

import javax.persistence.*;

import com.adfonic.util.Constrained;

/**
 * Examples:
 *  1) Small, Medium, Large, XL device classes for banners
 *  2) Flash vs. Silverlight vs. Javascript for interactive ads
 *  ...others TBD...
 */
@Entity
@Table(name="DISPLAY_TYPE")
public class DisplayType extends BusinessKey implements Named, Constrained {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="CONSTRAINTS",length=2048,nullable=true)
    private String constraints;

    DisplayType() {}

    public DisplayType(String systemName, String name, String constraints) {
	this.systemName = systemName;
	this.name = name;
	this.constraints = constraints;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * The constraints is an application-specific formatted value that describes
     * how the application is to identify suitable displays.
     * For instance, name/value pairs like "displayWidth>200;displayWidth<600"
     */
    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }
}
