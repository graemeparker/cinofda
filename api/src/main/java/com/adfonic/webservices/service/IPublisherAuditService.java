package com.adfonic.webservices.service;

import com.adfonic.domain.PublisherAuditedCreative;

public interface IPublisherAuditService {

    PublisherAuditedCreative getAuditedCreativeAndPublisher(String creativeExternalId, String publisherExternalId);
    
    void recordAuditorImpression(PublisherAuditedCreative auditedCreative);
    
    void recordAuditorClick(PublisherAuditedCreative auditedCreative);
    
    String getRedirectUrl(PublisherAuditedCreative auditedCreative);
}
