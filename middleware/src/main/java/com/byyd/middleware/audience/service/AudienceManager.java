package com.byyd.middleware.audience.service;

import java.util.List;
import java.util.Set;

import com.adfonic.domain.Audience;
import com.adfonic.domain.AudienceDataFee;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.DMPAttribute;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPVendor;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.byyd.middleware.audience.filter.AudienceDataFeeFilter;
import com.byyd.middleware.audience.filter.AudienceFilter;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.audience.filter.DMPAttributeFilter;
import com.byyd.middleware.audience.filter.DMPSelectorFilter;
import com.byyd.middleware.audience.filter.DMPVendorFilter;
import com.byyd.middleware.audience.filter.FirstPartyAudienceDeviceIdsUploadHistoryFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface AudienceManager extends BaseManager {
    
    //------------------------------------------------------------------------------
    // Audience
    //------------------------------------------------------------------------------

    Audience getAudienceById(String id, FetchStrategy... fetchStrategy);
    Audience getAudienceById(Long id, FetchStrategy... fetchStrategy);
    Audience getAudienceByExternalId(String externalId, FetchStrategy... fetchStrategy);
    Audience create(Audience audience, DMPAudience dmpAudience, FirstPartyAudience firstPartyAudience);
    Audience update(Audience audience);
    void delete(Audience audience);
    void deleteAudiences(List<Audience> list);
    
    Long countAudiences(AudienceFilter filter);
    List<Audience> getAudiences(AudienceFilter filter, FetchStrategy ... fetchStrategy);
    List<Audience> getAudiences(AudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<Audience> getAudiences(AudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    
    Long countCampaignsLinkedToAudience(Audience audience);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, Sorting sort, FetchStrategy... fetchStrategy);
    List<Campaign> getCampaignsLinkedToAudience(Audience audience, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // DMPAudience
    //------------------------------------------------------------------------------

    DMPAudience getDMPAudienceById(String id, FetchStrategy... fetchStrategy);
    DMPAudience getDMPAudienceById(Long id, FetchStrategy... fetchStrategy);
    DMPAudience create(DMPAudience dmpAudience);
    DMPAudience update(DMPAudience dmpAudience);
    void delete(DMPAudience dmpAudience);
    void deleteDMPAudiences(List<DMPAudience> list);
    
    //------------------------------------------------------------------------------
    // DMPVendor - reference data, query methods only
    //------------------------------------------------------------------------------
    DMPVendor getDMPVendorById(String id, FetchStrategy... fetchStrategy);
    DMPVendor getDMPVendorById(Long id, FetchStrategy... fetchStrategy);
    
    Long countDMPVendors();
    List<DMPVendor> getDMPVendors(FetchStrategy ... fetchStrategy);
    List<DMPVendor> getDMPVendors(Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPVendor> getDMPVendors(Pagination page, FetchStrategy ... fetchStrategy);

    Long countDMPVendors(DMPVendorFilter filter);
    List<DMPVendor> getDMPVendors(DMPVendorFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPVendor> getDMPVendors(DMPVendorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPVendor> getDMPVendors(DMPVendorFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    DMPVendor getDMPVendorByName(String name, FetchStrategy... fetchStrategy); 
    DMPVendor getDMPVendorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy); 

    //------------------------------------------------------------------------------
    // DMPAttribute - reference data, query methods only. The methods using
    // a DMPAttributeFilter can be used to retrieve attributes across several
    // vendors, presumably for admin purposes, but application-level calls
    // should always ensure a vendor is set.
    //------------------------------------------------------------------------------
    DMPAttribute getDMPAttributeById(String id, FetchStrategy... fetchStrategy);
    DMPAttribute getDMPAttributeById(Long id, FetchStrategy... fetchStrategy);
    
    Long countDMPAttributes(DMPAttributeFilter filter);
    List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countDMPAttributes(DMPVendor dmpVendor);
    List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, Pagination page, FetchStrategy ... fetchStrategy);

    DMPAttribute getDMPAttributeByName(DMPVendor dmpVendor, String name, FetchStrategy... fetchStrategy); 
    DMPAttribute getDMPAttributeByName(DMPVendor dmpVendor, String name, boolean caseSensitive, FetchStrategy... fetchStrategy); 

    //------------------------------------------------------------------------------
    // DMPSelector
    //------------------------------------------------------------------------------

    DMPSelector create(DMPSelector dmpSelector);
    DMPSelector update(DMPSelector dmpSelector);
    
    DMPSelector getDMPSelectorById(String id, FetchStrategy... fetchStrategy);
    DMPSelector getDMPSelectorById(Long id, FetchStrategy... fetchStrategy);
    
    Long countDMPSelectors(DMPSelectorFilter filter);
    List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countDMPSelectors(DMPAttribute dmpAttribute);
    List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, Sorting sort, FetchStrategy ... fetchStrategy);
    List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, Pagination page, FetchStrategy ... fetchStrategy);

    DMPSelector getDMPSelectorByName(DMPAttribute dmpAttribute, String name, FetchStrategy... fetchStrategy); 
    DMPSelector getDMPSelectorByName(DMPAttribute dmpAttribute, String name, boolean caseSensitive, FetchStrategy... fetchStrategy); 
    
    DMPSelector getDMPSelectorByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId, FetchStrategy... fetchStrategy); 
    
    Long countDMPSelectorsForDMPAudience(DMPAudience dmpAudience);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, FetchStrategy... fetchStrategy);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Sorting sort, FetchStrategy... fetchStrategy);
    List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------
    // FirstPartyAudience
    //------------------------------------------------------------------------------

    FirstPartyAudience getFirstPartyAudienceById(String id, FetchStrategy... fetchStrategy);
    FirstPartyAudience getFirstPartyAudienceById(Long id, FetchStrategy... fetchStrategy);
    FirstPartyAudience create(FirstPartyAudience firstPartyAudience);
    FirstPartyAudience update(FirstPartyAudience firstPartyAudience);
    void delete(FirstPartyAudience firstPartyAudience);
    void deleteFirstPartyAudiences(List<FirstPartyAudience> list);
    
    //------------------------------------------------------------------------------
    // FirstPartyAudienceDeviceIdsUploadHistory
    //------------------------------------------------------------------------------

    FirstPartyAudienceDeviceIdsUploadHistory newFirstPartyAudienceDeviceIdsUploadHistory(
            FirstPartyAudience firstPartyAudience,
            String filename,
            DeviceIdentifierType deviceIdentifierType,
            Long totalNumRecords,
            Long numValidatedRecords,
            Long numInsertedRecords);
    FirstPartyAudienceDeviceIdsUploadHistory getFirstPartyAudienceDeviceIdsUploadHistoryById(String id, FetchStrategy... fetchStrategy);
    FirstPartyAudienceDeviceIdsUploadHistory getFirstPartyAudienceDeviceIdsUploadHistoryById(Long id, FetchStrategy... fetchStrategy);
    FirstPartyAudienceDeviceIdsUploadHistory create(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory);
    FirstPartyAudienceDeviceIdsUploadHistory update(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory);
    void delete(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory);
    void deleteFirstPartyAudienceDeviceIdsUploadHistories(List<FirstPartyAudienceDeviceIdsUploadHistory> list);
    
    Long countAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, FetchStrategy ... fetchStrategy);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);

    //------------------------------------------------------------------------------
    // CampaignAudience
    //------------------------------------------------------------------------------

    CampaignAudience getCampaignAudienceById(String id, FetchStrategy... fetchStrategy);
    CampaignAudience getCampaignAudienceById(Long id, FetchStrategy... fetchStrategy);
    CampaignAudience create(CampaignAudience campaignAudience);
    CampaignAudience update(CampaignAudience campaignAudience);
    void delete(CampaignAudience campaignAudience);
    void deleteCampaignAudiences(List<CampaignAudience> list);
    
    Long countCampaignAudiences(CampaignAudienceFilter filter);
    List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, FetchStrategy ... fetchStrategy);
    List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    
    Campaign updateCampaignAudiences(Campaign campaign, Set<CampaignAudience> campaignAudiences);

    
    //------------------------------------------------------------------------------------------
    // AudienceDataFee  (historical table for CampaingAudiences prices)
    //------------------------------------------------------------------------------------------

    AudienceDataFee getAudienceDataFeeById(Long id, FetchStrategy... fetchStrategy);
    AudienceDataFee getAudienceDataFeeById(String id, FetchStrategy... fetchStrategy);
    AudienceDataFee create(AudienceDataFee audienceDataFee);
    AudienceDataFee update(AudienceDataFee audienceDataFee);
    void delete(AudienceDataFee audienceDataFee);
    void delete(List<AudienceDataFee> list);
    
    Long countAll(AudienceDataFeeFilter filter);
    List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, FetchStrategy ... fetchStrategy);
    List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
}
