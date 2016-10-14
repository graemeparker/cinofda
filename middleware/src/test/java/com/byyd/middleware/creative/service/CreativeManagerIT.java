package com.byyd.middleware.creative.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeAttribute;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class CreativeManagerIT extends AbstractAdfonicTest {

    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private AssetManager assetManager;
    
    @Autowired
    private PublicationManager publicationManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private TargetingManager targetingManager;
    
    @Autowired
    private FeeManager feeManager;
    
    @Autowired
    private DeviceManager deviceManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    
    @Autowired
    private CommonManager commonManager;

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetCreativeWithInvalidId() {
        assertNull(creativeManager.getCreativeById(0L));
    }

    @Test
    public void testCreative() {
        FetchStrategy fs = new FetchStrategyBuilder().addLeft(Campaign_.creatives).build();
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        Creative creative = null;
        try {

            assertTrue(creativeManager.isCreativeNameUnique(name, campaign, null));

            creative = creativeManager.newCreative(campaign, segment, format, name);
            assertNotNull(creative);
            long id = creative.getId();
            assertTrue(id > 0L);
            assertEquals(name, creative.getName());
            assertEquals(campaign, creative.getCampaign());

            assertFalse(creativeManager.isCreativeNameUnique(name, campaign, null));
            assertTrue(creativeManager.isCreativeNameUnique(name, campaign, creative));

            assertTrue(creativeManager.countCreativesWithNameForCampaign(name, campaign, null) > 0);

            campaign = campaignManager.getCampaignById(1L, fs);
            assertTrue(campaign.getCreatives().contains(creative));

            CreativeFilter filter = new CreativeFilter();
            filter.setStatuses(Collections.singletonList(creative.getStatus()));
            filter.setCampaign(campaign);
            filter.setName(name.toUpperCase(), false); // case-insensitive name test
            assertTrue(creativeManager.countAllCreatives(filter) > 0);
            assertTrue(creativeManager.getAllCreatives(filter).contains(creative));

            filter = new CreativeFilter();
            filter.setStatuses(Collections.singletonList(creative.getStatus()));
            filter.setCampaign(campaign);
            filter.setExcludedIds(Collections.singleton(creative.getId())); // test exclusion by id
            assertFalse(creativeManager.getAllCreatives(filter).contains(creative));

            filter = new CreativeFilter();
            Set<Creative.Status> statuses = new HashSet<Creative.Status>();
            for (Creative.Status status : Creative.Status.values()) {
                // Add every status *other* than the creative's status
                if (!status.equals(creative.getStatus())) {
                    statuses.add(status);
                }
            }
            filter.setStatuses(statuses);
            assertFalse(creativeManager.getAllCreatives(filter).contains(creative));

            creative = creativeManager.getCreativeById(id);
            assertNotNull(creative);
            assertEquals(id, creative.getId());

            creative = creativeManager.getCreativeById(Long.toString(id));
            assertNotNull(creative);
            assertEquals(id, creative.getId());

            Creative c = creativeManager.getCreativeByExternalId(creative.getExternalID());
            assertNotNull(c);
            assertEquals(c.getId(), creative.getId());

            String newName = name + " Changed";
            creative.setName(newName);
            creative = creativeManager.update(creative);
            creative = creativeManager.getCreativeById(creative.getId());
            assertEquals(newName, creative.getName());

            List<Creative> list = new ArrayList<Creative>();
            int nbCreatives = 10;
            for(int i = 0;i < nbCreatives;i++) {
                list.add(creativeManager.newCreative(campaign, segment, format, name + UUID.randomUUID().toString()));
            }
            campaign = campaignManager.getCampaignById(1L, fs);
            List<Creative> creatives = campaign.getCreatives();
            assertTrue(creatives.size() > 0);
            for(Creative ct : list) {
                assertTrue(creatives.contains(ct));
            }
            for(Creative as : list) {
                creativeManager.delete(as);
                assertNull(publicationManager.getAdSpaceById(as.getId()));
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(creative);
            assertNull(creativeManager.getCreativeById(creative.getId()));
        }
    }

    @Test
    public void testCreative2() {
        FetchStrategy fs = new FetchStrategyBuilder().addLeft(Campaign_.creatives).build();
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        Creative creative = null;
        try {

            assertTrue(creativeManager.isCreativeNameUnique(name, campaign, null));

            creative = campaign.makeNewCreative(segment, null); // Start with no format
            creative.setName(name);
            creative.setFormat(format);
            creative = creativeManager.newCreative(creative);
            assertNotNull(creative);
            long id = creative.getId();
            assertTrue(id > 0L);
            assertEquals(name, creative.getName());
            assertEquals(campaign, creative.getCampaign());

            assertFalse(creativeManager.isCreativeNameUnique(name, campaign, null));
            assertTrue(creativeManager.isCreativeNameUnique(name, campaign, creative));

            assertTrue(creativeManager.countCreativesWithNameForCampaign(name, campaign, null) > 0);

            campaign = campaignManager.getCampaignById(1L, fs);
            assertTrue(campaign.getCreatives().contains(creative));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(creative);
            assertNull(creativeManager.getCreativeById(creative.getId()));
        }
    }

    @Test
    public void testEligibleCreatives() {
        //Publication publication = creativeManager.getPublicationById(560L);
        Publication publication = publicationManager.getPublicationById(1L);
        try {
            List<Creative> approvedList = creativeManager.getApprovedCreativesForPublication(publication);
            if(approvedList != null) {
                for(Creative creative : approvedList) {
                    System.out.println(creative.getName());
                }
            }

            //publication = creativeManager.getPublicationById(4092L);
            publication = publicationManager.getPublicationById(1L);
            List<Creative> deniedList = creativeManager.getDeniedCreativesForPublication(publication);
            if(deniedList != null) {
                for(Creative creative : deniedList) {
                    System.out.println(creative.getName());
                }
            }
        } finally {
        }
    }

    @Test
    @Transactional
    public void testCreativeUpdate() {
        try {
            Creative newCreative = null;
            FetchStrategy creativeFs = new FetchStrategyBuilder()
                                    .addLeft(Creative_.campaign)
                                    .addLeft(Creative_.segment)
                                    .addInner(Creative_.format)
                                    .addLeft(Creative_.assetBundleMap)
                                    .addInner(Campaign_.advertiser)
                                    .build();
            newCreative = creativeManager.getCreativeById(1L, creativeFs);
            String destinationURL = "http://www.chez-pierre.com/";
            List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
            BeaconUrl beacon = new BeaconUrl("http://www.chez-pierre.com/");
            beacons.add(beacon);
            newCreative.setDestination(
                    creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(
                        newCreative.getCampaign().getAdvertiser(),        //Advertiser advertiser,
                        DestinationType.URL,                                //DestinationType destinationType,
                        destinationURL,                                    //String data,
                        true, //boolean createIfNotFound)
                        beacons //beacons 
                        ));                                        

            FetchStrategy formatFs = new FetchStrategyBuilder()
                                        .addLeft(Format_.displayTypes)
                                        .addLeft(Format_.components)
                                        .build();
            Format format = commonManager.getFormatById(newCreative.getFormat().getId(), formatFs);
            newCreative.setFormat(format);
            System.out.println(newCreative.getCampaign().getName());
            newCreative = creativeManager.update(newCreative);
            System.out.println(newCreative.getCampaign().getName());
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    @Transactional
    public void testCreativeUpdate2() {
        try {
            Creative newCreative = null;
            FetchStrategy creativeFs = new FetchStrategyBuilder()
                                    .addLeft(Creative_.campaign)
                                    .build();
            newCreative = creativeManager.getCreativeById(1L, creativeFs);
            newCreative.setName("Name" + System.currentTimeMillis());
            System.out.println(newCreative.getCampaign().getName());
            newCreative = creativeManager.update(newCreative);
            System.out.println(newCreative.getCampaign().getName());
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testCreativeSort() {
        try {
            Campaign campaign = campaignManager.getCampaignById(1L);
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addInner(Creative_.campaign)
                               .build();
            List<Creative> creatives = creativeManager.getAllCreativesForCampaign(campaign, new Sorting(asc(Campaign.class, "name")), fs);
            for(Creative creative : creatives) {
                System.out.println(creative.getName() + " - " + creative.getCampaign().getName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }

    }
    
    @Test
    public void testCreativeForCampaign() {
        try {
            Campaign campaign = campaignManager.getCampaignById(614L);
            System.out.println(campaign.getExternalID());
            FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder()
                                                    .addLeft(Creative_.destination)
                                                    .addInner(Creative_.format)
                                                    //.addLeft(Creative_.assetBundleMap)
                                                    .addLeft(Creative_.campaign)
                                                    .build();
            List<Creative> creatives = creativeManager.getAllCreativesForCampaign(campaign, CREATIVE_FETCH_STRATEGY);
            for(Creative creative : creatives) {
                System.out.println(creative.getId() + " - " + creative.getName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testPublicationRemoval() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Creative.class, "removedPublications", JoinType.LEFT);
        Creative creative = creativeManager.getCreativeById(3L, fs);
        Publication publication = publicationManager.getPublicationById(1L);
        RemovalInfo.RemovalType removalType = RemovalInfo.RemovalType.AD_OPS;
        try {
            creative = creativeManager.removePublicationFromCreative(creative, publication, removalType);
            assertTrue(creative.isPublicationRemoved(publication));
        } finally {
        }

    }
    
    @Test
    public void testApproveDenyCreativeForPublication() {
        try {
            Publication publication = publicationManager.getPublicationById(1L);
            Creative creative = creativeManager.getCreativeById(3L);
            publication = creativeManager.approveCreativeForPublication(publication, creative);
            List<Creative> approvedCreatives = creativeManager.getApprovedCreativesForPublication(publication);
            List<Creative> deniedCreatives = creativeManager.getDeniedCreativesForPublication(publication);
            assertTrue(approvedCreatives.contains(creative));
            assertTrue(!deniedCreatives.contains(creative));
            publication = creativeManager.denyCreativeForPublication(publication, creative);
            approvedCreatives = creativeManager.getApprovedCreativesForPublication(publication);
            deniedCreatives = creativeManager.getDeniedCreativesForPublication(publication);
            assertTrue(!approvedCreatives.contains(creative));
            assertTrue(deniedCreatives.contains(creative));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
       } finally {
        }
    }

    @Test
    public void testCreativeCreationDeux() {
        FetchStrategy campaignFs = new FetchStrategyBuilder()
                                   .addLeft(Campaign_.creatives)
                                   .addInner(Campaign_.advertiser)
                                   .build();
        FetchStrategy creativeFs = new FetchStrategyBuilder()
                                   .addLeft(Creative_.campaign)
                                   .addLeft(Creative_.segment)
                                   .addInner(Creative_.format)
                                   .addLeft(Creative_.destination)
                                   .build();

        Campaign campaign = campaignManager.getCampaignById(1L, campaignFs);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        String url = "http://www.destination.com/";
        List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
        BeaconUrl beacon = new BeaconUrl("http://www.destination.com/");
        beacons.add(beacon);
        Destination destination = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(
                campaign.getAdvertiser(),        //Advertiser advertiser,
                DestinationType.URL,                                //DestinationType destinationType,
                url,                                //String data,
                true,
                beacons);
        Creative creative = null;
        try {
           creative = campaign.makeNewCreative(segment, null);
           creative.setFormat(format);
           creative.setName(name);
           creative = creativeManager.newCreative(creative, creativeFs);

           creative.setDestination(destination);
           creative = creativeManager.update(creative);
           creative = creativeManager.getCreativeById(creative.getId(), creativeFs);
           System.out.println("Destination ID: " + creative.getDestination().getId());

           campaign = campaignManager.getCampaignById(1L, campaignFs);
           assertTrue(campaign.getCreatives().contains(creative));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(creative);
            assertNull(creativeManager.getCreativeById(creative.getId()));
        }
    }

    @Test
    public void testDeleteCreative() {
        FetchStrategyImpl campaignFs = new FetchStrategyImpl();
        campaignFs.addEagerlyLoadedFieldForClass(Campaign.class, "creatives", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, campaignFs);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        String assetName = "TestingAsset";
        int nbAssets = 5;
        ContentType contentType = commonManager.getContentTypeByName("Text");
        int nbDisplayTypes = 7;
        Creative creative = null;
        try {
            FetchStrategyImpl creativeFs = new FetchStrategyImpl();
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "campaign", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "segment", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "format", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "assetBundleMap", JoinType.LEFT);
            //creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "categories");
            creativeFs.addEagerlyLoadedFieldForClass(AssetBundle.class, "assetMap", JoinType.LEFT);
            creative = campaign.makeNewCreative(segment, null);
            creative.setFormat(format);
            creative.setName(name);
            creative = creativeManager.newCreative(creative, creativeFs);
            assertNotNull(creative);

            for(int dt = 1;dt <= nbDisplayTypes;dt++) {
                DisplayType displayType = deviceManager.getDisplayTypeById((long)dt);
                if (displayType == null) {
                    continue;
                }
                AssetBundle bundle = assetManager.newAssetBundle(creative, displayType);
                assertEquals(creative.getAssetBundle(displayType), bundle);

                for(int i = 1;i <= nbAssets;i++) {
                    Component component = assetManager.getComponentById((long)i);
                    if (component == null) { // tagline was removed, so we need to check
                        continue;
                    }
                    String thisAssetName = assetName + "-" + i + "-" + UUID.randomUUID().toString();
                    Asset asset = assetManager.newAsset(creative, contentType, thisAssetName.getBytes());
                    bundle.putAsset(component, asset);
                }
                bundle = assetManager.update(bundle);
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creative = creativeManager.getCreativeById(creative.getId());
            if (creative != null) {
                creativeManager.delete(creative);
                assertNull(creativeManager.getCreativeById(creative.getId()));
            }
            campaign = campaignManager.getCampaignById(campaign.getId(), campaignFs);
            if (campaign != null && creative != null) {
                assertFalse(campaign.getCreatives().contains(creative));
            }

        }
    }

    @Test
    public void testCopyCreative() {
        FetchStrategyImpl campaignFs = new FetchStrategyImpl();
        campaignFs.addEagerlyLoadedFieldForClass(Campaign.class, "creatives", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, campaignFs);
        Campaign campaignDeux = campaignManager.getCampaignById(2L, campaignFs);
        Segment segment = targetingManager.getSegmentById(2L);
        Segment copiedSegment = null;
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        String assetName = "TestingAsset";
        int nbAssets = 5;
        ContentType contentType = commonManager.getContentTypeByName("Text");
        int nbDisplayTypes = 7;
        Creative creative = null;
        Creative copiedCreative = null;
        try {
            FetchStrategyImpl creativeFs = new FetchStrategyImpl();
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "campaign", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "segment", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "format", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "assetBundleMap", JoinType.LEFT);
            creativeFs.addEagerlyLoadedFieldForClass(AssetBundle.class, "assetMap", JoinType.LEFT);
            creative = campaign.makeNewCreative(segment, null);
            creative.setFormat(format);
            creative.setName(name);
            creative = creativeManager.newCreative(campaign, segment, format, name);
            assertNotNull(creative);

            for(int dt = 1;dt <= nbDisplayTypes;dt++) {
                DisplayType displayType = deviceManager.getDisplayTypeById((long)dt);
                if (displayType == null) {
                    continue;
                }
                AssetBundle bundle = assetManager.newAssetBundle(creative, displayType);
                assertEquals(creative.getAssetBundle(displayType), bundle);

                for(int i = 1;i <= nbAssets;i++) {
                    Component component = assetManager.getComponentById((long)i);
                    if (component == null) { // tagline was removed, so we need to check
                        continue;
                    }
                    String thisAssetName = assetName + "-" + i + "-" + UUID.randomUUID().toString();
                    Asset asset = assetManager.newAsset(creative, contentType, thisAssetName.getBytes());
                    bundle.putAsset(component, asset);
                }
                bundle = assetManager.update(bundle);
            }

            copiedSegment = targetingManager.copySegment(segment, "Copy of " + segment.getName());
            copiedCreative = creativeManager.copyCreative(creative, campaignDeux, copiedSegment);

            String newCampaignName = campaignManager.getNewCampaignName(campaign);
            Campaign copiedCampaign = campaignManager.copyCampaign(campaign, true);
            assertNotNull(copiedCampaign);
            assertEquals(copiedCampaign.getName(), newCampaignName);

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creative = creativeManager.getCreativeById(creative.getId());
            if (creative != null) {
                creativeManager.delete(creative);
                assertNull(creativeManager.getCreativeById(creative.getId()));
            }
            if (copiedCreative != null) {
                creativeManager.delete(copiedCreative);
                assertNull(creativeManager.getCreativeById(copiedCreative.getId()));
            }
            if(copiedSegment != null) {
                targetingManager.delete(copiedSegment);
                assertNull(targetingManager.getSegmentById(copiedSegment.getId()));
            }
        }

    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetDestinationWithInvalidId() {
        assertNull(creativeManager.getDestinationById(0L));
    }

    @Test
    public void testDestination() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "destinations", JoinType.LEFT);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L, fs);
        DestinationType destinationType = DestinationType.URL;
        String urlString = "http://www.testing.com";
        String finalDestination = "http://www.testing.com/final";
        String finalDestinationChanged = finalDestination + "/changed";
        Destination destination = null;
        try {
            destination = creativeManager.newDestination(advertiser, destinationType, urlString, true, finalDestination);
            assertNotNull(destination);
            long id = destination.getId();
            assertTrue(id > 0L);
            // We passed a final destination, so middleware should have changed the isDataFinalDestination to false
            assertFalse(destination.isDataIsFinalDestination());
            
            destination = creativeManager.getDestinationById(id);
            assertNotNull(destination);
            assertEquals(id, destination.getId());
            
            // Testing updates of final destination while searching
            destination = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, urlString, false, null, false, finalDestinationChanged);
            assertNotNull(destination);
            assertEquals(id, destination.getId());
            assertEquals(destination.getFinalDestination(), finalDestinationChanged);

            // Testing nulling of final destination while searching
            destination = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, urlString, false, null, true, null);
            assertNotNull(destination);
            assertEquals(id, destination.getId());
            assertNull(destination.getFinalDestination());

            destination = creativeManager.getDestinationById(Long.toString(id));
            assertNotNull(destination);
            assertEquals(id, destination.getId());

            // Destination's data field is immutable
            //String newUrlString = urlString + "/changed.html";
            //destination.setData(newUrlString);
            //destination = creativeManager.update(destination);
            //destination = creativeManager.getDestinationById(destination.getId());
            //assertNotNull(destination);
            //assertEquals(newUrlString, destination.getData());

            advertiser = advertiserManager.getAdvertiserById(1L, fs);
            Set<Destination> destinations = advertiser.getDestinations();
            assertTrue(destinations.contains(destination));

            //Destination d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, newUrlString);
            //assertNotNull(d);
            //assertEquals(d.getId(), destination.getId());

            DestinationFilter filter = new DestinationFilter()
                .setAdvertiser(advertiser)
                .setDestinationTypes(Collections.singleton(destination.getDestinationType()))
                .setData(destination.getData());
            assertTrue(creativeManager.getAllDestinations(filter).contains(destination));

            filter = new DestinationFilter()
                .setAdvertiser(advertiser);
            assertTrue(creativeManager.getAllDestinations(filter).contains(destination));

            filter = new DestinationFilter()
                .setAdvertiser(advertiser)
                .setDestinationTypes(Collections.singleton(destination.getDestinationType()))
                .setData(destination.getData() + "GOBBLEDYGOOK");
            assertFalse(creativeManager.getAllDestinations(filter).contains(destination));

            Set<DestinationType> destTypes = new HashSet<DestinationType>();
            for (DestinationType destType : DestinationType.values()) {
                // Add every dest type *other* than the destinations's type
                if (!destType.equals(destination.getDestinationType())) {
                    destTypes.add(destType);
                }
            }
            filter = new DestinationFilter()
                .setAdvertiser(advertiser)
                .setDestinationTypes(destTypes)
                .setData(destination.getData());
            assertFalse(creativeManager.getAllDestinations(filter).contains(destination));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(destination);
            assertNull(creativeManager.getDestinationById(destination.getId()));
        }
    }

    @Test
    public void testDestinationDeux() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "creatives", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Creative.class, "destination", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "destinations", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "Testing" + UUID.randomUUID().toString();
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L, fs);
        DestinationType destinationType = DestinationType.URL;
        String urlString = "http://www.testing.com/" + UUID.randomUUID().toString() + "/";
        Destination destination = null;
        Destination destinationChanged = null;
        Creative creative = null;
        try {
            creative = creativeManager.newCreative(campaign, segment, format, name);
            assertNotNull(creative);
            destination = creativeManager.newDestination(advertiser, destinationType, urlString);
            assertNotNull(destination);
            assertTrue(destination.getId() > 0L);

            creative.setDestination(destination);
            creative = creativeManager.update(creative);

            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertEquals(creative.getDestination(), destination);

            String newUrlString = urlString + "changed.htrml";
            List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
            BeaconUrl beacon = new BeaconUrl(urlString + "changed.htrml");
            beacons.add(beacon);
            destinationChanged = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, newUrlString, true, beacons);
            creative.setDestination(destinationChanged);
            creative = creativeManager.update(creative);

            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertEquals(creative.getDestination(), destinationChanged);

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(destination);
            creativeManager.delete(creative);
            creativeManager.delete(destinationChanged);

        }

    }
    
    @Test
    public void testDestination3() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        DestinationType destinationType = DestinationType.URL;
        String urlString = "http://www.testing.com/" + UUID.randomUUID().toString();
        Destination destination = null;
        Creative creative = null;
        List<Creative> creativesToDelete = new ArrayList<Creative>();
        List<Destination> destinationsToDelete = new ArrayList<Destination>();
        try {
            for(int i = 0;i < 10;i++) {
                String name = "Testing" + UUID.randomUUID().toString();
                System.out.println(name);
                creative = creativeManager.newCreative(campaign, segment, format, name);
                assertNotNull(creative);
                creativesToDelete.add(creative);
                destination = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, urlString, true, null);
                assertNotNull(destination);
                destinationsToDelete.add(destination);
                System.out.println("Destination ID: " + destination.getId());
    
                creative.setDestination(destination);
                creative = creativeManager.update(creative);
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            for(Creative c : creativesToDelete) {
                creativeManager.delete(c);
            }
            for(Destination d : destinationsToDelete) {
                creativeManager.delete(d);
            }

        }
    }

    @Test
    public void testDestination4() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        Advertiser advertiser = advertiserManager.getAdvertiserById(1L);
        DestinationType destinationType = DestinationType.URL;
        String urlString = "http://www.testing.com/" + UUID.randomUUID().toString();
        List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
        BeaconUrl beacon = new BeaconUrl("Beacon" + UUID.randomUUID().toString());
        beacons.add(beacon);
        Destination destination = null;
        Creative creative = null;
        List<Creative> creativesToDelete = new ArrayList<Creative>();
        List<Destination> destinationsToDelete = new ArrayList<Destination>();
        try {
            for(int i = 0;i < 10;i++) {
                String name = "Testing" + UUID.randomUUID().toString();
                System.out.println(name);
                creative = creativeManager.newCreative(campaign, segment, format, name);
                assertNotNull(creative);
                creativesToDelete.add(creative);
                destination = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, urlString, true, beacons);
                assertNotNull(destination);
                destinationsToDelete.add(destination);
                System.out.println("Destination ID: " + destination.getId());
    
                creative.setDestination(destination);
                creative = creativeManager.update(creative);
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            for(Creative c : creativesToDelete) {
                creativeManager.delete(c);
            }
            for(Destination d : destinationsToDelete) {
                creativeManager.delete(d);
            }

        }
        
    }
    
    @Test
    public void testDestinationAO404() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(8L);
        DestinationType destinationType = DestinationType.URL;
        
        String noBeaconUrlString = "http://www.chez-pierre.com/nobeacon/";
        String beaconUrlString = "http://www.chez-pierre.com/withbeacon/";
        String beacon = "mybeacon";
        
        List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
        BeaconUrl beac = new BeaconUrl(beacon);
        beacons.add(beac);
        
        List<BeaconUrl> randomBeacons = new ArrayList<BeaconUrl>();
        beac = new BeaconUrl(UUID.randomUUID().toString());
        randomBeacons.add(beac);
        
        
        
        
        Destination noBeaconDest = null;
        Destination beaconDest = null;
        
        try {
            noBeaconDest = creativeManager.newDestination(advertiser, destinationType, noBeaconUrlString);
            beaconDest = creativeManager.newDestination(advertiser, destinationType, beaconUrlString, beacons);
             
            // We should bring back noBeaconDest
             Destination d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, noBeaconUrlString, false, null);
             assertNotNull(d);
             assertEquals(d, noBeaconDest);
             
             // We shoudlnt bring back anything
             d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, noBeaconUrlString, false, beacons);
             assertNull(d);
             
            // We shoudlnt bring back anything
             d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, beaconUrlString, false, null);
             assertNull(d);
             
            // We should bring back beaconDest
             d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, beaconUrlString, false, beacons);
             assertNotNull(d);
             assertEquals(d, beaconDest);
             
               // We shoudlnt bring back anything
             d = creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, beaconUrlString, false, randomBeacons);
             assertNull(d);
 
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            creativeManager.delete(noBeaconDest);
            creativeManager.delete(beaconDest);
        }
    }

    @Test
    public void testCreativeHistory() {
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String name = "TestCreative-" + randomHexString(20);
        assertTrue(creativeManager.isCreativeNameUnique(name, campaign, null));
        Creative creative = null;
        try {
            creative = creativeManager.newCreative(campaign, segment, format, name);

            assertTrue(creativeManager.getCreativeHistory(creative).isEmpty());
            
            CreativeHistory history = creativeManager.newCreativeHistory(creative);
            assertNotNull(history);
            assertTrue(history.getId() > 0);

            history = creativeManager.getCreativeHistoryById(history.getId());
            assertNotNull(history);

            assertEquals(creative.getStatus(), history.getStatus());

            assertEquals(1, creativeManager.getCreativeHistory(creative).size());
            assertTrue(creativeManager.getCreativeHistory(creative).contains(history));

            creative.setStatus(Creative.Status.ACTIVE);
            creative = creativeManager.update(creative);

            CreativeHistory history2 = creativeManager.newCreativeHistory(creative);
            history2 = creativeManager.getCreativeHistoryById(history2.getId());
            assertNotNull(history2);

            assertEquals(creative.getStatus(), history2.getStatus());
            
            assertEquals(2, creativeManager.getCreativeHistory(creative).size());
            assertTrue(creativeManager.getCreativeHistory(creative).contains(history));
            assertTrue(creativeManager.getCreativeHistory(creative).contains(history2));
        } finally {
            if (creative != null) {
                // history is cascade-deleted, so we should just be able to
                // delete the creative itself
                creativeManager.delete(creative);
            }
        }
    }
    
    //----------------------------------------------------------------------------------------
    @Test
    public void testCreativeAttribute() {
        CreativeAttribute attribute = null;
        
        try {
            List<CreativeAttribute> creativeAttributes = creativeManager.getAllCreativeAttributes();
            assertNotNull(creativeAttributes);
            assertTrue(creativeAttributes.size() > 0);
            
            long count = creativeManager.countAllCreativeAttributes();
            assert(count > 0);
            
            System.out.println("All creative attributes");
            for (CreativeAttribute a : creativeAttributes) {
                System.out.println(a.getName());
            }
            
            creativeAttributes = creativeManager.getAllCreativeAttributes(new Sorting(asc("name")));
            assertNotNull(creativeAttributes);
            assertTrue(creativeAttributes.size() > 0);
            System.out.println("All creative attributes, sorted name asc");
            for (CreativeAttribute a : creativeAttributes) {
                System.out.println(a.getName());
            }
            
            String name = "Testing" + System.currentTimeMillis();
            attribute = creativeManager.newCreativeAttribute(name);
            assertNotNull(attribute);
            assertTrue(attribute.getId() > 0);
            
            //update
            String newName = "Testing2" + System.currentTimeMillis();
            attribute.setName(newName);
            attribute = creativeManager.update(attribute);
            
            assertNull(creativeManager.getCreativeAttributeByName(name));
            assertNotNull(creativeManager.getCreativeAttributeByName(newName));
        } catch(Exception e) {
          String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
        } finally {
            if(attribute != null) {
                attribute = creativeManager.getCreativeAttributeById(attribute.getId());
                creativeManager.delete(attribute);
                assertNull(creativeManager.getCreativeAttributeById(attribute.getId()));
            }
        }             
    }
    
    @Test
    public void test_AO_212_SortOnRelationshipField() {
        CreativeFilter filter = new CreativeFilter()
            .setIncludedIds(1L, 2L, 3L, 4L, 5L);

        // Sort by campaign.name
        Sorting sorting = new Sorting(SortOrder.asc(Campaign.class, "name"));

        // Doesn't this force the join?
        FetchStrategy fs = new FetchStrategyBuilder()
            .addInner(Creative_.campaign)
            .build();
        
        for (Creative creative : creativeManager.getAllCreatives(filter, sorting, fs)) {
            System.out.println("Found Creative id=" + creative.getId() + ", campaign.name=" + creative.getCampaign().getName());
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCreativeLoad() {
        try {
            FetchStrategy campaignFs = new FetchStrategyBuilder()
                                       .addLeft(Campaign_.historicalRMAdServingFees)
                                       .addLeft(Campaign_.currentRichMediaAdServingFee)
                                       .build();
            FetchStrategy creativeFs = new FetchStrategyBuilder()
                                       .addInner(Creative_.campaign)
                                       .build();
            Campaign campaign1 = campaignManager.getCampaignById(1L, campaignFs);
            if(campaign1.getHistoricalRMAdServingFees() == null || campaign1.getHistoricalRMAdServingFees().size() == 0) {
                System.out.println("Creating RichMediaAdServingFees");
                for(int i = 0;i < 5;i++) {
                    feeManager.saveCampaignRichMediaAdServingFee(campaign1.getId(), BigDecimal.valueOf(1.0));
                }
            } else {
                System.out.println("Historical entries: " + campaign1.getHistoricalRMAdServingFees().size());
            }
            Creative creative = creativeManager.getCreativeById(1L, creativeFs);
            System.out.println(creative.getName());
            
            creative = creativeManager.getCreativeById(3L, creativeFs);
            System.out.println(creative.getName());

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testLoadCreativeWithRemovedPublications() {
        try {
            FetchStrategy fs = new FetchStrategyBuilder().addLeft(Creative_.removedPublications).build();
            long id = 1863L;
            Creative creative = creativeManager.getCreativeById(id, fs);
            System.out.println(creative.getRemovedPublications().size() + " removed publications");
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    @Test
    public void testCreativeFormat() {
        FetchStrategyImpl cfs = new FetchStrategyImpl();
        cfs.addEagerlyLoadedFieldForClass(Creative.class, "format", JoinType.INNER);
        cfs.addEagerlyLoadedFieldForClass(Creative.class, "removedPublications", JoinType.LEFT);
        Creative creative = creativeManager.getCreativeById(44827l, cfs);
        assertNotNull(creative);
        assertEquals(creative.getFormat().getName(), "Banner");
        assertEquals(creative.getRemovedPublications().size(), 0);
    }

}
