package com.adfonic.domain;

import javax.persistence.*;

import com.adfonic.util.Constrained;

@Entity
@Table(name="DEVICE_GROUP")
public class DeviceGroup extends BusinessKey implements Constrained {
    private static final long serialVersionUID = 1L;

    public static final String DEVICE_GROUP_TABLET_SYSTEM_NAME = "TABLET";
    public static final String DEVICE_GROUP_MOBILE_SYSTEM_NAME = "MOBILE";

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;

    @Column(name="CONSTRAINTS",length=2048,nullable=true)
    private String constraints;
    
    @Column(name="HIDDEN",nullable=false)
    private boolean hidden;


    DeviceGroup() {}

    public DeviceGroup(String systemName, String constraints) {
        this.systemName = systemName;
        this.constraints = constraints;
    }

    public long getId() { return id; };

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
    
    
}
