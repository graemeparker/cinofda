package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.*;

@Entity
@Table(name="VENDOR")
public class Vendor extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="REVIEWED",nullable=false)
    private boolean reviewed = false;
    @OneToMany(mappedBy="vendor",fetch=FetchType.LAZY)
    private Set<Model> models;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="VENDOR_ALIAS",joinColumns=@JoinColumn(name="VENDOR_ID"))
    @Column(name="ALIAS",length=18,nullable=false)
    private Set<String> aliases;

    {
        this.models = new HashSet<Model>();
        this.aliases = new TreeSet<String>();
    }
    
    Vendor() {}

    public Vendor(String name) {
        this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Set<Model> getModels() { return models; }

    public Set<String> getAliases() { return aliases; }

    public Model newModel(String name, DeviceGroup deviceGroup) {
        Model model = new Model(this, name, deviceGroup);
        models.add(model);
        return model;
    }
}
