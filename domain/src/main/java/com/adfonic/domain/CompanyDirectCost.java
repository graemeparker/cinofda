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
@Table(name = "COMPANY_DIRECT_COST")
public class CompanyDirectCost extends BusinessKey {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPANY_ID", nullable = true)
	private Company company;
	@Column(name = "DIRECT_COST", nullable = false)
	private BigDecimal directCost;
	@Column(name = "START_DATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;
	@Column(name = "END_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	{
		this.startDate = new Date();
	}

	public CompanyDirectCost() {
	}

	public CompanyDirectCost(Company company, BigDecimal directCost) {
		this.company = company;
		this.directCost = (directCost != null) ? directCost : BigDecimal.ZERO;
	}

	public long getId() {
		return id;
	};

	public Company getCompany() {
		return company;
	}

	public BigDecimal getDirectCost() {
		return directCost;
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

}
