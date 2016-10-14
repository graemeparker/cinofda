package com.byyd.middleware.publication.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Country;
import com.adfonic.domain.DefaultRateCard;
import com.adfonic.domain.Format;
import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationApprovalDashboardView;
import com.adfonic.domain.PublicationBundle;
import com.adfonic.domain.PublicationHistory;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;
import com.byyd.middleware.publication.filter.AdSpaceFilter;
import com.byyd.middleware.publication.filter.PublicationBundleFilter;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.filter.PublicationListFilter;

public interface PublicationManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // Publication
    //------------------------------------------------------------------------------------------
    Publication getPublicationById(String id, FetchStrategy... fetchStrategy);
    Publication getPublicationById(Long id, FetchStrategy... fetchStrategy);
    Publication create(Publication publication);
    Publication update(Publication publication);
    void delete(Publication publication);
    void deletePublications(List<Publication> list);

    Publication getPublicationByExternalId(String externalId, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationByExternalIds(List<String> externalsId, Date startDate,Date endDate,FetchStrategy... fetchStrategy);
    Publication getPublicationByIdOrExternalId(String key, FetchStrategy... fetchStrategy);

    Publication getPublicationByName(String name, Publisher publisher, FetchStrategy... fetchStrategy);

    Publication getPublicationByPublisherAndRtbId(Publisher publisher, String rtbId, FetchStrategy ... fetchStrategy);

    Long countPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses);
    List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForStatus(Publisher publisher, List<Publication.Status> statuses, Pagination page, FetchStrategy... fetchStrategy);
    List<Publication> getAllPublicationsLike(String name, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllPublications(PublicationFilter filter);
    List<Publication> getAllPublications(PublicationFilter filter, FetchStrategy ... fetchStrategy);
    List<Publication> getAllPublications(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<Publication> getAllPublications(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countAllPublicationsReadOnly(PublicationFilter filter);
    List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, FetchStrategy ... fetchStrategy);
    List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<Publication> getAllPublicationsReadOnly(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countActivePublicationsToDateForPublisher(Publisher publisher);
    List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getActivePublicationsToDateForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);

    Map<Publication,Long> getPublicationsWithPendingAdsMapForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);

    boolean updatePublicationStatus(Publication publication, Publication.Status status, Publication.AdOpsStatus adOpsStatus);

    Long countPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Pagination page, FetchStrategy... fetchStrategy);

    boolean isPublicationNameUnique(String name, Publisher publisher, Publication excludePublication);
    
    List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsByIdsAsList(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);
    
    Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, FetchStrategy... fetchStrategy);
    Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    Map<Long, Publication> getPublicationsByIdsAsMap(Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);
    

    //------------------------------------------------------------------------
    // PublicationHistory
    //------------------------------------------------------------------------
    PublicationHistory newPublicationHistory(Publication publication, FetchStrategy ... fetchStrategy);
    PublicationHistory newPublicationHistory(Publication publication, String comment, AdfonicUser adfonicUser, FetchStrategy ... fetchStrategy);

    PublicationHistory update(PublicationHistory publicationHistory);

    PublicationHistory getPublicationHistoryById(long id, FetchStrategy ... fetchStrategy);

    List<PublicationHistory> getPublicationHistory(Publication publication, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // PublicationType
    //------------------------------------------------------------------------------------------
    PublicationType newPublicationType(String name, String systemName, Medium medium, TrackingIdentifierType defaultTrackingIdentifierType, IntegrationType defaultIntegrationType, FetchStrategy... fetchStrategy);

    PublicationType getPublicationTypeById(String id, FetchStrategy... fetchStrategy);
    PublicationType getPublicationTypeById(Long id, FetchStrategy... fetchStrategy);
    PublicationType create(PublicationType publicationType);
    PublicationType update(PublicationType publicationType);
    void delete(PublicationType publicationType);
    void deletePublicationTypes(List<PublicationType> list);

    PublicationType getPublicationTypeByName(String name, FetchStrategy... fetchStrategy);
    PublicationType getPublicationTypeBySystemName(String systemName, FetchStrategy... fetchStrategy);

    Number countAllPublicationTypes();
    List<PublicationType> getAllPublicationTypes(FetchStrategy... fetchStrategy);
    List<PublicationType> getAllPublicationTypes(Sorting sort, FetchStrategy... fetchStrategy);
    List<PublicationType> getAllPublicationTypes(Pagination page, FetchStrategy... fetchStrategy);

    Long countPublicationTypeForSystemNames(List<String> systemNames);
    List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, FetchStrategy... fetchStrategy);
    List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy);
    List<PublicationType> getPublicationTypeForSystemNames(List<String> systemNames, Pagination page, FetchStrategy... fetchStrategy);

    List<PublicationType> getHouseAdPublicationTypes(FetchStrategy... fetchStrategy);

    boolean canPublicationBePhysicallyDeleted(Publication publication);

    //------------------------------------------------------------------------------------------
    // AdSpace
    //------------------------------------------------------------------------------------------
    AdSpace newAdSpace(Publication publication, FetchStrategy... fetchStrategy);
    AdSpace newAdSpace(Publication publication, String name, Collection<Format> formats, FetchStrategy... fetchStrategy);

    AdSpace getAdSpaceById(String id, FetchStrategy... fetchStrategy);
    AdSpace getAdSpaceById(Long id, FetchStrategy... fetchStrategy);
    //public AdSpace create(AdSpace adSpace);
    AdSpace update(AdSpace adSpace);
    void delete(AdSpace adSpace);
    void deleteAdSpaces(List<AdSpace> list);

    AdSpace getAdSpaceByExternalId(String externalId, FetchStrategy... fetchStrategy);
    AdSpace getAdSpaceByIdOrExternalId(String key, FetchStrategy... fetchStrategy);

    Long countAllAdSpacesForPublication(Publication publication);
    List<AdSpace> getAllAdSpacesForPublication(Publication publication, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllAdSpacesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllAdSpacesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy);

    List<AdSpace> getAllAdSpacesForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);

    Long countAllAdSpaces(AdSpaceFilter filter);
    List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAllAdSpaces(AdSpaceFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countUnverifiedAdSlotsForPublisher(Publisher publisher);
    Map<Publication,Long> getUnverifiedAdSlotsForPublisherCountMap(Publisher publisher);

    Long countHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, FetchStrategy... fetchStrategy);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Pagination page, FetchStrategy... fetchStrategy);

    Long countAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, FetchStrategy... fetchStrategy);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Pagination page, FetchStrategy... fetchStrategy);

    boolean isAdSpaceNameUnique(String name, Publication publication, AdSpace adSpace);

    Long countUnallocatedAdSpaceForPublisher(Publisher publisher);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, FetchStrategy... fetchStrategy);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy);
    List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // PublicationList
    //------------------------------------------------------------------------------------------
    PublicationList getPublicationListById(String id, FetchStrategy... fetchStrategy);
    PublicationList getPublicationListById(Long id, FetchStrategy... fetchStrategy);
    PublicationList create(PublicationList publicationList);
    PublicationList update(PublicationList publicationList);
    void delete(PublicationList publicationList);
    void deletePublicationLists(List<PublicationList> list);
   
    Long countAllPublicationLists(PublicationListFilter filter);
    List<PublicationList> getAllPublicationLists(PublicationListFilter filter, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAllPublicationLists(PublicationListFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAllPublicationLists(PublicationListFilter filter, Pagination page, FetchStrategy ... fetchStrategy);

    Long countPublicationsForPublicationList(PublicationList publicationList);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy);
    List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countAllPublicationListsForCompany(Company company);
    List<PublicationList> getAllPublicationListsForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAllPublicationListsForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);
    List<PublicationList> getAllPublicationListsForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // TransparentNetwork
    //------------------------------------------------------------------------------------------
    /**
     * Create and persist a new instance of TransparentNetwork
     * @param name (required)
     * @param description (optional, and blank or empty will be treated like null)
     * @param fetchStrategy (optional)
     * @return the newly created and persisted instance
     */
    TransparentNetwork newTransparentNetwork(String name, String description, FetchStrategy... fetchStrategy);

    TransparentNetwork getTransparentNetworkById(String id, FetchStrategy... fetchStrategy);
    TransparentNetwork getTransparentNetworkById(Long id, FetchStrategy... fetchStrategy);
    TransparentNetwork getTransparentNetworkByName(String name, FetchStrategy... fetchStrategy);
    //public TransparentNetwork create(TransparentNetwork transparentNetwork);
    TransparentNetwork update(TransparentNetwork transparentNetwork);
    void delete(TransparentNetwork transparentNetwork);
    void deleteTransparentNetworks(List<TransparentNetwork> list);

    Boolean checkTransparentNetworkApproval(Campaign campaign, TransparentNetwork network);

    Long countAvailableTransparentNetworksForCompany(Company company, boolean includePremium);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, FetchStrategy... fetchStrategy);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Sorting sort, FetchStrategy... fetchStrategy);
    List<TransparentNetwork> getAvailableTransparentNetworksForCompany(Company company, boolean includePremium, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // DefaultRateCard
    //------------------------------------------------------------------------------------------

    DefaultRateCard getDefaultRateCardById(String id, FetchStrategy... fetchStrategy);
    DefaultRateCard getDefaultRateCardById(Long id, FetchStrategy... fetchStrategy);
    DefaultRateCard create(DefaultRateCard defaultRateCard);
    DefaultRateCard update(DefaultRateCard defaultRateCard);
    void delete(DefaultRateCard defaultRateCard);
    void deleteDefaultRateCards(List<DefaultRateCard> list);

    DefaultRateCard getDefaultRateCardByBidType(BidType bidType, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // RateCard
    //------------------------------------------------------------------------------------------
    RateCard newRateCard(Country country, BigDecimal defaultMinimum, BigDecimal minimumBid, FetchStrategy... fetchStrategy);

    RateCard getRateCardById(String id, FetchStrategy... fetchStrategy);
    RateCard getRateCardById(Long id, FetchStrategy... fetchStrategy);
    RateCard create(RateCard rateCard);
    RateCard update(RateCard rateCard);
    void delete(RateCard rateCard);
    void deleteRateCards(List<RateCard> list);

    RateCard getRateCardByBidType(BidType bidType);


    //------------------------------------------------------------------------------
    // IntegrationType
    //------------------------------------------------------------------------------
    IntegrationType newIntegrationType(String name, String systemName, FetchStrategy... fetchStrategy);

    IntegrationType getIntegrationTypeById(String id, FetchStrategy... fetchStrategy);
    IntegrationType getIntegrationTypeById(Long id, FetchStrategy... fetchStrategy);
    IntegrationType update(IntegrationType integrationType);
    void delete(IntegrationType integrationType);
    void deleteIntegrationTypes(List<IntegrationType> list);

    IntegrationType getIntegrationTypeBySystemName(String systemName, FetchStrategy ... fetchStrategy);

    Long countAllIntegrationTypes();
    List<IntegrationType> getAllIntegrationTypes(FetchStrategy ... fetchStrategy);
    List<IntegrationType> getAllIntegrationTypes(Sorting sort, FetchStrategy ... fetchStrategy);
    List<IntegrationType> getAllIntegrationTypes(Pagination page, Sorting sort, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // Pub approval VW
    //------------------------------------------------------------------------------
    Long countAllPublicationApprovalDashboardView(PublicationFilter filter);
    List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter);
    List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter, Pagination page);    
    List<PublicationApprovalDashboardView> getAllPublicationApprovalDashboardView(PublicationFilter filter, Sorting sort);
    
    //------------------------------------------------------------------------------
    // Publication Bundle Information
    //------------------------------------------------------------------------------
    PublicationBundle getBundleById(Long id, FetchStrategy... fetchStrategy);
    PublicationBundle create(PublicationBundle publicationBundle);
    PublicationBundle update(PublicationBundle publicationBundle);
    void delete(PublicationBundle publicationBundle);
    void delete(List<PublicationBundle> publicationBundles);
   
    Long countAllPublicationBundles(PublicationBundleFilter filter);
    List<PublicationBundle> getAllPublicationBundles(PublicationBundleFilter filter, boolean retrievePublications);
    List<PublicationBundle> getAllPublicationBundles(PublicationBundleFilter filter, Pagination page, boolean retrievePublications);
    List<PublicationBundle> getAllPublicationBundlesByPublicationId(Long publicationId);
    
    PublicationBundle addPublicationToBundle(PublicationBundle publicationBundle, Publication publication);
    PublicationBundle addPublicationsToBundle(PublicationBundle publicationBundle, List<Publication> publications);
}
