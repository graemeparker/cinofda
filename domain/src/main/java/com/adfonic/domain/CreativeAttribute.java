package com.adfonic.domain;

import javax.persistence.*;

/**
 * Reference table for attributes that may be assigned to creatives.
 */
@Entity
@Table(name="CREATIVE_ATTRIBUTE")
public class CreativeAttribute extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    
    CreativeAttribute() {}

    public CreativeAttribute(String name) {
        this.name = name;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
