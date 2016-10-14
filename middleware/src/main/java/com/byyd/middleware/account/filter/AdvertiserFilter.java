package com.byyd.middleware.account.filter;

import java.util.Collection;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Company;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.LikeSpec;

public class AdvertiserFilter {
    private Collection<Long> advertiserIds;
    private String name;
    private boolean nameCaseSensitive;
    private String containsName;
    private LikeSpec containsNameLikeSpec;
    private boolean nameWithPreviousSpace;
    private Company company;
    private Collection<Advertiser.Status> statuses;
    private User user;

    public Collection<Long> getAdvertiserIds() {
        return advertiserIds;
    }
    public AdvertiserFilter setAdvertiserIds(Collection<Long> advertiserIds) {
        this.advertiserIds = advertiserIds;
        return this;
    }

    public String getName() {
        return name;
    }
    public AdvertiserFilter setName(String name, boolean caseSensitive) {
        this.name = name;
        this.nameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public AdvertiserFilter setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public Company getCompany() {
        return company;
    }
    public AdvertiserFilter setCompany(Company company) {
        this.company = company;
        return this;
    }

    public Collection<Advertiser.Status> getStatuses() {
        return statuses;
    }

    public AdvertiserFilter setStatuses(Collection<Advertiser.Status> statuses) {
        this.statuses = statuses;
        return this;
    }

    public User getUser() {
        return user;
    }

    public AdvertiserFilter setUser(User user) {
        this.user = user;
        return this;
    }
    public String getContainsName() {
        return containsName;
    }
    public AdvertiserFilter setContainsName(String containsName) {
        this.containsName = containsName;
        return this;
    }
    public boolean isNameWithPreviousSpace() {
        return nameWithPreviousSpace;
    }
    public AdvertiserFilter setNameWithPreviousSpace(boolean nameWithPreviousSpace) {
        this.nameWithPreviousSpace = nameWithPreviousSpace;
        return this;
    }
    public AdvertiserFilter setName(String name) {
        this.name = name;
        return this;
    }
    public LikeSpec getContainsNameLikeSpec() {
        return containsNameLikeSpec;
    }
    public void setContainsNameLikeSpec(LikeSpec containsNameLikeSpec) {
        this.containsNameLikeSpec = containsNameLikeSpec;
    }
    
    
}
