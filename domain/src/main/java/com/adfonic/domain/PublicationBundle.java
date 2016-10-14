package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="BUNDLE")
public class PublicationBundle extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;

    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String name;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="PUBLICATION_BUNDLE", joinColumns=@JoinColumn(name="BUNDLE_ID", referencedColumnName="ID"), inverseJoinColumns=@JoinColumn(name="PUBLICATION_ID", referencedColumnName="ID"))
    private Set <Publication> publications;
    
    {
        this.publications = new HashSet<>();
    }

    PublicationBundle() {
    }
    
    public PublicationBundle(String name){
        this.name = name;
    }

    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }
}
