package com.adfonic.tasks.combined.truste.dto;

import java.util.Date;

public class AdditionalIds {
	
	private String idName;
	private String idValue;
	private String tpid;
	private String appId;
	private Date changedDate;
	private Date createdDate;
	
	public Date getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public Date getChangedDate() {
        return changedDate;
    }
    public void setChangedDate(Date changedDate) {
        this.changedDate = changedDate;
    }
    public String getIdName() {
		return idName;
	}
	public void setIdName(String idName) {
		this.idName = idName;
	}
	public String getIdValue() {
		return idValue;
	}
	public void setIdValue(String idValue) {
		this.idValue = idValue;
	}
	public String getTpid() {
		return tpid;
	}
	public void setTpid(String tpid) {
		this.tpid = tpid;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
}
