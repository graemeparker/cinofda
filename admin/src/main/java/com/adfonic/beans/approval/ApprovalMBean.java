package com.adfonic.beans.approval;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.beans.BaseBean;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.service.ApprovalService;
import com.adfonic.util.LogUtils;

public class ApprovalMBean extends BaseBean {

    private static final int REPLICATION_DELAY = 2000;
    public static final String TILDE = "~";
    private static final String NO_ASSIGNEDTO_ID = "0";
    private static final transient Logger LOG = Logger.getLogger(ApprovalMBean.class.getName());
    
    // Assigned To Users properties
    private Long bulkAssignedToUserId;
    private List<NameIdModel> assignedToUsers;

    @Autowired
    private ApprovalService approvalService;

    /** Assigned to users retrieved by proc */
    private List<NameIdModel> getAndStoreAssignedToUsers() {
        assignedToUsers = approvalService.searchForAssignedToUsers();
        return assignedToUsers;
    }
    
    protected String resolveMultiSelectLabel(String labelPrefix, int selectedListSize, int allListSize) {
        // All was selected
        if (selectedListSize == allListSize || selectedListSize == 0) {
            return FacesUtils.getBundleMessage(labelPrefix + ".all", String.valueOf(allListSize));
        // Some were selected
        } else {
            return FacesUtils.getBundleMessage(labelPrefix + ".some", String.valueOf(selectedListSize));
        }
    }
    
    /** View data needs to be refreshed after operation, but replication data still need some time to be updated properly */
    protected void refreshView() {
        // Wait seconds before call the specific refresh process
        LogUtils.logWithTitle(LOG, Level.INFO, "Refresh View", "Wait for 2 seconds due to replication");
        try {
            Thread.sleep(REPLICATION_DELAY);
        } catch (InterruptedException e) {
            // Do nothing
        }
        LogUtils.logWithTitle(LOG, Level.INFO, "Refresh View", "Page refresh after 2 seconds");
        refresh();
    }
    
    protected void refresh() {
        // Need to be overridden where refresh view is required
    }

    // Getters for the view

    public List<NameIdModel> getAssignedToUsers() {
        return (assignedToUsers == null) ? getAndStoreAssignedToUsers() : assignedToUsers;
    }
    
    public String getNoAssignedToId() {
        return NO_ASSIGNEDTO_ID;
    }

    // Getter / Setters

    public Long getBulkAssignedToUserId() {
        return bulkAssignedToUserId;
    }

    public void setBulkAssignedToUserId(Long bulkAssignedToUserId) {
        this.bulkAssignedToUserId = bulkAssignedToUserId;
    }
}
