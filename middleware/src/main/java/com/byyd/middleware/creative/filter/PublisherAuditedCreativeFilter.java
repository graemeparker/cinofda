package com.byyd.middleware.creative.filter;

import java.util.Collection;
import java.util.HashSet;

import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;

public class PublisherAuditedCreativeFilter {
    
    private String sortingAlias;
    private Collection<Publisher> publishers;
    private Collection<PublisherAuditedCreative.Status> statuses;
    private Boolean excludeStatuses = false;
    private Boolean retrieveNullValuesAlso = false;
    
    public String getSortingAlias() {
        return sortingAlias;
    }
    public PublisherAuditedCreativeFilter setSortingAlias(String sortingAlias) {
        this.sortingAlias = sortingAlias;
        return this;
    }
    
    public Collection<Publisher> getPublishers() {
        return publishers;
    }
    public PublisherAuditedCreativeFilter setPublishers(Collection<Publisher> publishers) {
        this.publishers = publishers;
        return this;
    }
    
    public Collection<PublisherAuditedCreative.Status> getStatuses() {
        return statuses;
    }
    public PublisherAuditedCreativeFilter setStatuses(Collection<PublisherAuditedCreative.Status> statuses){
        this.statuses=statuses;
        return this;
    }
    public PublisherAuditedCreativeFilter addStatuses(PublisherAuditedCreative.Status... statuses){
        if (statuses.length>0){
            this.statuses = new HashSet<PublisherAuditedCreative.Status>(statuses.length);
            for(PublisherAuditedCreative.Status status : statuses){
                this.statuses.add(status);
            }
        }
        return this;
    }
    
    public Boolean getExcludeStatuses() {
        return excludeStatuses;
    }
    public PublisherAuditedCreativeFilter setExcludeStatuses(Boolean excludeStatuses) {
        this.excludeStatuses = excludeStatuses;
        return this;
    }
    
    public Boolean getRetrieveNullValuesAlso() {
        return retrieveNullValuesAlso;
    }
    public PublisherAuditedCreativeFilter setRetrieveNullValuesAlso(Boolean retrieveNullValuesAlso) {
        this.retrieveNullValuesAlso = retrieveNullValuesAlso;
        return this;
    }
}
