package com.adfonic.domain;

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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="GEOTARGET_TYPE")
public class GeotargetType extends BusinessKey implements Named {

	private static final long serialVersionUID = 1L;
	
	public static final String RADIUS_TYPE = "RADIUS";
	public static final String COORDINATES = "COORDINATES";
	public static final String POST_CODE = "Postal Code";
	public static final String ZIP_CODE = "Zip Code";

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @Column(name="NAME",length=45,nullable=false)
    private String name;

    @Column(name="TYPE",length=45,nullable=false)
    private String type;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="GEOTARGET_BY",joinColumns=@JoinColumn(name="GEOTARGET_TYPE_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="COUNTRY_ID",referencedColumnName="ID"))
    @Fetch(FetchMode.SELECT)
    Set<Country> countries;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<Country> getCountries() {
		return countries;
	}

	public void setCountries(Set<Country> countries) {
		this.countries = countries;
	}

    
}
