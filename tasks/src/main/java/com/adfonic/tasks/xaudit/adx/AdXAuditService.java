package com.adfonic.tasks.xaudit.adx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.ExtendedCreativeType_;
import com.adfonic.domain.Format_;
import com.adfonic.domain.IntegrationTypeMediaType_;
import com.adfonic.domain.IntegrationType_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.domain.Publisher_;
import com.adfonic.tasks.xaudit.RenderingService.RenderedCreative;
import com.adfonic.tasks.xaudit.impl.AuditCreativeRenderer;
import com.byyd.adx.AdxCreativeAttribute;
import com.byyd.adx.AdxRestrictedCategory;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.google.api.services.adexchangebuyer.model.Creative;
import com.google.api.services.adexchangebuyer.model.Creative.ServingRestrictions;
import com.google.api.services.adexchangebuyer.model.Creative.ServingRestrictions.DisapprovalReasons;

/**
 * Build up the necessary information to submit a creative for external approval.
 * These externally audited (approved or rejected) creatives are recorded 
 * for inspection in DomainCache via the PUBLISHER_AUDITED_CREATIVE table.
 * 
 * https://developers.google.com/ad-exchange/buyer-rest/creative-guide
 * 
 * Expiriences with AdX creative audit is that whole audit process takes +/- 1 hour, but it can also take 3 or 4 hours
 * Few minutes after submission creative is in UNKNOWN state and then is switched to NOT_CHECKED state.
 * Then creative usually goes to CONDITIONALLY_APPROVED, APPROVED or DISAPPROVED
 * 
 * CONDITIONALLY_APPROVED is difficult state and "servingRestrictions" section should be examined for reasons
 * but usually it means that it is waiting to be approved or cannot be bidded with in Russia (2643) or China (2156).
 * 
 * {
     "reason": "PENDING_REVIEW",
     "contexts": [
       { "contextType": "AUCTION_TYPE", "auctionType": [ "OPEN_AUCTION" ] },
         { "contextType": "LOCATION", "geoCriteriaId": [ 2643 ] }
     ]
   }
 * 
 */
public class AdXAuditService {

    private static final FastDateFormat FDF = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CreativeManager creativeManager;

    @Autowired
    private PublisherManager publisherManager;

    @Autowired
    private AuditCreativeRenderer renderer;

    @Autowired
    private AdXCreativeApiManager adXApi;

    private final Set<String> creativeDisplaySizes;

    private final boolean checkCreativeBeforeNewSubmit;

    private final long adXPendingGap;

    private final long publisherId;

    public static final FetchStrategy PUBLISHER_FETCH_STRATEGY = new FetchStrategyBuilder().addLeft(Publisher_.defaultIntegrationTypeMap)
            .addLeft(IntegrationType_.supportedFeatures).addLeft(IntegrationType_.supportedBeaconModes).addLeft(IntegrationType_.mediaTypeMap)
            .addLeft(IntegrationTypeMediaType_.contentForms).build();

    // beware that adding fetch for Destination.beaconUrls makes return 100 times more beacons - hibernate mapping glitch
    public static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(Creative_.campaign) //
            .addLeft(Campaign_.campaignAudiences) //
            .addInner(Campaign_.advertiser).addInner(Advertiser_.company).addLeft(Campaign_.category) //
            .addInner(Creative_.format).addInner(Format_.components).addInner(Format_.displayTypes)//
            .addLeft(Creative_.destination).addLeft(Creative_.creativeAttributes) //
            .addLeft(Creative_.assetBundleMap).addLeft(AssetBundle_.assetMap)//
            .addLeft(com.adfonic.domain.Component_.contentSpecMap).addLeft(com.adfonic.domain.ContentSpec_.contentTypes) //
            .addLeft(Creative_.extendedCreativeType).addLeft(ExtendedCreativeType_.features).addLeft(ExtendedCreativeType_.templateMap)//
            .addLeft(Creative_.extendedCreativeTemplates).build();

    /*
        public enum AdXCreativeStatus {
            APPROVED(PublisherAuditedCreative.Status.ACTIVE), //
            CONDITIONALLY_APPROVED(PublisherAuditedCreative.Status.PENDING), //
            DISAPPROVED(PublisherAuditedCreative.Status.REJECTED), //
            NOT_CHECKED(PublisherAuditedCreative.Status.PENDING), //
            UNKNOWN(PublisherAuditedCreative.Status.PENDING);
            private PublisherAuditedCreative.Status auditedCreativeStatus;

            AdXCreativeStatus(PublisherAuditedCreative.Status auditedCreativeStatus) {
                this.auditedCreativeStatus = auditedCreativeStatus;
            }

            public PublisherAuditedCreative.Status getAuditedCreativeStatus() {
                return auditedCreativeStatus;
            }
        }

        // List of the audited creative status to ignore.
        private static final Set<PublisherAuditedCreative.Status> IGNORABLE_STATUSES = EnumSet.of(//
                Status.ACTIVE,//
                //Status.CREATION_INITIATED, 
                Status.REJECTED, //
                Status.UNAUDITABLE, //
                Status.INTERNALLY_INELIGIBLE, //
                Status.MISC_UNMAPPED, //
                Status.BYPASS_ALLOW_CACHE_ONLY);

        //    private static final Set<Status> BYPASS_STATUSES = EnumSet.of(Status.BYPASS_ALLOW_CACHE_ONLY,
        //                                                                 // Status.BYPASS_ALLOW_AUDIT_ONLY,
        //                                                                 // Status.BYPASS_ALLOW_CACHE_AND_AUDIT
        //                                                                 );
    */
    public AdXAuditService(long publisherId, Set<String> creativeDisplaySizes, boolean checkCreativeBeforeNewSubmit, long adXPendingGap) {
        this.publisherId = publisherId;
        this.creativeDisplaySizes = creativeDisplaySizes;
        this.checkCreativeBeforeNewSubmit = checkCreativeBeforeNewSubmit;
        this.adXPendingGap = adXPendingGap;
    }

    public long getPublisherId() {
        return publisherId;
    }

    /**
     * Creative is first spotted in domainserializer
     */
    public void onNewCreative(long creativeId) {
        com.adfonic.domain.Creative byydCreative = creativeManager.getCreativeById(creativeId, CREATIVE_FETCH_STRATEGY);
        Publisher publisher = publisherManager.getPublisherById(publisherId, PUBLISHER_FETCH_STRATEGY);
        PublisherAuditedCreative auditRecord = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(publisher, byydCreative);
        if (auditRecord != null) {
            // Multiple DS send JMS in same time or some nasty glitch. Leave... 
            log.warn("Audit record PublisherAuditedCreative already exists for creative: " + creativeId + ", publisher: " + publisherId + ", record: " + auditRecord);
            return;
        } else {
            auditRecord = new PublisherAuditedCreative(publisher, byydCreative);
            auditRecord.setStatus(Status.CREATION_INITIATED);
            auditRecord.setExternalReference(byydCreative.getExternalID());
            auditRecord.setCreationTime(new Date());
            auditRecord = publisherManager.create(auditRecord);

            // New creative -> Submit it right away
            if (isAdXSupported(byydCreative)) {
                try {
                    Creative adxCreative = buildAdxCreative(byydCreative, publisher);
                    adxCreative = adXApi.submitAdxCreative(creativeId, adxCreative);
                    updateAuditRecord(auditRecord, Status.PENDING, null);
                } catch (Exception x) {
                    // Oh snap, but nevermind. It'll be tried later again
                    log.error("Failed to submit to AdX: " + auditRecord, x);
                    updateAuditRecord(auditRecord, Status.SUBMIT_FAILED, null);
                }
            } else {
                auditRecord.setStatus(Status.UNAUDITABLE);
                auditRecord.setLastAuditRemarks("Creative type not supported on AdX");
                publisherManager.update(auditRecord);
            }
        }
    }

    public void onScheduledCheck(long creativeId) {
        // Do not call auditRecord.getCreative() as it is not join-fetched. Use byydCreative with prefetched relations 
        com.adfonic.domain.Creative byydCreative = creativeManager.getCreativeById(creativeId, CREATIVE_FETCH_STRATEGY);
        Publisher byydPublisher = publisherManager.getPublisherById(publisherId, PUBLISHER_FETCH_STRATEGY);
        PublisherAuditedCreative auditRecord = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(byydPublisher, byydCreative);

        switch (auditRecord.getStatus()) {
        case CREATION_INITIATED:
        case LOCAL_INVALID:
            onLocalInvalid(auditRecord, byydCreative, byydPublisher);
            break;
        case SUBMIT_FAILED:
            onSumbitFailed(auditRecord, byydCreative, byydPublisher);
            break;
        case PENDING:
            onPending(auditRecord, byydCreative, byydPublisher);
            break;
        default:
            log.warn("Scheduled check for unsupported status: " + auditRecord.getStatus() + ", creative: " + creativeId);
            break;
        }
    }

    /**
     * Creative is changed and needs to be resubmitted/reapproved
     */
    private void onLocalInvalid(PublisherAuditedCreative auditRecord, com.adfonic.domain.Creative byydCreative, Publisher byydPublisher) {
        onSubmitNeeded(auditRecord, byydCreative, byydPublisher);
        // Reset message counter as it is used as failure counter 
        auditRecord.setMessageCount(1);
    }

    /**
     * Previous submit attempt failed for some reason 
     */
    private void onSumbitFailed(PublisherAuditedCreative auditRecord, com.adfonic.domain.Creative byydCreative, Publisher byydPublisher) {
        onSubmitNeeded(auditRecord, byydCreative, byydPublisher);
    }

    private void onSubmitNeeded(PublisherAuditedCreative auditRecord, com.adfonic.domain.Creative byydCreative, Publisher byydPublisher) {
        try {
            Creative adxCreative = buildAdxCreative(byydCreative, byydPublisher);
            adxCreative = adXApi.submitAdxCreative(byydCreative.getId(), adxCreative);
            updateAuditRecord(auditRecord, Status.PENDING, null);
        } catch (Exception x) {
            log.error("Failed to submit to AdX: " + auditRecord, x);
            long messageCount = auditRecord.getMessageCount();
            if (messageCount < 10) {
                // Might be temporary problem, keep trying
                updateAuditRecord(auditRecord, Status.SUBMIT_FAILED, null);
            } else {
                // Too many failures. Give up on this Creative.
                updateAuditRecord(auditRecord, Status.UNAUDITABLE, "Failed " + messageCount + " times: " + x);
                log.error("AdX audit submission failed too many times for Creative: " + byydCreative.getId() + "/" + byydCreative.getExternalID());
            }
        }
    }

    /**
     * Creative is submitted and we are waiting for result 
     */
    private void onPending(PublisherAuditedCreative auditRecord, com.adfonic.domain.Creative byydCreative, Publisher byydPublisher) {
        // Maybe check auditedCreative.getLatestFetchTime() here...
        Creative adxCreative = adXApi.getAdxCreative(auditRecord.getExternalReference());
        if (adxCreative == null) {
            // Very few very old creatives using asset external id instead of creative external id - remove this section in 2016
            log.warn("Audit record: " + auditRecord + " for creative: " + byydCreative.getId() + " is not registered in AdX API");
            // Resubmit it into AdX...
            onLocalInvalid(auditRecord, byydCreative, byydPublisher);
            return;
        }
        String adxStatus = adxCreative.getOpenAuctionStatus();
        switch (adxStatus) {
        case "APPROVED":
        case "CONDITIONALLY_APPROVED":
            updateAuditRecord(auditRecord, Status.ACTIVE, null);
            break;
        case "DISAPPROVED":
            String reasons = extractDisapprovalReasons(adxCreative);
            updateAuditRecord(auditRecord, Status.REJECTED, reasons);
            break;
        case "NOT_CHECKED":
        case "UNKNOWN":
            break; // Keep waiting for decisive state
        default:
            log.warn("Unsupported AdX creative API status: " + adxStatus + " for creative: " + byydCreative.getId() + " / " + auditRecord.getExternalReference());
        }
    }

    private boolean isAdXSupported(com.adfonic.domain.Creative byydCreative) {
        String formatName = byydCreative.getFormat().getSystemName();
        if (SystemName.FORMAT_NATIVE.equals(formatName) || SystemName.FORMAT_TEXT.equals(formatName) || formatName.startsWith(SystemName.FORMAT_VIDEO_PREFIX)) {
            //log.info("Skipping native/text creative: " + byydCreative.getId());
            return false;
        }
        return true;
    }

    public Creative buildAdxCreative(com.adfonic.domain.Creative byydCreative, Publisher publisher) throws IOException {

        Creative adxCreative = new Creative();
        adxCreative.setAccountId(adXApi.getApiAccountId());
        adxCreative.setBuyerCreativeId(byydCreative.getExternalID());

        RenderedCreative rendered = renderer.renderContent(byydCreative, publisher);

        adxCreative.setHTMLSnippet(rendered.getMarkup());
        adxCreative.setWidth(rendered.getAssetInfo().getWidth());
        adxCreative.setHeight(rendered.getAssetInfo().getHeight());
        adxCreative.setClickThroughUrl(Arrays.asList(rendered.getDestinationUrl()));

        // AdX declared attributes (may be empty)
        List<Integer> adxAttributes = new LinkedList<Integer>();

        ContentForm contentForm = rendered.getContext().getAttribute(TargetingContext.RENDERED_TRANSFORM);
        if (contentForm == ContentForm.MRAID_1_0) {
            adxAttributes.add(AdxCreativeAttribute.Mraid_1_0.getAdxId());
        }
        if (rendered.getContext().isSslRequired()) {
            adxAttributes.add(AdxCreativeAttribute.RichMediaCapabilitySSL.getAdxId());
        }

        adxCreative.setAttribute(adxAttributes);

        Category category = byydCreative.getCampaign().getCategory();
        if (category != null) {

            // AdX declared restricted categories
            Collection<AdxRestrictedCategory> adxRestricted = AdxRestrictedCategory.getByIabId(category.getIabId());
            List<Integer> listOfIds = new ArrayList<Integer>();
            for (AdxRestrictedCategory item : adxRestricted) {
                listOfIds.add(item.getAdxId());
            }
            adxCreative.setRestrictedCategories(listOfIds);
        }

        /*
        DisplayType displayType = rendered.getAssetInfo().getDisplayType();
        if (displayType == null || !creativeDisplaySizes.contains(displayType.getSystemName().toLowerCase())) {
            LOG.info("AdX Creative " + byydCreative.getId() + " DisplayType : " + displayType.getSystemName().toLowerCase() + " not allowed for sumbission: "
                    + creativeDisplaySizes);
            return null;
        }
        */

        /*
        Map<DisplayType, AssetBundle> assetBundleMap = byydCreative.getAssetBundleMap();
        DisplayType displayTypeForContentSpec = null;
        for (DisplayType displayType : assetBundleMap.keySet()) {
            displayTypeForContentSpec = displayType; // only contains one as advised by Wes
        }

        LOG.info("AdX about to render creative: " + byydCreative.getId());
        
        String renderedContent = null;
        try {
            renderedContent = renderingService.renderContent(byydCreative, displayTypeForContentSpec, publisher);

            // GSP HACK MAX-777: Dirty way to reuse the AppNexus routing controller.
            renderedContent = renderedContent.replace("http://appnexus-rtb${END_POINT}.byyd.net/anxs/ct/", "%%CLICK_URL_UNESC%%http://adx-east-rtb.byyd.net/anxs/ct/");
            renderedContent = renderedContent.replace("http://appnexus-rtb${END_POINT}.byyd.net/anxs/bc/", "http://adx-east-rtb.byyd.net/anxs/bc/"); // catch beacons
            renderedContent = renderedContent.replace("${ASP_ID}", "ASP_ID");
            renderedContent = renderedContent.replace("${AUCTION_IMP_ID}", "impression");

            // TODO send either HTMLSnippet, nativeAd (attribute setting), 
            // or videoURL (we don't really need this and VAST not on the cards for AdX yet).
            adxCreative.setHTMLSnippet(renderedContent);
        } catch (Exception e) {
            LOG.warn("AdX issue with render creative: " + byydCreative.getId() + " " + e, e);
            return null;
        }

        LOG.info("AdX Rendered content: " + renderedContent);

        // Configurable display types but only standard banner, extra large banner and fullScreen for now
        if (displayTypeForContentSpec != null && creativeDisplaySizes.contains(displayTypeForContentSpec.getSystemName().toLowerCase())) {
            com.adfonic.domain.Component component = byydCreative.getFormat().getComponent(0);
            ContentSpec contentSpec = component.getContentSpec(displayTypeForContentSpec); // only one component as advised by Wes

            Map<String, String> manifestProperties = contentSpec.getManifestProperties();
            String width = manifestProperties.get("width");
            String height = manifestProperties.get("height");
            adxCreative.setHeight(height != null ? Integer.valueOf(height) : 50); // fallback to default size
            adxCreative.setWidth(width != null ? Integer.valueOf(width) : 320);
            LOG.info("AdX Creative size determined: " + width + " x " + height);
        } else {
            LOG.info("AdX Creative " + byydCreative.getId() + " with incorrect size : " + displayTypeForContentSpec + " -- aborting submit");
            return null;
        }
        */

        String advertiserName = byydCreative.getDestination().getAdvertiser().getName();
        if (null == advertiserName || advertiserName.isEmpty()) {
            log.info("Missing Advertiser name for Creative: " + byydCreative.getId() + " Default 'Byyd' set instead");
            advertiserName = "Byyd";
        }
        adxCreative.setAdvertiserName(advertiserName);

        return adxCreative;
    }

    /*
        //@Override
        public void onCreateXXXxx(long creativeId, long publisherId) {
            com.adfonic.domain.Creative byydCreative = creativeManager.getCreativeById(creativeId, CREATIVE_FETCH_STRATEGY);
            Publisher publisher = publisherManager.getPublisherById(publisherId, PUBLISHER_FETCH_STRATEGY);

            PublisherAuditedCreative auditedCreative = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(publisher, byydCreative);
            if (auditedCreative == null) {
                log.info("AdX Creative with id " + creativeId + " not found in PUBLISHER_AUDITED_CREATIVE table, prepare to submit to AdX");
                newCreativeSubmission(byydCreative, publisher);
            } else {
                // Mirroring what happens in AuditedCreativesFilter
                if (IGNORABLE_STATUSES.contains(auditedCreative.getStatus())) {
                    log.info("AdX PublisherAuditedCreative status " + auditedCreative.getStatus() + " to be ignored. Creative id = " + creativeId);
                    return;
                }

                long lastTouch = System.currentTimeMillis() - auditedCreative.getLatestFetchTime().getTime();
                switch (auditedCreative.getStatus()) {
                case CREATION_INITIATED:
                    log.info("AdX Creative with CREATION_INITIATED updating to PENDING " + auditedCreative.getExternalReference());
                    updateAuditRecord(auditedCreative, PublisherAuditedCreative.Status.PENDING);
                    break;
                case PENDING:
                    if (lastTouch > adXPendingGap) {
                        // Call the AdX api to get status on this creative
                        Creative creative = adXApi.getAdxCreative(auditedCreative.getExternalReference());

                        if (creative != null) {
                            String adXStatus = creative.getOpenAuctionStatus();
                            log.info("AdX Creative with our id: " + creativeId + " found with status: " + adXStatus);

                            if (AdXCreativeStatus.UNKNOWN.toString().equals(adXStatus)) { // TODO check last fetch time here?
                                log.info("AdX Creative id: " + creativeId + " is UNKNOWN to Adx, try submitting it");
                                resubmitCreative(byydCreative, publisher, auditedCreative);
                                break;
                            }

                            if (AdXCreativeStatus.DISAPPROVED.toString().equals(adXStatus)) {
                                auditedCreative.setLastAuditRemarks(extractDisapprovalReasons(creative));
                            }
                            Status statusToUpdate = AdXCreativeStatus.valueOf(adXStatus).getAuditedCreativeStatus();
                            log.info("AdX Updating publisher audited creative table for creative with id " + creativeId + " and status " + statusToUpdate);
                            updateAuditRecord(auditedCreative, statusToUpdate);
                        } else {
                            log.info("AdX Creative with our id NOT found with status!" + auditedCreative.getExternalReference());
                            updateAuditRecord(auditedCreative, Status.INTERNALLY_INELIGIBLE);
                        }
                    }
                    break;
                case LOCAL_INVALID:
                    if (lastTouch > adXPendingGap) {
                        resubmitCreative(byydCreative, publisher, auditedCreative);
                    } else {
                        log.debug("AdX Creative " + creativeId + " will not be resubmitted yet");
                    }
                    break;
                default:
                    log.info("AdX Creative id: " + creativeId + " is DEFAULT to Adx, try submitting it");
                    updateAuditRecord(auditedCreative, auditedCreative.getStatus());
                    break;
                }
            }
        }

        protected void newCreativeSubmission(com.adfonic.domain.Creative byydCreative, Publisher publisher) {
            try {
                // Submit the creative for the first time
                AdXCreativeVO adXResponse = submitCreativeToApi(byydCreative, publisher, checkCreativeBeforeNewSubmit);
                log.info("AdX creativeVO: " + adXResponse + " for creative: " + byydCreative.getId());
                if (adXResponse != null) {
                    createAuditRecord(byydCreative, publisher, adXResponse);
                }
            } catch (IOException e) {
                log.warn("AdX Something went wrong during rendering this creative : " + byydCreative.getId() + " -- aborting submit" + "\n" + e.getStackTrace());
                //new AdXCreativeVO(Status.INTERNALLY_INELIGIBLE, "UNKNOWN");
            }
        }

        public AdXCreativeVO submitCreativeToApi(com.adfonic.domain.Creative byydCreative, Publisher publisher, boolean checkCreativeBeforeNewSubmit) throws IOException {

            String creativeExternalId = byydCreative.getExternalID();

            // Now we have external id we can easily check if AdX already know about this creative
            if (checkCreativeBeforeNewSubmit) {
                String adXCreativeResponse = checkCreativeExistsWithAdX(byydCreative, publisher);
                if (adXCreativeResponse != null && adXCreativeResponse != AdXCreativeStatus.UNKNOWN.toString()) {
                    log.info("AdX Creative id:" + byydCreative.getId() + " with extid: " + creativeExternalId + " FOUND");
                    return new AdXCreativeVO(AdXCreativeStatus.valueOf(adXCreativeResponse).getAuditedCreativeStatus(), creativeExternalId);
                } else if (AdXCreativeStatus.UNKNOWN.toString().equals(adXCreativeResponse)) {
                    // Problems create it so we don't keep pushing.
                    log.info("AdX Creative id:" + byydCreative.getId() + " with extid: " + creativeExternalId + ", state: CREATION_INITIATED");
                    return new AdXCreativeVO(Status.CREATION_INITIATED, creativeExternalId);
                }
                log.info("AdX Creative id:" + byydCreative.getId() + " with extid: " + creativeExternalId + " not found so try to submit it");
            }

            Creative adxCreative = buildAdxCreative(byydCreative, publisher);
            if (adxCreative == null) {
                return new AdXCreativeVO(Status.INTERNALLY_INELIGIBLE, creativeExternalId);
            }

            log.info("AdX About to submit creative... " + byydCreative.getId());
            PublisherAuditedCreative.Status submitStatus = adXApi.submitAdxCreative(byydCreative.getId(), adxCreative);
            if (submitStatus != null) {
                log.info("AdX submitted creative " + byydCreative.getId() + " received status: " + submitStatus);
            } else {
                log.info("AdX returns null status for creative " + byydCreative.getId() + ". Setting status to INTERNALLY_INELIGIBLE");
                submitStatus = Status.INTERNALLY_INELIGIBLE;
            }

            return new AdXCreativeVO(submitStatus, creativeExternalId);
        }

        private String checkCreativeExistsWithAdX(com.adfonic.domain.Creative creative, Publisher publisher) {
            Creative adXCreative = adXApi.getAdxCreative(creative.getExternalID());

            if (adXCreative != null) {
                String adXStatus = adXCreative.getOpenAuctionStatus();
                log.info("AdX Creative with our id: " + creative.getId() + " found with AdX status: " + adXStatus);

                if (!AdXCreativeStatus.UNKNOWN.toString().equals(adXStatus)) { // APPROVED, NOT_CHECKED or DISAPPROVED
                    PublisherAuditedCreative auditedCreative = createAuditRecord(creative, publisher, new AdXCreativeVO(
                            AdXCreativeStatus.valueOf(adXStatus).getAuditedCreativeStatus(), adXCreative));
                    if (AdXCreativeStatus.DISAPPROVED.toString().equals(adXStatus)) {
                        auditedCreative.setLastAuditRemarks(extractDisapprovalReasons(adXCreative));
                        updateAuditRecord(auditedCreative, AdXCreativeStatus.valueOf(adXStatus).getAuditedCreativeStatus());
                    }
                    return adXStatus;
                } else if (AdXCreativeStatus.UNKNOWN.toString().equals(adXStatus)) {
                    return adXStatus;
                }
            } else {
                log.info("AdX Creative with our id: " + creative.getId() + " NOT found with AdX");
            }
            return null;
        }

        private void resubmitCreative(com.adfonic.domain.Creative byydCreative, Publisher publisher, PublisherAuditedCreative existingAuditedCreative) {
            try {
                AdXCreativeVO submittedCreative = submitCreativeToApi(byydCreative, publisher, false);
                if (submittedCreative != null) {
                    updateAuditRecord(existingAuditedCreative, submittedCreative.getStatus());
                }
            } catch (IOException e) {
                log.warn("AdX Something went wrong during rendering this creative : " + byydCreative.getId() + " -- aborting submit" + "\n" + e);
            }
        }

        private PublisherAuditedCreative createAuditRecord(com.adfonic.domain.Creative creative, Publisher publisher, AdXCreativeVO creativeVo) {
            log.info("Inserting AdX AuditedCreative " + creativeVo.getBuyerCreativeId() + " status: " + creativeVo.getStatus());
            PublisherAuditedCreative auditedCreative = new PublisherAuditedCreative(publisher, creative);
            auditedCreative.setCreationTime(new Date());
            auditedCreative.setLatestFetchTime(new Date());
            auditedCreative.setExternalReference(creativeVo.getBuyerCreativeId()); // AdX rely on our external id as reference 'buyerCreativeId'
            auditedCreative.setStatus(creativeVo.getStatus());
            PublisherAuditedCreative record = publisherManager.create(auditedCreative);
            log.info("AdX AuditedCreative record with id: " + record.getId());
            return record;
        }
    */
    public void updateAuditRecord(PublisherAuditedCreative auditRecord, PublisherAuditedCreative.Status status, String remarks) {
        log.info("Updating AdX AuditedCreative " + auditRecord.getExternalReference() + " status: " + auditRecord.getStatus() + " to status: " + status);
        Date now = new Date();
        if (remarks != null) {
            auditRecord.setLastAuditRemarks(remarks);
        } else {
            // Build some reasonable mesage
            StringBuilder sb = new StringBuilder();
            sb.append(auditRecord.getStatus()).append("(");
            if (auditRecord.getLatestFetchTime() != null) {
                sb.append(FDF.format(auditRecord.getLatestFetchTime()));
            } else {
                sb.append(FDF.format(auditRecord.getCreationTime()));
            }
            sb.append(") -> ").append(status).append("(").append(FDF.format(now)).append(")");
            auditRecord.setLastAuditRemarks(sb.toString());
        }
        auditRecord.setMessageCount(auditRecord.getMessageCount() + 1);
        auditRecord.setLatestFetchTime(now);
        auditRecord.setStatus(status);
        publisherManager.update(auditRecord);
    }

    private String extractDisapprovalReasons(Creative adxApiCreative) {
        StringBuffer responsesBuf = new StringBuffer();
        List<ServingRestrictions> restrictions = adxApiCreative.getServingRestrictions();
        if (restrictions != null && restrictions.size() > 0) {
            responsesBuf.append("DISAPPROVED: ");
            for (ServingRestrictions restriction : restrictions) {
                if ("DISAPPROVAL".equals(restriction.getReason())) {
                    List<DisapprovalReasons> disapprovalReasons = restriction.getDisapprovalReasons();
                    for (DisapprovalReasons disapprovalReason : disapprovalReasons) {
                        responsesBuf.append(disapprovalReason.getReason());
                        responsesBuf.append(", ");
                        if (disapprovalReason.getDetails() != null) {
                            for (String disapprovalReasonDetail : disapprovalReason.getDetails()) {
                                responsesBuf.append(disapprovalReasonDetail);
                                responsesBuf.append(", ");
                            }
                        }
                    }
                }
            }
            responsesBuf.deleteCharAt(responsesBuf.lastIndexOf(","));
        }

        if (responsesBuf.length() > 1) {
            log.info("AdX Updating creative id: " + adxApiCreative.getBuyerCreativeId() + " with status " + adxApiCreative.getOpenAuctionStatus() + " and remarks "
                    + responsesBuf.toString());
            return responsesBuf.toString();
        }
        return null;
    }
    /*
    {
     "kind": "adexchangebuyer#creative",
     "accountId": 71846476,
     "buyerCreativeId": "4c68b21a-cf7f-431e-8646-d2c04f0fd95a",
     "version": 3,
     "HTMLSnippet": "<!-- Banner image -->\n<a href=\"%%CLICK_URL_UNESC%%http%3A%2F%2Fadx-emea-rtb.byyd.net%2Fct%2Fb05dfa5c-3d5f-4553-8ea5-ca64a57c4592%2Fb1e314fa-15e4-db84-b26d-7a73f84b334e\" target=\"_blank\"><img border=\"0\" alt=\"\" src=\"https://as.byyd.net/as//a586abbd-87f8-4a9d-981b-915bb74a89ea?b=0000FF\" width=\"320\" height=\"50\" /></a>\n\n<!-- Beacon 1 -->\n<img width=1 height=1 src=\"https://adx-emea-rtb.byyd.net/bc/b05dfa5c-3d5f-4553-8ea5-ca64a57c4592/b1e314fa-15e4-db84-b26d-7a73f84b334e.gif?sp=%%WINNING_PRICE%%\"/>\n",
     "clickThroughUrl": [
      "https://www.marktjagd.de/top-angebote/picks-raus:a69653?"
     ],
     "attribute": [ 47, 50 ],
     "width": 320,
     "height": 50,
     "advertiserName": "marktjagd.de",
     "dealsStatus": "APPROVED",
     "openAuctionStatus": "DISAPPROVED",
     "servingRestrictions": [
      {
       "reason": "DISAPPROVAL",
       "contexts": [
        { "contextType": "AUCTION_TYPE", "auctionType": [ "OPEN_AUCTION" ] }
       ],
       "disapprovalReasons": [
        { "reason": "MEDIA_NOT_FUNCTIONAL", "details": [ "redirection fails" ] }
       ]
      },
      {
       "reason": "PENDING_REVIEW",
       "contexts": [
        { "contextType": "AUCTION_TYPE", "auctionType": [ "OPEN_AUCTION" ] },
        { "contextType": "LOCATION", "geoCriteriaId": [ 2156 ] }
       ]
      },
      {
       "reason": "PENDING_REVIEW",
       "contexts": [
        { "contextType": "AUCTION_TYPE", "auctionType": [ "OPEN_AUCTION" ] },
        { "contextType": "LOCATION", "geoCriteriaId": [ 2643 ] }
       ]
      }
     ],
     "productCategories": [ 10010, 10004 ],
     "filteringReasons": {
      "date": "2015-12-11",
      "reasons": [
       { "filteringStatus": 10, "filteringCount": "413924" },
       { "filteringStatus": 85, "filteringCount": "154079" },
       { "filteringStatus": 18, "filteringCount": "13" }
      ]
     }
    }
    */
}
