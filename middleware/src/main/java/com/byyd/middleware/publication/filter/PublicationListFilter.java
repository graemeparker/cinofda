package com.byyd.middleware.publication.filter;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Company;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.byyd.middleware.iface.dao.LikeSpec;

public class PublicationListFilter {
    
    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;
    private Company company;
    private Advertiser advertiser;
    private Boolean whiteList;
    private PublicationListLevel publicationListLevel;
    
    public PublicationListFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }
    
    public PublicationListFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
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
    public String getName() {
        return name;
    }
    
    public Company getCompany() {
        return company;
    }
    public PublicationListFilter setCompany(Company company) {
        this.company = company;
        return this;
    }
    public Advertiser getAdvertiser() {
        return advertiser;
    }
    public PublicationListFilter setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        return this;
    }

    public Boolean getWhiteList() {
        return whiteList;
    }

    public PublicationListFilter setWhiteList(Boolean whiteList) {
        this.whiteList = whiteList;
        return this;
    }

    public PublicationListLevel getPublicationListLevel() {
        return publicationListLevel;
    }

    public PublicationListFilter setPublicationListLevel(PublicationListLevel publicationListLevel) {
        this.publicationListLevel = publicationListLevel;
        return this;
    }
    
    

}
