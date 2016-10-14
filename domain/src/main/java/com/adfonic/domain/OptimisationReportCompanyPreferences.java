package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="OPTI_REPORT_COMPANY_PREFS")
public class OptimisationReportCompanyPreferences extends BusinessKey  {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="OPTI_REPORT_COMPANY_PREFS_FIELDS",joinColumns=@JoinColumn(name="OPTI_REPORT_COMPANY_PREFS_ID",referencedColumnName="ID"))
    @Column(name="FIELD_NAME",nullable=false)
    @Enumerated(EnumType.STRING)
    private Set<OptimisationReportFields> reportFields;
    
    {
    	reportFields = new HashSet<>();
    }
    
    public long getId() { return id; }

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Set<OptimisationReportFields> getReportFields() {
		return reportFields;
	}

	public void setReportFields(Set<OptimisationReportFields> reportFields) {
		this.reportFields = reportFields;
	}
}
