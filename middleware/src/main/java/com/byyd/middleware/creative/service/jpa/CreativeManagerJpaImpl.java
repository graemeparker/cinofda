package com.byyd.middleware.creative.service.jpa;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.adx.client.AdXClient;
import com.adfonic.adx.client.AdXClient.Outcome;
import com.adfonic.adx.client.AdXClientException;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignNotificationFlag;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Component;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative.Status;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.CreativeAttribute;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.CreativeRemovedPublicationHistory;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment;
import com.adfonic.domain.User;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.dao.BeaconUrlDao;
import com.byyd.middleware.creative.dao.CreativeAttributeDao;
import com.byyd.middleware.creative.dao.CreativeDao;
import com.byyd.middleware.creative.dao.CreativeHistoryDao;
import com.byyd.middleware.creative.dao.CreativeRemovedPublicationHistoryDao;
import com.byyd.middleware.creative.dao.DestinationDao;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.filter.CreativeStateSyncingFilter;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.creative.service.ExtendedCreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("creativeManager")
public class CreativeManagerJpaImpl extends BaseJpaManagerImpl implements CreativeManager {
    
    private static final transient Logger LOG = Logger.getLogger(CreativeManagerJpaImpl.class.getName());
    
    /**
     * Seems to be the same sorting for all kinds of creatives queries. If the DaoJpaImpl is modified to use something else than
     * native SQL, another version will need to be added which will use the "higher level" field names abstraction.
     * @return
     */
    private static final Sorting SORTING_NATIVE_SQL_CREATIVES_FOR_PUBLICATION = new Sorting(SortOrder.desc("CAMPAIGN.START_DATE"),
                                                                                            SortOrder.asc("CAMPAIGN.NAME"),
                                                                                            SortOrder.asc("CREATIVE.NAME"));
    
    @Autowired(required = false)
    private CreativeDao creativeDao;
    
    @Autowired(required = false)
    private CreativeRemovedPublicationHistoryDao creativeRemovedPublicationHistoryDao;
    
    @Autowired(required = false)
    private CreativeHistoryDao creativeHistoryDao;
    
    @Autowired(required = false)
    private DestinationDao destinationDao;
    
    @Autowired(required = false)
    private BeaconUrlDao beaconUrlDao;
    
    @Autowired(required = false)
    private CreativeAttributeDao creativeAttributeDao;
    
    @Autowired(required=false)
    private AdXClient adXClient;
    
    // ------------------------------------------------------------------------------------------
    // Creative
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Creative newCreative(Campaign campaign, Segment segment, Format format, String name, FetchStrategy... fetchStrategy) {
        Creative creative = campaign.makeNewCreative(segment, format);
        creative.setName(name);
        creative = create(creative);
        try {
            campaign.getCreatives().add(creative);
        } catch(Exception e) {
            // Ignore this, it just means the creatives are not hydrated on the campaign
        }
        
        if (fetchStrategy != null && fetchStrategy.length != 0) {
            creative = getCreativeById(creative.getId(), fetchStrategy);
        }
        return creative;
    }

    @Override
    @Transactional(readOnly = false)
    public Creative newCreative(Creative creative, FetchStrategy... fetchStrategy) {
        creative = create(creative);
        try {
            creative.getCampaign().getCreatives().add(creative);
        } catch(Exception e) {
            // Ignore this, it just means the creatives are not hydrated on the campaign
        }
        
        if (fetchStrategy != null && fetchStrategy.length != 0) {
            creative = getCreativeById(creative.getId(), fetchStrategy);
        }
        return creative;

    }

    @Override
    public void syncStates(Creative creative, Creative newCreative, CreativeStateSyncingFilter params) {
        if(params.getSyncName()) {
            newCreative.setName(creative.getName());
        }

        // Destinations are shared / immutable
        newCreative.setDestination(creative.getDestination());

        newCreative.setLanguage(creative.getLanguage());
        newCreative.setEnglishTranslation(creative.getEnglishTranslation());

        if(params.getSyncDateUpdated()) {
            newCreative.setLastUpdated(creative.getLastUpdated());
        } else {
            newCreative.setLastUpdated(new Date());
        }

        newCreative.setPluginBased(creative.isPluginBased());
        newCreative.setPriority(creative.getPriority());
        newCreative.setEndDate(creative.getEndDate());

        newCreative.setExtendedCreativeType(creative.getExtendedCreativeType());

        if(!CollectionUtils.isEmpty(creative.getExtendedCreativeTemplates()) && newCreative.getExtendedCreativeTemplates()==null){
            creative.setExtendedCreativeTemplates(new HashSet<ExtendedCreativeTemplate>());
        }

        for(ExtendedCreativeTemplate ect : creative.getExtendedCreativeTemplates()){
            ExtendedCreativeManager extendedCreativeManager = AdfonicBeanDispatcher.getBean(ExtendedCreativeManager.class);
            ExtendedCreativeTemplate template = extendedCreativeManager.newExtendedCreativeTemplate(newCreative, ect.getContentForm(), ect.getTemplateOriginal());
            newCreative.getExtendedCreativeTemplates().add(template);
        }

        newCreative.getExtendedData().clear();
        newCreative.getExtendedData().putAll(creative.getExtendedData());
        newCreative.setAllowExternalAudit(creative.isAllowExternalAudit());
        // MAD-1668 - Closed Mode Change
        // Closed mode true only in case of tag based creative
        if (creative.getExtendedCreativeType() != null) {
            newCreative.setClosedMode(true);
        } else {
            newCreative.setClosedMode(false);
        }
        
        // SSL Compliance
        newCreative.setSslCompliant(creative.isSslCompliant());
        newCreative.setSslOverride(creative.isSslOverride());
    }

    @Override
    @Transactional(readOnly = false)
    public Creative copyCreative(Creative creative, Campaign destinationCampaign, Segment destinationSegment, FetchStrategy... fetchStrategy) {
        return this.copyCreative(creative, destinationCampaign, destinationSegment, false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Creative copyCreative(Creative creative, Campaign destinationCampaign, Segment destinationSegment, boolean copyRemovedPublications, FetchStrategy... fetchStrategy) {
        creative = this.getCreativeById(creative.getId());

        Creative newCreative = newCreative(destinationCampaign, destinationSegment, creative.getFormat(), creative.getName());
        syncStates(creative, newCreative, CreativeStateSyncingFilter.FOR_NEW_INSTANCE);

        // Copy assets
        AssetManager assetManager = AdfonicBeanDispatcher.getBean(AssetManager.class);
        for (DisplayType dt : creative.getAssetBundleMap().keySet()) {
            AssetBundle ab = creative.getAssetBundle(dt);
            AssetBundle newBundle = assetManager.newAssetBundle(newCreative, dt);
            for (Component c : ab.getAssetMap().keySet()) {
                Asset oldAsset = ab.getAsset(c);
                Asset newAsset = assetManager.newAsset(newCreative, oldAsset.getContentType(), oldAsset.getData());
                newAsset.setData(oldAsset.getData());
                newBundle.putAsset(c, newAsset);
            }
            newBundle = assetManager.update(newBundle);
        }

        // Copy attributes
        newCreative.setCreativeAttributes(new HashSet<CreativeAttribute>());
        if(CollectionUtils.isNotEmpty(creative.getCreativeAttributes())){
            newCreative.getCreativeAttributes().addAll(creative.getCreativeAttributes());
        }

        if (copyRemovedPublications) {
            CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
            newCreative.getRemovedPublications().clear();
            for (Map.Entry<Publication,RemovalInfo> entry : creative.getRemovedPublications().entrySet()) {
                // We need to "clone" the RemovalInfo values from the other creative
                RemovalInfo removalInfo = new RemovalInfo(entry.getValue());
                removalInfo = commonManager.create(removalInfo);
                newCreative.getRemovedPublications().put(entry.getKey(), removalInfo);
            }
        }

        newCreative = update(newCreative);
        return getCreativeById(newCreative.getId(), fetchStrategy);
    }


    @Override
    @Transactional(readOnly = true)
    public Creative getCreativeById(String id, FetchStrategy... fetchStrategy) {
        return getCreativeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Creative getCreativeById(Long id, FetchStrategy... fetchStrategy) {
        return creativeDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<Creative> getCreativesById(List<Long> ids, FetchStrategy... fetchStrategy) {
        return this.getCreativesByIdsAsList(ids, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Creative create(Creative creative) {
        return creativeDao.create(creative);
    }

    @Override
    @Transactional(readOnly = false)
    public Creative update(Creative creative) {
        return creativeDao.update(creative);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Creative creative) {
        creativeDao.delete(creative);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCreatives(List<Creative> list) {
        if (list != null && !list.isEmpty()) {
            for (Creative entry : list) {
                delete(entry);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Creative getCreativeByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return creativeDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Creative getCreativeByIdOrExternalId(String key, FetchStrategy... fetchStrategy) {
        Creative creative = getCreativeByExternalId(key, fetchStrategy);
        if (creative == null) {
            creative = getCreativeById(key, fetchStrategy);
        }
        return creative;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCreatives(CreativeFilter filter) {
        return creativeDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreatives(CreativeFilter filter, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreatives(CreativeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreatives(CreativeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllCreativesForCampaign(Campaign campaign) {
        return creativeDao.countAll(new CreativeFilter().setCampaign(campaign));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreativesForCampaign(Campaign campaign, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(new CreativeFilter().setCampaign(campaign), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreativesForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(new CreativeFilter().setCampaign(campaign), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreativesForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(new CreativeFilter().setCampaign(campaign), page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getAllCreativesForCampaignIds(Collection<Long> campaignIds, FetchStrategy... fetchStrategy) {
        return creativeDao.getAll(new CreativeFilter().setCampaignIds(campaignIds), fetchStrategy);
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countPendingCreativesForPublication(Publication publication) {
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getPendingCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return new ArrayList<Creative>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getPendingCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
        return new ArrayList<Creative>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getPendingCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
        return new ArrayList<Creative>();
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countApprovedCreativesForPublication(Publication publication) {
        Long count = 0L;
        if(!publication.isAutoApproval()) {
            count = creativeDao.countApprovedCreativesForPublication(publication);
        }
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getApprovedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        List<Creative> creatives;
        if(publication.isAutoApproval()) {
            creatives = this.getApprovedCreativesForPublication(publication, SORTING_NATIVE_SQL_CREATIVES_FOR_PUBLICATION, fetchStrategy);
        } else {
            creatives = creativeDao.getApprovedCreativesForPublication(publication, fetchStrategy);
        }
        return creatives;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getApprovedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
       List<Creative> creatives;
       if(publication.isAutoApproval()) {
           creatives = new ArrayList<Creative>();
       } else {
           creatives = creativeDao.getApprovedCreativesForPublication(publication, sort, fetchStrategy);
       }
       return creatives;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getApprovedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
        if(page.getSorting() == null) {
            page = new Pagination(page, SORTING_NATIVE_SQL_CREATIVES_FOR_PUBLICATION);
        }
        List<Creative> creatives;
        if(publication.isAutoApproval()) {
            creatives = new ArrayList<Creative>();
        } else {
            creatives = creativeDao.getApprovedCreativesForPublication(publication, page, fetchStrategy);
        }
        return creatives;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countDeniedCreativesForPublication(Publication publication) {
        return creativeDao.countDeniedCreativesForPublication(publication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getDeniedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return creativeDao.getDeniedCreativesForPublication(publication, SORTING_NATIVE_SQL_CREATIVES_FOR_PUBLICATION, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getDeniedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
       return creativeDao.getDeniedCreativesForPublication(publication, sort, fetchStrategy);
     }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getDeniedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
        if(page.getSorting() == null) {
            page = new Pagination(page, SORTING_NATIVE_SQL_CREATIVES_FOR_PUBLICATION);
        }
        return creativeDao.getDeniedCreativesForPublication(publication, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Creative removePublicationFromCreative(Creative creative, Publication publication, RemovalInfo.RemovalType removalType) {
        return removePublicationFromCreative(creative, publication, removalType, null, null);
    }

    @Override
    @Transactional(readOnly = false)
    public Creative removePublicationFromCreative(
            Creative creative,
            Publication publication,
            RemovalInfo.RemovalType removalType,
            User user,
            AdfonicUser adfonicUser) {
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        
        // Reload the creative beforehand to make sure we have the latest removal info
        creative = this.getCreativeById(creative.getId());
        RemovalInfo removalInfo = creative.getRemovedPublications().get(publication);
        if (removalInfo != null) {
            if (removalInfo.getRemovalType().equals(removalType)) {
                // It's already set up as removed (or unremoved) with the given removalType.
                // Silently do nothing...
                return creative;
            } else {
                // The removal type is changing.
                creative.getRemovedPublications().remove(publication);
                // ...and fall through to the code below, which will add the replacement.
            }
        }

        removalInfo = new RemovalInfo(removalType);
        removalInfo = commonManager.create(removalInfo);

        creative.getRemovedPublications().put(publication, removalInfo);

        creative = update(creative);

        // Create a History row
        CreativeRemovedPublicationHistory history = new CreativeRemovedPublicationHistory();
        history.setCreative(creative);
        history.setPublication(publication);
        history.setRemovalType(removalType);
        history.setRemovalTime(removalInfo.getRemovalTime());
        history.setAdfonicUser(adfonicUser);
        history.setUser(user);

        this.creativeRemovedPublicationHistoryDao.create(history);

        return creative;
    }

    @Override
    @Transactional(readOnly = false)
    public Creative unremovePublicationFromCreative(Creative creative, Publication publication) {
        return this.unremovePublicationFromCreative(creative, publication, null, null);
    }

    @Override
    @Transactional(readOnly = false)
    public Creative unremovePublicationFromCreative(
            Creative creative,
            Publication publication,
            User user,
            AdfonicUser adfonicUser) {
        return this.removePublicationFromCreative(creative, publication, RemovalInfo.RemovalType.UNREMOVED, user, adfonicUser);
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = false)
    public boolean stopCreative(Creative creative) {
        boolean stopped = false;
        Creative.Status status = creative.getStatus();
        if (status == Status.PENDING || status == Status.ACTIVE || status == Status.PAUSED || status == Status.PENDING_PAUSED) {
            creative.setStatus(Status.STOPPED);
            update(creative);
            stopped = true;
        }
        return stopped;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean startCreative(Creative creative) {
        boolean started = false;
        Creative.Status status = creative.getStatus();
        if (status == Status.PAUSED) {
            creative.setStatus(Status.ACTIVE);
            update(creative);
            started = true;
        } else if (status == Status.PENDING_PAUSED) {
            creative.setStatus(Status.PENDING);
            update(creative);
            started = true;
        }
        return started;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean pauseCreative(Creative creative) {
        boolean paused = false;
        Creative.Status status = creative.getStatus();
        if (status == Status.PENDING) {
            creative.setStatus(Status.PENDING_PAUSED);
            update(creative);
            paused = true;
        } else if (status == Status.ACTIVE) {
            creative.setStatus(Status.PAUSED);
            update(creative);
            paused = true;
        }
        return paused;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = false)
    public boolean stopCreatives(List<Creative> creatives) {
        boolean rc = true;
        if(creatives == null || creatives.isEmpty()) {
            rc = false;
        }else{
            for(Creative c : creatives) {
                if(!stopCreative(c)) {
                    rc = false;
                }
            }
        }
        return rc;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean startCreatives(List<Creative> creatives) {
        boolean rc = true;
        if(creatives == null || creatives.isEmpty()) {
            rc = false;
        }else{        
            for(Creative c : creatives) {
                if(!startCreative(c)) {
                    rc = false;
                }
            }
        }
        return rc;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean pauseCreatives(List<Creative> creatives) {
        boolean rc = true;
        
        if(creatives == null || creatives.isEmpty()) {
            rc = false;
        }else{
            for(Creative c : creatives) {
                if(!pauseCreative(c)) {
                    rc = false;
                }
            }
        }
        return rc;
    }


    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getCreativesEligibleForAdXReprovisioning(Campaign campaign, FetchStrategy... fetchStrategy) {
        CreativeFilter creativeFilter = new CreativeFilter();
        creativeFilter.setCampaign(campaign);
        creativeFilter.setStatuses(ADX_REPROVISION_CREATIVE_STATUSES);
        return getAllCreatives(creativeFilter,fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public List<Creative> updateCreativeStatusForAdXReprovisioning(Creative creative) {
        List<Creative> list = new ArrayList<>();
        list.add(creative);
        return updateCreativeStatusForAdXReprovisioning(list);
    }

    @Override
    @Transactional(readOnly = false)
    public List<Creative> updateCreativeStatusForAdXReprovisioning(List<Creative> creatives) {
        List<Creative> updatedCreatives = new ArrayList<>();
        for (Creative creative : creatives) {
            boolean updated = false;
            switch (creative.getStatus()) {
                case ACTIVE:
                    creative.setStatus(Creative.Status.PENDING);
                    updated = true;
                    break;
                case PAUSED:
                case STOPPED:
                    creative.setStatus(Creative.Status.PENDING_PAUSED);
                    updated = true;
                    break;
                default:
                    break;
            }

            if(updated) {
                try {
                    creative.setLastUpdated(new Date());
                    creative = update(creative);
                    updatedCreatives.add(creative);
                } catch (Exception e) {
                    LOG.log(
                            Level.INFO,
                            "Error updating creative status creative item id=" +
                            creative.getId() +
                            e);
                    continue;
                }
            }
        }
        return updatedCreatives;
    }

    @Override
    @Transactional(readOnly = false)
    public Creative submitCreative(Creative creative) {
        // AO-146 - track submission time
        creative.setSubmissionTime(new Date());
        creative = update(creative);

        return creative;
    }

    @Override
    @Transactional(readOnly = false)
    public List<Creative> resubmitAllCreativesNeedingAdXReprovisioning(Campaign campaign) {
        List<Creative> submittedCreatives = new ArrayList<>();
        // Sanity check
        if(campaign.isInstallTrackingAdXEnabled()) {
            List<Creative> creatives = getCreativesEligibleForAdXReprovisioning(campaign);
            List<Creative> updatedCreatives = updateCreativeStatusForAdXReprovisioning(creatives);
            
            for(Creative creative : updatedCreatives) {
                try {
                    submitCreative(creative);
                    submittedCreatives.add(creative);
                } catch(Exception e) {
                    LOG.log(Level.SEVERE, "Could not submit Creative id=" + creative.getId(), e);
                }
            }
        }
        return submittedCreatives;
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> updateCreativeStatusAsMap(Creative creative, Creative.Status newStatus) {
        Map<String, Object> rcMap = new HashMap<>();

        LOG.fine("Update status of " + creative.getExternalID() + " to " + newStatus.toString());
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        Campaign campaign = campaignManager.getCampaignById(creative.getCampaign().getId(), new FetchStrategyBuilder()
                                                                                        .addInner(Campaign_.advertiser)
                                                                                        .addInner(Advertiser_.company)
                                                                                        .build());

        if (newStatus == Creative.Status.ACTIVE){
            rcMap = updateActiveCreative(creative, newStatus, rcMap, campaignManager, campaign);
        } else if (newStatus == Creative.Status.REJECTED) {
            updateRejectedCreative(creative, newStatus, rcMap, campaign);
        }else{
            addUpdateErrorRCMap(creative, newStatus, rcMap);
        }
        
        return rcMap;
    }

    private Map<String, Object> updateActiveCreative(Creative creative, Creative.Status newStatus, Map<String, Object> rcMap, CampaignManager campaignManager, Campaign campaign) {
        
        // for adx must, provision successfully to go active
        if (campaign.isInstallTrackingAdXEnabled()) {
            if(adXClient == null) {
                String msg = "adXClient is null. Spring injection must have failed.";
                LOG.severe(msg);
                rcMap.put(CREATIVE_STATUS_UPDATE_STATUS, false);
                rcMap.put(CREATIVE_STATUS_UPDATE_ERROR_MESSAGE, msg);
                return rcMap;
            }
            DeviceManager deviceManager = AdfonicBeanDispatcher.getBean(DeviceManager.class);
            try {
                Outcome outcome = adXClient.provisionCreative(
                        campaign.getApplicationID(),
                        campaign.getAdvertiser().getExternalID(),
                        creative.getExternalID(),
                        deviceManager.isAppleOnly(creative.getSegment()) ? AdXClient.Platform.iOS : AdXClient.Platform.Android,
                        creative.getDestination().getData().toString());
                if (Outcome.UPDATED == outcome || Outcome.CREATED == outcome) {
                    // Worked fine, stuff the Outcome in the map and continue
                    rcMap.put(CREATIVE_STATUS_UPDATE_ADX_PROVISIONING_OUTCOME, outcome);
                }
            } catch (AdXClientException ace) {
                LOG.log(Level.INFO, "Error provisioning creative " + creative.getId(), ace);
                rcMap.put(CREATIVE_STATUS_UPDATE_STATUS, false);
                rcMap.put(CREATIVE_STATUS_UPDATE_ERROR_MESSAGE, ace.getMessage());
                return rcMap;
            }
        }

        if (creative.getStatus() == Status.PENDING || creative.getStatus() == Status.REJECTED) {
            creative.setStatus(Status.ACTIVE);
        } else if (creative.getStatus() == Status.PENDING_PAUSED) {
            creative.setStatus(Status.PAUSED);
        } else {
            addUpdateErrorRCMap(creative, newStatus, rcMap);
            return rcMap;
        }
        
        /**
         * When the first creative is approved, the campaign may transition status.
         * mustNotify will be set true if the campaign is now "currently active."  This is the case
         * if the campaign has transitioned to ACTIVE (i.e. it wasn't ACTIVE prior
         * to now) *and* the campaign is ready for use "right now" per the time
         * period constraints configured, if any.
         * In other words, set to true if the advertiser should be notified
         * immediately that the campaign "went live," otherwise it is set to false.
         */
        boolean mustNotify = false;
        if (campaign.getStatus() == Campaign.Status.PENDING) {
            campaign.setStatus(Campaign.Status.ACTIVE);
            campaign = campaignManager.update(campaign);
            mustNotify = true;
        } else if (campaign.getStatus() == Campaign.Status.PENDING_PAUSED) {
            campaign.setStatus(Campaign.Status.PAUSED);
            campaign = campaignManager.update(campaign);
        }

        if (mustNotify && campaign.isCurrentlyActive()) {
            // Campaign just went live
            // Create the NotificationFlag so it's not picked
            // up by the ScheduledCampaignLiveNotifier task.
            campaignManager.newCampaignNotificationFlag(campaign, CampaignNotificationFlag.Type.WENT_LIVE, null);
            createCompanyMessage(campaign, "campaign.live", campaign.getName(), null);
        }
        
        createCompanyMessage(campaign, "creative.approved", creative.getName(), campaign.getName());
        creative.setApprovedDate(new Date());
        update(creative);
        rcMap.put(CREATIVE_STATUS_UPDATE_STATUS, true);
        return rcMap;
    }
    
    private void updateRejectedCreative(Creative creative, Creative.Status status, Map<String, Object> rcMap, Campaign campaign) {
        if (creative.getStatus() == Status.PENDING || 
                creative.getStatus() == Status.PENDING_PAUSED ||
                creative.getStatus() == Status.ACTIVE ||
                creative.getStatus() == Status.PAUSED ||
                creative.getStatus() == Status.STOPPED) {
            
            creative.setStatus(Status.REJECTED);
            createCompanyMessage(campaign, "creative.rejected", creative.getName(), campaign.getName());
            update(creative);
            rcMap.put(CREATIVE_STATUS_UPDATE_STATUS, true);
        } else {
            addUpdateErrorRCMap(creative, status, rcMap);
        }
    }

    private void addUpdateErrorRCMap(Creative creative, Creative.Status status, Map<String, Object> rcMap) {
        rcMap.put(CREATIVE_STATUS_UPDATE_ERROR_MESSAGE, 
                  "Status change from " + creative.getStatus() + " to " + status + " is not supported");
        rcMap.put(CREATIVE_STATUS_UPDATE_STATUS, false);
    }
    
    private void createCompanyMessage(Campaign campaign, String systemName, String arg0, String arg1) {
        CompanyManager companyManager = AdfonicBeanDispatcher.getBean(CompanyManager.class);
        
        CompanyMessage cm = new CompanyMessage(campaign, systemName);
        cm.setArg0(arg0);
        cm.setArg1(arg1);
        
        companyManager.create(cm);
    }

    //------------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative) {
        if(excludeCreative != null && isPersisted(excludeCreative)) {
            return creativeDao.countCreativesWithNameForCampaign(name, campaign, excludeCreative);
        } else {
            return creativeDao.countCreativesWithNameForCampaign(name, campaign, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, FetchStrategy... fetchStrategy) {
        return creativeDao.getCreativesWithNameForCampaign(name, campaign, excludeCreative, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Sorting sort, FetchStrategy... fetchStrategy) {
        return creativeDao.getCreativesWithNameForCampaign(name, campaign, excludeCreative, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Pagination page, FetchStrategy... fetchStrategy) {
        return creativeDao.getCreativesWithNameForCampaign(name, campaign, excludeCreative, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCreativeNameUnique(String name, Campaign campaign, Creative excludeCreative) {
        Long count = this.countCreativesWithNameForCampaign(name, campaign, excludeCreative);
        return count < 1;
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public List<Creative> getCreativesByIdsAsList(Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Creative.class, ids, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Creative> getCreativesByIdsAsList(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Creative.class, ids, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Creative> getCreativesByIdsAsList(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Creative.class, ids, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Creative.class, ids, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Creative.class, ids, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Creative> getCreativesByIdsAsMap(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Creative.class, ids, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=false)
    public Publication denyCreativeForPublication(Publication publication, Creative creative) {
        creativeDao.denyCreativeForPublication(publication, creative);
        return publication;
    }

    @Override
    @Transactional(readOnly=false)
    public Publication approveCreativeForPublication(Publication publication, Creative creative) {
        creativeDao.approveCreativeForPublication(publication, creative);
        return publication;
    }


    // ------------------------------------------------------------------------------------------
    // CreativeHistory
    // ------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=false)
    public CreativeHistory newCreativeHistory(Creative creative, FetchStrategy ... fetchStrategy) {
        return newCreativeHistory(creative, null, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CreativeHistory newCreativeHistory(Creative creative, String comment, AdfonicUser adfonicUser, FetchStrategy ... fetchStrategy) {
        CreativeHistory creativeHistory = new CreativeHistory(creative);
        creativeHistory.setComment(comment);
        creativeHistory.setAdfonicUser(adfonicUser);
        creativeHistory = creativeHistoryDao.create(creativeHistory);
        try {
            creative.getHistory().add(creativeHistory);
        } catch(Exception e) {
            // Ignore this, it just means the creativeHistory isn't hydrated
        }
        
        if (fetchStrategy != null && fetchStrategy.length != 0) {
            creativeHistory = getCreativeHistoryById(creativeHistory.getId(), fetchStrategy);
        }
        return creativeHistory;
    }

    @Override
    @Transactional(readOnly = false)
    public CreativeHistory update(CreativeHistory creativeHistory) {
        return creativeHistoryDao.update(creativeHistory);
    }

    @Override
    @Transactional(readOnly=true)
    public CreativeHistory getCreativeHistoryById(long id, FetchStrategy ... fetchStrategy) {
        return creativeHistoryDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CreativeHistory> getCreativeHistory(Creative creative, FetchStrategy ... fetchStrategy) {
        return creativeHistoryDao.getAll(creative, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Destination
    // ------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly = true)
    public Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, FetchStrategy... fetchStrategy) {
        return newDestination(advertiser, destinationType, urlString, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, boolean dataIsFinalDestination, String finalDestination, FetchStrategy... fetchStrategy) {
        return newDestination(advertiser, destinationType, urlString, null, dataIsFinalDestination, finalDestination, fetchStrategy);
    }


    @Override
    @Transactional(readOnly = false)
    public Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy) {
        return this.newDestination(advertiser, destinationType, urlString, beacons, true, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Destination newDestination(Advertiser advertiser, DestinationType destinationType, String urlString, List<BeaconUrl> beacons, boolean dataIsFinalDestination, String finalDestination, FetchStrategy... fetchStrategy) {
        boolean destinationsSet = false;
        try {
            advertiser.getDestinations().size();
            destinationsSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!destinationsSet) {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Advertiser_.destinations)
                               .build();
            AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
            advertiser = advertiserManager.getAdvertiserById(advertiser.getId(), fs);
        }
        Destination destination = advertiser.newDestination(destinationType, urlString, beacons, dataIsFinalDestination, finalDestination);
        // We enforce consistency, no matter what is sent in
        if(StringUtils.isEmpty(destination.getFinalDestination())) {
            destination.setDataIsFinalDestination(true);
        } else {
            destination.setDataIsFinalDestination(false);
        }
        
        destination = create(destination);
        
        if(destination.getBeaconUrls()!=null){
            for(BeaconUrl beacon : destination.getBeaconUrls()){
                create(beacon);
            }
        }
        
        if(fetchStrategy != null && fetchStrategy.length != 0) {
            destination = getDestinationById(destination.getId(), fetchStrategy);            
        }
        
        return destination;
    }

    @Override
    @Transactional(readOnly = true)
    public Destination getDestinationById(String id, FetchStrategy... fetchStrategy) {
        return getDestinationById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Destination getDestinationById(Long id, FetchStrategy... fetchStrategy) {
        return destinationDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Destination create(Destination destination) {
        return destinationDao.create(destination);
    }

    @Override
    @Transactional(readOnly = false)
    public Destination update(Destination destination) {
        // We enforce consistency, no matter what is sent in
        if(StringUtils.isEmpty(destination.getFinalDestination())) {
            destination.setDataIsFinalDestination(true);
        } else {
            destination.setDataIsFinalDestination(false);
        }
        return destinationDao.update(destination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BeaconUrl getBeaconById(Long id, FetchStrategy... fetchStrategy) {
        return beaconUrlDao.getById(id, fetchStrategy);
    }
    
    @Transactional(readOnly = false)
    public BeaconUrl create(BeaconUrl beaconUrl) {
        return beaconUrlDao.create(beaconUrl);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Destination destination) {
        //first delete the beacons associated
        if(destination.getBeaconUrls()!=null){
            for(BeaconUrl beacon : destination.getBeaconUrls()){
                beaconUrlDao.delete(beacon);
            }
        }
        
        destinationDao.delete(destination);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteDestinations(List<Destination> list) {
        if (list != null && !list.isEmpty()) {
            for (Destination entry : list) {
                delete(entry);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllDestinations(DestinationFilter filter) {
        return destinationDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Destination> getAllDestinations(DestinationFilter filter, FetchStrategy... fetchStrategy) {
        return destinationDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Destination> getAllDestinations(DestinationFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return destinationDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Destination> getAllDestinations(DestinationFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return destinationDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser,
            DestinationType destinationType,
            String data,
            FetchStrategy... fetchStrategy) {
        return getDestinationForAdvertiserAndDestinationTypeAndData(
                advertiser,
                destinationType,
                data,
                true,
                null,
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser,
            DestinationType destinationType,
            String data,
            List<BeaconUrl> beacons,
            FetchStrategy... fetchStrategy) {
        return getDestinationForAdvertiserAndDestinationTypeAndData(
                advertiser,
                destinationType,
                data,
                beacons,
                   true,
                null,
                fetchStrategy);
    }

    @Transactional(readOnly = true)
    public Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser,
            DestinationType destinationType,
            String data,
            List<BeaconUrl> beacons,
            boolean dataIsFinalDestination,
            String finalDestination,
            FetchStrategy... fetchStrategy) {
        return getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, false, beacons, dataIsFinalDestination, finalDestination, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser,
            DestinationType destinationType,
            String data,
            boolean createIfNotFound,
            List<BeaconUrl> beacons,
            FetchStrategy... fetchStrategy) {
        return getDestinationForAdvertiserAndDestinationTypeAndData(
                advertiser,
                destinationType,
                data,
                createIfNotFound,
                beacons,
                true,
                null,
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Destination getDestinationForAdvertiserAndDestinationTypeAndData(
            Advertiser advertiser,
            DestinationType destinationType,
            String data,
            boolean createIfNotFound,
            List<BeaconUrl> beacons,
            boolean dataIsFinalDestination,
            String finalDestination,
            FetchStrategy... fetchStrategy) {
        Destination destination = null;
        List<Destination> list = destinationDao.getForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, new Sorting(asc("id")), beacons, fetchStrategy);
        if(CollectionUtils.isNotEmpty(list)) {
            Destination destinationFromList = list.get(0);
            if(StringUtils.isEmpty(finalDestination)) {
                if(!StringUtils.isEmpty(destinationFromList.getFinalDestination())) {
                    destinationFromList.setDataIsFinalDestination(true);
                    destinationFromList.setFinalDestination(null);
                    destinationFromList = update(destinationFromList);
                }
            } else if(StringUtils.isEmpty(destinationFromList.getFinalDestination()) || !destinationFromList.getFinalDestination().equals(finalDestination)) {
                destinationFromList.setDataIsFinalDestination(false);
                destinationFromList.setFinalDestination(finalDestination);
                destinationFromList = update(destinationFromList);
            }
            destination = destinationFromList;
        }else if(createIfNotFound) {
            destination = this.newDestination(advertiser, destinationType, data, beacons, dataIsFinalDestination, finalDestination, fetchStrategy);
        }
        return destination;
    }

    
    // ------------------------------------------------------------------------------------------
    // CreativeAttribute
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public CreativeAttribute newCreativeAttribute(String name, FetchStrategy... fetchStrategy) {
        CreativeAttribute creativeAttribute = create(new CreativeAttribute(name));
        if (fetchStrategy != null && fetchStrategy.length != 0) {
            creativeAttribute = getCreativeAttributeById(creativeAttribute.getId(), fetchStrategy);
        }
        return creativeAttribute;
    }

    @Override
    @Transactional(readOnly = true)
    public CreativeAttribute getCreativeAttributeById(String id, FetchStrategy... fetchStrategy) {
        return getCreativeAttributeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CreativeAttribute getCreativeAttributeById(Long id, FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public CreativeAttribute create(CreativeAttribute creativeAttribute) {
        return creativeAttributeDao.create(creativeAttribute);
    }

    @Override
    @Transactional(readOnly = false)
    public CreativeAttribute update(CreativeAttribute creativeAttribute) {
        return creativeAttributeDao.update(creativeAttribute);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(CreativeAttribute creativeAttribute) {
        creativeAttributeDao.delete(creativeAttribute);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCreativeAttributes(List<CreativeAttribute> list) {
        if (list != null && !list.isEmpty()) {
            for (CreativeAttribute entry : list) {
                delete(entry);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CreativeAttribute getCreativeAttributeByName(String name, FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getByName(name, fetchStrategy);
    }

    @Transactional(readOnly = true)
    public CreativeAttribute getCreativeAttributeByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCreativeAttributes() {
        return creativeAttributeDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreativeAttribute> getAllCreativeAttributes(FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreativeAttribute> getAllCreativeAttributes(Sorting sort, FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreativeAttribute> getAllCreativeAttributes(Pagination page, FetchStrategy... fetchStrategy) {
        return creativeAttributeDao.getAll(page, fetchStrategy);
    }

}
