package com.adfonic.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="PLUGIN_VENDOR")
public class PluginVendor extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;
	
    @Column(name="NAME")
	private String name;
    
    @Column(name="API_USER")
	private String apiUser;

    @Column(name="API_KEY")
	private String apiPassword;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLUGIN_VENDOR_ID", nullable = true)
    private Set<CampaignTrigger> campaignTriggers;
    
    public long getId() {
    	return id;
    }
    
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getApiUser() {
		return apiUser;
	}

	public void setApiUser(String apiUser) {
		this.apiUser = apiUser;
	}

	public String getApiPassword() {
		return apiPassword;
	}

	public void setApiPassword(String apiPassword) {
		this.apiPassword = apiPassword;
	}
}
