package com.adfonic.webservices.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Company;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.adfonic.domain.PublicationList_;
import com.adfonic.domain.Publication_;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.service.IPublicationListService;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.PublicationListFilter;
import com.byyd.middleware.publication.service.PublicationManager;

@Service
public class PublicationListService implements IPublicationListService{

    @Autowired
    private PublicationManager publicationManager;

    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;

    private FetchStrategy publicationListFs = new FetchStrategyBuilder()
    .addLeft(PublicationList_.publications)
    .addLeft(Publication_.externalID)
    .build();

    @Override
    public void deletePublicationList(long publicationListId) {
        PublicationList publicationList = publicationManager.getPublicationListById(publicationListId);
        publicationManager.delete(publicationList);
    }

    // Every service method which doesn't take key params go through this. Those with key's are also expected to have gotten the key through this
    /// Also no differences in role applicability accross CRUD. Therefore authorization is performed here
    //// DSP read-only is not considered for any of these ops.. they can see the list name at the best
    @Override
    public PublicationList getPublicationListByName(String name, long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel) {

        Company company=companyManager.getCompanyById(companyId);
        // allow all t d n h
        //authorizeCompany(company, isWhiteList);
        
        PublicationListFilter filter = new PublicationListFilter();
        filter.setCompany(company);
        if (advertiserId != null) {
            filter.setAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        }
        filter.setWhiteList(isWhiteList);
        filter.setPublicationListLevel(pubListLevel);
        filter.setName(name, true);
        
        List<PublicationList> pubLists = publicationManager.getAllPublicationLists(filter, publicationListFs);
        return pubLists.isEmpty() ? null : pubLists.get(0);
    }

    @Override
    public PublicationList createPublicationList(String name, Long companyId, Long advertiserId, boolean isWhiteList, PublicationListLevel pubListLevel, List<String> publicationExternalIDs) {
        if(getPublicationListByName(name, companyId, advertiserId, isWhiteList, pubListLevel)!=null){
            throw new ServiceException(ErrorCode.ENTITY_ALREADY_EXISTS, "Already exists!");
        }
        
        PublicationList pl = new PublicationList();
        if(companyId!=null){
            pl.setCompany(companyManager.getCompanyById(companyId));
        }
        if(advertiserId!=null){
            pl.setAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        }
        pl.setPublicationListLevel(pubListLevel);
        pl.setName(name);
        pl.setWhiteList(isWhiteList);
        
        return publicationManager.create(updatePublicationList(pl, publicationExternalIDs));
    }

    @Override
    public PublicationList updatePublicationList(long publicationListId, List<String> publicationExternalIDs) {
        PublicationList publicationList=publicationManager.getPublicationListById(publicationListId);
        return updatePublicationList(publicationList, publicationExternalIDs);
    }
    
    private PublicationList updatePublicationList(PublicationList publicationList, List<String> publicationExternalIDs){
        Set<Publication> publications=new HashSet<>(publicationManager.getPublicationByExternalIds(publicationExternalIDs, null, null, publicationListFs));
        if(existsNonActiveMemberIn(publications)){
            throw new ServiceException(ErrorCode.VALIDATION_ERROR, "Can add only publications active at the time");
        }
        
        publicationList.setPublications(publications);
        publicationList.setSnapshotDateTime(new Date());
        return publicationList;
    }
    

    private boolean existsNonActiveMemberIn(Set<Publication> publications) {
        for (Publication publication : publications) {
            if (publication.getStatus() != Publication.Status.ACTIVE) {
                return true;
            }
        }
        return false;
    }

}
