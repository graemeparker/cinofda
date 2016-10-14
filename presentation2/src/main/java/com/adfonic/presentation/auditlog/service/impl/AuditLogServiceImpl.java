package com.adfonic.presentation.auditlog.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Segment;
import com.adfonic.domain.auditlog.AuditLogEntity;
import com.adfonic.domain.auditlog.AuditLogEntry;
import com.adfonic.domain.auditlog.AuditLogEntry.AuditLogEntryType;
import com.adfonic.dto.auditlog.AuditLogDto;
import com.adfonic.dto.auditlog.AuditLogEntityDto;
import com.adfonic.dto.auditlog.AuditLogEntryDto;
import com.adfonic.dto.auditlog.AuditLogSearchDto;
import com.adfonic.presentation.auditlog.service.AuditLogService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.auditlog.filter.AuditEntityInformation;
import com.byyd.middleware.auditlog.filter.AuditLogEntityFilter;
import com.byyd.middleware.auditlog.service.AuditLogManager;
import com.byyd.middleware.campaign.filter.BidDeductionFilter;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.ibm.icu.util.Calendar;

/**
 * Service for providing Audit Log details for a specific campaign.
 */
@Service("auditLogService")
public class AuditLogServiceImpl extends GenericServiceImpl implements AuditLogService {
    
    private static final Set<String> FIELDS_TO_REMOVE = new HashSet<>(Arrays.asList(new String[]{"com.adfonic.domain.Campaign.currentTradingDeskMargin.tradingDeskMargin"}));
	
    @Autowired
    private AuditLogManager auditLogManager;

    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private AudienceManager audienceManager;
    
    @Autowired
    private BiddingManager biddingManager;

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogHistoryForCampaign(AuditLogSearchDto searchDto) {
        // Prepare filters
        AuditLogEntityFilter auditLogEntityFilter = getAuditLogEntityFilter(searchDto);

        // Prepare pagination
        Pagination pagination = getPagination(searchDto);

        // Retrieving information
        List<AuditLogEntity> auditLogEntities = auditLogManager.getAll(auditLogEntityFilter, pagination);

        // Convert to DTO
        List<AuditLogEntityDto> auditLogEntitiesDto = getList(auditLogEntities);

        return flattenAuditLogEntityDto(auditLogEntitiesDto);
    }

	private AuditLogEntityFilter getAuditLogEntityFilter(AuditLogSearchDto searchDto) {
    	// Getting current campaign information
        Campaign campaign = campaignManager.getCampaignById(searchDto.getCampaignDto().getId());
        
        List<AuditEntityInformation> auditEntitiesInformation = getCampaignAuditEntitiesInformation(campaign);

        // Entities to retrieve
        AuditLogEntityFilter filter = new AuditLogEntityFilter();
        filter.setAuditEntitiesInformation(auditEntitiesInformation);
        
        // From date (last N months or since the submission for approval time for the campaign)
        Date fromDate = getFromDate(searchDto.getMonthsToShow());
        filter.setFromDate(fromDate);

        return filter;
    }

	private List<AuditEntityInformation> getCampaignAuditEntitiesInformation(Campaign campaign) {
        // Collect campaign related entities for audit (Campaign, Advertiser, Segment and Creatives)
        List<AuditEntityInformation> auditEntitiesInformation = new ArrayList<AuditEntityInformation>();
        auditEntitiesInformation.add(new AuditEntityInformation(Campaign.class.getName(), campaign.getId()));
        auditEntitiesInformation.add(new AuditEntityInformation(Advertiser.class.getName(), campaign.getAdvertiser().getId()));
        auditEntitiesInformation.add(new AuditEntityInformation(Segment.class.getName(), campaign.getSegments().get(0).getId()));
        for (Creative creative : campaign.getCreatives()) {
            auditEntitiesInformation.add(new AuditEntityInformation(Creative.class.getName(), creative.getId()));
        }
        
        // Campaign Audiences
        CampaignAudienceFilter caFilter = new CampaignAudienceFilter();
        caFilter.setCampaign(campaign);
        List<CampaignAudience> campaignAudiences = audienceManager.getCampaignAudiences(caFilter);
        for (CampaignAudience campaignAudience: campaignAudiences) {
            auditEntitiesInformation.add(new AuditEntityInformation(CampaignAudience.class.getName(), campaignAudience.getId()));
        }
        
        // Bid Deductions
        BidDeductionFilter bdFilter = new BidDeductionFilter();
        bdFilter.setCampaign(campaign);
        List<BidDeduction> bidDeductions = biddingManager.getBidDeductions(bdFilter);
        for (BidDeduction bidDeduction: bidDeductions) {
            auditEntitiesInformation.add(new AuditEntityInformation(BidDeduction.class.getName(), bidDeduction.getId()));
        }

        return auditEntitiesInformation;
    }
	
    private Date getFromDate(Integer monthsToShow) {
    	Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -monthsToShow);
        return cal.getTime();
	}
	
    private Pagination getPagination(AuditLogSearchDto searchDto) {
        Sorting sorting = null;
        if (searchDto.getSortField() != null) {
            sorting = new Sorting((searchDto.getAscending() ? Direction.ASC : Direction.DESC), searchDto.getSortField());
        }

        Pagination pagination = new Pagination(searchDto.getFirst(), searchDto.getPageSize(), sorting);

        return pagination;
    }

    private List<AuditLogDto> flattenAuditLogEntityDto(List<AuditLogEntityDto> auditLogEntities) {
        List<AuditLogDto> list = new ArrayList<AuditLogDto>();
        for (AuditLogEntityDto entity : auditLogEntities) {
            for (AuditLogEntryDto entry : entity.getAuditLogEntries()) {
                if (!FIELDS_TO_REMOVE.contains(entity.getEntityName() + "." + entry.getName())){
                    list.add(new AuditLogDto(entity, entry));
                }
            }
        }
        return list;
    }

    public List<AuditLogEntityDto> getList(final List<AuditLogEntity> auditLogEntities) {
    	List<AuditLogEntityDto> result = new ArrayList<AuditLogEntityDto>();
    	if(!CollectionUtils.isEmpty(auditLogEntities)) {
    		for(AuditLogEntity auditLogEntity :auditLogEntities){
    			if (isCampaignSubmissionEvent(auditLogEntity)){
    				break;
    			}
    			result.add(getObjectDto(AuditLogEntityDto.class, (Object) auditLogEntity));
    		}
    	}
		return result;
    }

	private boolean isCampaignSubmissionEvent(AuditLogEntity auditLogEntity) {
	    if (!auditLogEntity.getAuditLogEntries().isEmpty()){
    		AuditLogEntry entry = auditLogEntity.getAuditLogEntries().get(0);
    		if ((AuditLogEntryType.VARCHAR.equals(entry.getAuditLogEntryType())) &&
    			(Campaign.Status.NEW_REVIEW.name().equals(entry.getOldValueVarchar()))){
    			return true;
    		}
	    }
		return false;
	}
}
