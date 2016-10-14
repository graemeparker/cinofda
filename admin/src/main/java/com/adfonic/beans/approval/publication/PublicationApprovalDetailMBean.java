package com.adfonic.beans.approval.publication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.beans.approval.ApprovalMBean;
import com.adfonic.dto.publication.enums.AdOpsStatus;
import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.category.service.CategoryTypeAheadSearchService;
import com.adfonic.presentation.category.sql.mappers.CategoriesNameIdRowMapper;
import com.adfonic.presentation.learnings.service.LearningAlgorithmService;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationAssignedToUserModel;
import com.adfonic.presentation.publication.model.PublicationHistoryModel;
import com.adfonic.presentation.publication.model.PublicationWatcherModel;
import com.adfonic.presentation.publication.service.PublicationApprovalService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.util.LogUtils;
import com.adfonic.util.PublicationEmailUtils;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
    @URLMapping(id = "approval-publication",
                pattern = "/admin/approval/publication/#{publicationApprovalDetailMBean.id}",
                viewId = "/admin/approval/publication.xhtml") })
public class PublicationApprovalDetailMBean extends ApprovalMBean {

    private static final transient Logger LOG = Logger.getLogger(PublicationApprovalDetailMBean.class.getName());
    
    // Approval properties
    private List<PublicationHistoryModel> publicationHistories;
    private List<NameIdModel> publicationWatchers;
    private String origAssignedToUserIdStr;
    
    /** Publication details to be shown on the UI */
    private PublicationApprovalDetailModel publicationDetails;
    
    // Services
    
    @Autowired
    private PublicationApprovalService publicationApprovalService;
    @Autowired
    private CategoryTypeAheadSearchService categoryService;
    @Autowired
    private PublicationService publicationService;
    @Autowired
    private PublicationEmailUtils publicationEmailUtils;
    @Autowired
    private LearningAlgorithmService learningAlgorithmService = AdfonicBeanDispatcher.getBean(LearningAlgorithmService.class);
    
    /** Publication id URL parameter */
    private Long publicationId;

    @URLAction(onPostback = false)
    public void init() {
        publicationDetails = (publicationId == null) ? null : publicationApprovalService.getPublicationApprovalDetail(publicationId);
        
        if (publicationDetails != null) {
            
            // Populate histories
            publicationHistories = publicationApprovalService.searchForPublicationHistories(publicationId);
            
            // Populate excluded categories
            List<NameIdModel> publicationExcludedCategories = publicationApprovalService.getPublicationExcludedCategories(publicationId);
            publicationDetails.setExcludedCategories(
                    (publicationExcludedCategories == null) ? Collections.<NameIdModel> emptyList() : publicationExcludedCategories);
            
            // Populate AdOpsStatus
            String adOpsStatus = publicationApprovalService.getPublicationAdOpsStatus(publicationId);
            publicationDetails.setAdOpsStatus((adOpsStatus == null) ? null : AdOpsStatus.valueOf(adOpsStatus));
            
            // Populate Assigned To
            List<NameIdModel> publicationAssignedToUsers = publicationApprovalService.searchForPublicationAssignedToUsers(publicationId);
            publicationDetails.setAssignedTo(
                    (CollectionUtils.isNotEmpty(publicationAssignedToUsers) && ((PublicationAssignedToUserModel)publicationAssignedToUsers.get(0)).getAssignedToCurrent()) ?
                            ((PublicationAssignedToUserModel)publicationAssignedToUsers.get(0)).getId().toString() : getNoAssignedToId());
            
            // Save this original assigned to user id
            origAssignedToUserIdStr = publicationDetails.getAssignedTo();
            
            // Populate watchers
            publicationWatchers = publicationApprovalService.getPublicationWatchers(publicationId);
            
            // Populate selected watchers
            Set<Long> selectedWatcherIds = new HashSet<Long>();
            
            // Add current adfonic user by default
            selectedWatcherIds.add(adfonicUser().getId());
            for (NameIdModel nameIdModel : publicationWatchers) {
                
                // The result set is ordered by is_watcher desc   
                if(((PublicationWatcherModel)nameIdModel).getIsWatcher()) {
                    selectedWatcherIds.add(nameIdModel.getId());
                } else {
                    break;
                }
            }
            publicationDetails.setWatchers(new ArrayList<Long>(selectedWatcherIds));
            
            LogUtils.logWithTitle(LOG, Level.FINE, "Publication Approval Detail (Populated)", publicationDetails);
        }
    }
    
    /** Category search after each user input */
    public List<NameIdModel> searchCategory(String categoryNamePrefix) {         
        return categoryService.searchForCategories(categoryNamePrefix);
    }
    
    /** Handle save publication details */
    public void savePublicationDetail() {
        normalizeFields();
        
        LogUtils.logWithTitle(LOG, Level.INFO,
                "Save Publication Details", "Publication id=" + publicationDetails.getInternalId() + " is being updated by " + adfonicUser().getFullName());
        LogUtils.logWithTitle(LOG, Level.FINE, "Publication Approval Detail (New)", publicationDetails);
        
        boolean assignedToUserChanged = (origAssignedToUserIdStr.equals(publicationDetails.getAssignedTo())) ? false : true;
        boolean commentChanged = (StringUtils.isNotBlank(publicationDetails.getComment())) ? true : false;
        boolean statusOrAdOpsStatusChanged = publicationService.savePublicationApprovalDetails(publicationDetails, adfonicUser(), assignedToUserChanged, commentChanged);
        
        // Send email to watchers 
        if (publicationDetails.getNotifyWatchers() && (statusOrAdOpsStatusChanged || assignedToUserChanged || commentChanged)) {
            sendEmailToWatchers();
        }
    
        // Send email to publisher only if the check box was selected, and
        // then only if the status changed or a comment was entered.
        if (publicationDetails.getNotifyPublisher() && (statusOrAdOpsStatusChanged || commentChanged)) {
            sendCommentEmailToPublisher();
        }
        
        refreshView();
    }
    
    
    /** Learning algorithm action listeners */
    public void includePublicationFromAlgorithm(){
        if (publicationId!=null){
            this.learningAlgorithmService.includePublicationToLearningAlgorithm(publicationId, adfonicUser().getId());
            refreshView();
        }
    }
    public void excludePublicationFromAlgorithm(){
        if (publicationId!=null){
            this.learningAlgorithmService.excludePublicationFromLearningAlgorithm(publicationId, adfonicUser().getId());
            refreshView();
        }
    }
    public void resetPublicationLearnings(){
        if (publicationId!=null){
            this.learningAlgorithmService.removePublicationLearnings(publicationId, adfonicUser().getId());
        }
    }

    /** Refresh some view part after saving publication*/
    @Override
    protected void refresh() {
        publicationDetails = publicationApprovalService.getPublicationApprovalDetail(publicationId);

        // Populate histories
        publicationHistories = publicationApprovalService.searchForPublicationHistories(publicationId);
    }
    
    /** Normalize empty properties to nulls for proc */
    private void normalizeFields() {
        publicationDetails.setSamplingRate(emptyToNull(publicationDetails.getSamplingRate()));
    }

    /** Send a comment email to the publisher */
    private void sendCommentEmailToPublisher() {
        LogUtils.logWithTitle(LOG, Level.INFO, "Sending Comment Email",
           "Sending comment email for Publication internal id=" + publicationDetails.getInternalId() + ", comment=" + publicationDetails.getComment());
        try {
            publicationEmailUtils.sendPublicationCommentEmail(Long.valueOf(publicationDetails.getInternalId()), publicationDetails.getComment(), FacesContext.getCurrentInstance());
        } catch (Exception e) {
            LogUtils.logWithTitle(LOG, Level.SEVERE, "Sending Comment Email Failed",
               "Failed to send comment email for Publication internal id=" + publicationDetails.getInternalId() + ", comment=" + publicationDetails.getComment(), e);
        }
    }

    /** Send an email to all of the publication's watchers */
    private void sendEmailToWatchers() {
        LogUtils.logWithTitle(LOG, Level.INFO, "Sending Update Emails",
                "Sending update email to watchers for Publication internal id=" + publicationDetails.getInternalId());
        try {
            publicationEmailUtils.sendUpdateEmailToWatchers(Long.valueOf(publicationDetails.getInternalId()), FacesContext.getCurrentInstance());
        } catch (Exception ex) {
            LogUtils.logWithTitle(LOG, Level.SEVERE, "Sending Update Emails Failed",
                 "Failed to send update email to watchers for Publication internal id=" + publicationDetails.getInternalId(), ex);
        }
    }
    
    
    // Getters for the view
    
    public PublicationApprovalDetailModel getPublicationDetails() {
        return publicationDetails;
    }
    
    public List<PublicationHistoryModel> getPublicationHistories() {
        return publicationHistories;
    }
    
    public List<NameIdModel> getPublicationWatchers() {
        return publicationWatchers;
    }
    
    /** The last section in the hierarchy is the category itself */
    public String getOnlyCategoryName(String hierarchicalName) {
        String[] categories = hierarchicalName.split(CategoriesNameIdRowMapper.CATEGORY_SEPARATOR);
        return categories[categories.length - 1];
    }
    
    public Long getPublicationId() {
        return publicationId;
    }
    
    public String getPublicationMultiSelectWatcherLabel() {
        return resolveMultiSelectLabel("page.approval.publication.approval.watchers.label", 
                                        (publicationDetails.getWatchers()==null? 0 : publicationDetails.getWatchers().size()), 
                                        publicationWatchers.size());
    }
    
    // Setters / Getters
    
    public String getId() {
        return String.valueOf(publicationId);
    }

    public void setId(String idParam) {
        publicationId = null;
        try {
            publicationId = Long.valueOf(idParam);
        } catch (NumberFormatException nfe) {
            // do nothing
        }
    }
}
