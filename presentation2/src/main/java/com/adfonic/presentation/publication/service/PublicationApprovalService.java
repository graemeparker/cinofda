package com.adfonic.presentation.publication.service;

import java.util.List;

import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchResultModel;
import com.adfonic.presentation.publication.model.PublicationHistoryModel;

public interface PublicationApprovalService {

    // Publication Approval Dash board

    PublicationApprovalSearchResultModel searchForPublicationApprovals(PublicationApprovalSearchModel dto);

    List<NameIdModel> searchForPublicationTypes();

    List<NameIdModel> searchForPublicationAssignedToUsers(Long publicationId);

    List<NameIdModel> searchForPublicationAccountTypes();

    List<NameIdModel> searchForPublicationAlgorithmStatuses();

    List<NameIdModel> searchForPublicationDeadzoneStatuses();

    // Publication Approval Detail
    
    List<PublicationHistoryModel> searchForPublicationHistories(Long publicationId);

    PublicationApprovalDetailModel getPublicationApprovalDetail(Long publicationId);
    
    List<NameIdModel> getPublicationWatchers(Long publicationId);
    
    List<NameIdModel> getPublicationExcludedCategories(Long publicationId);
    
    String getPublicationAdOpsStatus(Long publicationId);
}
