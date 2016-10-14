package com.adfonic.beans.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.beans.BaseBean;
import com.adfonic.beans.approval.creative.dto.CreativeDto;
import com.adfonic.beans.approval.creative.dto.PublisherAuditedInfoDto;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative.Status;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.util.UUIDUtils;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.filter.PublisherAuditedCreativeFilter;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;

public class LazyCreativeDataModel extends ApprovalAbstractLazyDataModel<CreativeDto, CreativeManager> {
    private static final transient Logger LOG = Logger.getLogger(LazyCreativeDataModel.class.getName());
    
    private static final long serialVersionUID = 1L;
    
    private static final String ADX_PUBLISHER_SORTING_ALIAS = "paAdx";
    private static final String APN_PUBLISHER_SORTING_ALIAS = "paApn";
    
    private PublisherManager publisherManager = null; 
    private Collection<Publisher> adxPublishers;
    private Collection<Publisher> apnPublishers;

    public LazyCreativeDataModel(CreativeManager creativeManager, PublisherManager publisherManager, Collection<Publisher> adxPublishers, Collection<Publisher> apnPublishers) {
        super(creativeManager);
        this.publisherManager = publisherManager;
        this.adxPublishers = adxPublishers;
        this.apnPublishers = apnPublishers;
    }

    @Override
    public CreativeDto getRowData(String rowKey) {
        CreativeDto dto = null;
        
        Creative creative = getManager().getCreativeByExternalId(rowKey, CreativeDto.FETCH_STRATEGY);
        
        if (creative != null){
            dto = new CreativeDto(creative);
            loadPublisherAuditedCreativeInfo(creative, dto);
        }
        return dto;
    }

    @Override
    public String getRowKey(CreativeDto creative) {
        return creative.getExternalID();
    }

    @Override
    @Transactional(readOnly=true)
    public List<CreativeDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading first=" + first + ", pageSize=" + pageSize + ", sortField=" + sortField + ", sortOrder=" + sortOrder + ", filters=" + filters);
        }

        // We normally want to query for PENDING and/or PENDING_PAUSED creatives
        CreativeFilter filter = new CreativeFilter().setStatuses(Creative.Status.PENDING, Creative.Status.PENDING_PAUSED);

        // Apply any additionally specified filter criteria from the UI
        applySpecifiedFilters(filters, filter);
        
        if(BaseBean.isRestrictedUser()){
            filter.setAdvertisers(BaseBean.getAdvertisersForAdfonicUser());
            filter.setFilterByAdvertisers(true);
        }
        
        //Get Pagination
        Pagination pagination = getPagination(first, pageSize, sortField, sortOrder, filter);

        // Query for a page of data and transform into DTOs
        List<CreativeDto> data = new ArrayList<>();
        
        // First, running the query just to retrieve Creative Information (due to performance issues the fetch strategy is not used here)
        List<Creative> creatives = getManager().getAllCreatives(filter, pagination);
        
        // Second, retrieve the full info for all creatives returned   
        if (!creatives.isEmpty()){
            Collection<Long> creativesIds = getIdList(creatives);
            creatives = getManager().getCreativesByIdsAsList(creativesIds, pagination.getSorting(), CreativeDto.FETCH_STRATEGY);
            for (Creative creative : creatives) {
                CreativeDto dto = new CreativeDto(creative);
                loadPublisherAuditedCreativeInfo(creative, dto);
                data.add(dto);
            }
        
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Resulting page size=" + data.size());
            }
        }

        // If the filters changed, update the total count
        if (getCurrentFilters() == null || !getCurrentFilters().equals(filters)) {
            int numRows = getManager().countAllCreatives(filter).intValue();
            setRowCount(numRows);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Filters changed, new total row count: " + numRows);
            }

            setCurrentFilters(filters);
        }
        
        return data;
    }

    private List<Long> getIdList(List<Creative> creatives) {
        List<Long> ids = new ArrayList<>();
        for (Creative creative : creatives){
            ids.add(creative.getId());
        }
        return ids;
    }

    private Pagination getPagination(int first, int pageSize, String sortField, SortOrder sortOrder, CreativeFilter filter) {
        if (StringUtils.isEmpty(sortField)) {
            return new Pagination(first, pageSize, getSorting("submissionTime", SortOrder.DESCENDING, filter));
        } else {
            return new Pagination(first, pageSize, getSorting(sortField, sortOrder, filter));
        }
    }

    private Sorting getSorting(String sortField, SortOrder sortOrder, CreativeFilter filter) {
        Direction direction = SortOrder.ASCENDING.equals(sortOrder) ? Direction.ASC : Direction.DESC;
        switch (sortField) {
        case "id":
            return new Sorting(direction, "id");
        case "name":
            return new Sorting(direction, "name");
        case "campaignName":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Campaign.class, "name"));
        case "campaignAdvertiserDomain":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Campaign.class, "advertiserDomain"));
        case "externalID":
            return new Sorting(direction, "externalID");
        case "status":
            return new Sorting(direction, "status");
        case "adxStatus":
            // Check if there is a filter set for adx
            addDefaultPublisherFilter(filter, ADX_PUBLISHER_SORTING_ALIAS);
            return new Sorting(direction, ADX_PUBLISHER_SORTING_ALIAS + ".status");
        case "apnStatus":
            addDefaultPublisherFilter(filter, APN_PUBLISHER_SORTING_ALIAS);
            return new Sorting(direction, APN_PUBLISHER_SORTING_ALIAS + ".status");
        case "submissionTime":
            return new Sorting(direction, "submissionTime");
        case "assignedTo":
            return new Sorting( new com.byyd.middleware.iface.dao.SortOrder(direction, AdfonicUser.class, "firstName"),
                                new com.byyd.middleware.iface.dao.SortOrder(direction, AdfonicUser.class, "lastName"));
        default:
            LOG.warning("Sort field not supported (yet): " + sortField);
            return null;
        }
    }

    private void addDefaultPublisherFilter(CreativeFilter filter, String publisherSortAlias) {
        PublisherAuditedCreativeFilter publisherAuditedFilter = filter.getPublisherAuditedCreativeFilter(publisherSortAlias);
        if (publisherAuditedFilter == null){
            filter.addPublisherAuditedCreativeFilter(publisherSortAlias, null, false, null, false);
        }
    }

    private void applySpecifiedFilters(Map<String,String> filters, CreativeFilter filter) {
        if (MapUtils.isEmpty(filters)) {
            return;
        }
        for (Map.Entry<String,String> entry : filters.entrySet()) {
            String property = entry.getKey();
            String value = entry.getValue();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Applying filter: " + property + "=" + value);
            }
            switch (property) {
            case "id":
                if (!filters.containsKey("status")) {
                    filter.setStatuses(Collections.<Status> emptySet()); // allow searching by id regardless of status
                }
                filter.setIncludedIds(Collections.singleton(Long.valueOf(value)));
                break;
            case "name":
                filter.setContainsName(value, false);
                break;
            case "campaignName":
                filter.setCampaignNameContains(value);
                break;
            case "campaignAdvertiserDomain":
                filter.setCampaignAdvertiserDomainContains(value);
                break;
            case "status":
                filter.setStatuses(Collections.singleton(Creative.Status.valueOf(value)));
                break;
            case "adxStatus":
                PublisherAuditedInfoDto.StatusOption adxStatusOption = PublisherAuditedInfoDto.StatusOption.valueOf(value);
                boolean isAdxNullValue = (adxStatusOption==PublisherAuditedInfoDto.StatusOption.NULL?true:false);
                filter.addPublisherAuditedCreativeFilter(ADX_PUBLISHER_SORTING_ALIAS, adxPublishers, isAdxNullValue, getAdxStatuses(adxStatusOption), isAdxNullValue);
                break;
            case "apnStatus":
                PublisherAuditedInfoDto.StatusOption apnStatusOption = PublisherAuditedInfoDto.StatusOption.valueOf(value);
                boolean isApnNullValue = (apnStatusOption==PublisherAuditedInfoDto.StatusOption.NULL?true:false);
                filter.addPublisherAuditedCreativeFilter(APN_PUBLISHER_SORTING_ALIAS, apnPublishers, isApnNullValue, getApnStatuses(apnStatusOption), isApnNullValue);
                break;
            case "assignedTo":
                filter.setAssignedToFullNameContains(value);
                break;
            case "keyAccount":
                filter.setAdvertiserIsKey("Key".equalsIgnoreCase(value));
                break;
            case "country":
                // AO-221 - allow searching for "Global"
                if ("Global".equalsIgnoreCase(value)) {
                    filter.setCountryTargetingGlobal(true);
                } else {
                    filter.setCountryNameContains(value);
                }
                break;
            case "destination":
                filter.setDestinationContains(value);
                break;
            case "advertiser":
                filter.setCompanyNameContains(value);
                break;
            case "externalID":
                if (UUIDUtils.isUUID(value)) {
                    // This is an explicit search by full externalID.
                    // Override any status filters to allow the full search.
                    filter.setStatuses(Collections.<Status> emptySet());
                }
                filter.setExternalIdContains(value);
                break;
            case "fromAddress":
                filter.setAccountManagerEmailContains(value);
                break;
            default:
                LOG.warning("Filter not (yet) supported: " + property);
                break;
            }
        }

    }

    private Collection<PublisherAuditedCreative.Status> getApnStatuses(PublisherAuditedInfoDto.StatusOption value) {
        Collection<PublisherAuditedCreative.Status> statuses = new HashSet<>();
        switch (value) {
            case NULL:
                //Exclude statuses to retrieve also null values
                statuses.add(PublisherAuditedCreative.Status.PENDING);
                statuses.add(PublisherAuditedCreative.Status.ACTIVE);
                statuses.add(PublisherAuditedCreative.Status.REJECTED);
                statuses.add(PublisherAuditedCreative.Status.UNAUDITABLE);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_AUDIT_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_AND_AUDIT);
                break;
            case PENDING:
                statuses.add(PublisherAuditedCreative.Status.PENDING);
                break;
            case APPROVED:
                statuses.add(PublisherAuditedCreative.Status.ACTIVE);
                break;
            case REJECTED:
                statuses.add(PublisherAuditedCreative.Status.REJECTED);
                statuses.add(PublisherAuditedCreative.Status.UNAUDITABLE);
                break;
            case MANUAL:
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_AUDIT_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_AND_AUDIT);
                break;
            default:
                break;
        }
        return statuses;
    }

    private Collection<PublisherAuditedCreative.Status> getAdxStatuses(PublisherAuditedInfoDto.StatusOption value) {
        Collection<PublisherAuditedCreative.Status> statuses = new HashSet<>();
        switch (value) {
            case NULL:
                //Exclude statuses to retrieve also null values
                statuses.add(PublisherAuditedCreative.Status.PENDING);
                statuses.add(PublisherAuditedCreative.Status.ACTIVE);
                statuses.add(PublisherAuditedCreative.Status.REJECTED);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_AUDIT_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_AND_AUDIT);
                break;
            case PENDING:
                statuses.add(PublisherAuditedCreative.Status.PENDING);
                break;
            case APPROVED:
                statuses.add(PublisherAuditedCreative.Status.ACTIVE);
                break;
            case REJECTED:
                statuses.add(PublisherAuditedCreative.Status.REJECTED);
                break;
            case MANUAL:
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_AUDIT_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_ONLY);
                statuses.add(PublisherAuditedCreative.Status.BYPASS_ALLOW_CACHE_AND_AUDIT);
                break;
            default:
                break;
        }
        return statuses;
    }
    

    private void loadPublisherAuditedCreativeInfo(Creative creative, CreativeDto creativeDto) {
        List<PublisherAuditedCreative> publishersAuditedCreative = publisherManager.getAllPublisherAuditedCreativesForCreative(creative, CreativeDto.PUBLISHER_AUDITED_CREATIVE_FS);
        PublisherAuditedInfoDto adxPublisherAuditedInfoDto = getAdxPublisherAuditedInfo(publishersAuditedCreative, adxPublishers, creative.getId()); 
        creativeDto.setAdxPublisherAuditedInfo((adxPublisherAuditedInfoDto==null?new PublisherAuditedInfoDto():adxPublisherAuditedInfoDto));
        PublisherAuditedInfoDto apnPublisherAuditedInfoDto = getApnPublisherAuditedInfo(publishersAuditedCreative, apnPublishers, creative.getId());
        creativeDto.setApnPublisherAuditedInfo((apnPublisherAuditedInfoDto==null?new PublisherAuditedInfoDto():apnPublisherAuditedInfoDto));
    }

    public static PublisherAuditedInfoDto getApnPublisherAuditedInfo(Collection<PublisherAuditedCreative> publishersAuditedCreative, Collection<Publisher> apnPublishers, Long creativeId) {
        PublisherAuditedInfoDto publisherAuditedInfoDto = null;
        
        for(Publisher apnPublisher : apnPublishers){
            PublisherAuditedCreative pac = getPublisherAudited(publishersAuditedCreative, apnPublisher);
            if (pac!=null){
                PublisherAuditedInfoDto.StatusOption apnStatus = null;
                switch(pac.getStatus()){
                    case CREATION_INITIATED:
                    case LOCAL_INVALID:
                    case INTERNALLY_INELIGIBLE:
                    case MISC_UNMAPPED:
                    case SUBMIT_FAILED:
                        apnStatus = PublisherAuditedInfoDto.StatusOption.NULL;
                        break;
                    case PENDING:
                        apnStatus = PublisherAuditedInfoDto.StatusOption.PENDING;
                        break;
                    case ACTIVE:
                        apnStatus = PublisherAuditedInfoDto.StatusOption.APPROVED;
                        break;
                    case REJECTED:
                    case UNAUDITABLE:
                        apnStatus = PublisherAuditedInfoDto.StatusOption.REJECTED;
                        break;
                    case BYPASS_ALLOW_AUDIT_ONLY:
                    case BYPASS_ALLOW_CACHE_ONLY:
                    case BYPASS_ALLOW_CACHE_AND_AUDIT:
                        apnStatus = PublisherAuditedInfoDto.StatusOption.MANUAL;
                        break;
                }
                publisherAuditedInfoDto = new PublisherAuditedInfoDto("APN", apnPublisher.getId(), creativeId, apnStatus, pac.getExternalReference(), pac.getLastAuditRemarks());
            }
        }
        
        if (publisherAuditedInfoDto==null){
            publisherAuditedInfoDto = new PublisherAuditedInfoDto("APN", null, creativeId, PublisherAuditedInfoDto.StatusOption.NULL, null, null);
        }
        
        return publisherAuditedInfoDto;
    }

    @SuppressWarnings("incomplete-switch")
    public static PublisherAuditedInfoDto getAdxPublisherAuditedInfo(Collection<PublisherAuditedCreative> publishersAuditedCreative, Collection<Publisher> adxPublishers, Long creativeId) {
        PublisherAuditedInfoDto publisherAuditedInfoDto = null;
        
        for(Publisher adxPublisher : adxPublishers){
            PublisherAuditedCreative pac = getPublisherAudited(publishersAuditedCreative, adxPublisher);
            if (pac!=null){
                PublisherAuditedInfoDto.StatusOption adxStatus = null;
                switch(pac.getStatus()){
                    case CREATION_INITIATED:
                    case LOCAL_INVALID:
                    case INTERNALLY_INELIGIBLE:
                        adxStatus = PublisherAuditedInfoDto.StatusOption.NULL;
                        break;
                    case PENDING:
                        adxStatus = PublisherAuditedInfoDto.StatusOption.PENDING;
                        break;
                    case ACTIVE:
                        adxStatus = PublisherAuditedInfoDto.StatusOption.APPROVED;
                        break;
                    case REJECTED:
                        adxStatus = PublisherAuditedInfoDto.StatusOption.REJECTED;
                        break;
                    case BYPASS_ALLOW_AUDIT_ONLY:
                    case BYPASS_ALLOW_CACHE_ONLY:
                    case BYPASS_ALLOW_CACHE_AND_AUDIT:
                        adxStatus = PublisherAuditedInfoDto.StatusOption.MANUAL;
                        break;
                }
                publisherAuditedInfoDto = new PublisherAuditedInfoDto("ADX", adxPublisher.getId(), creativeId, adxStatus, pac.getExternalReference(), pac.getLastAuditRemarks());
            }
        }
        
        if (publisherAuditedInfoDto==null){
            publisherAuditedInfoDto = new PublisherAuditedInfoDto("ADX", null, creativeId, PublisherAuditedInfoDto.StatusOption.NULL, null, null);
        }
        
        return publisherAuditedInfoDto;
    }
    
    private static PublisherAuditedCreative getPublisherAudited(Collection<PublisherAuditedCreative> publishersAuditedCreative, Publisher publisher) {
        for (PublisherAuditedCreative publisherAuditedCreative : publishersAuditedCreative){
            if (publisherAuditedCreative.getPublisher().getId() == publisher.getId()){
                return publisherAuditedCreative;
            }
        }
        return null;
    }
}
