package com.adfonic.adserver.controller.dbg.dto;

import java.util.Map;

import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;

public class DbgPublisherDto {

    private PublisherDto publisher;

    private Map<Long, IntegrationTypeDto> integrations;

    public Map<Long, IntegrationTypeDto> getIntegrations() {
        return integrations;
    }

    public void setIntegrations(Map<Long, IntegrationTypeDto> integrations) {
        this.integrations = integrations;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

}
