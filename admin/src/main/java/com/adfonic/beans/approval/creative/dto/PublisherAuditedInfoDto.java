package com.adfonic.beans.approval.creative.dto;


public class PublisherAuditedInfoDto {
    
    public enum StatusOption{
        NULL,
        PENDING,
        APPROVED,
        REJECTED,
        MANUAL
    }
    
    private String name;
    private Long publisherId;
    private Long creativeId;
    private StatusOption status;
    private String externalReference;
    private String lastAuditRemarks;
    
    public PublisherAuditedInfoDto(){
        super();
    }
    
    public PublisherAuditedInfoDto(String name, Long publisherId, Long creativeId, StatusOption status, String externalReference, String lastAuditRemarks) {
        super();
        this.name = name;
        this.publisherId = publisherId;
        this.creativeId = creativeId;
        this.status = status;
        this.externalReference = externalReference;
        this.lastAuditRemarks = lastAuditRemarks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public Long getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(Long creativeId) {
        this.creativeId = creativeId;
    }

    public StatusOption getStatus() {
        return status;
    }

    public void setStatus(StatusOption status) {
        this.status = status;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getLastAuditRemarks() {
        return lastAuditRemarks;
    }

    public void setLastAuditRemarks(String lastAuditRemarks) {
        this.lastAuditRemarks = lastAuditRemarks;
    }
}
