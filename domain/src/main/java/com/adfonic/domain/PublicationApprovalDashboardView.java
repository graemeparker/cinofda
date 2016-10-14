package com.adfonic.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.BusinessKey;
import com.adfonic.domain.HasExternalID;
import com.adfonic.domain.Named;
import com.adfonic.domain.Publication;

@Entity
@Table(name="vw_PUBLICATION_APPROVAL_DASHBOARD")
public class PublicationApprovalDashboardView extends BusinessKey implements Named, HasExternalID {

    private static final long serialVersionUID = 1L;

    @Id @Column(name="ID")
    private long id;
    
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    
    @Column(name="FRIENDLY_NAME",length=255,nullable=true)
    private String friendlyName;
    
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Publication.Status status;
    
    @Column(name="AD_OPS_STATUS",length=32,nullable=true)
    @Enumerated(EnumType.STRING)
    private Publication.AdOpsStatus adOpsStatus;
    
    @Column(name="ASSIGNED_TO_FIRST_NAME",length=80,nullable=true)
    private String assignedToFirstName;

    @Column(name="ASSIGNED_TO_LAST_NAME",length=80,nullable=true)
    private String assignedToLastName;

    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    
    @Column(name="PUBLISHER_IS_KEY",nullable=false)
    private boolean publisherIsKey;
    
    @Column(name="COMPANY_NAME",length=255,nullable=false)
    private String companyName;
    
    @Column(name="ACCOUNT_MANAGER_FIRST_NAME",length=80,nullable=true)
    private String accountManagerFirstName;
    
    @Column(name="ACCOUNT_MANAGER_LAST_NAME",length=80,nullable=true)
    private String accountManagerLastName;
   
    @Column(name="ACCOUNT_MANAGER_EMAIL",length=80,nullable=true)
    private String accountManagerEmail;
   
    @Column(name="PUBLICATION_TYPE_NAME",length=255,nullable=false)
    private String publicationTypeName;
    
    @Column(name="SUBMISSION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionTime;
    
    @Column(name="CREATION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    
	public long getId() {
		return id;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public String getName() {
		return name;
	}


	public String getFriendlyName() {
		return friendlyName;
	}


	public Publication.Status getStatus() {
		return status;
	}


	public Publication.AdOpsStatus getAdOpsStatus() {
		return adOpsStatus;
	}


	public String getAssignedToFirstName() {
		return assignedToFirstName;
	}


	public String getAssignedToLastName() {
		return assignedToLastName;
	}
	
	public String getAssignedToFullName() {
        StringBuilder bld = new StringBuilder();
        if (StringUtils.isNotBlank(assignedToFirstName)) {
            bld.append(assignedToFirstName);
        }
        if (StringUtils.isNotBlank(assignedToLastName)) {
            if (bld.length() > 0) {
                bld.append(' ');
            }
            bld.append(assignedToLastName);
        }
        return bld.toString();
	}


	public String getExternalID() {
		return externalID;
	}


	public boolean isPublisherIsKey() {
		return publisherIsKey;
	}


	public String getCompanyName() {
		return companyName;
	}


	public String getAccountManagerFirstName() {
		return accountManagerFirstName;
	}


	public String getAccountManagerLastName() {
		return accountManagerLastName;
	}
	
	public String getAccountManagerFullName() {
        StringBuilder bld = new StringBuilder();
        if (StringUtils.isNotBlank(accountManagerFirstName)) {
            bld.append(accountManagerFirstName);
        }
        if (StringUtils.isNotBlank(accountManagerLastName)) {
            if (bld.length() > 0) {
                bld.append(' ');
            }
            bld.append(accountManagerLastName);
        }
        return bld.toString();
	}


	public String getAccountManagerEmail() {
		return accountManagerEmail;
	}


	public String getPublicationTypeName() {
		return publicationTypeName;
	}


	public Date getSubmissionTime() {
		return submissionTime;
	}


	public Date getCreationTime() {
		return creationTime;
	}


}
