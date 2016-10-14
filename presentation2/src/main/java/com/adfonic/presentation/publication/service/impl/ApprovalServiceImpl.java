package com.adfonic.presentation.publication.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.dao.PublicationApprovalDao;
import com.adfonic.presentation.publication.service.ApprovalService;

@Service("approvalService")
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private PublicationApprovalDao publicationApprovalDao;

    // Approval Dash board

    @Override
    public List<NameIdModel> searchForAssignedToUsers() {
        return publicationApprovalDao.searchForPublicationAssignedToUsers(null);
    }

}
