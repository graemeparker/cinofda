package com.byyd.middleware.publication.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static com.byyd.middleware.iface.dao.SortOrder.desc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.DefaultRateCard;
import com.adfonic.domain.DefaultRateCard_;
import com.adfonic.domain.Format;
import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationHistory;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList_;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.RateCard_;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.Campaign.InventoryTargetingType;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.AdSpaceFilter;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.filter.PublicationListFilter;
import com.byyd.middleware.utils.TransactionalRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class PublicationManagerIT extends AbstractAdfonicTest{

    @Autowired
    private PublicationManager publicationManager;
    
    @Autowired
    private CommonManager commonManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private DeviceManager deviceManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private CompanyManager companyManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;

    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private TransactionalRunner transactionalRunner;
    
    @Test
    public void testPublicationExcludedCategories() {
        FetchStrategyImpl pubFs = new FetchStrategyImpl();
        pubFs.addEagerlyLoadedFieldForClass(Publication.class, "excludedCategories", JoinType.LEFT);

        try {
            Publication pub = publicationManager.getPublicationById(680L, pubFs);
            pub = publicationManager.update(pub);
            pub = publicationManager.getPublicationById(pub.getId(), pubFs);

            System.out.println("<br/>Current Excluded Categories for publication: <br/>");
            for (Category cat : pub.getExcludedCategories()) {
                System.out.println("Name: " + cat.getName() + " --> ID: "
                        + cat.getId() + "<br/>");
            }

            Set<Category> excludedCategories = new HashSet<Category>();
            // excludedCategories.add(sm.getCategoryById(142L, categoryFs));
            Category cat1 = commonManager.getCategoryById(60L);
            Category cat2 = commonManager.getCategoryById(61L);
            excludedCategories.add(cat1);
            excludedCategories.add(cat2);

            System.out.println("Our Chosen Categories to exclude: <br/>");
            for (Category cat : excludedCategories) {
                System.out.println(cat.getName() + " --> ID: " + cat.getId()
                        + "<br/>");
            }

            pub.getExcludedCategories().addAll(excludedCategories);
            System.out.println("<br/>Publication Excluded Categories before Persistence: <br/>");
            for (Category cat : pub.getExcludedCategories()) {
                System.out.println("Name: " + cat.getName() + " --> ID: "
                        + cat.getId() + "<br/>");
            }

            pub = publicationManager.update(pub);
            pub = publicationManager.getPublicationById(pub.getId(), pubFs);
            assertTrue(pub.getExcludedCategories().contains(cat1));
            assertTrue(pub.getExcludedCategories().contains(cat2));

            System.out.println("<br/>Publication Excluded Categories after Persistence and reload: <br/>");
            for (Category cat : pub.getExcludedCategories()) {
                System.out.println("Name: " + cat.getName() + " --> ID: "
                        + cat.getId() + "<br/>");
            }

            for (Category cat : excludedCategories) {
                pub.getExcludedCategories().remove(cat);
            }

            pub = publicationManager.update(pub);
            pub = publicationManager.getPublicationById(pub.getId(), pubFs);
            assertFalse(pub.getExcludedCategories().contains(cat1));
            assertFalse(pub.getExcludedCategories().contains(cat2));

            System.out.println("<br/>Publication Excluded Categories after removal of added Categories, Persistence and reload: <br/>");
            for (Category cat : pub.getExcludedCategories()) {
                System.out.println("Name: " + cat.getName() + " --> ID: "
                        + cat.getId() + "<br/>");
            }

        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAdSpaceWithInvalidId() {
        assertNull(publicationManager.getAdSpaceById(0L));
    }

    @Test
    public void testAdSpace() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Publication.class, "adSpaces", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Publication.class, "publisher", JoinType.LEFT);
        Publication publication = publicationManager.getPublicationById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        AdSpace adSpace = null;
        try {
            Set<Format> formats = new HashSet<Format>();
            formats.add(commonManager.getFormatBySystemName("banner"));
            formats.add(commonManager.getFormatBySystemName("text"));

            adSpace = publicationManager.newAdSpace(publication, name, formats);
            assertNotNull(adSpace);
            long id = adSpace.getId();
            assertTrue(id > 0);
            assertEquals(name, adSpace.getName());

            adSpace = publicationManager.getAdSpaceById(id);
            assertNotNull(adSpace);
            assertEquals(id, adSpace.getId());

            adSpace = publicationManager.getAdSpaceById(Long.toString(id));
            assertNotNull(adSpace);
            assertEquals(id, adSpace.getId());

            adSpace = publicationManager.getAdSpaceByExternalId(adSpace.getExternalID());
            assertNotNull(adSpace);
            assertEquals(adSpace.getId(), id);

            String newName =  name + " Changed";
            adSpace.setName(newName);
            adSpace = publicationManager.update(adSpace);
            adSpace = publicationManager.getAdSpaceById(adSpace.getId());
            assertEquals(newName, adSpace.getName());

            Long count = publicationManager.countAllAdSpacesForPublication(publication);
            assertTrue(count > 0L);
            List<AdSpace> adSpaces = publicationManager.getAllAdSpacesForPublication(publication);
            assertTrue(adSpaces.size() > 0);
            assertTrue(adSpaces.contains(adSpace));

            AdSpaceFilter filter = new AdSpaceFilter()
                .setName(adSpace.getName().toUpperCase(), false)
                .setPublication(publication)
                .setStatuses(Collections.singleton(adSpace.getStatus()));
            assertEquals(1, publicationManager.countAllAdSpaces(filter).intValue());
            assertTrue(publicationManager.getAllAdSpaces(filter).contains(adSpace));

            filter.setExcludedIds(Collections.singleton(adSpace.getId()));
            assertEquals(0, publicationManager.countAllAdSpaces(filter).intValue());
            assertEquals(0, publicationManager.getAllAdSpaces(filter).size());

            filter.setExcludedIds(null);
            filter.setName(adSpace.getName() + "garbarge", false);
            assertEquals(0, publicationManager.countAllAdSpaces(filter).intValue());
            assertEquals(0, publicationManager.getAllAdSpaces(filter).size());

            List<AdSpace> list = new ArrayList<AdSpace>();
            int nbAdSpaces = 10;
            for(int i = 0;i < nbAdSpaces;i++) {
                list.add(publicationManager.newAdSpace(publication, name, formats));
            }
            adSpaces = publicationManager.getAllAdSpacesForPublication(publication);
            assertTrue(adSpaces.size() > 0);
            for(AdSpace as : list) {
                assertTrue(adSpaces.contains(as));
            }
            for(AdSpace as : list) {
                publicationManager.delete(as);
                assertNull(publicationManager.getAdSpaceById(as.getId()));
            }

            list = publicationManager.getAllAdSpacesForPublisher(publication.getPublisher(), new Sorting(asc("name")));
            assertTrue(list.contains(adSpace));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            publicationManager.delete(adSpace);
            assertNull(publicationManager.getAdSpaceById(adSpace.getId()));
        }
    }
    
    @Test
    public void testCountUnverifiedAdSlotsForPublisher() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        try {
            Long count = publicationManager.countUnverifiedAdSlotsForPublisher(publisher);
            System.out.println(count);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testGetUnverifiedAdSlotsForPublisherCountMap() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        try {
            Map<Publication,Long> map = publicationManager.getUnverifiedAdSlotsForPublisherCountMap(publisher);
            Set<Entry<Publication,Long>> entries = map.entrySet();
            for(Entry<Publication,Long> entry : entries) {
                System.out.println(entry.getKey().getName() + ": " + entry.getValue());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testCountHouseAdEligibleAdSlotsForPublisher() {
        Publisher publisher = publisherManager.getPublisherById(1L);

        List<PublicationType> publicationTypes = new ArrayList<PublicationType>();
        publicationTypes.add(publicationManager.getPublicationTypeById(1L));
        publicationTypes.add(publicationManager.getPublicationTypeById(3L));

        try {
            Long count = publicationManager.countHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes);
            System.out.println(count);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testGetHouseAdEligibleAdSlotsForPublisher() {
        Publisher publisher = publisherManager.getPublisherById(1L);

        List<PublicationType> publicationTypes = new ArrayList<PublicationType>();
        publicationTypes.add(publicationManager.getPublicationTypeById(1L));
        publicationTypes.add(publicationManager.getPublicationTypeById(3L));

        try {
            List<AdSpace> list = publicationManager.getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes);
            for(AdSpace adSpace : list) {
                System.out.println(adSpace.getName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testAdSpaceNameUnique() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Publication.class, "adSpaces", JoinType.LEFT);
        Publication publication = publicationManager.getPublicationById(1L, fs);
        String name = "Testing" + UUID.randomUUID().toString();
        AdSpace adSpace = null;
        try {
            long count = publicationManager.countAdSpacesWithNameForPublication(name, false, publication, null);
            assertTrue(count == 0);
            assertTrue(publicationManager.isAdSpaceNameUnique(name, publication, null));
            assertTrue(publicationManager.isAdSpaceNameUnique(name.toUpperCase(), publication, null));
            assertTrue(publicationManager.isAdSpaceNameUnique(name.toLowerCase(), publication, null));

            Set<Format> formats = new HashSet<Format>();
            formats.add(commonManager.getFormatBySystemName("banner"));
            formats.add(commonManager.getFormatBySystemName("text"));

            adSpace = publicationManager.newAdSpace(publication, name, formats);
            assertNotNull(adSpace);

            count = publicationManager.countAdSpacesWithNameForPublication(name, false, publication, null);
            assertTrue(count == 1);
            assertFalse(publicationManager.isAdSpaceNameUnique(name, publication, null));
            assertFalse(publicationManager.isAdSpaceNameUnique(name.toUpperCase(), publication, null));
            assertFalse(publicationManager.isAdSpaceNameUnique(name.toLowerCase(), publication, null));

            List<AdSpace> list = publicationManager.getAdSpacesWithNameForPublication(name, false, publication, null);
            assertTrue(list.contains(adSpace));
            list = publicationManager.getAdSpacesWithNameForPublication(name, false, publication, adSpace);
            assertFalse(list.contains(adSpace));

            count = publicationManager.countAdSpacesWithNameForPublication(name, false, publication, adSpace);
            assertTrue(count == 0);
            assertTrue(publicationManager.isAdSpaceNameUnique(name, publication, adSpace));
            assertTrue(publicationManager.isAdSpaceNameUnique(name.toUpperCase(), publication, adSpace));
            assertTrue(publicationManager.isAdSpaceNameUnique(name.toLowerCase(), publication, adSpace));

            adSpace.setStatus(AdSpace.Status.DELETED);
            adSpace = publicationManager.update(adSpace);

            // Now that the AdSpace has been marked DELETED, its name should again
            // be eligible for use.
            assertTrue(publicationManager.isAdSpaceNameUnique(adSpace.getName(), publication, null));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            publicationManager.delete(adSpace);
            assertNull(publicationManager.getAdSpaceById(adSpace.getId()));
        }
    }

    @Test
    public void testGetUnallocatedAdSpaceForPublisher() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        try {
            Long count = publicationManager.countUnallocatedAdSpaceForPublisher(publisher);
            System.out.println(count);
            List<AdSpace> list = publicationManager.getUnallocatedAdSpaceForPublisher(publisher);
            for(AdSpace adSpace : list) {
                System.out.println(adSpace.getName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        }
    }

    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    @Transactional
    public void testPublicationsWithPendingAdsMap() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        try {
            Map<Publication,Long> map = publicationManager.getPublicationsWithPendingAdsMapForPublisher(publisher);
            Set<Entry<Publication,Long>> entries = map.entrySet();
            for(Entry<Publication,Long> entry : entries) {
                System.out.println(entry.getKey().getName() + ": " + entry.getValue());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetPublicationTypeWithInvalidId() {
        assertNull(publicationManager.getPublicationTypeById(0L));
    }

    @Test
    public void testPublicationType() {
        PublicationType publicationType = null;
        String name = "Testing";
        String systemName = "SystemName Testing";
        Medium medium = Medium.SITE;
        TrackingIdentifierType defaultTrackingIdentifierType = TrackingIdentifierType.COOKIE;
        IntegrationType defaultIntegrationType = null;
        for (long id = 1; defaultIntegrationType == null && id < 100; ++id) {
            defaultIntegrationType = publicationManager.getIntegrationTypeById(id);
        }
        assumeNotNull(defaultIntegrationType);
        try {
            publicationType = publicationManager.newPublicationType(name, systemName, medium, defaultTrackingIdentifierType, defaultIntegrationType);
            assertNotNull(publicationType);
            long id = publicationType.getId();
            assertTrue(id > 0L);

            assertEquals(publicationType, publicationManager.getPublicationTypeById(id));
            assertEquals(publicationType, publicationManager.getPublicationTypeById(Long.toString(id)));

            String newName = name + " Changed";
            publicationType.setName(newName);
            publicationType = publicationManager.update(publicationType);
            publicationType = publicationManager.getPublicationTypeById(publicationType.getId());
            assertEquals(newName, publicationType.getName());

            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(PublicationType.class, "platforms", JoinType.LEFT);
            publicationType = publicationManager.getPublicationTypeById(publicationType.getId(), fs);

            Platform iOsPlatform = deviceManager.getPlatformByName("iOS");
            publicationType.getPlatforms().add(iOsPlatform);
            publicationType = publicationManager.update(publicationType);
            publicationType = publicationManager.getPublicationTypeById(publicationType.getId(), fs);
            assertNotNull(publicationType);
            assertTrue(publicationType.getPlatforms().contains(iOsPlatform));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if (publicationType != null) {
                publicationManager.delete(publicationType);
                assertNull(publicationManager.getPublicationTypeById(publicationType.getId()));
            }
        }
    }

    @Test
    public void testGetAllPublicationType() {
        Number count = publicationManager.countAllPublicationTypes();
        List<PublicationType> list = publicationManager.getAllPublicationTypes();
        assertEquals(count.intValue(), list.size());

        for(PublicationType pt : list) {
            System.out.println(pt.getName());
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetPublicationWithInvalidId() {
        assertNull(publicationManager.getPublicationById(0L));
    }

    @Test
    public void testPublication() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        PublicationType publicationType = publicationManager.getPublicationTypeById(1L);
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        Publication publication = null;
        try {
            publication = new Publication(publisher);
            publication.setName(name);
            publication.setPublicationType(publicationType);
            publication.setCategory(category);
            publication = publicationManager.create(publication);
            assertNotNull(publication);
            long id = publication.getId();
            assertTrue(id > 0);
            assertEquals(name, publication.getName());

            assertEquals(publication, publicationManager.getPublicationById(id));
            assertEquals(publication, publicationManager.getPublicationById(Long.toString(id)));
            assertEquals(publication, publicationManager.getPublicationByExternalId(publication.getExternalID()));
            assertEquals(publication, publicationManager.getPublicationByName(publication.getName(), publisher));
            assertEquals(publication, publicationManager.getPublicationByIdOrExternalId(publication.getExternalID()));
            assertEquals(publication, publicationManager.getPublicationByIdOrExternalId(Long.toString(publication.getId())));

            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(Publisher.class, "publications", JoinType.LEFT);
            publisher = publisherManager.getPublisherById(publisher.getId(), fs);
            assertTrue(publisher.getPublications().contains(publication));

            publication.setAdOpsStatus(Publication.AdOpsStatus.MORE_INFO_REQUIRED);
            publication = publicationManager.update(publication);

            PublicationFilter filter = new PublicationFilter();
            filter.setStatuses(Collections.singletonList(publication.getStatus()));
            filter.setAdOpsStatuses(Collections.singleton(Publication.AdOpsStatus.MORE_INFO_REQUIRED));
            assertTrue(publicationManager.getAllPublications(filter).contains(publication));

            filter = new PublicationFilter();
            filter.setAdOpsStatuses(Collections.singleton(Publication.AdOpsStatus.HIGHER_APPROVAL_REQUIRED));
            assertFalse(publicationManager.getAllPublications(filter).contains(publication));
            
            publication.setAutoApproval(true);
            publication = publicationManager.update(publication);

            filter = new PublicationFilter();
            filter.setAutoApproval(false);
            assertFalse(publicationManager.getAllPublications(filter).contains(publication));

            filter.setAutoApproval(true);
            assertTrue(publicationManager.getAllPublications(filter).contains(publication));
            
            publication.setAutoApproval(false);
            publication = publicationManager.update(publication);
            filter.setAutoApproval(false);
            assertTrue(publicationManager.getAllPublications(filter).contains(publication));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if (publication != null) {
                publicationManager.delete(publication);
                assertNull(publicationManager.getPublicationById(publication.getId()));
            }
        }
    }

    @Test
    public void testPublicationNameUnique() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        PublicationType publicationType = publicationManager.getPublicationTypeById(1L);
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        Publication publication = null;
        try {
            long count = publicationManager.countPublicationsWithNameForPublisher(name, false, publisher, null);
            assertTrue(count == 0);
            assertTrue(publicationManager.isPublicationNameUnique(name, publisher, null));

            publication = new Publication(publisher);
            publication.setName(name);
            publication.setPublicationType(publicationType);
            publication.setCategory(category);
            publication = publicationManager.create(publication);
            assertNotNull(publication);

            count = publicationManager.countPublicationsWithNameForPublisher(name, false, publisher, null);
            assertTrue(count == 1);
            assertFalse(publicationManager.isPublicationNameUnique(name, publisher, null));

            List<Publication> list = publicationManager.getPublicationsWithNameForPublisher(name, false, publisher, null);
            assertTrue(list.contains(publication));
            list = publicationManager.getPublicationsWithNameForPublisher(name, false, publisher, publication);
            assertFalse(list.contains(publication));

            count = publicationManager.countPublicationsWithNameForPublisher(name, false, publisher, publication);
            assertTrue(count == 0);
            assertTrue(publicationManager.isPublicationNameUnique(name, publisher, publication));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            publicationManager.delete(publication);
            assertNull(publicationManager.getPublicationById(publication.getId()));
        }
    }

    @Test
    public void testPublicationDelete() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        PublicationType publicationType = publicationManager.getPublicationTypeById(1L);
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        Publication publication = null;
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Publication.class, "adSpaces", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Publication.class, "publisher", JoinType.LEFT);
        try {
            publication = new Publication(publisher);
            publication.setName(name);
            publication.setPublicationType(publicationType);
            publication.setCategory(category);
            publication = publicationManager.create(publication);
            assertNotNull(publication);
            long id = publication.getId();
            assertTrue(id > 0);
            assertEquals(name, publication.getName());

            publication = publicationManager.getPublicationById(publication.getId(), fs);

            Set<Format> formats = new HashSet<Format>();
            formats.add(commonManager.getFormatBySystemName("banner"));
            formats.add(commonManager.getFormatBySystemName("text"));

            AdSpace adSpace1 = publicationManager.newAdSpace(publication, name, formats);
            assertNotNull(adSpace1);
            id = adSpace1.getId();
            assertTrue(id > 0);
            assertEquals(name, adSpace1.getName());

            AdSpace adSpace2 = publicationManager.newAdSpace(publication, name, formats);
            assertNotNull(adSpace2);
            id = adSpace2.getId();
            assertTrue(id > 0);
            assertEquals(name, adSpace2.getName());

            publication = publicationManager.getPublicationById(publication.getId(), fs);
            assertTrue(publication.getAdSpaces().contains(adSpace1));
            assertTrue(publication.getAdSpaces().contains(adSpace2));

            for(AdSpace adSpace : publication.getAdSpaces()) {
                System.out.println(adSpace.getName() + " - " + adSpace.getId());
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            try {
                publicationManager.delete(publication);
                assertNull(publicationManager.getPublicationById(publication.getId()));
            } catch(Exception e) {
                String stackTrace = ExceptionUtils.getStackTrace(e);
                System.out.println(stackTrace);
                fail(stackTrace);
            }
        }
    }

    @Test
    public void testPublicationFilter() {
        try {
            PublicationFilter filter = new PublicationFilter();

            System.out.println("ps contained, applying to both");
            filter.setFriendlyName("ps", LikeSpec.CONTAINS, false, true);
            List<Publication> list = publicationManager.getAllPublications(filter);
            for(Publication entry : list) {
                assertTrue(entry.getName().toLowerCase().indexOf("ps") != -1 || entry.getFriendlyName().toLowerCase().indexOf("ps") != -1);
                System.out.println(entry.getName() + " - " + entry.getFriendlyName());
            }
            System.out.println("ps contained, applying to friendly name only");
            filter.setFriendlyName("ps", LikeSpec.CONTAINS, false, false);
            list = publicationManager.getAllPublications(filter);
            for(Publication entry : list) {
                assertTrue(entry.getFriendlyName().toLowerCase().indexOf("ps") != -1);
                System.out.println(entry.getName() + " - " + entry.getFriendlyName());
            }
            System.out.println("ps1 strict equals, applying to friendly name only");
            filter.setFriendlyName("ps1", false);
            list = publicationManager.getAllPublications(filter);
            for(Publication entry : list) {
                assertEquals(entry.getFriendlyName().toLowerCase(), "ps1");
                System.out.println(entry.getName() + " - " + entry.getFriendlyName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testTransparentNetwork01_noDescription() throws Exception {
        TransparentNetwork tn = null;
        try {
            String name = "Testing" + UUID.randomUUID().toString();
            tn = publicationManager.newTransparentNetwork(name, null);
            assertNotNull(tn);
            assertEquals(name, tn.getName());
            assertNull(tn.getDescription());

            String newName = name + "Changed";
            tn.setName(newName);
            tn = publicationManager.update(tn);
            tn = publicationManager.getTransparentNetworkById(tn.getId());
            assertNotNull(tn);

            assertEquals(newName, tn.getName());

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            publicationManager.delete(tn);
            assertNull(publicationManager.getTransparentNetworkById(tn.getId()));
        }
    }

    @Test
    public void testTransparentNetwork02_emptyDescription() throws Exception {
        TransparentNetwork tn = null;
        try {
            String name = "Testing" + UUID.randomUUID().toString();
            tn = publicationManager.newTransparentNetwork(name, "");
            assertNotNull(tn);
            assertEquals(name, tn.getName());
            assertNull(tn.getDescription());
        } finally {
            publicationManager.delete(tn);
            assertNull(publicationManager.getTransparentNetworkById(tn.getId()));
        }
    }

    @Test
    public void testTransparentNetwork03_blankDescription() throws Exception {
        TransparentNetwork tn = null;
        try {
            String name = "Testing" + UUID.randomUUID().toString();
            tn = publicationManager.newTransparentNetwork(name, "     ");
            assertNotNull(tn);
            assertEquals(name, tn.getName());
            assertNull(tn.getDescription());
        } finally {
            publicationManager.delete(tn);
            assertNull(publicationManager.getTransparentNetworkById(tn.getId()));
        }
    }

    @Test
    public void testTransparentNetwork04_withDescription() throws Exception {
        TransparentNetwork tn = null;
        try {
            String name = "Testing" + UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();
            tn = publicationManager.newTransparentNetwork(name, description);
            assertEquals(name, tn.getName());
            assertEquals(description, tn.getDescription());
        } finally {
            publicationManager.delete(tn);
            assertNull(publicationManager.getTransparentNetworkById(tn.getId()));
        }
    }

    @Test
    public void testCheckTransparentNetworkApproval() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        TransparentNetwork network = publicationManager.getTransparentNetworkById(1L);
        try {
            Boolean rc = publicationManager.checkTransparentNetworkApproval(campaign, network);
            System.out.println(rc);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    @Transactional
    public void testGetAvailableTransparentNetworksForCompany() {
        Company company = companyManager.getCompanyById(2L);
        boolean includePremium = false;
        try {
            List<TransparentNetwork> list = publicationManager.getAvailableTransparentNetworksForCompany(company, includePremium);
            if(list != null) {
                for(TransparentNetwork network : list) {
                    System.out.println(network.getName());
                }
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testWithoutFs() {
        publicationManager.getDefaultRateCardById(1L);
    }
    
    @Test
    public void testWithSimpleFs() {
        FetchStrategy simpleFs = new FetchStrategyBuilder()
            .addInner(DefaultRateCard_.rateCard)
            .build();
        
        publicationManager.getDefaultRateCardById(1L, simpleFs);
    }

    @Test
    public void testTwoStepFetch() {
        FetchStrategy simpleFs = new FetchStrategyBuilder()
            .addInner(DefaultRateCard_.rateCard)
            .build();
        
        DefaultRateCard drc = publicationManager.getDefaultRateCardById(1L, simpleFs);
        
        FetchStrategy rateCardFs = new FetchStrategyBuilder()
            .addLeft(RateCard_.minimumBidMap)
            .build();

        publicationManager.getRateCardById(drc.getRateCard().getId(), rateCardFs);
    }
    
    @Test
    public void testWithDeepFs() {
        FetchStrategy deepFs = new FetchStrategyBuilder()
            .addInner(DefaultRateCard_.rateCard)
            .addLeft(RateCard_.minimumBidMap)
            .build();

        // This throws NPE:
        publicationManager.getDefaultRateCardById(1L, deepFs);
        /*
          java.lang.NullPointerException
              at org.hibernate.loader.BasicLoader.isBag(BasicLoader.java:99)
              at org.hibernate.loader.BasicLoader.postInstantiate(BasicLoader.java:78)
              at org.hibernate.loader.hql.QueryLoader.<init>(QueryLoader.java:122)
              at org.hibernate.hql.ast.QueryTranslatorImpl.doCompile(QueryTranslatorImpl.java:208)
              at org.hibernate.hql.ast.QueryTranslatorImpl.compile(QueryTranslatorImpl.java:138)
              at org.hibernate.engine.query.HQLQueryPlan.<init>(HQLQueryPlan.java:101)
              at org.hibernate.engine.query.HQLQueryPlan.<init>(HQLQueryPlan.java:80)
              at org.hibernate.engine.query.QueryPlanCache.getHQLQueryPlan(QueryPlanCache.java:124)
              at org.hibernate.impl.AbstractSessionImpl.getHQLQueryPlan(AbstractSessionImpl.java:156)
              at org.hibernate.impl.AbstractSessionImpl.createQuery(AbstractSessionImpl.java:135)
              at org.hibernate.impl.SessionImpl.createQuery(SessionImpl.java:1770)
              at org.hibernate.ejb.AbstractEntityManagerImpl.createQuery(AbstractEntityManagerImpl.java:468)
              at org.hibernate.ejb.criteria.CriteriaQueryCompiler.compile(CriteriaQueryCompiler.java:227)
              at org.hibernate.ejb.AbstractEntityManagerImpl.createQuery(AbstractEntityManagerImpl.java:603)
              at com.byyd.middleware.dao.jpa.BusinessKeyDaoJpaImpl.getCriteriaApiQuery(BusinessKeyDaoJpaImpl.java:575)
              at com.byyd.middleware.dao.jpa.BusinessKeyDaoJpaImpl.find(BusinessKeyDaoJpaImpl.java:582)
              at com.byyd.middleware.dao.jpa.DefaultRateCardDaoJpaImpl.getByBidType(DefaultRateCardDaoJpaImpl.java:27)
              at com.byyd.middleware.service.jpa.publicationManagerJpaImpl.getDefaultRateCardByBidType(publicationManagerJpaImpl.java:3038)
        */
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testRateCard() {
        RateCard card = null;
        try {
            double minimumBid = 12.34;
            double defaultMinimumBid = 10.00;
            Country country = commonManager.getCountryByIsoCode("US");

            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(RateCard.class, "minimumBidMap", JoinType.LEFT);
            card = publicationManager.newRateCard(country, BigDecimal.valueOf(defaultMinimumBid), BigDecimal.valueOf(minimumBid), fs);
            assertNotNull(card);
            long id = card.getId();
            assertTrue(id > 0L);
            assertEquals(card.getDefaultMinimum().doubleValue(), defaultMinimumBid, 0);
            assertEquals(card.getMinimumBid(country).doubleValue(), minimumBid, 0);

            card = publicationManager.getRateCardById(id, fs);
            assertNotNull(card);
            assertEquals(card.getId(), id);

            card = publicationManager.getRateCardById(Long.toString(id), fs);
            assertNotNull(card);
            assertEquals(card.getId(), id);

            double newMinimumBid = 56.78;
            card.setMinimumBid(country, BigDecimal.valueOf(newMinimumBid));
            card = publicationManager.update(card);
            card = publicationManager.getRateCardById(card.getId(), fs);
            assertEquals(card.getMinimumBid(country).doubleValue(), newMinimumBid, 0);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(card != null) {
                publicationManager.delete(card);
                assertNull(publicationManager.getRateCardById(card.getId()));
            }
        }
     }

    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testIntegrationType() {
        String name = "Testing" + System.currentTimeMillis();
        String systemName = "SystemTesting" + System.currentTimeMillis();
        IntegrationType integrationType = null;
        try {
            integrationType = publicationManager.newIntegrationType(name, systemName);
            assertNotNull(integrationType);
            assertTrue(integrationType.getId() > 0);

            assertEquals(integrationType, publicationManager.getIntegrationTypeById(integrationType.getId()));
            assertEquals(integrationType, publicationManager.getIntegrationTypeById(Long.toString(integrationType.getId())));

            String newSystemName = systemName + "Changed";
            integrationType.setSystemName(newSystemName);
            integrationType = publicationManager.update(integrationType);

            integrationType = publicationManager.getIntegrationTypeById(integrationType.getId());
            assertEquals(integrationType.getSystemName(), newSystemName);

            List<IntegrationType> list = publicationManager.getAllIntegrationTypes();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(integrationType));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            publicationManager.delete(integrationType);
            assertNull(publicationManager.getIntegrationTypeById(integrationType.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------
    
    @Test
    public void induceNastyCrossJoin() {
        FetchStrategy fs = new FetchStrategyBuilder()
            .addInner(Publication_.publisher)
            .addInner(Publisher_.company)
            .addLeft(Company_.accountManager)
            .addInner(Publication_.publicationType)
            .addLeft(Publication_.assignedTo)
            .build();
        
        PublicationFilter filter = new PublicationFilter()
            .setStatuses(Publication.Status.PENDING)
            .setAccountManagerEmailContains("zhustar@gmail.com");

        Pagination pagination = new Pagination(0, 25, new Sorting(Direction.DESC, "submissionTime"));

        List<Publication> pubs = publicationManager.getAllPublications(filter, pagination, fs);
        System.out.println("Query returned " + pubs.size());
    }
    
    @Test
    public void testTransactionalRunner_callable() {
        try {
            transactionalRunner.callTransactional(
                    new Callable<Integer>() {
                        public Integer call() throws Exception {
                            return publicationDeleteMustFail();
                        }
                    }
                );
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }

    }

    @Test
    public void testTransactionalRunner_runnable() {
        try {
            transactionalRunner.runTransactional(new Runnable() {
                    public void run() {
                        try {
                            publicationDeleteMustFail();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }

    @Test
    public void testTransactionalRunner_target() {
        try {
            transactionalRunner.runTransactional(this, "publicationDeleteMustFail");
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }

    @Test
    public void testTransactionalRunner_target_noSuchMethod() {
        try {
            transactionalRunner.runTransactional(this, "publicationDeleteMustFailXXXXXX");
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }

    
    public Integer publicationDeleteMustFail() throws Exception {
        Publisher publisher = publisherManager.getPublisherById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        PublicationType publicationType = publicationManager.getPublicationTypeById(1L);
        Category category = commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME);
        Publication publication = null;

        publication = new Publication(publisher);
        publication.setName(name);
        publication.setPublicationType(publicationType);
        publication.setCategory(category);
        publication = publicationManager.create(publication);
        assertNotNull(publication);
        long id = publication.getId();
        assertTrue(id > 0);
        System.out.println("Publication ID: " + id);
        assertEquals(name, publication.getName());

        throw new Exception("Making shit fail");
    }
    
    @Test
    public void testPublicationHistory() {
        Publisher publisher = publisherManager.getPublisherById(1L);
        PublicationType publicationType = publicationManager.getPublicationTypeById(1L);
        String name = "TestPublication-" + randomHexString(20);
        assertTrue(publicationManager.isPublicationNameUnique(name, publisher, null));
        Publication publication = new Publication(publisher);
        publication.setName(name);
        publication.setPublicationType(publicationType);
        publication.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));
        try {
            publication = publicationManager.create(publication);

            assertTrue(publicationManager.getPublicationHistory(publication).isEmpty());
            
            PublicationHistory history = publicationManager.newPublicationHistory(publication);
            assertNotNull(history);
            assertTrue(history.getId() > 0);

            history = publicationManager.getPublicationHistoryById(history.getId());
            assertNotNull(history);

            assertEquals(publication.getStatus(), history.getStatus());

            assertEquals(1, publicationManager.getPublicationHistory(publication).size());
            assertTrue(publicationManager.getPublicationHistory(publication).contains(history));

            publication.setStatus(Publication.Status.ACTIVE);
            publication = publicationManager.update(publication);

            PublicationHistory history2 = publicationManager.newPublicationHistory(publication);
            history2 = publicationManager.getPublicationHistoryById(history2.getId());
            assertNotNull(history2);

            assertEquals(publication.getStatus(), history2.getStatus());
            
            assertEquals(2, publicationManager.getPublicationHistory(publication).size());
            assertTrue(publicationManager.getPublicationHistory(publication).contains(history));
            assertTrue(publicationManager.getPublicationHistory(publication).contains(history2));
        } finally {
            if (publication != null) {
                // history is cascade-deleted, so we should just be able to
                // delete the publication itself
                publicationManager.delete(publication);
            }
        }
    }

    @Test
    public void testPublicationWatchers() {
        FetchStrategy fs = new FetchStrategyBuilder().addLeft(Publication_.watchers).build();
        Publication publication = publicationManager.getPublicationById(1L, fs);

        publication.getWatchers().clear();
        publicationManager.update(publication);
        publication = publicationManager.getPublicationById(1L, fs);
        assertTrue(publication.getWatchers().isEmpty());
        
        AdfonicUser watcher1 = userManager.getAdfonicUserById(9L);
        publication.getWatchers().add(watcher1);
        publicationManager.update(publication);
        publication = publicationManager.getPublicationById(1L, fs);
        assertEquals(1, publication.getWatchers().size());
        assertTrue(publication.getWatchers().contains(watcher1));
        
        AdfonicUser watcher2 = userManager.getAdfonicUserById(4L);
        publication.getWatchers().add(watcher2);
        publicationManager.update(publication);
        publication = publicationManager.getPublicationById(1L, fs);
        assertEquals(2, publication.getWatchers().size());
        assertTrue(publication.getWatchers().contains(watcher1));
        assertTrue(publication.getWatchers().contains(watcher2));
        
        publication.getWatchers().clear();
        publicationManager.update(publication);
    }
    
    //----------------------------------------------------------------------------------------------------------------
    
    protected void comparePublicationCollections(Set<Publication> set, List<Publication> list) {
        for(Publication p : list) {
            assertTrue(set.contains(p));
        }
        for(Publication p : set) {
            assertTrue(list.contains(p));
        }
    }
    protected void comparePublicationCollections(Set<Publication> set1, Set<Publication> set2) {
        for(Publication p : set1) {
            assertTrue(set2.contains(p));
        }
        for(Publication p : set2) {
            assertTrue(set1.contains(p));
        }
    }

    @Test
    public void testPublicationList() {
        PublicationList pl = null;
           PublicationList pl2 = null;
         try {
            Company company = companyManager.getCompanyById(2L);
            Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
            Set<Publication> publicationSet = new HashSet<>();
            int noPubs = 100;
            for(long i = 0;i < noPubs;i++) {
                Publication pub = publicationManager.getPublicationById(i);
                if(pub != null) {
                    publicationSet.add(pub);
                }
            }
            String name = "Testing";
            pl = new PublicationList();
            pl.setCompany(company);
            pl.setPublicationListLevel(PublicationListLevel.COMPANY_LEVEL);
            pl.setName(name);
            pl.setWhiteList(true);
            pl.setSnapshotDateTime(new Date());
            pl.setPublications(publicationSet);
            pl = publicationManager.create(pl);
            
            assertTrue(publicationManager.getAllPublicationListsForCompany(company).contains(pl));
            
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(PublicationList_.publications)
                               .build();
            
            pl = publicationManager.getPublicationListById(pl.getId(), fs);
            comparePublicationCollections(publicationSet, pl.getPublications());
            
            PublicationListFilter filter = new PublicationListFilter();
            filter.setCompany(company);
            filter.setWhiteList(true);
            filter.setPublicationListLevel(PublicationListLevel.COMPANY_LEVEL);
            assertTrue(publicationManager.getAllPublicationLists(filter).contains(pl));
            
            // We are passing database field names, not entity variable names. So, all caps
            //Sorting sorting = new Sorting(asc(Publication.class, "name"), asc(Publisher.class, "name"), asc(PublicationType.class, "name"));
            Sorting sorting = new Sorting(desc(Publication.class, "id"));
            List<Publication> list = publicationManager.getPublicationsForPublicationList(pl, sorting);
            for(Publication publication : list) {
                System.out.println(publication.getId() + ": " + publication.getName());
            }
            comparePublicationCollections(publicationSet, list);
            
            list = new ArrayList<>();
            int offset = 0;
            int limit = 25;
            Pagination page = new Pagination(offset, limit);
            List<Publication> l = null;
            int iterations = 0;
            do {
                iterations++;
                l = publicationManager.getPublicationsForPublicationList(pl, page);
                System.out.println("Iteration " + iterations + ": " + l.size() + " records");
                list.addAll(l);
                offset += limit;
                page = new Pagination(offset, limit);
            } while(l != null && l.size() > 0);
            System.out.println("Total iterations: " + iterations + ", final size: " + list.size());
            assertTrue(iterations > 1);
            comparePublicationCollections(publicationSet, list);
            
            // I know this makes no sense, as the same list cant be both black and white, but what
            // is tested here is persistence, not business logic. Unless otherwise specified, the UI
            // handles these things
            company.setPublicationWhiteList(pl);
            company.setPublicationBlackList(pl);
            
            company = companyManager.update(company);
            assertEquals(company.getPublicationBlackList(), pl);
            assertEquals(company.getPublicationWhiteList(), pl);
            
             try {
                   publicationManager.delete(pl);
                fail("We were able to delete a PublicationList still pointed to by a Company");
             } catch(Exception e) {
                    company.setPublicationWhiteList(null);
                company.setPublicationBlackList(null);
                companyManager.update(company);
             }

            
            Campaign campaign = campaignManager.getCampaignById(1L);
            if(campaign != null) {
                campaign.setInventoryTargetingType(InventoryTargetingType.WHITELIST);
                campaign.setPublicationList(pl);
                campaign.setStatus(Status.ACTIVE);
                campaignManager.update(campaign);
            }
            
            assertTrue(campaignManager.getCampaignsForPublicationList(pl).contains(campaign));
            
            campaign.setStatus(Status.DELETED);
            campaign = campaignManager.update(campaign);
            
            assertFalse(campaignManager.getCampaignsForPublicationList(pl).contains(campaign));
            
            FetchStrategy campaignFs = new FetchStrategyBuilder()
                                       .addLeft(Campaign_.publicationList)
                                       .addLeft(PublicationList_.publications)
                                       .build();
            campaign = campaignManager.getCampaignById(1L, campaignFs);
            assertNotNull(campaign.getPublicationList());
            assertEquals(campaign.getPublicationList(), pl);
             comparePublicationCollections(publicationSet, campaign.getPublicationList().getPublications());
             
             try {
                if(pl != null) {
                    publicationManager.delete(pl);
                }
                pl = null;
             } catch(Exception e) {
                fail("We were unable to delete a PublicationList still pointed to by a Campaign");
             }
             
            campaign = campaignManager.getCampaignById(1L, campaignFs);
             assertNull(campaign.getPublicationList());
             
            pl2 = new PublicationList();
            pl2.setCompany(company);
            pl2.setAdvertiser(advertiser);
            pl2.setPublicationListLevel(PublicationListLevel.ADVERTISER_LEVEL);
            pl2.setName(name + "-adv");
            pl2.setWhiteList(true);
            pl2.setSnapshotDateTime(new Date());
            pl2.setPublications(publicationSet);
            pl2 = publicationManager.create(pl2);

               filter = new PublicationListFilter();
            filter.setCompany(company);
            filter.setAdvertiser(advertiser);
            filter.setWhiteList(true);
            filter.setPublicationListLevel(PublicationListLevel.ADVERTISER_LEVEL);
            assertTrue(publicationManager.getAllPublicationLists(filter).contains(pl2));
            
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(pl != null) {
                publicationManager.delete(pl);
            }
            if(pl2 != null) {
                publicationManager.delete(pl2);
            }
        }
    }

    @Test
    public void testPublicationSorting() {
          try {
            Sorting sorting = new Sorting(asc(Publication.class, "name"), asc(Publisher.class, "name"), asc(PublicationType.class, "name"));
            System.out.println(sorting.toString(true));
            List<Publication> list = publicationManager.getAllPublications(new PublicationFilter().setNameLike("PUBLISHER", LikeSpec.CONTAINS, false), sorting);
            for(Publication publication : list) {
                System.out.println(publication.getId() + ": " + publication.getName());
            }
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
         }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testPublicationAttributes() {
           try {
               FetchStrategy fs = new FetchStrategyBuilder()
                                  .addLeft(Publication_.publicationAttributes)
                                  .build();
               Publication publication = publicationManager.getPublicationById(1L, fs);
               publication.setSoftFloor(true);
               publication = publicationManager.update(publication);
               publication = publicationManager.getPublicationById(1L, fs);
               assertTrue(publication.getSoftFloor());
               
               publication.setSoftFloor(false);
               publication = publicationManager.update(publication);
               publication = publicationManager.getPublicationById(1L, fs);
               assertFalse(publication.getSoftFloor());
               
               publication.getPublicationAttributes().clear();
               publication = publicationManager.update(publication);
               publication = publicationManager.getPublicationById(1L, fs);
               assertTrue(publication.getPublicationAttributes().size() == 0);
               assertFalse(publication.getSoftFloor());
               
           } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testAdSpaceFormat() {
        FetchStrategyImpl asfs = new FetchStrategyImpl();
        //asfs.addEagerlyLoadedFieldForClass(AdSpace.class, "formats");
        asfs.addEagerlyLoadedFieldForClass(AdSpace.class, "publication", JoinType.LEFT);
        AdSpace adSpace = publicationManager.getAdSpaceById(1L, asfs);
        assertNotNull(adSpace);
        assertNotNull(adSpace.getFormats());
        assertNotNull(adSpace.getPublication());
        System.out.println("pub " + adSpace.getPublication().getId());
        System.out.println("formats " + adSpace.getFormats().size());
    }
}
