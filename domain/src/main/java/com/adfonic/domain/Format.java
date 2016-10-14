package com.adfonic.domain;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * An advertising format.  Consists of a set of named content
 * specifications, for example a text and image together.
 */
@Entity
@Table(name="FORMAT")
public class Format extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @Column(name="SYSTEM_NAME",length=32,nullable=false)
    private String systemName;

    @Column(name="NAME",length=255,nullable=false)
    private String name;

    @OneToMany(fetch=FetchType.LAZY)
    @OrderColumn(name="FORMAT_ORDER",nullable=false,insertable=true,updatable=true)
    //@OrderBy("id")
    @JoinColumn(name="FORMAT_ID",nullable=true)
    private List<Component> components;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="FORMAT_DISPLAY_TYPE_LIST",joinColumns=@JoinColumn(name="FORMAT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DISPLAY_TYPE_ID",referencedColumnName="ID"))
    @OrderColumn(name="FORMAT_ORDER",nullable=false,insertable=true,updatable=true)
    //@OrderBy("id")
    private List<DisplayType> displayTypes;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="FORMAT_DEVICE_GROUP",joinColumns=@JoinColumn(name="FORMAT_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="DEVICE_GROUP_ID",referencedColumnName="ID"))
    private Set<DeviceGroup> deviceGroups;

    {
        this.components = new LinkedList<Component>();
        this.displayTypes = new LinkedList<DisplayType>();
        this.deviceGroups = new HashSet<DeviceGroup>();
    }

    Format() {}

    public Format(String systemName, String name) {
        this.systemName = systemName;
        this.name = name;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Component> getComponents() {
        return components;
    }

    public Component getComponent(int index) {
        return components.get(index);
    }

    public Component newComponent(String systemName, String name) {
        Component component = new Component(this, systemName, name);
        components.add(component);
        return component;
    }

    public List<DisplayType> getDisplayTypes() {
        return displayTypes;
    }

    public DisplayType getDisplayType(int index) {
        return displayTypes.get(index);
    }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public Set<DeviceGroup> getDeviceGroups() {
        return deviceGroups;
    }
}
