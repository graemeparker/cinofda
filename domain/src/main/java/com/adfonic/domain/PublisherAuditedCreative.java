package com.adfonic.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "PUBLISHER_AUDITED_CREATIVE")
public class PublisherAuditedCreative extends BusinessKey {

    private static final long serialVersionUID = 1L;

    public enum Status {
        CREATION_INITIATED, SUBMIT_FAILED, PENDING, ACTIVE, REJECTED, LOCAL_INVALID, UNAUDITABLE, INTERNALLY_INELIGIBLE, MISC_UNMAPPED, BYPASS_ALLOW_CACHE_ONLY, BYPASS_ALLOW_AUDIT_ONLY, BYPASS_ALLOW_CACHE_AND_AUDIT
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLISHER_ID", updatable = false)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATIVE_ID", updatable = false)
    private Creative creative;

    @Column(name = "EXTERNAL_REFERENCE", length = 255, nullable = true)
    private String externalReference;

    @Column(name = "STATUS", length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "LAST_AUDIT_REMARKS", length = 1024, nullable = true)
    private String lastAuditRemarks;

    @Column(name = "CREATION_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "LATEST_FETCH_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestFetchTime;

    @Column(name = "MESSAGE_COUNT")
    private long messageCount;

    @Column(name = "LATEST_AUDITOR_IMPRESSION_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestImpressionTime;

    @Column(name = "IMPRESSION_COUNT")
    private long impressionCount;

    @Column(name = "LATEST_AUDITOR_CLICK_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestClickTime;

    @Column(name = "CLICK_COUNT")
    private long clickCount;

    public PublisherAuditedCreative() {

    }

    public PublisherAuditedCreative(Publisher publisher, Creative creative) {
        this.publisher = publisher;
        this.creative = creative;
        this.status = Status.CREATION_INITIATED;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLastAuditRemarks() {
        return lastAuditRemarks;
    }

    public void setLastAuditRemarks(String lastAuditRemarks) {
        this.lastAuditRemarks = lastAuditRemarks;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLatestFetchTime() {
        return latestFetchTime;
    }

    public void setLatestFetchTime(Date latestFetchTime) {
        this.latestFetchTime = latestFetchTime;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    @Override
    public long getId() {
        return id;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Creative getCreative() {
        return creative;
    }

    public Date getLatestImpressionTime() {
        return latestImpressionTime;
    }

    public void setLatestImpressionTime(Date latestImpressionTime) {
        this.latestImpressionTime = latestImpressionTime;
    }

    public long getImpressionCount() {
        return impressionCount;
    }

    public void setImpressionCount(long impressionCount) {
        this.impressionCount = impressionCount;
    }

    public Date getLatestClickTime() {
        return latestClickTime;
    }

    public void setLatestClickTime(Date latestClickTime) {
        this.latestClickTime = latestClickTime;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }

    @Override
    public String toString() {
        return "PublisherAuditedCreative {id=" + id + ", externalReference=" + externalReference + ", status=" + status + ", creationTime=" + creationTime + ", latestFetchTime="
                + latestFetchTime + "}";
    }

}
