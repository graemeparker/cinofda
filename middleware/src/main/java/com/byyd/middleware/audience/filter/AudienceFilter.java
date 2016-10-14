package com.byyd.middleware.audience.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Audience;
import com.byyd.middleware.iface.dao.LikeSpec;

public class AudienceFilter {

    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;
    private Advertiser advertiser;
    private List<Audience.Status> statusesNotIncluded;
    private List<Audience.Status> statusesIncluded;
    
    public AudienceFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }
    
    public AudienceFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
        this.name = name;
        this.nameLikeSpec = nameLikeSpec;
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public AudienceFilter setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        return this;
    }

    public String getName() {
        return name;
    }

    public LikeSpec getNameLikeSpec() {
        return nameLikeSpec;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }

    public List<Audience.Status> getStatusesNotIncluded() {
        return statusesNotIncluded;
    }

    public void setStatusesNotIncluded(List<Audience.Status> statusesNotIncluded) {
        this.statusesNotIncluded = statusesNotIncluded;
    }
    
    public void setStatusesNotIncluded(Audience.Status... statusesNotIncluded) {
        this.statusesNotIncluded = new ArrayList<Audience.Status>(Arrays.asList(statusesNotIncluded));
    }

    public List<Audience.Status> getStatusesIncluded() {
        return statusesIncluded;
    }

    public void setStatusesIncluded(List<Audience.Status> statusesIncluded) {
        this.statusesIncluded = statusesIncluded;
    }
    
    public void setStatusesIncluded(Audience.Status... statusesIncluded) {
        this.statusesIncluded = new ArrayList<Audience.Status>(Arrays.asList(statusesIncluded));
    }
}
