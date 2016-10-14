package com.byyd.middleware.audience.service.jpa;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Audience;
import com.adfonic.domain.AudienceDataFee;
import com.adfonic.domain.AudiencePrices;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignDataFee;
import com.adfonic.domain.DMPAttribute;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPVendor;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.byyd.middleware.audience.dao.AudienceDao;
import com.byyd.middleware.audience.dao.AudienceDataFeeDao;
import com.byyd.middleware.audience.dao.CampaignAudienceDao;
import com.byyd.middleware.audience.dao.DMPAttributeDao;
import com.byyd.middleware.audience.dao.DMPAudienceDao;
import com.byyd.middleware.audience.dao.DMPSelectorDao;
import com.byyd.middleware.audience.dao.DMPVendorDao;
import com.byyd.middleware.audience.dao.FirstPartyAudienceDao;
import com.byyd.middleware.audience.dao.FirstPartyAudienceDeviceIdsUploadHistoryDao;
import com.byyd.middleware.audience.filter.AudienceDataFeeFilter;
import com.byyd.middleware.audience.filter.AudienceFilter;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.audience.filter.DMPAttributeFilter;
import com.byyd.middleware.audience.filter.DMPSelectorFilter;
import com.byyd.middleware.audience.filter.DMPVendorFilter;
import com.byyd.middleware.audience.filter.FirstPartyAudienceDeviceIdsUploadHistoryFilter;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.campaign.dao.CampaignDao;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.jpa.FeeManagerJpaImpl.DataFeeCalculationResult;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("audienceManager")
public class AudienceManagerJpaImpl extends BaseJpaManagerImpl implements AudienceManager {
    
    @Autowired(required = false)
    private AudienceDao audienceDao;
    @Autowired(required = false)
    private DMPAudienceDao dmpAudienceDao;
    @Autowired(required = false)
    private DMPVendorDao dmpVendorDao;
    @Autowired(required = false)
    private DMPAttributeDao dmpAttributeDao;
    @Autowired(required = false)
    private DMPSelectorDao dmpSelectorDao;
    @Autowired(required = false)
    private FirstPartyAudienceDao firstPartyAudienceDao;
    @Autowired(required = false)
    private FirstPartyAudienceDeviceIdsUploadHistoryDao firstPartyAudienceDeviceIdsUploadHistoryDao;
    @Autowired(required = false)
    private CampaignAudienceDao campaignAudienceDao;
    @Autowired(required=false)
    private AudienceDataFeeDao audienceDataFeeDao;
    
    //------------------------------------------------------------------------------
    // Audience
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Audience getAudienceById(String id, FetchStrategy... fetchStrategy) {
        return this.getAudienceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Audience getAudienceById(Long id, FetchStrategy... fetchStrategy) {
        return audienceDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Audience getAudienceByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return audienceDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public Audience create(Audience audience, DMPAudience dmpAudience, FirstPartyAudience firstPartyAudience) {
        if (dmpAudience!=null){
            DMPAudience dbDmpAudience = create(dmpAudience);
            audience.setDmpAudience(dbDmpAudience);
            audience.setFirstPartyAudience(null);
        }else if (firstPartyAudience!=null){
            FirstPartyAudience dbFirstPartyAudience = create(firstPartyAudience);
            audience.setFirstPartyAudience(dbFirstPartyAudience);
            audience.setDmpAudience(null);
        }

        return audienceDao.create(audience);
    }

    @Override
    @Transactional(readOnly=false)
    public Audience update(Audience audience) {
        DMPAudience dmpAudience = audience.getDmpAudience();
        FirstPartyAudience firstPartyAudience = audience.getFirstPartyAudience();

        if (dmpAudience!=null){
            update(dmpAudience);
            audience.setFirstPartyAudience(null);
        }else if (firstPartyAudience!=null){
            update(firstPartyAudience);
            audience.setDmpAudience(null);
        }

        Audience persistedAudience = audienceDao.update(audience);

        // We recalculate again the data-fee when the audience is DMP
        // If in the future we start to sell first party audiences then we have to remove this condition
        if (dmpAudience!=null){
            recalculateCampaignsDatafee(persistedAudience);
        }

        return persistedAudience;
    }

    private void recalculateCampaignsDatafee(Audience audience) {
        List<Campaign> campaigns = getCampaignsLinkedToAudience(audience);

        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        for(Campaign campaign : campaigns){
            updateCampaignAudiences(campaignManager, feeManager, campaign, audience);
        }
    }


    @Override
    @Transactional(readOnly=false)
    public void delete(Audience audience) {
        // removing the audience (logic delete)
        audienceDao.delete(audience);

        // We have to recalculate again data fee value
        // First, take all CampaignAudiences related with this audience
        CampaignAudienceFilter filter = new CampaignAudienceFilter();
        filter.setAudience(audience);
        filter.setDeleted(false);
        List<CampaignAudience> campaignAudiences = getCampaignAudiences(filter);

        // Go over all campaign audiences
        Campaign campaign = null;
        Set<CampaignAudience> campaignAudiencesFromCampaign = null;
        for(CampaignAudience campaignAudience : campaignAudiences){
            // Recover the campaign entity
            campaign = campaignAudience.getCampaign();

            // Catch all campaign audiences for this campaign
            campaignAudiencesFromCampaign = new HashSet<CampaignAudience>(campaign.getCampaignAudiences());

            // Removing the
            campaignAudiencesFromCampaign.remove(campaignAudience);

            // Updating campaign with the new audience composition (this method will also update date fee value)
            updateCampaignAudiences(campaign, campaignAudiencesFromCampaign);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAudiences(List<Audience> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Audience entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAudiences(AudienceFilter filter) {
        return audienceDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Audience> getAudiences(AudienceFilter filter, FetchStrategy... fetchStrategy) {
        return audienceDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Audience> getAudiences(AudienceFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return audienceDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Audience> getAudiences(AudienceFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return audienceDao.getAll(filter, page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countCampaignsLinkedToAudience(Audience audience) {
        CampaignDao campaignDao = AdfonicBeanDispatcher.getBean(CampaignDao.class);
        return campaignDao.countCampaignsLinkedToAudience(audience);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, FetchStrategy... fetchStrategy) {
        CampaignDao campaignDao = AdfonicBeanDispatcher.getBean(CampaignDao.class);
        return campaignDao.getCampaignsLinkedToAudience(audience, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, Sorting sort, FetchStrategy... fetchStrategy) {
        CampaignDao campaignDao = AdfonicBeanDispatcher.getBean(CampaignDao.class);
        return campaignDao.getCampaignsLinkedToAudience(audience, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, Pagination page, FetchStrategy... fetchStrategy) {
        CampaignDao campaignDao = AdfonicBeanDispatcher.getBean(CampaignDao.class);
        return campaignDao.getCampaignsLinkedToAudience(audience, page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------
    // DMPAudience
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public DMPAudience getDMPAudienceById(String id, FetchStrategy... fetchStrategy) {
        return this.getDMPAudienceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPAudience getDMPAudienceById(Long id, FetchStrategy... fetchStrategy) {
        return dmpAudienceDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public DMPAudience create(DMPAudience dmpAudience) {
        return dmpAudienceDao.create(dmpAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public DMPAudience update(DMPAudience dmpAudience) {
        DMPAudience dbDmpAudience;
        if (isPersisted(dmpAudience)){
            dbDmpAudience = dmpAudienceDao.update(dmpAudience);
        }else{
            dbDmpAudience = create(dmpAudience);
        }
        return dbDmpAudience;
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(DMPAudience dmpAudience) {
        dmpAudienceDao.delete(dmpAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteDMPAudiences(List<DMPAudience> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (DMPAudience entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------
    // DMPVendor - reference data, query methods only
    //------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public DMPVendor getDMPVendorById(String id, FetchStrategy... fetchStrategy) {
        return getDMPVendorById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPVendor getDMPVendorById(Long id, FetchStrategy... fetchStrategy) {
        return dmpVendorDao.getById(id, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countDMPVendors() {
        return dmpVendorDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(FetchStrategy ... fetchStrategy) {
        return dmpVendorDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(Sorting sort, FetchStrategy ... fetchStrategy) {
        return dmpVendorDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(Pagination page, FetchStrategy ... fetchStrategy) {
        return dmpVendorDao.getAll(page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    protected DMPVendorFilter getDmpVendorFilter(String name, LikeSpec like, boolean caseSensitive, Boolean restricted) {
        return new DMPVendorFilter()
                   .setName(name, like, caseSensitive)
                   .setRestricted(restricted);
    }

    protected DMPVendorFilter getDmpVendorFilter(String name, boolean caseSensitive) {
        return new DMPVendorFilter()
                   .setName(name, caseSensitive);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countDMPVendors(DMPVendorFilter filter) {
        return dmpVendorDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(DMPVendorFilter filter, FetchStrategy... fetchStrategy) {
        return dmpVendorDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(DMPVendorFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return dmpVendorDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPVendor> getDMPVendors(DMPVendorFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return dmpVendorDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public DMPVendor getDMPVendorByName(String name, FetchStrategy... fetchStrategy) {
        return getDMPVendorByName(name, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPVendor getDMPVendorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        // Name is unique
        List<DMPVendor> list = getDMPVendors(getDmpVendorFilter(name, caseSensitive), fetchStrategy);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    //------------------------------------------------------------------------------
    // DMPAttribute - reference data, query methods only
    //------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public DMPAttribute getDMPAttributeById(String id, FetchStrategy... fetchStrategy) {
        return getDMPAttributeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPAttribute getDMPAttributeById(Long id, FetchStrategy... fetchStrategy) {
        return dmpAttributeDao.getById(id, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    protected DMPAttributeFilter getDmpAttributeFilter(DMPVendor dmpVendor, String name, LikeSpec like, boolean caseSensitive) {
        return new DMPAttributeFilter()
                   .setName(name, like, caseSensitive)
                   .setDMPVendor(dmpVendor);
    }

    protected DMPAttributeFilter getDmpAttributeFilter(DMPVendor dmpVendor, String name, boolean caseSensitive) {
        return new DMPAttributeFilter()
                   .setName(name, caseSensitive)
                   .setDMPVendor(dmpVendor);
    }

    protected DMPAttributeFilter getDmpAttributeFilter(DMPVendor dmpVendor) {
        return new DMPAttributeFilter()
                   .setDMPVendor(dmpVendor);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countDMPAttributes(DMPAttributeFilter filter) {
        return dmpAttributeDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, FetchStrategy... fetchStrategy) {
        return dmpAttributeDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return dmpAttributeDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPAttributeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return dmpAttributeDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countDMPAttributes(DMPVendor dmpVendor) {
        return dmpAttributeDao.countAll(getDmpAttributeFilter(dmpVendor));
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, FetchStrategy ... fetchStrategy) {
        return dmpAttributeDao.getAll(getDmpAttributeFilter(dmpVendor), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, Sorting sort, FetchStrategy ... fetchStrategy) {
        return dmpAttributeDao.getAll(getDmpAttributeFilter(dmpVendor), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPAttribute> getDMPAttributes(DMPVendor dmpVendor, Pagination page, FetchStrategy ... fetchStrategy) {
        return dmpAttributeDao.getAll(getDmpAttributeFilter(dmpVendor), page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public DMPAttribute getDMPAttributeByName(DMPVendor dmpVendor, String name, FetchStrategy... fetchStrategy) {
        return getDMPAttributeByName(dmpVendor, name, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPAttribute getDMPAttributeByName(DMPVendor dmpVendor, String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        // Name is unique
        List<DMPAttribute> list = getDMPAttributes(getDmpAttributeFilter(dmpVendor, name, caseSensitive), fetchStrategy);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    //------------------------------------------------------------------------------
    // DMPSelector
    //------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=false)
    public DMPSelector create(DMPSelector dmpSelector) {
    	return dmpSelectorDao.create(dmpSelector);
    }
    
    @Override
    @Transactional(readOnly=false)
    public DMPSelector update(DMPSelector dmpSelector) {
    	return dmpSelectorDao.update(dmpSelector);
    }
    
    //------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=true)
    public DMPSelector getDMPSelectorById(String id, FetchStrategy... fetchStrategy) {
        return getDMPSelectorById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPSelector getDMPSelectorById(Long id, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getById(id, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    protected DMPSelectorFilter getDmpSelectorFilter(DMPAttribute dmpAttribute, String name, LikeSpec like, boolean caseSensitive) {
        return new DMPSelectorFilter()
                   .setName(name, like, caseSensitive)
                   .setDMPAttribute(dmpAttribute);
    }

    protected DMPSelectorFilter getDmpSelectorFilter(DMPAttribute dmpAttribute, String name, boolean caseSensitive) {
        return new DMPSelectorFilter()
                   .setName(name, caseSensitive)
                   .setDMPAttribute(dmpAttribute);
    }

    protected DMPSelectorFilter getDmpSelectorFilter(DMPAttribute dmpAttribute) {
        return new DMPSelectorFilter()
                   .setDMPAttribute(dmpAttribute);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countDMPSelectors(DMPSelectorFilter filter) {
        return dmpSelectorDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPSelectorFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countDMPSelectors(DMPAttribute dmpAttribute) {
        return dmpSelectorDao.countAll(getDmpSelectorFilter(dmpAttribute));
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, FetchStrategy ... fetchStrategy) {
        return dmpSelectorDao.getAll(getDmpSelectorFilter(dmpAttribute), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, Sorting sort, FetchStrategy ... fetchStrategy) {
        return dmpSelectorDao.getAll(getDmpSelectorFilter(dmpAttribute), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectors(DMPAttribute dmpAttribute, Pagination page, FetchStrategy ... fetchStrategy) {
        return dmpSelectorDao.getAll(getDmpSelectorFilter(dmpAttribute), page, fetchStrategy);
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public DMPSelector getDMPSelectorByName(DMPAttribute dmpAttribute, String name, FetchStrategy... fetchStrategy) {
        return getDMPSelectorByName(dmpAttribute, name, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPSelector getDMPSelectorByName(DMPAttribute dmpAttribute, String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        // Name is unique
        List<DMPSelector> list = getDMPSelectors(getDmpSelectorFilter(dmpAttribute, name, caseSensitive), fetchStrategy);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countDMPSelectorsForDMPAudience(DMPAudience dmpAudience) {
        return dmpSelectorDao.countDMPSelectorsForDMPAudience(dmpAudience);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getDMPSelectorsForDMPAudience(dmpAudience, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Sorting sort, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getDMPSelectorsForDMPAudience(dmpAudience, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Pagination page, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getDMPSelectorsForDMPAudience(dmpAudience, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DMPSelector getDMPSelectorByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId, FetchStrategy... fetchStrategy) {
        return dmpSelectorDao.getByExternalIdAndDmpVendorId(externalId, dmpVendorId, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------
    // FirstPartyAudience
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public FirstPartyAudience getFirstPartyAudienceById(String id, FetchStrategy... fetchStrategy) {
        return this.getFirstPartyAudienceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public FirstPartyAudience getFirstPartyAudienceById(Long id, FetchStrategy... fetchStrategy) {
        return firstPartyAudienceDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public FirstPartyAudience create(FirstPartyAudience firstPartyAudience) {
        return firstPartyAudienceDao.create(firstPartyAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public FirstPartyAudience update(FirstPartyAudience firstPartyAudience) {
        FirstPartyAudience dbFirstPartyAudience;
        if (isPersisted(firstPartyAudience)){
            dbFirstPartyAudience = firstPartyAudienceDao.update(firstPartyAudience);
        }else{
            dbFirstPartyAudience = create(firstPartyAudience);
        }
        return dbFirstPartyAudience;
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(FirstPartyAudience firstPartyAudience) {
        firstPartyAudienceDao.delete(firstPartyAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteFirstPartyAudiences(List<FirstPartyAudience> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (FirstPartyAudience entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------
    // FirstPartyAudienceDeviceIdsUploadHistory
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=false)
    public FirstPartyAudienceDeviceIdsUploadHistory newFirstPartyAudienceDeviceIdsUploadHistory(
            FirstPartyAudience firstPartyAudience,
            String filename,
            DeviceIdentifierType deviceIdentifierType,
            Long totalNumRecords,
            Long numValidatedRecords,
            Long numInsertedRecords) {
        FirstPartyAudienceDeviceIdsUploadHistory obj = new FirstPartyAudienceDeviceIdsUploadHistory(
                firstPartyAudience,
                filename,
                deviceIdentifierType,
                totalNumRecords,
                numValidatedRecords,
                numInsertedRecords);
        return firstPartyAudienceDeviceIdsUploadHistoryDao.create(obj);
    }

    @Override
    @Transactional(readOnly=true)
    public FirstPartyAudienceDeviceIdsUploadHistory getFirstPartyAudienceDeviceIdsUploadHistoryById(String id, FetchStrategy... fetchStrategy) {
        return this.getFirstPartyAudienceDeviceIdsUploadHistoryById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public FirstPartyAudienceDeviceIdsUploadHistory getFirstPartyAudienceDeviceIdsUploadHistoryById(Long id, FetchStrategy... fetchStrategy) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public FirstPartyAudienceDeviceIdsUploadHistory create(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.create(firstPartyAudienceDeviceIdsUploadHistory);
    }

    @Override
    @Transactional(readOnly=false)
    public FirstPartyAudienceDeviceIdsUploadHistory update(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.update(firstPartyAudienceDeviceIdsUploadHistory);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(FirstPartyAudienceDeviceIdsUploadHistory firstPartyAudienceDeviceIdsUploadHistory) {
        firstPartyAudienceDeviceIdsUploadHistoryDao.delete(firstPartyAudienceDeviceIdsUploadHistory);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteFirstPartyAudienceDeviceIdsUploadHistories(List<FirstPartyAudienceDeviceIdsUploadHistory> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (FirstPartyAudienceDeviceIdsUploadHistory entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, FetchStrategy ... fetchStrategy) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAllFirstPartyAudienceDeviceIdsUploadHistories(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return firstPartyAudienceDeviceIdsUploadHistoryDao.getAll(filter, sort, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------
    // CampaignAudience
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public CampaignAudience getCampaignAudienceById(String id, FetchStrategy... fetchStrategy) {
        return this.getCampaignAudienceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public CampaignAudience getCampaignAudienceById(Long id, FetchStrategy... fetchStrategy) {
        return campaignAudienceDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignAudience create(CampaignAudience campaignAudience) {
        return campaignAudienceDao.create(campaignAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignAudience update(CampaignAudience campaignAudience) {
        return campaignAudienceDao.update(campaignAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CampaignAudience campaignAudience) {
        campaignAudienceDao.delete(campaignAudience);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteCampaignAudiences(List<CampaignAudience> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CampaignAudience entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countCampaignAudiences(CampaignAudienceFilter filter) {
        return campaignAudienceDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, FetchStrategy... fetchStrategy) {
        return campaignAudienceDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignAudienceDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignAudience> getCampaignAudiences(CampaignAudienceFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignAudienceDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public Campaign updateCampaignAudiences(Campaign campaign, Set<CampaignAudience> newCampaignAudiences){
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        FeeManager feeManager = AdfonicBeanDispatcher.getBean(FeeManager.class);
        // Calculate Campaign Data Fee
        DataFeeCalculationResult dataFeeCalculationResult = feeManager.calculateCampaignAudiencesDataFee(newCampaignAudiences);

        return updateCampaignAudiences(campaignManager, feeManager, campaign, newCampaignAudiences, dataFeeCalculationResult);
    }

    private Campaign updateCampaignAudiences(CampaignManager campaignManager, FeeManager feeManager, Campaign campaign, Set<CampaignAudience> newCampaignAudiences, DataFeeCalculationResult dataFeeCalculationResult){
        // Catching old campaign audiences
        Campaign persistedCampaign = campaignManager.getCampaignById(campaign.getId());

        Set<CampaignAudience> currentCampaignAudiences = new HashSet<>(persistedCampaign.getCampaignAudiences());
        Set<CampaignAudience> campaignAudiencesToDelete = new HashSet<CampaignAudience> (currentCampaignAudiences);
        
        // Taking current time
        Date currentTime = new Date();
        
        if  ((persistedCampaign.getCurrentDataFee()==null) ||
            ((persistedCampaign.getCurrentDataFee()!=null) && (persistedCampaign.getCurrentDataFee().getDataFee().compareTo(dataFeeCalculationResult.getDataFee())!=0))){
            // Creating a new CampaignDataFee entity
            persistedCampaign = createNewCampaignDataFee(feeManager, persistedCampaign, currentTime, dataFeeCalculationResult);
        }
        
        // Clearing all current campaign audiences from Campaign
        persistedCampaign.getCampaignAudiences().clear();
        
        // Iterating the new/update campaign audiences
        for (CampaignAudience newCampaignAudience : newCampaignAudiences){
            //Calculate datafee for this audience
            AudiencePrices audiencePrices = feeManager.calculateAudienceDataFee(newCampaignAudience.getAudience());

            // Creating the new AudienceDataFee entity
            AudienceDataFee newAudienceDataFee = null;
            
            // Setting the campaign
            newCampaignAudience.setCampaign(persistedCampaign);
            
            // Look up the audience
            CampaignAudience currentCampaignAudience = searchCampaignAudience(currentCampaignAudiences, newCampaignAudience);
            
            if (currentCampaignAudience!=null){ // Exist (update)
                // Creating new  AudienceDataFee
                AudienceDataFee currentAudienceDataFee = currentCampaignAudience.getAudienceDataFee();
                
                newAudienceDataFee = updateAudienceDataFee(currentAudienceDataFee, 
                                                           audiencePrices, 
                                                           currentCampaignAudience, 
                                                           persistedCampaign.getCurrentDataFee(), 
                                                           currentTime);

                // Update include and Recency values
                currentCampaignAudience.setInclude(newCampaignAudience.isInclude());
                currentCampaignAudience.setRecencyDateFrom(newCampaignAudience.getRecencyDateFrom());
                currentCampaignAudience.setRecencyDateTo(newCampaignAudience.getRecencyDateTo());
                currentCampaignAudience.setRecencyDaysFrom(newCampaignAudience.getRecencyDaysFrom());
                currentCampaignAudience.setRecencyDaysTo(newCampaignAudience.getRecencyDaysTo());

                // Updating current AudienceDataFee in CampaignAudience
                if (newAudienceDataFee!=null){
                    currentCampaignAudience.setAudienceDataFee(newAudienceDataFee);
                }
            
                currentCampaignAudience = update(currentCampaignAudience);

                // Adding the campaign audience to the campaign
                persistedCampaign.getCampaignAudiences().add(currentCampaignAudience);

                // Removing current campaign audience from to delete CAs set
                campaignAudiencesToDelete.remove(currentCampaignAudience);
            }else{ // Does not Exist (create new one)
                // Creating Audience Data Retail information
                newAudienceDataFee = new AudienceDataFee();
                newAudienceDataFee.setCampaignDataFee(persistedCampaign.getCurrentDataFee());
                newAudienceDataFee.setStartTime(currentTime);
                newAudienceDataFee.setDataRetail(audiencePrices.getDataRetail());
                newAudienceDataFee.setDataWholesale(audiencePrices.getDataWholesale());
                newAudienceDataFee = create(newAudienceDataFee);

                // Creating new Campaign Audience
                newCampaignAudience.setId(0L);
                newCampaignAudience.setCampaign(persistedCampaign);
                newCampaignAudience.setDeleted(false);
                newCampaignAudience.setAudienceDataFee(newAudienceDataFee);
                newCampaignAudience = create(newCampaignAudience);

                // Assigning CampaignAudience to AudienceDataFee (to avoid circular dependencies)
                newAudienceDataFee.setCampaignAudience(newCampaignAudience);
                newAudienceDataFee = update(newAudienceDataFee);

                // Adding this new CampaignAudience for include it in Campaign entity
                persistedCampaign.getCampaignAudiences().add(newCampaignAudience);
            }
            
            // Updating is_maximum_per_vendor flag
            if (newAudienceDataFee!=null){
                updateMaximumPerVendorFlag(newCampaignAudience, dataFeeCalculationResult.getDataFeePerVendor(), newAudienceDataFee);
            }
        }
        
        // Deleting campaigns audiences not used by campaign anymore
        for (CampaignAudience campaignAudienceToDelete : campaignAudiencesToDelete){
            AudienceDataFee audienceDataFee = campaignAudienceToDelete.getAudienceDataFee();
            if (audienceDataFee!=null){
                audienceDataFee.setEndTime(currentTime);
                update(audienceDataFee);
            }
        }

        // Updating relationship between CampaignAudience and Campaign
        return campaignManager.update(persistedCampaign);
    }

    private AudienceDataFee updateAudienceDataFee(AudienceDataFee currentAudienceDataFee, 
                                                  AudiencePrices audiencePrices, 
                                                  CampaignAudience currentCampaignAudience,
                                                  CampaignDataFee campaignDataFee, 
                                                  Date currentTime) {
        AudienceDataFee localNewAudienceDataFee = null;
        if (audienceDataFeeHasChange(currentAudienceDataFee, audiencePrices)){
            // Updating endDate in current AudienceDataFee
            if (currentAudienceDataFee != null){
                currentAudienceDataFee.setEndTime(currentTime);
                update(currentAudienceDataFee);
            }
   
            // Creating a new AudienceDataFee for the new price
            localNewAudienceDataFee = new AudienceDataFee();
            localNewAudienceDataFee.setCampaignAudience(currentCampaignAudience);
            localNewAudienceDataFee.setCampaignDataFee(campaignDataFee);
            localNewAudienceDataFee.setStartTime(currentTime);
            localNewAudienceDataFee.setDataRetail(audiencePrices.getDataRetail());
            localNewAudienceDataFee.setDataWholesale(audiencePrices.getDataWholesale());
            localNewAudienceDataFee = create(localNewAudienceDataFee);
        }
        return localNewAudienceDataFee;
    }


    private boolean audienceDataFeeHasChange(AudienceDataFee currentAudienceDataFee, AudiencePrices audiencePrices) {
        return (currentAudienceDataFee.getDataRetail().compareTo(audiencePrices.getDataRetail())!=0) || 
               (currentAudienceDataFee.getDataWholesale().compareTo(audiencePrices.getDataWholesale())!=0);
    }

    private Campaign updateCampaignAudiences(CampaignManager campaignManager, FeeManager feeManager, Campaign campaign, Audience audience){
        // Calculate Campaign Data Fee
        DataFeeCalculationResult dataFeeCalculationResult = feeManager.calculateCampaignAudiencesDataFee(campaign.getCampaignAudiences());

        if (campaign.getCurrentDataFee().getDataFee().compareTo(dataFeeCalculationResult.getDataFee())!=0){
            updateCampaignAudiences(campaignManager, feeManager, campaign, new HashSet<CampaignAudience>(campaign.getCampaignAudiences()), dataFeeCalculationResult);
        }else{
            // Taking current time
            Date currentTime = new Date();

            // Calculate datafee for this audience
            AudiencePrices audiencePrices = feeManager.calculateAudienceDataFee(audience);

            // Look up the old campaign audience pointing to the audience
            CampaignAudienceFilter filter = new CampaignAudienceFilter();
            filter.setAudience(audience);
            filter.setCampaign(campaign);
            filter.setDeleted(false);
            List<CampaignAudience> campaignAudiences = getCampaignAudiences(filter);

            for (CampaignAudience currentCampaignAudience : campaignAudiences){
                AudienceDataFee currentAudienceDataFee = currentCampaignAudience.getAudienceDataFee();

                if (currentAudienceDataFee.getDataRetail().compareTo(audiencePrices.getDataRetail())!=0){
                    // Updating endDate in current AudienceDataFee
                    currentAudienceDataFee.setEndTime(currentTime);
                    currentAudienceDataFee = update(currentAudienceDataFee);

                    // Creating a new AudienceDataFee for the new price
                    AudienceDataFee newAudienceDataFee = new AudienceDataFee();
                    newAudienceDataFee.setCampaignAudience(currentCampaignAudience);
                    newAudienceDataFee.setCampaignDataFee(campaign.getCurrentDataFee());
                    newAudienceDataFee.setStartTime(currentTime);
                    newAudienceDataFee.setDataRetail(audiencePrices.getDataRetail());
                    newAudienceDataFee.setDataWholesale(audiencePrices.getDataWholesale());
                    newAudienceDataFee.setMaximumForVendor(false);
                    newAudienceDataFee = create(newAudienceDataFee);

                    // Updating current AudienceDataFee in CampaignAudience
                    currentCampaignAudience.setAudienceDataFee(newAudienceDataFee);
                    currentCampaignAudience = update(currentCampaignAudience);
                }
            }

            updateMaximumPerVendorFlag(campaign, dataFeeCalculationResult);
        }

        return campaignManager.getCampaignById(campaign.getId());
    }
    
    private CampaignAudience searchCampaignAudience(Set<CampaignAudience> campaignAudiences, CampaignAudience ca){
        CampaignAudience result = null;
        for (CampaignAudience element : campaignAudiences){
            if (element.equals(ca)){
                result = element;
                break;
            }
        }
        return result;
    }

    private Campaign createNewCampaignDataFee(FeeManager feeManager, Campaign campaign, Date currentTime, DataFeeCalculationResult dataFeeCalculationResult){

        // Updating the old one (endDate=currentDate)
        CampaignDataFee currentCampaignDataFee = campaign.getCurrentDataFee();
        if ((currentCampaignDataFee!=null)&&(currentCampaignDataFee.getEndDate()==null)){
            currentCampaignDataFee.setEndDate(currentTime);
            feeManager.update(currentCampaignDataFee);
        }

        if (dataFeeCalculationResult!=null){
            // Creating the new one (startDate=currentDate)
            CampaignDataFee newCampaignDataFee = new CampaignDataFee(campaign, currentTime, dataFeeCalculationResult.getDataFee());
            newCampaignDataFee = feeManager.create(newCampaignDataFee);

            // Setting new one as current data fee
            campaign.setCurrentDataFee(newCampaignDataFee);
        }

        return campaign;
    }

    private void updateMaximumPerVendorFlag(CampaignAudience campaignAudience, Map<DMPVendor, BigDecimal> dataFeePerVendor, AudienceDataFee audienceDataFee) {
        DMPAudience currentDMPAudience = campaignAudience.getAudience().getDmpAudience();
        if (currentDMPAudience !=null){
            DMPVendor currentDMPVendor = currentDMPAudience.getDmpVendor();
            BigDecimal vendorMaxDataRetail = dataFeePerVendor.get(currentDMPVendor);
            if (audienceDataFee.getDataRetail()==vendorMaxDataRetail){
                audienceDataFee.setMaximumForVendor(true);
                update(audienceDataFee);
                dataFeePerVendor.remove(currentDMPVendor);
            }
        }
    }

    private void updateMaximumPerVendorFlag(Campaign campaign, DataFeeCalculationResult dataFeeCalculationResult) {
        Set<CampaignAudience> campaignAudiences = campaign.getCampaignAudiences();
        Map<DMPVendor, BigDecimal> dataFeePerVendor = new HashMap<DMPVendor, BigDecimal>(dataFeeCalculationResult.getDataFeePerVendor());
        for (CampaignAudience campaignAudience : campaignAudiences){
            updateMaximumPerVendorFlag(campaignAudience, dataFeePerVendor, campaignAudience.getAudienceDataFee());
        }
    }

    //------------------------------------------------------------------------------------------
    // AudienceDataFee  (historical table for CampaingAudiences prices)
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public AudienceDataFee getAudienceDataFeeById(Long id, FetchStrategy... fetchStrategy){
        return audienceDataFeeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public AudienceDataFee getAudienceDataFeeById(String id, FetchStrategy... fetchStrategy){
        return this.getAudienceDataFeeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public AudienceDataFee create(AudienceDataFee audienceDataFee){
        return audienceDataFeeDao.create(audienceDataFee);
    }

    @Override
    @Transactional(readOnly=false)
    public AudienceDataFee update(AudienceDataFee audienceDataFee){
        return audienceDataFeeDao.update(audienceDataFee);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(AudienceDataFee audienceDataFee){
        audienceDataFeeDao.delete(audienceDataFee);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(List<AudienceDataFee> list){
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AudienceDataFee entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAll(AudienceDataFeeFilter filter){
        return audienceDataFeeDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, FetchStrategy ... fetchStrategy){
        return audienceDataFeeDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy){
        return audienceDataFeeDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AudienceDataFee> getAll(AudienceDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy){
        return audienceDataFeeDao.getAll(filter, sort, fetchStrategy);
    }

}
