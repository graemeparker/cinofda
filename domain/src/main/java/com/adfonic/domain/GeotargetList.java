package com.adfonic.domain;

import javax.persistence.*;
import com.adfonic.domain.Geotarget;

@Entity
@Table(name="GEOTARGET_LIST")
public class GeotargetList extends BusinessKey {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue @Column(name="ID")
    private long id;
  
    @Column(name="GEOTARGET_BY_ID",nullable=false)
    private long geotargetById;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="GEOTARGET_ID",nullable=false)
    private Geotarget geotarget;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getGeotargetById() {
		return geotargetById;
	}

	public void setGeotargetById(long geotargetById) {
		this.geotargetById = geotargetById;
	}

	public Geotarget getGeotarget() {
		return geotarget;
	}

	public void setGeotarget(Geotarget geotarget) {
		this.geotarget = geotarget;
	}
	    
}
