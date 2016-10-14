package com.byyd.middleware.campaign.filter;

import com.adfonic.domain.Advertiser;
import com.byyd.middleware.iface.dao.LikeSpec;

public class LocationTargetFilter {

    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;
    private Advertiser advertiser;

    public String getName() {
        return name;
    }
    
    public LocationTargetFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }
    
    public LocationTargetFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
        this.name = name;
        this.nameLikeSpec = nameLikeSpec;
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }
    
    public LikeSpec getNameLikeSpec() {
        return nameLikeSpec;
    }
    
    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    
    public Advertiser getAdvertiser() {
        return advertiser;
    }
    
    public LocationTargetFilter setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        return this;
    }

}
