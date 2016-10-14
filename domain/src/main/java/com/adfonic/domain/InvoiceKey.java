package com.adfonic.domain;

import java.io.Serializable;
import javax.persistence.*;

// This is the composite primary key for the invoice detail domain


@Embeddable
public class InvoiceKey implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name="GMT_TIME_ID")
	private int gmtTimeId;
	@Column(name="ADVERTISER_TIME_ID")
	private int advertiserTimeId;
	@Column(name="CAMPAIGN_ID")
	private long campaignId;
	@Column(name="ACTION_TYPE")
	private String actionType;
	@Column(name="INVOICE_HEADER_ID")
	private long invoiceHeaderId;
	public int getGmtTimeId() {
		return gmtTimeId;
	}
	public void setGmtTimeId(int gmtTimeId) {
		this.gmtTimeId = gmtTimeId;
	}
	public int getAdvertiserTimeId() {
		return advertiserTimeId;
	}
	public void setAdvertiserTimeId(int advertiserTimeId) {
		this.advertiserTimeId = advertiserTimeId;
	}
	public long getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(long campaignId) {
		this.campaignId = campaignId;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public long getInvoiceHeaderId() {
		return invoiceHeaderId;
	}
	public void setInvoiceHeaderId(long invoiceHeaderId) {
		this.invoiceHeaderId = invoiceHeaderId;
	}
	
	public long getId(){
		return gmtTimeId*37+advertiserTimeId*51+campaignId*5+actionType.hashCode()+invoiceHeaderId*3;
	}
	
	public int hashCode(){
		return gmtTimeId*37+advertiserTimeId*51+((int) campaignId)*5+actionType.hashCode()+((int) invoiceHeaderId)*3;
	}
	
	public boolean equals(Object o){
		if (!(o instanceof InvoiceKey)) return false;
		InvoiceKey ik = (InvoiceKey) o;
		if (ik.gmtTimeId!=gmtTimeId) return false;
		if (ik.advertiserTimeId!=advertiserTimeId) return false;
		if (ik.campaignId!=campaignId) return false;
		if (ik.actionType!=actionType) return false;
		return ik.invoiceHeaderId==invoiceHeaderId;	
	}
	
}
