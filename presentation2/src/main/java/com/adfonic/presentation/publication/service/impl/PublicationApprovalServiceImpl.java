package com.adfonic.presentation.publication.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.dao.PublicationApprovalDao;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchResultModel;
import com.adfonic.presentation.publication.model.PublicationHistoryModel;
import com.adfonic.presentation.publication.service.PublicationApprovalService;

@Service("publicationApprovalService")
public class PublicationApprovalServiceImpl implements PublicationApprovalService {

    @Autowired
    private PublicationApprovalDao publicationApprovalDao;

    // Publication Approval Dash board
    
    @Override
    public PublicationApprovalSearchResultModel searchForPublicationApprovals(PublicationApprovalSearchModel dto) {
        return publicationApprovalDao.searchForPublicationApprovals(dto);
    }

    @Override
    public List<NameIdModel> searchForPublicationTypes() {
        return publicationApprovalDao.searchForPublicationTypes();
    }

    @Override
    public List<NameIdModel> searchForPublicationAssignedToUsers(Long publicationId) {
        return publicationApprovalDao.searchForPublicationAssignedToUsers(publicationId);
    }

    @Override
    public List<NameIdModel> searchForPublicationAccountTypes() {
        return publicationApprovalDao.searchForPublicationAccountTypes();
    }

    @Override
    public List<NameIdModel> searchForPublicationAlgorithmStatuses() {
        return publicationApprovalDao.searchForPublicationAlgorithmStatuses();
    }

    @Override
    public List<NameIdModel> searchForPublicationDeadzoneStatuses() {
        return publicationApprovalDao.searchForPublicationDeadzoneStatuses();
    }

    // Publication Approval Detail
    
    @Override
    public List<PublicationHistoryModel> searchForPublicationHistories(Long publicationId) {
        return publicationApprovalDao.searchForPublicationHistories(publicationId); // publication_id should be null for getting all publication statuses
    }
    
    @Override
    public PublicationApprovalDetailModel getPublicationApprovalDetail(Long publicationId) {
        return publicationApprovalDao.getPublicationApprovalDetail(publicationId);
    }

    @Override
    public List<NameIdModel> getPublicationWatchers(Long publicationId) {
        return publicationApprovalDao.getPublicationWatchers(publicationId);
    }
    
    @Override
    public List<NameIdModel> getPublicationExcludedCategories(Long publicationId) {
        return publicationApprovalDao.getPublicationExcludedCategories(publicationId);
    }

    @Override
    public String getPublicationAdOpsStatus(Long publicationId) {
        return publicationApprovalDao.getPublicationAdOpsStatus(publicationId);
    }

}
