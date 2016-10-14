package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AudiencePrices {
	@Column(name="DATA_RETAIL",nullable=false)
    private BigDecimal dataRetail;
    @Column(name="DATA_WHOLESALE",nullable=false)
    private BigDecimal dataWholesale;
    
    public AudiencePrices(){
    	
    }
    
    public AudiencePrices(BigDecimal dataRetail, BigDecimal dataWholesale){
    	this.dataRetail = dataRetail;
    	this.dataWholesale = dataWholesale;
    }
    
	public BigDecimal getDataRetail() {
		return dataRetail;
	}
	public void setDataRetail(BigDecimal dataRetail) {
		this.dataRetail = dataRetail;
	}
	public BigDecimal getDataWholesale() {
		return dataWholesale;
	}
	public void setDataWholesale(BigDecimal dataWholesale) {
		this.dataWholesale = dataWholesale;
	}
}
