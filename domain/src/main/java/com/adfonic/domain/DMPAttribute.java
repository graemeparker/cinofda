package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="DMP_ATTRIBUTE")
public class DMPAttribute extends BusinessKey implements Named {

    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DMP_VENDOR_ID",nullable=false)
    private DMPVendor dmpVendor;
    @Column(name="DISPLAY_ORDER",nullable=false)
    private Integer displayOrder;
    @OneToMany(mappedBy="dmpAttribute",fetch=FetchType.LAZY)
    private Set<DMPSelector> dmpSelectors;
    
    {
    	dmpSelectors = new HashSet<DMPSelector>();
    }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DMPVendor getDmpVendor() {
		return dmpVendor;
	}
	public void setDmpVendor(DMPVendor dmpVendor) {
		this.dmpVendor = dmpVendor;
	}
	public Integer getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
	public long getId() {
		return id;
	}
	public Set<DMPSelector> getDmpSelectors() {
		return dmpSelectors;
	}
	public void setDmpSelectors(Set<DMPSelector> dmpSelectors) {
		this.dmpSelectors = dmpSelectors;
	}
	
    public String getDmpSelectorsAsString() {
        return NamedUtils.namedCollectionToString(dmpSelectors);
    }

	
}
