package com.adfonic.tasks.combined.truste.dto;

import java.util.Date;
import java.util.List;

import com.adfonic.tasks.combined.truste.TrusteDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class BatchPreference {
	
	private String company;
	private String appID;
	private boolean optinFlag;
	private Date changedDate;
	private Date createdDate;
	private String tpid;
	private List<AdditionalIds> additionalIds;
	
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public boolean isOptinFlag() {
		return optinFlag;
	}
	public void setOptinFlag(boolean optinFlag) {
		this.optinFlag = optinFlag;
	}
	public Date getChangedDate() {
		return changedDate;
	}
	@JsonDeserialize(contentUsing=TrusteDateDeserializer.class)
	public void setChangedDate(Date changedDate) {
		this.changedDate = changedDate;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	@JsonDeserialize(contentUsing=TrusteDateDeserializer.class)
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getTpid() {
		return tpid;
	}
	public void setTpid(String tpid) {
		this.tpid = tpid;
	}
	public List<AdditionalIds> getAdditionalIds() {
		return additionalIds;
	}
	public void setAdditionalIds(List<AdditionalIds> additionalIds) {
		this.additionalIds = additionalIds;
	}
}
