package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="LANGUAGE")
public class Language extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="ISO_CODE",length=255,nullable=false)
    private String isoCode;
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    Language() {}

    public Language(String name, String isoCode) {
	this.name = name;
	this.isoCode = isoCode;
    }

    public long getId() { return id; };
    
    public String getISOCode() { return isoCode; }
    public void setISOCode(String isoCode) { this.isoCode = isoCode; }

    public String getName() { return name; }
    public void setName(String name) {
	this.name = name;
    }
}
