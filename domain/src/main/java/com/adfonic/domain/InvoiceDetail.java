package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="INVOICE_DETAIL")
public class InvoiceDetail extends BusinessKey {

	private static final long serialVersionUID = 0x14756L;

	@EmbeddedId
	InvoiceKey primaryKey;
	
	@Column(name="COST")
	double cost;
	
	@Column(name="ADVERTISER_VAT")
	double advertiserVat;
	
	public long getId(){
		return primaryKey.getId();
	}

	public InvoiceKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(InvoiceKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double tcost) {
		cost = tcost;
	}

	public double getAdvertiserVat() {
		return advertiserVat;
	}

	public void setAdvertiserVat(double advertiserVat) {
		this.advertiserVat = advertiserVat;
	}
	
	public long getCampaignId(){
		return primaryKey.getCampaignId();
	}

	public String getActionType(){
		return primaryKey.getActionType();
	}
	
	public long getInvoiceHeaderId(){
		return primaryKey.getInvoiceHeaderId();
	}
	
	public int getGmtTimeId(){
		return primaryKey.getGmtTimeId();
	}
	
	public int getAdvertiserTimeId(){
		return primaryKey.getAdvertiserTimeId();
	}
}

