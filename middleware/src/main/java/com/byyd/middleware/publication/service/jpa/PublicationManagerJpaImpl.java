package com.byyd.middleware.publication.service.jpa;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DefaultRateCard;
import com.adfonic.domain.DefaultRateCard_;
import com.adfonic.domain.Format;
import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationApprovalDashboardView;
import com.adfonic.domain.PublicationBundle;
import com.adfonic.domain.PublicationHistory;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationProvidedInfo;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyJpaImpl;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.publication.dao.AdSpaceDao;
import com.byyd.middleware.publication.dao.DefaultRateCardDao;
import com.byyd.middleware.publication.dao.IntegrationTypeDao;
import com.byyd.middleware.publication.dao.PublicationApprovalDashboardViewDao;
import com.byyd.middleware.publication.dao.PublicationBundleDao;
import com.byyd.middleware.publication.dao.PublicationDao;
import com.byyd.middleware.publication.dao.PublicationHistoryDao;
import com.byyd.middleware.publication.dao.PublicationProvidedInfoDao;
import com.byyd.middleware.publication.dao.PublicationListDao;
import com.byyd.middleware.publication.dao.PublicationReadOnlyDao;
import com.byyd.middleware.publication.dao.PublicationTypeDao;
import com.byyd.middleware.publication.dao.RateCardDao;
import com.byyd.middleware.publication.dao.TransparentNetworkDao;
import com.byyd.middleware.publication.filter.AdSpaceFilter;
import com.byyd.middleware.publication.filter.PublicationBundleFilter;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.filter.PublicationListFilter;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("publicationManager")
public class PublicationManagerJpaImpl extends BaseJpaManagerImpl implements PublicationManager {
    
    private static final Logger LOG = Logger.getLogger(PublicationManagerJpaImpl.class.getName());

    @Autowired(required=false)
    private PublicationDao publicationDao;
    
    @Autowired(required=false)
    private PublicationReadOnlyDao publicationReadOnlyDao;
    
    @Autowired(required=false)
    private PublicationHistoryDao publicationHistoryDao;
    
    @Autowired(required=false)
    private PublicationTypeDao publicationTypeDao;
    
    @Autowired(required = false)
    private AdSpaceDao adSpaceDao;
    
    @Autowired(required = false)
    private PublicationListDao publicationListDao;
    
    @Autowired(required = false)
    private TransparentNetworkDao transparentNetworkDao;
    
    @Autowired(required=false)
    private DefaultRateCardDao defaultRateCardDao;
    
    @Autowired(required=false)
    private RateCardDao rateCardDao;
    
    @Autowired(required = false)
    private IntegrationTypeDao integrationTypeDao;
    
    @Autowired(required=false)
    private PublicationApprovalDashboardViewDao publicationApprovalDashboardViewDao;
    
    @Autowired(required=false)
    private PublicationProvidedInfoDao publicationProvidedInfoDao;
    
    @Autowired(required = false)
    private PublicationBundleDao publicationBundleDao;
    
    //------------------------------------------------------------------------------------------
    // Publication
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public Publication getPublicationById(String id, FetchStrategy... fetchStrategy) {
        return getPublicationById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Publication getPublicationById(Long id, FetchStrategy... fetchStrategy) {
        return publicationDao.getById(id, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=false)
    public Publication create(Publication publication) {
        publication = publicationDao.create (publication);
        Set<PublicationProvidedInfo> publicationProvidedInfos = publication.getPublicationProvidedInfos();
        if(publicationProvidedInfos != null) {
            for(PublicationProvidedInfo ppInfo : publicationProvidedInfos) {
                publicationProvidedInfoDao.create(ppInfo);
            }
        }
        
        publication.getLanguages();
        return publication;
    }

    @Override
    @Transactional(readOnly=false)
    public Publication update(Publication publication) {
        publication = publicationDao.update(publication);
        publication.getLanguages();
        return publication;
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(Publication publication) {
        //this.deleteAdSpaces(this.getAllAdSpacesForPublication(publication));
        //publication.getAdSpaces().clear();
        //publication = update(publication);
        publicationDao.delete(publication);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePublications(List<Publication> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(Publication publication : list) {
            delete(publication);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Publication getPublicationByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return publicationDao.getByExternalId(externalId, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationByExternalIds(List<String> externalIds,Date startDate,Date endDate, FetchStrategy... fetchStrategy) {
        return publicationDao.getByExternalIds(externalIds,"approvedDate",startDate,endDate, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
   public Publication getPublicationByName(String name, Publisher publisher, FetchStrategy... fetchStrategy) {
        return publicationDao.getByName(name, publisher, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
    public Publication getPublicationByPublisherAndRtbId(Publisher publisher, String rtbId, FetchStrategy ... fetchStrategy) {
        return publicationDao.getByPublisherAndRtbId(publisher, rtbId, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
    public Publication getPublicationByIdOrExternalId(String key, FetchStrategy... fetchStrategy) {
        Publication publication = getPublicationByExternalId(key, fetchStrategy);
        if(publication != null) {
            return publication;
        }
        return getPublicationById(key, fetchStrategy);
    }

    private FetchStrategy[] addLanguageFs(FetchStrategy[] fetchStrategy) {
        FetchStrategy[] localFetchStrategy = fetchStrategy;
        FetchStrategyBuilder fsb = null;
        if (ArrayUtils.isNotEmpty(localFetchStrategy)) {
            fsb = new FetchStrategyBuilder(localFetchStrategy[0]);
        }else{
            fsb = new FetchStrategyBuilder();
            localFetchStrategy = new FetchStrategy[1];
        }
        FetchStrategy fs = fsb.addLeft(Publication_.languages).build();
        localFetchStrategy[0] = fs;
        return localFetchStrategy;
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses) {
        return publicationDao.countForStatus(publisher, statuses);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, FetchStrategy... fetchStrategy) {
        return publicationDao.getForStatus(publisher, statuses, fetchStrategy);
     }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy) {
        return publicationDao.getForStatus(publisher, statuses, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, Pagination page, FetchStrategy... fetchStrategy) {
        return publicationDao.getForStatus(publisher, statuses, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublicationsLike(String name, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllPublicationsLike(name, LikeSpec.CONTAINS, page, fetchStrategy);
    }

    @Transactional(readOnly=true)
    public List<Publication> getAllPublicationsLike(String name, LikeSpec likeSpec, Pagination page, FetchStrategy... fetchStrategy) {
        return publicationDao.getAllLike(formatLikeSearchTarget(name, likeSpec), page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllPublications(PublicationFilter filter) {
        return publicationDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublications(PublicationFilter filter, FetchStrategy ... fetchStrategy) {
        return publicationDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublications(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return publicationDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublications(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return publicationDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllPublicationsReadOnly(PublicationFilter filter) {
        return publicationReadOnlyDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, FetchStrategy ... fetchStrategy) {
        return publicationReadOnlyDao.getAll(filter, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return publicationReadOnlyDao.getAll(filter, sort, addLanguageFs(fetchStrategy));
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return publicationReadOnlyDao.getAll(filter, page, addLanguageFs(fetchStrategy));
    }

    //------------------------------------------------------------------------------------------

    protected List<Publication.Status> getPublicationStatusesForActiveToDate() {
        List<Publication.Status> list = new ArrayList<Publication.Status>();
        list.add(Publication.Status.ACTIVE);
        list.add(Publication.Status.STOPPED);
        list.add(Publication.Status.PAUSED);
        return list;
    }

    @Override
    @Transactional(readOnly=true)
    public Long countActivePublicationsToDateForPublisher(Publisher publisher) {
        return countPublicationsForStatus(publisher, getPublicationStatusesForActiveToDate());
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        Sorting sort = new Sorting(SortOrder.asc("name"));

        return getActivePublicationsToDateForPublisher(publisher, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return getPublicationsForStatus(publisher, getPublicationStatusesForActiveToDate(), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        if(page.getSorting() == null) {
            page = new Pagination(page, new Sorting(SortOrder.asc("name")));
        }
        return getPublicationsForStatus(publisher, getPublicationStatusesForActiveToDate(), page, fetchStrategy);
    }


    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Map<Publication,Long> getPublicationsWithPendingAdsMapForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return publicationDao.getPublicationsWithPendingAdsMapForPublisher(publisher, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    /**
     * Update a publication's status and or adOpsStatus
     * @return true if either Publication.status or Publication.adOpsStatus was changed
     */
    @Override
    @Transactional(readOnly=false)
    public boolean updatePublicationStatus(Publication publication, Publication.Status status, Publication.AdOpsStatus adOpsStatus) {
        CompanyManager companyManager = AdfonicBeanDispatcher.getBean(CompanyManager.class);

        if (status == null) {
            LOG.warning("Publication status cannot be set to null");
            return false;
        }

        // AO-203 - ensure that adOpsStatus gets un-set if the status
        // transitions to anything other than PENDING.
        Publication.AdOpsStatus localAdOpsStatus = adOpsStatus;
        if (localAdOpsStatus != null && !Publication.Status.PENDING.equals(status)) {
            LOG.info("Nulling out adOpsStatus for non-PENDING status: " + status);
            localAdOpsStatus = null;
        }

        boolean statusChanged = !publication.getStatus().equals(status);
        boolean adOpsStatusChanged = !ObjectUtils.equals(publication.getAdOpsStatus(), localAdOpsStatus);
        if (statusChanged || adOpsStatusChanged) {
            LOG.fine("Updating " + publication.getExternalID() + " to status=" + status + ", adOpsStatus=" + localAdOpsStatus);
            publication.setStatus(status);
            publication.setAdOpsStatus(localAdOpsStatus);
            update(publication);
        }

        if (statusChanged) {
            FetchStrategy pubFs = new FetchStrategyBuilder()
                .addInner(Publication_.publisher)
                .addInner(Publication_.publicationType)
                .addInner(Publisher_.company)
                .addLeft(Company_.accountManager)
                .build();

            // Ensure hydrated enough to create the CompanyMessage
            try {
                publication.getPublicationType();
                publication.getPublisher().getCompany().getAccountManager();
            } catch (Exception e) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Re-fetching Publication id=" + publication.getId());
                }
                publication = getPublicationById(publication.getId(), pubFs);
            }

            if (Publication.Status.ACTIVE.equals(publication.getStatus())) {
                publication.setApprovedDate(new Date());
                update(publication);
                CompanyMessage cm = new CompanyMessage(
                        publication.getPublisher(), "publication.approved");
                cm.setArg0(publication.getName());
                companyManager.create(cm);
            } else if (Publication.Status.REJECTED.equals(publication.getStatus())) {
                CompanyMessage cm = new CompanyMessage(
                        publication.getPublisher(), "publication.rejected");
                cm.setArg0(publication.getName());
                companyManager.create(cm);
            }
        }

        return statusChanged || adOpsStatusChanged;
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication) {
        return publicationDao.countPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, FetchStrategy... fetchStrategy) {
        return publicationDao.getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Sorting sort, FetchStrategy... fetchStrategy) {
        return publicationDao.getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Pagination page, FetchStrategy... fetchStrategy) {
        return publicationDao.getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, page, fetchStrategy);
    }


    @Override
    @Transactional(readOnly=true)
    public boolean isPublicationNameUnique(String name, Publisher publisher, Publication excludePublication) {
        return this.countPublicationsWithNameForPublisher(name, false, publisher, excludePublication) < 1;
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Publication.class, ids, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Publication.class, ids, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return getObjectsByIds(Publication.class, ids, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Publication.class, ids, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Publication.class, ids, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return getObjectsByIdsAsMap(Publication.class, ids, page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // PublicationHistory
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public PublicationHistory newPublicationHistory(Publication publication, FetchStrategy ... fetchStrategy) {
        return newPublicationHistory(publication, null, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PublicationHistory newPublicationHistory(Publication publication, String comment, AdfonicUser adfonicUser, FetchStrategy ... fetchStrategy) {
        PublicationHistory publicationHistory = new PublicationHistory(publication);
        publicationHistory.setComment(comment);
        publicationHistory.setAdfonicUser(adfonicUser);
        publicationHistory = publicationHistoryDao.create(publicationHistory);
        try {
            publication.getHistory().add(publicationHistory);
        } catch(Exception e) {
            // Ignore this, it just means the publicationHistory isn't hydrated
        }
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return publicationHistory;
        } else {
            return getPublicationHistoryById(publicationHistory.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public PublicationHistory update(PublicationHistory publicationHistory) {
        return publicationHistoryDao.update(publicationHistory);
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationHistory getPublicationHistoryById(long id, FetchStrategy ... fetchStrategy) {
        return publicationHistoryDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationHistory> getPublicationHistory(Publication publication, FetchStrategy ... fetchStrategy) {
        return publicationHistoryDao.getAll(publication, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // PublicationType
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public PublicationType newPublicationType(String name, String systemName, Medium medium, TrackingIdentifierType defaultTrackingIdentifierType, IntegrationType defaultIntegrationType, FetchStrategy... fetchStrategy) {
        PublicationType type = new PublicationType(name, medium);
        type.setSystemName(systemName);
        type.setDefaultTrackingIdentifierType(defaultTrackingIdentifierType);
        type.setDefaultIntegrationType(defaultIntegrationType);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(type);
        } else {
            type = create(type);
            return getPublicationTypeById(type.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationType getPublicationTypeById(String id, FetchStrategy... fetchStrategy) {
        return getPublicationTypeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationType getPublicationTypeById(Long id, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PublicationType create(PublicationType publicationType) {
        return publicationTypeDao.create(publicationType);
    }

    @Override
    @Transactional(readOnly=false)
    public PublicationType update(PublicationType publicationType) {
        return publicationTypeDao.update(publicationType);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PublicationType publicationType) {
        publicationTypeDao.delete(publicationType);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePublicationTypes(List<PublicationType> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PublicationType publicationType : list) {
            delete(publicationType);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationType getPublicationTypeByName(String name, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationType getPublicationTypeBySystemName(String systemName, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getBySystemName(systemName, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllPublicationTypes() {
        return publicationTypeDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getAllPublicationTypes(FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getAllPublicationTypes(Sorting sort, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getAllPublicationTypes(Pagination page, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getAll(page, fetchStrategy);
    }



    @Override
    @Transactional(readOnly=true)
    public Long countPublicationTypeForSystemNames(List<String> systemNames) {
        return publicationTypeDao.countForSystemNames(systemNames);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getForSystemNames(systemNames, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getForSystemNames(systemNames, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return publicationTypeDao.getForSystemNames(systemNames, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationType> getHouseAdPublicationTypes(FetchStrategy... fetchStrategy) {
        Sorting sort = new Sorting(SortOrder.asc("name"));
        List<String> systemNames = new ArrayList<String>();
        systemNames.add("IPHONE_APP");
        systemNames.add("IPAD_APP");
        systemNames.add("ANDROID_APP");
        return getPublicationTypeForSystemNames(systemNames, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean canPublicationBePhysicallyDeleted(Publication publication) {
        // If the pub is approved, then don't allow it to be deleted
        if (publication.isApproved()) {
            return false;
        }

        // AF-885: don't allow pubs to be physically deleted if they have any
        // AdSpace having status VERIFIED or DORMANT.
        return countAllAdSpaces(new AdSpaceFilter()
                                .setPublication(publication)
                                .setStatuses(Arrays.asList(AdSpace.Status.VERIFIED, AdSpace.Status.DORMANT))) == 0;
    }

    // ------------------------------------------------------------------------------------------
    // AdSpace
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public AdSpace newAdSpace(Publication publication, FetchStrategy... fetchStrategy) {
        return newAdSpace(publication, null, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public AdSpace newAdSpace(Publication publication, String name, Collection<Format> formats, FetchStrategy... fetchStrategy) {
        boolean adSpacesSet = false;
        try {
            publication.getAdSpaces().size();
            adSpacesSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!adSpacesSet) {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Publication_.adSpaces)
                               .build();
            publication = getPublicationById(publication.getId(), fs);
        }
        AdSpace adSpace = publication.newAdSpace();
        adSpace.setName(name);
        if (formats != null) {
            adSpace.getFormats().addAll(formats);
        }
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(adSpace);
        } else {
            adSpace = create(adSpace);
            return getAdSpaceById(adSpace.getId(), fetchStrategy);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public AdSpace getAdSpaceById(String id, FetchStrategy... fetchStrategy) {
        return getAdSpaceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AdSpace getAdSpaceById(Long id, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public AdSpace create(AdSpace adSpace) {
        return adSpaceDao.create(adSpace);
    }

    @Override
    @Transactional(readOnly = false)
    public AdSpace update(AdSpace adSpace) {
        return adSpaceDao.update(adSpace);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(AdSpace adSpace) {
        adSpaceDao.delete(adSpace);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAdSpaces(List<AdSpace> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AdSpace entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdSpace getAdSpaceByExternalId(String externalId,
            FetchStrategy... fetchStrategy) {
        return adSpaceDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AdSpace getAdSpaceByIdOrExternalId(String key,
            FetchStrategy... fetchStrategy) {
        AdSpace adSpace = getAdSpaceByExternalId(key, fetchStrategy);
        if (adSpace != null) {
            return adSpace;
        }
        return getAdSpaceById(key, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllAdSpacesForPublication(Publication publication) {
        return adSpaceDao.countAllForPublication(publication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpacesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAllForPublication(publication, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpacesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAllForPublication(publication, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpacesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAllForPublication(publication, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpacesForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAllForPublisher(publisher, sort, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllAdSpaces(AdSpaceFilter filter) {
        return adSpaceDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAll(filter, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countUnverifiedAdSlotsForPublisher(Publisher publisher) {
        return adSpaceDao.countUnverifiedAdSlotsForPublisher(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Publication,Long> getUnverifiedAdSlotsForPublisherCountMap(Publisher publisher) {
        return adSpaceDao.getUnverifiedAdSlotsForPublisherCountMap(publisher);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes) {
        return adSpaceDao.countHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, FetchStrategy... fetchStrategy) {
        Sorting sort = new Sorting(asc(Publication.class, "name"), asc("name"));
        return getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Pagination page, FetchStrategy... fetchStrategy) {
        Sorting sort = page.getSorting();
        if(sort == null) {
            sort = new Sorting(asc(Publication.class, "name"), asc(AdSpace.class, "name"));
            page = new Pagination(page, sort);
        }
        return adSpaceDao.getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace) {
        return adSpaceDao.countAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Pagination page, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdSpaceNameUnique(String name, Publication publication, AdSpace excludeAdSpace) {
        // It's unique if there's no *other* adspace (if excludeAdSpace is specified)
        // belonging to the given publication with the same name (case-insensitive).
        // AdSpaces with status=DELETED are omitted from the search.
        AdSpaceFilter filter = new AdSpaceFilter()
            .setPublication(publication)
            .setStatuses(EnumSet.complementOf(EnumSet.of(AdSpace.Status.DELETED)))
            .setName(name, false); // case-insensitive
        if (excludeAdSpace != null) {
            filter.setExcludedIds(Collections.singleton(excludeAdSpace.getId()));
        }

        return countAllAdSpaces(filter) < 1;
   }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countUnallocatedAdSpaceForPublisher(Publisher publisher) {
        return adSpaceDao.countUnallocatedAdSpaceForPublisher(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getUnallocatedAdSpaceForPublisher(publisher, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getUnallocatedAdSpaceForPublisher(publisher, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return adSpaceDao.getUnallocatedAdSpaceForPublisher(publisher, page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // PublicationList
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public PublicationList getPublicationListById(String id, FetchStrategy... fetchStrategy) {
        return getPublicationListById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PublicationList getPublicationListById(Long id, FetchStrategy... fetchStrategy) {
        return publicationListDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PublicationList create(PublicationList publicationList) {
        return publicationListDao.create(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public PublicationList update(PublicationList publicationList) {
        return publicationListDao.update(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PublicationList publicationList) {
        publicationListDao.delete(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePublicationLists(List<PublicationList> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PublicationList entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllPublicationLists(PublicationListFilter filter) {
        return publicationListDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationLists(PublicationListFilter filter, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationLists(PublicationListFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationLists(PublicationListFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllPublicationListsForCompany(Company company) {
        return countAllPublicationLists(new PublicationListFilter().setCompany(company));
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return getAllPublicationLists(new PublicationListFilter().setCompany(company), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(new PublicationListFilter().setCompany(company), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(new PublicationListFilter().setCompany(company), page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    public Long countAllPublicationListsForAdvertiser(Advertiser advertiser) {
        return countAllPublicationLists(new PublicationListFilter().setAdvertiser(advertiser));
    }

    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForAdvertiser(Advertiser advertiser, FetchStrategy ... fetchStrategy) {
        return getAllPublicationLists(new PublicationListFilter().setAdvertiser(advertiser), fetchStrategy);
    }

    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(new PublicationListFilter().setAdvertiser(advertiser), sort, fetchStrategy);
    }

    @Transactional(readOnly=true)
    public List<PublicationList> getAllPublicationListsForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy ... fetchStrategy) {
        return publicationListDao.getAll(new PublicationListFilter().setAdvertiser(advertiser), page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countPublicationsForPublicationList(PublicationList publicationList) {
        return publicationDao.countPublicationsForPublicationList(publicationList);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy) {
        return getPublicationsForPublicationList(publicationList, new Sorting(asc(Publication.class, "name")), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy) {
        return publicationDao.getPublicationsForPublicationList(publicationList, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy) {
        if(page.getSorting() == null) {
            page = new Pagination(page, new Sorting(asc(Publication.class, "name")));
        }
        return publicationDao.getPublicationsForPublicationList(publicationList, page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // TransparentNetwork
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public TransparentNetwork newTransparentNetwork(String name, String description, FetchStrategy... fetchStrategy) {
        TransparentNetwork transparentNetwork = new TransparentNetwork(name);
        if (StringUtils.isNotBlank(description)) {
            transparentNetwork.setDescription(description);
        }
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(transparentNetwork);
        } else {
            transparentNetwork = create(transparentNetwork);
            return getTransparentNetworkById(transparentNetwork.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransparentNetwork getTransparentNetworkById(String id, FetchStrategy... fetchStrategy) {
        return getTransparentNetworkById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public TransparentNetwork getTransparentNetworkById(Long id, FetchStrategy... fetchStrategy) {
        return transparentNetworkDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public TransparentNetwork getTransparentNetworkByName(String name, FetchStrategy... fetchStrategy) {
        return transparentNetworkDao.getByName(name, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public TransparentNetwork create(TransparentNetwork transparentNetwork) {
        return transparentNetworkDao.create(transparentNetwork);
    }

    @Override
    @Transactional(readOnly = false)
    public TransparentNetwork update(TransparentNetwork transparentNetwork) {
        return transparentNetworkDao.update(transparentNetwork);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(TransparentNetwork transparentNetwork) {
        transparentNetworkDao.delete(transparentNetwork);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteTransparentNetworks(List<TransparentNetwork> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (TransparentNetwork entry : list) {
            delete(entry);
        }
    }

    /**
     * Checks if any of the campaign's creatives have been approved
     * on any of the TransparentNetwork's publications.
     *
     * The goal of this method is to show whether or not the campaign
     * is (at least potentially) running on the given network.
     *
     * Returns:
     *  Boolean.TRUE if any active creative has been approved for any active publication (or there are auto-approval publications in the network)
     *  Boolean.FALSE if all active creatives have been rejected for all active publications
     *  null otherwise, indicating "Pending" status.
     */
    @Override
    @Transactional(readOnly = true)
    public Boolean checkTransparentNetworkApproval(Campaign campaign, TransparentNetwork network) {
        // Hydration test
        Campaign localCampaign = campaign;
        try {
            localCampaign.getCreatives().size();
        } catch(Exception e) {
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(Campaign.class, "creatives", JoinType.LEFT);
            CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
            localCampaign = campaignManager.getCampaignById(localCampaign.getId(), fs);
        }

        // First let's see if there are any active creatives.
        int activeCount = 0;
        for (Creative c : localCampaign.getCreatives()) {
            if (c.getStatus() == Creative.Status.ACTIVE) {
                ++activeCount;
            }
        }
        if (activeCount == 0) {
            // Nothing to do
            return null;
        }

        // We need things 3 levels deep. Just reload the TransparentNetwork object with all the proper stuff
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(TransparentNetwork.class, "publications", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Publication.class, "approvedCreatives", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Publication.class, "deniedCreatives", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Creative.class, "campaign", JoinType.LEFT);
        TransparentNetwork dbNetwork = this.getTransparentNetworkById(network.getId(), fs);

        for (Publication p : dbNetwork.getPublications()) {
            if (p.getStatus() == Publication.Status.ACTIVE) {
                if (p.isAutoApproval()) {
                    return Boolean.TRUE;
                } else {
                    for (Creative c : p.getApprovedCreatives()) {
                        if ((c.getStatus() == Creative.Status.ACTIVE) && (c.getCampaign() == localCampaign)) {
                            // Hey, something was approved.
                            return Boolean.TRUE;
                        }
                    }
                    int deniedCount = 0;
                    for (Creative c : p.getDeniedCreatives()) {
                        if ((c.getStatus() == Creative.Status.ACTIVE) && (c.getCampaign() == localCampaign)) {
                            deniedCount++;
                        }
                    }
                    if (deniedCount < activeCount) {
                        // Not everything was denied. We live in hope.
                        return null;
                    }
                } // manual approval
            } // active publication
        } // each publication

        // All active publications rejected all active creatives.
        return Boolean.FALSE;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAvailableTransparentNetworksForCompany(Company company, boolean includePremium) {
        return transparentNetworkDao.countAvailableTransparentNetworksForCompany(company, includePremium);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, FetchStrategy... fetchStrategy) {
        Sorting sort = new Sorting(asc(TransparentNetwork.class, "name"));
        return transparentNetworkDao.getAvailableTransparentNetworksForCompany(company, includePremium, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Sorting sort, FetchStrategy... fetchStrategy) {
        return transparentNetworkDao.getAvailableTransparentNetworksForCompany(company, includePremium, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Pagination page, FetchStrategy... fetchStrategy) {
        if(page.getSorting() == null){
            Sorting sort = new Sorting(asc(TransparentNetwork.class, "name"));
            page = new Pagination(page, sort);
        }
        return transparentNetworkDao.getAvailableTransparentNetworksForCompany(company, includePremium, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // DefaultRateCard
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public DefaultRateCard getDefaultRateCardById(String id, FetchStrategy... fetchStrategy) {
        return getDefaultRateCardById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public DefaultRateCard getDefaultRateCardById(Long id, FetchStrategy... fetchStrategy) {
        return defaultRateCardDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public DefaultRateCard create(DefaultRateCard defaultRateCard) {
        return defaultRateCardDao.create(defaultRateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public DefaultRateCard update(DefaultRateCard defaultRateCard) {
        return defaultRateCardDao.update(defaultRateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(DefaultRateCard defaultRateCard) {
        defaultRateCardDao.delete(defaultRateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteDefaultRateCards(List<DefaultRateCard> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(DefaultRateCard entry : list) {
            delete(entry);
        }
    }


    @Override
    @Transactional(readOnly=true)
    public DefaultRateCard getDefaultRateCardByBidType(BidType bidType, FetchStrategy... fetchStrategy) {
        return defaultRateCardDao.getByBidType(bidType, fetchStrategy);
    }


    //------------------------------------------------------------------------------------------
    // RateCard
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public RateCard newRateCard(Country country, BigDecimal defaultMinimumBid, BigDecimal minimumBid, FetchStrategy... fetchStrategy) {
        RateCard card = new RateCard();
        card.setDefaultMinimum(defaultMinimumBid);
        card.setMinimumBid(country, minimumBid);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(card);
        } else {
            card = create(card);
            return getRateCardById(card.getId(), fetchStrategy);
        }
    }


    @Override
    @Transactional(readOnly=true)
    public RateCard getRateCardById(String id, FetchStrategy... fetchStrategy) {
        return getRateCardById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public RateCard getRateCardById(Long id, FetchStrategy... fetchStrategy) {
        return rateCardDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public RateCard create(RateCard rateCard) {
        return rateCardDao.create(rateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public RateCard update(RateCard rateCard) {
        return rateCardDao.update(rateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(RateCard rateCard) {
        rateCardDao.delete(rateCard);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteRateCards(List<RateCard> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(RateCard entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public RateCard getRateCardByBidType(BidType bidType) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getRateCardByBidType() - bidType: " + bidType.toString());
        }

        // We are programatically forcing the hydration of RateCard in a DefaultRateCard object, so we can return it.
        FetchStrategyJpaImpl<DefaultRateCard> fetchStrategy = new FetchStrategyJpaImpl<DefaultRateCard>();
        fetchStrategy.addEagerlyLoadedFieldForClass(DefaultRateCard_.rateCard);

        DefaultRateCard defaultRateCard = getDefaultRateCardByBidType(bidType, fetchStrategy);
        if(defaultRateCard == null) {
            return null;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getRateCardByBidType() - Got defaultRateCard " + defaultRateCard.getId());
        }
        //return defaultRateCard.getRateCard();
        RateCard rateCard = defaultRateCard.getRateCard();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getRateCardByBidType() - got rateCard " + rateCard.getId());
        }
        // Cant use a FetchStrategyJpaImpl here, because minimumBidMap's values are not entities extending BusinessKey
        FetchStrategyImpl rateCardFs = new FetchStrategyImpl();
        rateCardFs.addEagerlyLoadedFieldForClass(RateCard.class, "minimumBidMap", JoinType.LEFT);
        rateCard = this.getRateCardById(rateCard.getId(), rateCardFs);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getRateCardByBidType() - reloaded rateCard " + rateCard.getId());
        }
        return rateCard;
      }

    // ------------------------------------------------------------------------------------------
    // IntegrationType
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public IntegrationType newIntegrationType(String name, String systemName, FetchStrategy... fetchStrategy) {
        IntegrationType type = new IntegrationType(name, systemName);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(type);
        } else {
            type = create(type);
            return getIntegrationTypeById(type.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrationType getIntegrationTypeById(String id, FetchStrategy... fetchStrategy) {
        return getIntegrationTypeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrationType getIntegrationTypeById(Long id, FetchStrategy... fetchStrategy) {
        return integrationTypeDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public IntegrationType create(IntegrationType integrationType) {
        return integrationTypeDao.create(integrationType);
    }

    @Override
    @Transactional(readOnly = false)
    public IntegrationType update(IntegrationType integrationType) {
        return integrationTypeDao.update(integrationType);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(IntegrationType integrationType) {
        integrationTypeDao.delete(integrationType);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteIntegrationTypes(List<IntegrationType> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (IntegrationType entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllIntegrationTypes() {
        return integrationTypeDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationType> getAllIntegrationTypes(FetchStrategy... fetchStrategy) {
        return integrationTypeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationType> getAllIntegrationTypes(Sorting sort, FetchStrategy... fetchStrategy) {
        return integrationTypeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationType> getAllIntegrationTypes(Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return integrationTypeDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrationType getIntegrationTypeBySystemName(String systemName, FetchStrategy... fetchStrategy) {
        return integrationTypeDao.getBySystemName(systemName, fetchStrategy);
    }
    
  //------------------------------------------------------------------------------------------
  // PublicationApprovalDashboardView
  //------------------------------------------------------------------------------------------

  @Override
  @Transactional(readOnly=true)
  public Long countAllPublicationApprovalDashboardView(PublicationFilter filter) {
      return publicationApprovalDashboardViewDao.countAll(filter);
  }

  @Override
  @Transactional(readOnly=true)
  public List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter) {
      return publicationApprovalDashboardViewDao.getAll(filter);
  }

  @Override
  @Transactional(readOnly=true)
  public List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter, Pagination page) {
      return publicationApprovalDashboardViewDao.getAll(filter, page);
  }

  @Override
  @Transactional(readOnly=true)
  public List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter, Sorting sort) {
      return publicationApprovalDashboardViewDao.getAll(filter, sort);
  }
  
  //------------------------------------------------------------------------------
  // Publication Bundle Information
  //------------------------------------------------------------------------------
  @Override
  @Transactional(readOnly=true)
  public PublicationBundle getBundleById(Long id, FetchStrategy... fetchStrategy){
      return publicationBundleDao.getById(id, fetchStrategy);
  }
  
  @Override
  @Transactional
  public PublicationBundle create(PublicationBundle publicationBundle){
      return publicationBundleDao.create(publicationBundle);
  }
  
  @Override
  @Transactional
  public PublicationBundle update(PublicationBundle publicationBundle){
      return publicationBundleDao.update(publicationBundle);
  }
  
  @Override
  @Transactional
  public void delete(PublicationBundle publicationBundle){
      publicationBundleDao.delete(publicationBundle);
  }
  
  @Override
  @Transactional
  public void delete(List<PublicationBundle> publicationBundles){
      if(CollectionUtils.isNotEmpty(publicationBundles)) {
          for(PublicationBundle bundle : publicationBundles) {
              delete(bundle);
          }
      }
  }
  
  @Override
  @Transactional(readOnly=true)
  public Long countAllPublicationBundles(PublicationBundleFilter filter){
      return publicationBundleDao.countAll(filter);
  }
  
  @Override
  @Transactional(readOnly=true)
  public List<PublicationBundle> getAllPublicationBundles(PublicationBundleFilter filter, boolean retrievePublications){
      return getAllPublicationBundles(filter, null, retrievePublications);
  }
  
  @Override
  @Transactional(readOnly=true)
  public List<PublicationBundle> getAllPublicationBundles(PublicationBundleFilter filter, Pagination page, boolean retrievePublications){
      List<PublicationBundle> bundles = publicationBundleDao.getAll(filter, page);
      if (retrievePublications){
          for (PublicationBundle bundle : bundles){
              bundle.getPublications();
          }
      }
      return bundles;
  }
  

  @Override
  @Transactional(readOnly=true)
  public List<PublicationBundle> getAllPublicationBundlesByPublicationId(Long publicationId) {
      return publicationBundleDao.getAll(new PublicationBundleFilter().setPublicationId(publicationId));
  }
  
  @Override
  @Transactional
  public PublicationBundle addPublicationToBundle(PublicationBundle publicationBundle, Publication publication){
      PublicationBundle persistedBundle = publicationBundleDao.getById(publicationBundle.getId());
      if ((persistedBundle!=null)&&(!persistedBundle.getPublications().contains(publication))){
          persistedBundle.getPublications().add(publication);
          persistedBundle = publicationBundleDao.update(persistedBundle);
      }
      return persistedBundle;
  }
  
  @Override
  @Transactional
  public PublicationBundle addPublicationsToBundle(PublicationBundle publicationBundle, List<Publication> publications){
      PublicationBundle persistedBundle = publicationBundleDao.getById(publicationBundle.getId());
      if ((persistedBundle!=null)&&(!persistedBundle.getPublications().containsAll(publications))){
          persistedBundle.getPublications().addAll(publications);
          persistedBundle = publicationBundleDao.update(persistedBundle);
      }
      return persistedBundle;
  }

}
