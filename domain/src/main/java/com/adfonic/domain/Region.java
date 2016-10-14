package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name="REGION")
public class Region extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @OneToMany(mappedBy="region",fetch=FetchType.LAZY)
    private Set<Country> countries;
    
    {
	this.countries = new HashSet<Country>();
    }
    
    Region() {}

    public Region(String name) {
	this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Country> getCountries() { return countries; }
}
