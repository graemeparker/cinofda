package com.adfonic.dto.campaign.publicationlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.adfonic.dto.AbstractPaginationSearch;
import com.adfonic.dto.advertiser.AdvertiserDto;

public class PublicationSearchForListDto extends AbstractPaginationSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> excludedPublications = new ArrayList<Long>();

    private List<PublicationForListDto> publications = new ArrayList<PublicationForListDto>();

    private String searchName;

    private String searchType;

    private AdvertiserDto advertiser;

    public List<Long> getExcludedPublications() {
        return excludedPublications;
    }

    public void setExcludedPublications(List<Long> excludedPublications) {
        this.excludedPublications = excludedPublications;
    }

    public List<PublicationForListDto> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationForListDto> publications) {
        this.publications = publications;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

}
