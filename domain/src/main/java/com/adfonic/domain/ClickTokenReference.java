package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CLICK_TOKEN_REFERENCE")
public class ClickTokenReference extends BusinessKey {
	
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    @Column(name="TOKEN",length=64,nullable=false)
    private String token;

    @Column(name="TYPE",length=64,nullable=false)
    private String type;
    
    @Column(name="EXAMPLE_OUTPUT",length=64,nullable=false)
    private String exampleOutput;
   
    @Column(name="DESCRIPTION",length=512,nullable=false)
    private String description;

    @Column(name="SUBSTITUTION_STRING",length=128,nullable=true)
    private String substitutionString;

    public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExampleOutput() {
		return exampleOutput;
	}

	public void setExampleOutput(String exampleOutput) {
		this.exampleOutput = exampleOutput;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getId() {
		return id;
	}

	public String getSubstitutionString() {
		return substitutionString;
	}

	public void setSubstitutionString(String substitutionString) {
		this.substitutionString = substitutionString;
	}
    
    
}
