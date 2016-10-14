package com.adfonic.tasks.combined.consumers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Category;
import com.adfonic.domain.Format;
import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.PublicationBundle;
import com.adfonic.domain.PublicationBundle_;
import com.adfonic.domain.PublicationProvidedInfo;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.util.KeyedSynchronizer;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.PublicationBundleFilter;
import com.byyd.middleware.publication.service.PublicationManager;

@Component
public class RtbPersistenceHandler {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    static final String FALLBACK_RTB_INTEGRATION_TYPE_SYSTEM_NAME = "rtb";

    @Autowired
    private CommonManager commonManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private DormantAdSpaceReactivator dormantAdSpaceReactivator;

    final LRUMap lruLookupMap;
    final LRUMap lruBundleLookupMap;

    private final KeyedSynchronizer<String> keyedSynchronizer = new KeyedSynchronizer<>();

    @Autowired
    EntityAuditor entityAuditor;

    @Value("${RtbPersistenceHandler.auditor.enabled:false}")
    private boolean isAuditorEnabled;

    @Autowired
    public RtbPersistenceHandler(@Value("${RtbPersistenceHandler.lruLookupMap.capacity}") int lruLookupMapCapacity) {
        LOG.debug("LRU lookup map capacity: {}", lruLookupMapCapacity);
        lruLookupMap = new LRUMap(lruLookupMapCapacity);
        lruBundleLookupMap = new LRUMap(lruLookupMapCapacity);
    }

    public void onRtbPublicationPersistenceRequest(Map msg) {
        boolean auditorStatusOnEntry = entityAuditor.setEnabledForCurrentThread(isAuditorEnabled);
        try {
            LOG.debug("Handling RTB persistence request: {}", msg);

            long publisherId = (Long) msg.get("publisher.id");
            String publicationRtbId = (String) msg.get("publication.rtbId");

            // Avoid concurrently handling the same exact request...since we
            // know very well that we get duplicate requests from across the
            // given adserver sub-shard.
            String key = publisherId + "/" + publicationRtbId;
            if (keyedSynchronizer.tryAcquire(key)) {
                // We have the lock, it's on us to handle this
                try {
                    String publicationName = (String) msg.get("publication.name");
                    String publicationTypeSystemName = (String) msg.get("publication.publicationType.systemName");
                    String publicationUrlString = (String) msg.get("publication.urlString");
                    List<String> publicationIabIds = (List<String>) msg.get("publication.iabIds");
                    Integer sellerNetworkId = (Integer) msg.get("publicationProvidedInfo.sellerNetworkId");
                    String bundleName = (String) msg.get("publication.bundle");
                    handleRtbPublicationPersistenceRequest(publisherId, publicationRtbId, publicationName, publicationTypeSystemName, publicationUrlString, publicationIabIds,
                            sellerNetworkId, bundleName);
                } catch (Exception e) {
                    LOG.error("Failure while handling: {} {}", msg, e);
                } finally {
                    keyedSynchronizer.release(key);
                }
            } else {
                LOG.debug("Another thread is already handling {}", key);
            }
        } finally {
            entityAuditor.setEnabledForCurrentThread(auditorStatusOnEntry);
        }
    }

    public void onRtbBundlePersistenceRequest(Map msg) {
        LOG.debug("Handling RTB Bundle creation request: {}", msg);

        Long publicationId = (Long) msg.get("publication.id");
        String bundleName = (String) msg.get("publication.bundle");

        try {
            Publication publication = publicationManager.getPublicationById(publicationId);
            if (publication != null) {
                establishBundleInformation(publication, bundleName);
            } else {
                LOG.error("Failure while handling RTB Bundle creation request, publication Id does not exist: {}", msg);
            }

        } catch (Exception e) {
            LOG.error("Failure while handling RTB Bundle creation request: {} {}", msg, e);
        }
    }

    private void handleRtbPublicationPersistenceRequest(long publisherId, String publicationRtbId, String publicationName, String publicationTypeSystemName,
            String publicationUrlString, List<String> publicationIabIds, Integer sellerNetworkId, String bundle) {
        // Before we involve the database, let's make sure we haven't already
        // handled this particular combination.  Construct a C/P map key.
        String lruKey = publisherId + "/" + publicationRtbId;
        synchronized (lruLookupMap) {
            if (lruLookupMap.containsKey(lruKey)) {
                LOG.debug("Already handled: {}", lruKey);
                return;
            }

            // Note that we've handled this one already.  I'm choosing to do this here
            // instead of at the end of the method.  This is optimistic...or "confident"
            // that we'll handle it.  We just don't want a zillion other threads all
            // trying to handle the same thing at the same time and entering into
            // race conditions, i.e. one inserts the Publication, the other inserts
            // the AdSpace, that sort of thing.
            lruLookupMap.put(lruKey, true);
        }

        Publisher publisher = publisherManager.getPublisherById(publisherId, new FetchStrategyBuilder().addLeft(Publisher_.defaultIntegrationTypeMap).build());
        if (publisher == null) {
            // I think this is severe, not just warning.  It means adserver
            // thinks publisher id=xxx exists but it actually doesn't.  I don't
            // think it's ever gonna happen, but you never know...
            LOG.error("WTF?! Publisher id={} not found", publisherId);
            return;
        }

        LOG.debug("Handling Publisher.id={} ({}), Publication.rtbId={}", publisher.getId(), publisher.getName(), publicationRtbId);

        PublicationType publicationType = publicationManager.getPublicationTypeBySystemName(publicationTypeSystemName);
        if (publicationType == null) {
            LOG.error("WTF?! PublicationType not found: {}", publicationTypeSystemName);
            return;
        }

        // Create the Publication and its associated single AdSpace
        establishPublicationAndAdSpace(publisher, publicationType, publicationRtbId, publicationName, publicationUrlString, publicationIabIds, sellerNetworkId, bundle);
    }

    public void onRtbAdSpaceAddFormatRequest(Map msg) {
        LOG.debug("Handling RTB AdSpace add Format request: {}", msg);

        long adSpaceId = (Long) msg.get("adSpace.id");
        String formatSystemName = (String) msg.get("format.systemName");

        try {
            Format format = commonManager.getFormatBySystemName(formatSystemName);
            if (format == null) {
                LOG.error("WTF?! Invalid format specified: {}", formatSystemName);
                return;
            }

            AdSpace adSpace = publicationManager.getAdSpaceById(adSpaceId);
            if (adSpace == null) {
                // I think this is severe, not just warning.  It means adserver
                // thinks AdSpace id=xxx exists but it actually doesn't.  I don't
                // think it's ever gonna happen, but you never know...
                LOG.error("WTF?! AdSpace id={} not found", adSpaceId);
                return;
            }

            if (adSpace.getFormats().contains(format)) {
                LOG.debug("AdSpace id={} already has Format \"{}\"", adSpace.getId(), format.getSystemName());
                return;
            }

            LOG.info("Adding Format \"{}\" to AdSpace id={}", format.getSystemName(), adSpace.getId());

            adSpace.getFormats().add(format);
            /*adSpace =*/publicationManager.update(adSpace);
        } catch (Exception e) {
            LOG.error("Failure while handling: {} {}", msg, e);
        }
    }

    /**
     * Look up a Publication, creating it on the fly if necessary.
     * When auto-creating, an AdSpace will also be auto-generated.
     */
    void establishPublicationAndAdSpace(Publisher publisher, PublicationType publicationType, String rtbId, String name, String urlString, List<String> iabIds,
            Integer sellerNetworkId, String bundle) {
        // Before we bother, let's see if it already exists, i.e. another consumer
        // already handled this.  The nature of adserver being clustered means we'll
        // get duplicate requests for the exact same Publisher/Publication rtbId combo.
        // Note that the fetch strategy includes statedCategories.  We do this since
        // if the Publication exists but the AdSpace doesn't, we may end up adding
        // statedCategories to the Publication when we create the AdSpace.  We will
        // at least be checking statedCategories.
        Publication publication = publicationManager.getPublicationByPublisherAndRtbId(publisher, rtbId, new FetchStrategyBuilder().addLeft(Publication_.statedCategories).build());
        if (publication != null) {
            // Already exists...assume the other consumer has set it up properly
            LOG.debug("Publication id={} already exists for Publisher.id={} and rtbId={}", publication.getId(), publisher.getId(), rtbId);

            // Before we bail, let's check to make sure the publication's single
            // AdSpace isn't DORMANT.  If it is, we need to reactivate it.
            // AF-186 - we used to use publication.getAdSpaces().iterator().next() here,
            // but we found out the hard way that some Publications have no AdSpaces.
            // I believe they were failing to be created because we weren't doing
            // the re-query-after-unique-constraint-violation approach on the AdSpace
            // persist originally...which we are now.  But anyway, watch out for the
            // case where a Publication has no AdSpace.
            AdSpace adSpace = getRtbAdSpaceByPublication(publication);
            if (adSpace != null) {
                // The AdSpace exists already, so just check to see if it's DORMANT
                if (AdSpace.Status.DORMANT.equals(adSpace.getStatus()) && Publication.Status.ACTIVE.equals(publication.getStatus())) {
                    // Yup, it was DORMANT, so we can simply reactivate it now
                    LOG.debug("Reactivating DORMANT AdSpace " + adSpace.getId() + " / " + adSpace.getExternalID());
                    dormantAdSpaceReactivator.reactivateDormantAdSpace(adSpace.getExternalID());
                }
                return; // either way, the AdSpace exists already, so we're done
            }

            // AF-186 - no AdSpaces for the Publication...that's ok, we can deal
            LOG.warn("Publication id={} has no AdSpaces...recreating one now", publication.getId());
        } else {
            // It doesn't exist yet, so create the new Publication
            publication = createRtbPublication(publisher, publicationType, rtbId, name, urlString, iabIds, sellerNetworkId);

            // Ensure that the Publication is hydrated sufficiently in terms of
            // the relationships we may need in createRtbAdSpace().
            publication = publicationManager.getPublicationById(publication.getId(),
                    new FetchStrategyBuilder().addLeft(Publication_.statedCategories).addLeft(Publication_.publicationProvidedInfos).build());
        }

        // Create the AdSpace
        createRtbAdSpace(publication, iabIds);

        // Manage bundleId information
        if ((publicationType.getMedium() == Medium.APPLICATION) && (StringUtils.isNotBlank(bundle))) {
            establishBundleInformation(publication, bundle);
        }
    }

    Publication createRtbPublication(Publisher publisher, PublicationType publicationType, String rtbId, String name, String urlString, List<String> iabIds, Integer sellerNetworkId) {
        // AF-1511 - we used to just always use the "rtb" IntegrationType.
        // Now we'll see if the Publisher has a default configured for the
        // given PublicationType.
        IntegrationType integrationType = publisher.getDefaultIntegrationType(publicationType);
        if (integrationType == null) {
            // Fall back on the regular "rtb" IntegrationType
            integrationType = publicationManager.getIntegrationTypeBySystemName(FALLBACK_RTB_INTEGRATION_TYPE_SYSTEM_NAME);
        }

        Publication publication = new Publication(publisher);
        publication.setPublicationType(publicationType);
        publication.setStatus(Publication.Status.PENDING); // AF-1129 // MAX-49
        publication.setSafetyLevel(PublicationSafetyLevel.UN_CATEGORISED); // MAD-954 Ravi
        publication.setRtbId(rtbId);
        publication.setName(name);
        publication.setAutoApproval(true);
        publication.setBackfillEnabled(false);
        publication.setDefaultIntegrationType(integrationType);
        publication.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));

        PublicationProvidedInfo ppInfo = new PublicationProvidedInfo(publication);
        ppInfo.setInfoType(PublicationProvidedInfo.InfoType.SellerNetworkId);
        ppInfo.setIntegerValue(sellerNetworkId);
        publication.getPublicationProvidedInfos().add(ppInfo);

        if (urlString != null) {
            publication.setURLString(urlString);
        }

        if (iabIds != null) {
            for (String iabId : iabIds) {
                Category category = commonManager.getCategoryByIabId(iabId);
                if (category == null) {
                    LOG.warn("Unrecognized Category IAB id: {}", iabId);
                } else {
                    LOG.debug("Adding stated category id={}, iabId={} to Publication id={}", category.getId(), category.getIabId(), publication.getId());
                    publication.getStatedCategories().add(category);
                }
            }
        }

        publication.setDisclosed(publisher.isDisclosed());

        //  We're about to submit
        publication.setSubmissionTime(new Date());

        LOG.trace("Attempting to persist Publication rtbId={}", publication.getRtbId());
        publication = publicationManager.create(publication);

        // Ok, we've successfully created the new Publication
        LOG.debug("Created new Publication id={} for Publisher id={} and rtbId={}", publication.getId(), publisher.getId(), rtbId);

        return publication;
    }

    AdSpace createRtbAdSpace(Publication publication, List<String> iabIds) {
        if (iabIds != null) {
            boolean anyUpdates = false;
            for (String iabId : iabIds) {
                Category category = commonManager.getCategoryByIabId(iabId);
                if (category == null) {
                    LOG.warn("Unrecognized Category IAB id: {}", iabId);
                } else if (!publication.getStatedCategories().contains(category)) {
                    LOG.debug("Adding stated category id={}, iabId={} to Publication id={}", category.getId(), category.getIabId(), publication.getId());
                    publication.getStatedCategories().add(category);
                    anyUpdates = true;
                }
            }
            if (anyUpdates) {
                publicationManager.update(publication);
            }
        }

        LOG.trace("Attempting to persist AdSpace for Publication id={}", publication.getId());
        AdSpace adSpace = publicationManager.newAdSpace(publication, "Auto-Generated", commonManager.getAllFormats());
        LOG.trace("Created new AdSpace id={} for Publication id={}", adSpace.getId(), publication.getId());

        adSpace.setStatus(AdSpace.Status.VERIFIED);
        adSpace.setBackfillEnabled(false);
        adSpace = publicationManager.update(adSpace);

        // Ok, we've successfully created the new AdSpace
        LOG.debug("Created new RTB AdSpace id={} for Publication id={}", adSpace.getId(), publication.getId());

        return adSpace;
    }

    AdSpace getRtbAdSpaceByPublication(Publication publication) {
        List<AdSpace> adSpaces = publicationManager.getAllAdSpacesForPublication(publication);
        return adSpaces.isEmpty() ? null : adSpaces.iterator().next();
    }

    private void establishBundleInformation(Publication publication, String bundleName) {
        if (StringUtils.isNotBlank(bundleName)) {
            PublicationBundle bundle = getBundle(bundleName);
            if (bundle != null) {
                publicationManager.addPublicationToBundle(bundle, publication);
            } else {
                LOG.error("Can not establish bundle information for publication id {} and bundle name \"{}\"", new Object[] { publication.getId(), bundleName });
            }
        } else {
            LOG.info("Can not establish empty bundle information for publication id {}", publication.getId());
        }
    }

    private PublicationBundle getBundle(String bundleName) {
        PublicationBundle bundle = null;
        Long id = null;

        synchronized (lruBundleLookupMap) {
            if (lruBundleLookupMap.containsKey(bundleName)) {
                LOG.debug("Already handled: {}", bundleName);
                id = (Long) lruBundleLookupMap.get(bundleName);
            }
        }

        if (id != null) {
            bundle = publicationManager.getBundleById(id, new FetchStrategyBuilder().addLeft(PublicationBundle_.publications).build());
        } else {
            // Checking if bundle already exist
            PublicationBundleFilter filter = new PublicationBundleFilter().setName(bundleName);
            filter.setNameCaseSensitive(false);
            List<PublicationBundle> bundles = publicationManager.getAllPublicationBundles(filter, true);

            if (CollectionUtils.isEmpty(bundles)) { // Not bundle information exists, we will create one
                bundle = publicationManager.create(new PublicationBundle(bundleName));
                LOG.info("New bundle information created \"{}\"({})", new Object[] { bundleName, bundle.getId() });
            } else {
                bundle = bundles.get(0);
                if (bundles.size() > 1) {
                    LOG.warn("More than 1 bundle information found for bundle \"{}\": {} matches", new Object[] { bundleName, bundles.size() });
                }
            }
            synchronized (lruBundleLookupMap) {
                lruBundleLookupMap.put(bundleName, bundle.getId());
            }
        }
        return bundle;
    }
}
