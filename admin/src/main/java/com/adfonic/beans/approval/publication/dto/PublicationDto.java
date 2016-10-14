package com.adfonic.beans.approval.publication.dto;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationApprovalDashboardView;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class PublicationDto {
    private static final FetchStrategy FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Publication_.publisher)
        .addInner(Publisher_.company)
        .addLeft(Company_.accountManager)
        .addInner(Publication_.publicationType)
        .addLeft(Publication_.assignedTo)
        .build();

    public static FetchStrategy getFetchStrategy() {
        return FETCH_STRATEGY;
    }
    
    private long id;
    private String name;
    private String friendlyName;
    private Publication.Status status;
    private Publication.AdOpsStatus adOpsStatus;
    private String assignedTo;
    private String externalID;
    private boolean keyAccount;
    private String publisher;
    private String publicationType;
    private String fromAddress;
    private Date submissionTime;

    public PublicationDto(PublicationApprovalDashboardView publicationView) {
        this.id = publicationView.getId();
        this.name = publicationView.getName();
        this.friendlyName = publicationView.getFriendlyName();
        this.status = publicationView.getStatus();
        this.adOpsStatus = publicationView.getAdOpsStatus();
        this.assignedTo = publicationView.getAssignedToFirstName() == null ? null : publicationView.getAssignedToFullName();
        this.externalID = publicationView.getExternalID();
        
        this.keyAccount = publicationView.isPublisherIsKey();
        
        if (!StringUtils.isBlank(publicationView.getCompanyName())) {
            this.publisher = publicationView.getCompanyName();
        } else if (!StringUtils.isBlank(publicationView.getAccountManagerFullName())) {
            this.publisher = publicationView.getAccountManagerFullName();
        }

        this.publicationType = publicationView.getPublicationTypeName();
        
        this.fromAddress = publicationView.getAccountManagerEmail();

        if (publicationView.getSubmissionTime() != null) {
            this.submissionTime = publicationView.getSubmissionTime();
        } else {
            this.submissionTime = publicationView.getCreationTime();
        }
    }

    public PublicationDto(Publication publication) {
        this.id = publication.getId();
        this.name = publication.getName();
        this.friendlyName = publication.getFriendlyName();
        this.status = publication.getStatus();
        this.adOpsStatus = publication.getAdOpsStatus();
        this.assignedTo = publication.getAssignedTo() == null ? null : publication.getAssignedTo().getFullName();
        this.externalID = publication.getExternalID();
        
        Company company = publication.getPublisher().getCompany();
        User accountManager = company.getAccountManager();
        
        this.keyAccount = publication.getPublisher().isKey();
        
        if (!StringUtils.isBlank(company.getName())) {
            this.publisher = company.getName();
        } else if (accountManager != null) {
            this.publisher = accountManager.getFullName();
        }

        this.publicationType = publication.getPublicationType().getName();
        
        if (accountManager != null) {
            this.fromAddress = accountManager.getEmail();
        }

        if (publication.getSubmissionTime() != null) {
            this.submissionTime = publication.getSubmissionTime();
        } else {
            this.submissionTime = publication.getCreationTime();
        }
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Publication.Status getStatus() {
        return status;
    }
    public void setStatus(Publication.Status status) {
        this.status = status;
    }

    public Publication.AdOpsStatus getAdOpsStatus() {
        return adOpsStatus;
    }
    public void setAdOpsStatus(Publication.AdOpsStatus adOpsStatus) {
        this.adOpsStatus = adOpsStatus;
    }

    public String getAssignedTo() {
        return assignedTo;
    }
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getExternalID() {
        return externalID;
    }
    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public boolean isKeyAccount() {
        return keyAccount;
    }
    public void setKeyAccount(boolean keyAccount) {
        this.keyAccount = keyAccount;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationType() {
        return publicationType;
    }
    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getFromAddress() {
        return fromAddress;
    }
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }
    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }
}
