package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A device type, e.g. iPhone or Nokia 6680.  The externalID will be used to
 * map the device to an external authority such as DeviceAtlas.
 */
@Entity
@Table(name="MODEL")
public class Model extends BusinessKey implements Named, HasExternalID {
    private static final long serialVersionUID = 5L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="VENDOR_ID",nullable=false)
    private Vendor vendor;

    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @Column(name="EXTERNAL_ID",length=255,nullable=true)
    private String externalID;

    @Column(name="DELETED",nullable=false)
    private boolean deleted;

    @Column(name="HIDDEN",nullable=false)
    private boolean hidden;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="MODEL_PLATFORM",joinColumns=@JoinColumn(name="MODEL_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="PLATFORM_ID",referencedColumnName="ID"))
    private Set<Platform> platforms;

    @ManyToOne
    @JoinColumn(name="DEVICE_GROUP_ID",nullable=true)
    private DeviceGroup deviceGroup;

    {
        platforms = new HashSet<Platform>();
        deleted = false;
        hidden = false;
    }

    Model() {}

    Model(Vendor vendor, String name, DeviceGroup deviceGroup) {
        this.vendor = vendor;
        this.name = name;
        this.deviceGroup = deviceGroup;
    }

    public long getId() { return id; };

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExternalID() { return externalID; }
    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Set<Platform> getPlatforms() { return platforms; }

    public DeviceGroup getDeviceGroup() {
        return deviceGroup;
    }
    public void setDeviceGroup(DeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
    }
}
