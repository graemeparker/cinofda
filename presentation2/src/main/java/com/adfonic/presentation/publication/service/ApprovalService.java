package com.adfonic.presentation.publication.service;

import java.util.List;

import com.adfonic.presentation.NameIdModel;

public interface ApprovalService  {

    // Approval Dash board

    List<NameIdModel> searchForAssignedToUsers();
}
