package com.adfonic.webservices.service;

import java.util.List;

import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList.PublicationListLevel;


public interface IPublicationListService {

    public void deletePublicationList(long publicationListId);
    
    public PublicationList getPublicationListByName(String name, long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel);
    
    public PublicationList createPublicationList(String name, Long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel, List<String> publicationExternalIDs);
    
    public PublicationList updatePublicationList(long publicationListId, List<String> publicationExternalIDs);
    
}
