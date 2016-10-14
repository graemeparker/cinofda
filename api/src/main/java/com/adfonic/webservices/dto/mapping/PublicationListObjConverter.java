package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.adfonic.webservices.service.IPublicationListService;

public class PublicationListObjConverter extends BaseReferenceEntityConverter<PublicationList> {

    private final Advertiser advertiser;
    private final IPublicationListService publicationListService;
    private final boolean isWhiteList;


    public PublicationListObjConverter(Advertiser advertiser, IPublicationListService publicationListService, boolean isWhiteList) {
        super(PublicationList.class, "name");
        this.advertiser = advertiser;
        this.publicationListService = publicationListService;
        this.isWhiteList = isWhiteList;
    }


    public PublicationListObjConverter() {
        this(null, null, false);
    }


    @Override
    public PublicationList resolveEntity(String name) {
        if (advertiser == null) {
            return null;
        }

        return publicationListService.getPublicationListByName(name, advertiser.getCompany().getId(), advertiser.getId(), isWhiteList, PublicationListLevel.ADVERTISER_LEVEL);
    }

}
