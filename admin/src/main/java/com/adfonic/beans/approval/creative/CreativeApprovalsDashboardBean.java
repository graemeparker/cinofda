package com.adfonic.beans.approval.creative;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.ObjectUtils;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.beans.AuditedPublishersBean;
import com.adfonic.beans.approval.AbstractApprovalDashboardMBean;
import com.adfonic.beans.approval.creative.dto.CreativeDto;
import com.adfonic.beans.approval.creative.dto.PublisherAuditedInfoDto;
import com.adfonic.beans.datamodel.LazyCreativeDataModel;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "approval-creatives", pattern = "/admin/approval/creatives", viewId = "/admin/approval/creatives.jsf") })
public class CreativeApprovalsDashboardBean extends AbstractApprovalDashboardMBean<CreativeDto> {
    private static final transient Logger LOG = Logger.getLogger(CreativeApprovalsDashboardBean.class.getName());

    private static final SelectItem[] ACCOUNT_TYPE_OPTIONS = createFilterOptions(new String[] { "Key", "Standard" });
    private static final SelectItem[] STATUS_OPTIONS = createFilterOptions(Creative.Status.values());
    private static final SelectItem[] ADX_STATUS_OPTIONS = createFilterOptions(PublisherAuditedInfoDto.StatusOption.values());
    private static final SelectItem[] APN_STATUS_OPTIONS = createFilterOptions(PublisherAuditedInfoDto.StatusOption.values());
    
    private static final FetchStrategy CREATIVE_FS = new FetchStrategyBuilder()
        .addLeft(Creative_.assignedTo)
        .addLeft(Creative_.campaign)
        .build();
    
    private static final FetchStrategy CAMPAIGN_WITH_WATCHERS_FS = new FetchStrategyBuilder()
        .addLeft(Campaign_.watchers)
        .build();
    
    @Autowired
    private AuditedPublishersBean auditedPublishersBean;
    
    private LazyDataModel<CreativeDto> lazyModel;
    
    @PostConstruct
    public void init() {
        lazyModel = createLazyModel();
    }
    
    public final SelectItem[] getAccountTypeOptions() {
        return ACCOUNT_TYPE_OPTIONS;
    }
    
    public SelectItem[] getStatusOptions() {
        return STATUS_OPTIONS;
    }
    
    public SelectItem[] getAdxStatusOptions() {
        return ADX_STATUS_OPTIONS;
    }
    
    public SelectItem[] getApnStatusOptions() {
        return APN_STATUS_OPTIONS;
    }

    private LazyDataModel<CreativeDto> createLazyModel() {
        return new LazyCreativeDataModel(getCreativeManager(), getPublisherManager(), auditedPublishersBean.getAdxPublishers(), auditedPublishersBean.getApnPublishers());
    }

    @Override
    protected void assignToUser(CreativeDto creativeDto, Long assignToUserId) {
        AdfonicUser assignTo = getUserManager().getAdfonicUserById(assignToUserId);
        Creative creative = getCreativeManager().getCreativeById(creativeDto.getId(), CREATIVE_FS);
        
        // Only do anything if the assignedTo is actually changing
        if (ObjectUtils.equals(assignTo, creative.getAssignedTo())) {
            return;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reassigning Creative id=" + creativeDto.getId() + " to " + (assignTo == null ? "nobody" : assignTo.getFullName()));
        }

        // Create a new history entry to track the assignment, who did it, etc.
        String comment;
        if (creative.getAssignedTo() == null) {
            comment = "Assigned";
        } else if (assignTo == null) {
            comment = "Unassigned";
        } else {
            comment = "Reassigned";
        }
                
        creative.setAssignedTo(assignTo);
        getCreativeManager().update(creative);
                
        getCreativeManager().newCreativeHistory(creative, comment, adfonicUser());

        if (assignTo != null) {
            // Make sure the just-assigned-to user is a watcher of the campaign
            Campaign campaign = getCampaignManager().getCampaignById(creative.getCampaign().getId(), CAMPAIGN_WITH_WATCHERS_FS);
            if (!campaign.getWatchers().contains(assignTo)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Adding " + assignTo.getFullName() + " as a watcher to Campaign id=" + campaign.getId());
                }
                campaign.getWatchers().add(assignTo);
                getCampaignManager().update(campaign);
            }
        }
    }

    // Getters for the view
    
    public LazyDataModel<CreativeDto> getLazyModel() {
        return lazyModel;
    }
    
}
