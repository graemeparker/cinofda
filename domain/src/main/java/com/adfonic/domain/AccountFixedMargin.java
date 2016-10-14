package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="ACCOUNT_FIXED_MARGIN")
public class AccountFixedMargin extends BusinessKey {
    private static final long serialVersionUID = 1L;
    
    public static final double DEFAULT_ACCOUNT_FIXED_MARGIN = 0.05;

    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=true)
    private Company company;
    
    @Column(name="MARGIN",nullable=false)
    private BigDecimal margin;
    
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    {
        this.startDate = new Date();
    }

    AccountFixedMargin() {}
    
    AccountFixedMargin(Company company, BigDecimal margin) {
    	this.company = company;
    	this.margin = margin;
    }

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public BigDecimal getMargin() {
		return margin;
	}

	public void setMargin(BigDecimal margin) {
		this.margin = margin;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public long getId() {
		return id;
	}
    
}