package com.adfonic.beans.approval.creative.dto;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.PublisherAuditedCreative_;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class CreativeDto {
    public static final FetchStrategy FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Creative_.campaign)
        .addInner(Campaign_.advertiser)
        .addInner(Creative_.segment)
        .addInner(Advertiser_.company)
        .addLeft(Company_.accountManager)
        .addLeft(Company_.currentMediaCostMargin)
        .addLeft(Creative_.assignedTo)
        .addLeft(Creative_.destination)
        .build();
    
    public static final FetchStrategy PUBLISHER_AUDITED_CREATIVE_FS = new FetchStrategyBuilder().addInner(PublisherAuditedCreative_.publisher).build();
    
    private long id;
    private String name;
    private String campaignName;
    private String campaignAdvertiserDomain;
    private Creative.Status status;
    private String assignedTo;
    private String externalID;
    private boolean keyAccount;
    private String country;
    private String destination;
    private String advertiser;
    private String fromAddress;
    private Date submissionTime;
    
    private PublisherAuditedInfoDto adxPublisherAuditedInfo;
    private PublisherAuditedInfoDto apnPublisherAuditedInfo;

    public CreativeDto(Creative creative) {
        this.id = creative.getId();
        this.name = creative.getName();
        this.campaignName = creative.getCampaign().getName();
        this.campaignAdvertiserDomain = creative.getCampaign().getAdvertiserDomain();
        this.status = creative.getStatus();
        this.assignedTo = creative.getAssignedTo() == null ? null : creative.getAssignedTo().getFullName();
        this.externalID = creative.getExternalID();
        
        Company company = creative.getCampaign().getAdvertiser().getCompany();
        User accountManager = company.getAccountManager();
        
        this.keyAccount = creative.getCampaign().getAdvertiser().isKey();
        
        if (creative.getSegment().isGeographyTargeted()) {
            this.country = creative.getSegment().getCountriesAsString();
            // Add a space after each comma, otherwise creatives having lots of
            // countries can force the column to go ridiculously wide since it
            // can't wrap without whitespace.
            this.country = this.country.replaceAll(",", ", ");
        } else {
            this.country = "Global";
        }
        
        if (creative.getDestination() != null) {
            this.destination = creative.getDestination().getData();
        }

        if (!StringUtils.isBlank(company.getName())) {
            this.advertiser = company.getName();
        } else if (accountManager != null) {
            this.advertiser = accountManager.getFullName();
        }
        
        if (accountManager != null) {
            this.fromAddress = accountManager.getEmail();
        }

        if (creative.getSubmissionTime() != null) {
            this.submissionTime = creative.getSubmissionTime();
        } else {
            this.submissionTime = creative.getCreationTime();
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

    public String getCampaignName() {
        return campaignName;
    }
    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCampaignAdvertiserDomain() {
        return campaignAdvertiserDomain;
    }

    public void setCampaignAdvertiserDomain(String campaignAdvertiserDomain) {
        this.campaignAdvertiserDomain = campaignAdvertiserDomain;
    }

    public Creative.Status getStatus() {
        return status;
    }
    public void setStatus(Creative.Status status) {
        this.status = status;
    }

    public PublisherAuditedInfoDto getAdxPublisherAuditedInfo() {
        return adxPublisherAuditedInfo;
    }

    public void setAdxPublisherAuditedInfo(PublisherAuditedInfoDto adxPublisherAuditedInfo) {
        this.adxPublisherAuditedInfo = adxPublisherAuditedInfo;
    }

    public PublisherAuditedInfoDto getApnPublisherAuditedInfo() {
        return apnPublisherAuditedInfo;
    }

    public void setApnPublisherAuditedInfo(PublisherAuditedInfoDto apnPublisherAuditedInfo) {
        this.apnPublisherAuditedInfo = apnPublisherAuditedInfo;
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

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAdvertiser() {
        return advertiser;
    }
    public void setAdvertiser(String advertiser) {
        this.advertiser = advertiser;
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
