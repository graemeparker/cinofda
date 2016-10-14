package com.adfonic.adserver.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class SimpleTargetingContext implements TargetingContext {

    private Map<String, Object> store = new HashMap<String, Object>();
    private DomainCache domainCache;

    @Override
    public void setExchangePublisherId(Long id) {
        store.put("ExchangePublisherId", id);
    }

    @Override
    public Long getExchangePublisherId() {
        return (Long) store.get("ExchangePublisherId");
    }

    @Override
    public void setEffectivePublisher(PublisherDto publisher) {
        store.put("EffectivePublisher", publisher);
    }

    @Override
    public PublisherDto getEffectivePublisher() {
        return (PublisherDto) store.get("EffectivePublisher");
    }

    @Override
    public AdSpaceDto getAdSpace() {
        return (AdSpaceDto) store.get(AdSpaceDto.class.getName());
    }

    @Override
    public void setAdSpace(AdSpaceDto adSpace) {
        store.put(AdSpaceDto.class.getName(), adSpace);
    }

    @Override
    public DomainCache getDomainCache() {
        return domainCache;
    }

    public void setDomainCache(DomainCache domainCache) {
        this.domainCache = domainCache;
    }

    @Override
    public AdserverDomainCache getAdserverDomainCache() {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return store;
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> clazz) {
        return (T) store.get(attribute);
    }

    @Override
    public <T> T getAttribute(String attribute) {
        return (T) store.get(attribute);
    }

    @Override
    public void setAttribute(String attribute, Object value) {
        store.put(attribute, value);
    }

    @Override
    public boolean isSslRequired() {
        return (boolean) store.get("sslRequired");
    }

    @Override
    public void setSslRequired(boolean sslRequired) {
        store.put("sslRequired", sslRequired);
    }

    @Override
    public String getHeader(String header) {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public Map<String, String> getHeaders() {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public String getCookie(String name) {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public void setIpAddress(String ip) throws InvalidIpAddressException {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public void setUserAgent(String userAgent) {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public String getEffectiveUserAgent() {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public void populateImpression(Impression impression, SelectedCreative selectedCreative) {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public void populateAdEvent(AdEvent event, Impression impression, CreativeDto creative) {
        throw new UnsupportedOperationException("No no no");
    }

    @Override
    public boolean isFlagTrue(String attribute) {
        Object value = getAttribute(attribute);
        if (value != null && value == Boolean.TRUE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setFlagTrue(String attribute) {
        setAttribute(attribute, Boolean.TRUE);
    }

    @Override
    public void setFlagFalse(String attribute) {
        setAttribute(attribute, Boolean.FALSE);
    }
}