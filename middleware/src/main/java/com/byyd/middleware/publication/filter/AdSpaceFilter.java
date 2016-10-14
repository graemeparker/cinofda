package com.byyd.middleware.publication.filter;

import java.util.Collection;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;

public class AdSpaceFilter {
    private String name;
    private boolean nameCaseSensitive;
    private Publication publication;
    private Collection<AdSpace.Status> statuses;
    private Collection<Long> excludedIds;

    public String getName() {
        return name;
    }
    public AdSpaceFilter setName(String name, boolean caseSensitive) {
        this.name = name;
        this.nameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public AdSpaceFilter setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public Publication getPublication() {
        return publication;
    }
    public AdSpaceFilter setPublication(Publication publication) {
        this.publication = publication;
        return this;
    }

    public Collection<AdSpace.Status> getStatuses() {
        return statuses;
    }
    public AdSpaceFilter setStatuses(Collection<AdSpace.Status> statuses) {
        this.statuses = statuses;
        return this;
    }

    public Collection<Long> getExcludedIds() {
        return excludedIds;
    }
    public AdSpaceFilter setExcludedIds(Collection<Long> excludedIds) {
        this.excludedIds = excludedIds;
        return this;
    }
}
