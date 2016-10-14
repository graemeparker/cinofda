package com.adfonic.adserver.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.AdSpaceUtils;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.adserver.monitor.AdserverMonitor;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.jms.AdSpaceVerifiedMessage;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.util.stats.CounterManager;

/**
 * Abstract base class for adserver controllers
 */
public abstract class AbstractAdServerController extends WebApplicationObjectSupport {

    private static final transient Logger LOG = Logger.getLogger(AbstractAdServerController.class.getName());

    @Autowired
    private TargetingContextFactory targetingContextFactory;
    @Autowired
    private PreProcessor preProcessor;
    @Autowired
    private ImpressionService impressionService;
    @Autowired
    private TargetingEngine targetingEngine;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private BackupLogger backupLogger;
    @Autowired
    private AdSpaceUtils adSpaceUtils;
    @Autowired
    private DisplayTypeUtils displayTypeUtils;
    @Autowired
    private VhostManager vhostManager;
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;
    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    private ParallelModeBidManager parallelModeBidManager;
    @Autowired
    private AdResponseLogic adResponseLogic;
    @Autowired
    private TrackingIdentifierLogic trackingIdentifierLogic;
    @Autowired
    private StatusChangeManager statusChangeManager;
    @Autowired
    private CounterManager counterManager;
    @Autowired
    private AdserverMonitor adserverMonitor;
    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;

    /** Override this to perform any subclass-specific setup on the context */
    protected void setupTargetingContext(TargetingContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    /**
     * Generic ad-generating request handler
     * @param request the servlet request
     * @param response the servlet response
     * @param adSpaceExternalID the external ID of the AdSpace
     * @param modelMap the MVC model map
     * @param useHttpHeaders whether or not the direct HTTP headers should be
     * used when setting up the TargetingContext, or whether we should only
     * use proxied headers
     */
    @SuppressWarnings("deprecation")
    protected void handleGenerateAd(HttpServletRequest request, HttpServletResponse response, String adSpaceExternalID, ModelMap modelMap, boolean useHttpHeaders)
            throws java.io.IOException, com.adfonic.adserver.BlacklistedException {

        // Create the targeting context
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, useHttpHeaders);
        } catch (InvalidIpAddressException e) {
            // The provided IP address must have been invalid
            modelMap.addAttribute("error", e.getMessage());
            backupLogger.logAdRequestFailure(e.getMessage(), null);
            return;
        } catch (Exception e) {
            // Any other exception is unexpected...must be some sort of
            // RuntimeException...log it and return an error message
            //LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleGenerateAd", "Failed to createTargetingContext", e);
            modelMap.addAttribute("error", "No ad available due to internal error");
            backupLogger.logAdRequestFailure("exception", null, e.getClass().getName(), e.getMessage());
            return;
        }

        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            backupLogger.logAdRequestFailure("blacklisted", context, e.getMessage());
            throw e;
        }

        modelMap.addAttribute("targetingContext", context);

        // Look up the AdSpace
        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            // See if the AdSpace had been marked DORMANT
            if (context.getAdserverDomainCache().getDormantAdSpaceExternalIds().contains(adSpaceExternalID)) {
                if (LOG.isLoggable(Level.INFO)) {
                    //LOG.info("Dormant adSpaceExternalID: " + adSpaceExternalID);
                    LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "handleGenerateAd", "Dormant adSpaceExternalID: " + adSpaceExternalID);
                }
                // Queue the AdSpace for reactivation
                adSpaceUtils.reactivateDormantAdSpace(adSpaceExternalID);
                modelMap.addAttribute("error", "Reactivating dormant AdSpace, try again later");
                backupLogger.logAdRequestFailure("AdSpace dormant", context, adSpaceExternalID);
            } else {
                if (LOG.isLoggable(Level.INFO)) {
                    //LOG.info("Invalid adSpaceExternalID: " + adSpaceExternalID);
                    LoggingUtils.log(LOG, Level.INFO, null, context, this.getClass(), "handleGenerateAd", "Invalid adSpaceExternalID: " + adSpaceExternalID);
                }
                modelMap.addAttribute("error", "Invalid AdSpace ID: " + adSpaceExternalID);
                backupLogger.logAdRequestFailure("AdSpace not found", context, adSpaceExternalID);
            }
            return;
        }
        // Set it in the context, since it's required by some derivers
        context.setAdSpace(adSpace);

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("AdSpace \"" + adSpace.getName() + "\" externalID=" + adSpace.getExternalID());
            LoggingUtils.log(LOG, Level.FINE, null, context, this.getClass(), "handleGenerateAd", "AdSpace \"" + adSpace.getName() + "\" externalID=" + adSpace.getExternalID());
        }

        // #970 - Enforce that we were passed a User-Agent...previously we were
        // just letting the request go through the motions and fail, since the
        // targeting engine would end up bailing when it couldn't resolve
        // device properties.  Now we'll cut it off at the pass and give the
        // caller a more specific error message in the response, letting 'em
        // know that it was the lack of a User-Agent that caused this.
        String userAgent = context.getEffectiveUserAgent();
        if (userAgent == null) {
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_USER_AGENT);
            modelMap.addAttribute("error", "No User-Agent supplied");
            backupLogger.logAdRequestFailure("no User-Agent", context);
            return;
        }

        if (context.getAttribute(TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM, Boolean.class)) {
            if (LOG.isLoggable(Level.FINEST)) {
                //LOG.finest("Bailing on request from isRobot|isChecker|isSpam");
                LoggingUtils.log(LOG, Level.FINEST, null, context, this.getClass(), "handleGenerateAd", "Bailing on request from isRobot|isChecker|isSpam");
            }
            modelMap.addAttribute("error", "Your User-Agent is not welcome here.");
            backupLogger.logAdRequestFailure("robot/checker/spam", context);
            return;
        }

        // Generate the Impression object
        final Impression impression = new Impression();
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Generated Impression externalID=" + impression.getExternalID());
            LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "handleGenerateAd", "Generated Impression externalID=" + impression.getExternalID());
        }
        impression.setAdSpaceId(adSpace.getId());
        modelMap.addAttribute("impression", impression);

        PublicationDto pub = adSpace.getPublication();

        // Before we go any further, if the Publication.status==PENDING,
        // we need to serve up a special static test ad.
        if (Publication.Status.PENDING.equals(statusChangeManager.getStatus(pub))) {
            if (LOG.isLoggable(Level.FINE)) {
                //LOG.fine("Publication id=" + pub.getId() + " has status=PENDING, serving a test ad");
                LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "handleGenerateAd", "Publication id=" + pub.getId()
                        + " has status=PENDING, serving a test ad");
            }
            switch (pub.getPublisher().getPendingAdType()) {
            case NO_AD:
                // Unfilled request, just don't bother adding anything to modelMap
                break;
            case HOLDING_AD:
            default:
                // Generate an AdComponents for the test ad response
                try {
                    modelMap.addAttribute("adComponents", adResponseLogic.generateTestAdComponents(context, adSpace, request));
                } catch (Exception e) {
                    //LOG.log(Level.WARNING, "Failed to generate test ad components", e);
                    LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "handleGenerateAd", "Failed to generate test ad components", e);
                    modelMap.addAttribute("error", "No ad available due to internal error");
                }
                break;
            }

            // Mark the AdSpace verified if need be
            verifyAdSpace(adSpace);
            return;
        }

        // Before we bother doing any real work, let's make sure that if
        // the caller passed in specific Format(s), that the given AdSpace
        // actually supports it/them.
        Set<Long> allowedFormatIds = null;
        final String formatSpec = context.getAttribute(Parameters.FORMATS);
        if (formatSpec != null) {
            allowedFormatIds = new HashSet<Long>();
            for (String formatName : StringUtils.split(formatSpec, ',')) {
                if ("".equals(formatName = formatName.trim())) {
                    continue;
                }
                FormatDto allowedFormat = context.getDomainCache().getFormatBySystemName(formatName);
                if (allowedFormat != null) {
                    allowedFormatIds.add(allowedFormat.getId());
                } else {
                    //LOG.warning("Invalid format supplied: " + formatName);
                    LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "handleGenerateAd", "Invalid format supplied: " + formatName);
                    modelMap.addAttribute("error", "Invalid format: " + formatName);
                    backupLogger.logAdRequestFailure("invalid Format", context, formatName);
                    return;
                }
            }

            // Make sure the AdSpace.formats intersects
            if (!CollectionUtils.containsAny(adSpace.getFormatIds(), allowedFormatIds)) {
                if (LOG.isLoggable(Level.INFO)) {
                    //LOG.info("Allowed formats (" + formatSpec + ") doesn't intersect with AdSpace id=" + adSpace.getId());
                    LoggingUtils.log(LOG, Level.INFO, impression, context, this.getClass(), "handleGenerateAd", "Allowed formats (" + formatSpec
                            + ") doesn't intersect with AdSpace id=" + adSpace.getId());
                }
                modelMap.addAttribute("error", "Format(s) not supported by ad slot");
                backupLogger.logAdRequestFailure("Format mismatch", context, StringUtils.join(adSpace.getFormatIds(), ','), StringUtils.join(allowedFormatIds, ','));
                return;
            }
        }

        SelectedCreative selectedCreative = null;
        UnfilledReason unfilledReason = null;
        CreativeDto creative = null;
        ProxiedDestination pd = null;
        AdComponents adComponents = null;
        Exception caughtException = null;
        try {
            // Allow the subclass to set up the context, if need be
            setupTargetingContext(context, request, response);

            // Target an ad if possible.  This call will completely populate the
            // Impression object, regardless of whether or not a creative was selected.
            // This is necessary because we use the Impression to populate the AdEvent
            // later one, even for unfilled requests.
            selectedCreative = targetAd(adSpace, allowedFormatIds, impression, context);

            if (selectedCreative != null) {
                creative = selectedCreative.getCreative();
                pd = selectedCreative.getProxiedDestination();

                // Generate the AdComponents object which holds all of the key,
                // um, components needed to render the ad
                adComponents = adResponseLogic.generateFullAdComponents(context, adSpace, creative, pd, impression, request);
            }
        } catch (InvalidTrackingIdentifierException e) {
            //LOG.warning(e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "handleGenerateAd", e.getMessage());
            modelMap.addAttribute("error", e.getMessage());
            unfilledReason = UnfilledReason.EXCEPTION; // TODO: give this case its own reason?
            caughtException = e;
        } catch (Exception e) {
            //LOG.log(Level.WARNING, "Failed to generate ad", e);
            LoggingUtils.log(LOG, Level.WARNING, impression, context, this.getClass(), "handleGenerateAd", "Failed to generate ad", e);
            modelMap.addAttribute("error", "No ad available due to internal error");
            unfilledReason = UnfilledReason.EXCEPTION;
            caughtException = e;
        } finally {
            // Log the ad event as needed
            AdEvent event = null;
            if (adComponents != null) {
                modelMap.addAttribute("creative", creative);

                // Post-process the AdComponents if needed, doing any text
                // substitution as required, i.e. for %man% and %phn%, etc.
                adResponseLogic.postProcessAdComponents(adComponents, context);

                if (LOG.isLoggable(Level.FINE)) {
                    //LOG.fine("Generated " + adComponents);
                    LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "handleGenerateAd.finally", "Generated " + adComponents);
                }
                modelMap.addAttribute("adComponents", adComponents);

                // Save the Impression object for subsequent clickthrough calls
                getImpressionService().saveImpression(impression);

                // Normally we need to log an AD_SERVED event, but before we do that,
                // let's see if we're handling a "parallel mode" request.
                if ("1".equals(context.getAttribute(Parameters.PARALLEL))) {
                    // It's parallel mode...instead of logging the event now,
                    // just save it as a "bid" that will either get a win notice
                    // or will expire and get logged as BID_FAILED.
                    if (LOG.isLoggable(Level.FINE)) {
                        //LOG.fine("Storing parallel mode bid details, not logging AdEvent");
                        LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "handleGenerateAd.finally",
                                "Storing parallel mode bid details, not logging AdEvent");
                    }
                    parallelModeBidManager.saveBidDetails(new ParallelModeBidDetails(context, impression), pub.getPublisher().getRtbConfig().getRtbLostTimeDuration());
                } else {
                    // Regular mode...log the event
                    event = adEventFactory.newInstance(AdAction.AD_SERVED);
                }
            } else {
                // No AdComponents means either no creative was selected, or some other
                // error occurred while rendering the AdComponents...either way this
                // should be logged as an unfilled request.
                if (LOG.isLoggable(Level.FINE)) {
                    //LOG.fine("Logging unfilled request");
                    LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "handleGenerateAd.finally", "Logging unfilled request");
                }

                // If we haven't established an unfilled reason already, see if it
                // was set by the targeting engine.
                if (unfilledReason == null) {
                    unfilledReason = context.getAttribute(TargetingContext.UNFILLED_REASON);
                    if (unfilledReason == null) {
                        unfilledReason = UnfilledReason.UNKNOWN;
                    }
                }

                event = adEventFactory.newInstance(AdAction.UNFILLED_REQUEST);
                event.setUnfilledReason(unfilledReason);
            }

            // If we need to log an AdEvent, finish populating it and then log it
            if (event != null) {
                context.populateAdEvent(event, impression, creative);
                //TODO clean up adEventLogger
//                adEventLogger.logAdEvent(event, context);

                if (AdAction.AD_SERVED.equals(event.getAdAction())) {
                    backupLogger.logAdServed(impression, event.getEventTime(), context);
                } else {
                    backupLogger.logUnfilledRequest(event.getUnfilledReason(), event.getEventTime(), context);
                }
            } else if (caughtException != null) {
                backupLogger.logAdRequestFailure("exception", context, caughtException.getClass().getName(), caughtException.getMessage());
            } else if (modelMap.containsKey("error")) {
                backupLogger.logAdRequestFailure((String) modelMap.get("error"), context);
            } // Otherwise, it's probably a parallel mode bid

            // Mark the AdSpace verified if need be
            verifyAdSpace(adSpace);
        }
    }

    protected void verifyAdSpace(AdSpaceDto adSpace) {
        // See if we need to mark the AdSpace verified
        if (AdSpace.Status.UNVERIFIED.equals(statusChangeManager.getStatus(adSpace))) {
            synchronized (adSpace) {
                if (AdSpace.Status.UNVERIFIED.equals(statusChangeManager.getStatus(adSpace))) {
                    // Queue a JMS message indicating that the AdSpace
                    // should be marked verified
                    if (LOG.isLoggable(Level.INFO)) {
                        //LOG.info("Queueing AdSpaceVerifiedMessage for AdSpace id=" + adSpace.getId());
                        LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "verifyAdSpace", "Queueing AdSpaceVerifiedMessage for AdSpace id=" + adSpace.getId());
                    }

                    jmsUtils.sendObject(centralJmsTemplate, JmsResource.ADSPACE_VERIFIED, new AdSpaceVerifiedMessage(adSpace.getId()));

                    // Also, update the domain-cached AdSpace object itself.
                    // We do this in a synchronized block so that no other
                    // thread tries to update the given cached AdSpace
                    // concurrently.
                    // NOTE: this update doesn't get persisted here, we're
                    // just modifying the detached AdSpace POJO here.
                    adSpace.setStatus(AdSpace.Status.VERIFIED);
                }
            }
        }
    }

    protected SelectedCreative targetAd(AdSpaceDto adSpace, Collection<Long> allowedFormatIds, Impression impression, TargetingContext context) {
        // Set a maximum targeting time limit according to the publication
        TimeLimit timeLimit = new TimeLimit(System.currentTimeMillis(), adSpace.getPublication().getEffectiveAdRequestTimeout());
        //overriding the targetEventListener, it always comes null from this path

        TargetingEventListener targetingEventListener = adserverMonitor.getTargetingEventListener();
        // Select the ad
        SelectedCreative selectedCreative = targetingEngine.selectCreative(adSpace, allowedFormatIds, context, false, false, timeLimit, targetingEventListener);
        if (LOG.isLoggable(Level.FINE)) {
            if (selectedCreative != null) {
                CreativeDto creative = selectedCreative.getCreative();
                //LOG.fine("Selected Creative.externalID=" + creative.getExternalID() + ", name=" + creative.getName() + ", priority=" + creative.getPriority() + ", format.id=" + creative.getFormatId());
                LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "targetAd", "Selected Creative.externalID=" + creative.getExternalID() + ", name="
                        + creative.getName() + ", priority=" + creative.getPriority() + ", format.id=" + creative.getFormatId());
            } else {
                //LOG.fine("No Creative selected");
                LoggingUtils.log(LOG, Level.FINE, impression, context, this.getClass(), "targetAd", "No Creative selected");
            }
        }

        if (selectedCreative != null) {
            // This is an extra step we take in order to expose the SelectedCreative
            // to any custom logging plugins at the event logging phase.
            context.setAttribute(TargetingContext.SELECTED_CREATIVE, selectedCreative);
        }

        // Finish populating the Impression from the context.  This is key whether
        // we selected a creative or not, since the impression fields are used to
        // build the AdEvent that gets logged, even for UNFILLED_REQUEST.
        context.populateImpression(impression, selectedCreative);

        return selectedCreative;
    }

    protected final String getPixelImageUrl(HttpServletRequest request) {
        return adserverDataCacheManager.getCache().getProperties().getProperty("asset.pixel.url");
    }

    // These getters are protected so that subclasses can access the injected
    // beans that live at this level.  They do NOT need setters, since the beans
    // get injected by Spring due to the @Autowired on the variables themselves.

    protected final DisplayTypeUtils getDisplayTypeUtils() {
        return displayTypeUtils;
    }

    protected final VhostManager getVhostManager() {
        return vhostManager;
    }

    protected final AdEventFactory getAdEventFactory() {
        return adEventFactory;
    }

    protected final BackupLogger getBackupLogger() {
        return backupLogger;
    }

    protected final AdSpaceUtils getAdSpaceUtils() {
        return adSpaceUtils;
    }

    protected final AdResponseLogic getAdResponseLogic() {
        return adResponseLogic;
    }

    protected final ParallelModeBidManager getParallelModeBidManager() {
        return parallelModeBidManager;
    }

    protected final PreProcessor getPreProcessor() {
        return preProcessor;
    }

    protected final TargetingContextFactory getTargetingContextFactory() {
        return targetingContextFactory;
    }

    protected final ImpressionService getImpressionService() {
        return impressionService;
    }

    protected final TrackingIdentifierLogic getTrackingIdentifierLogic() {
        return trackingIdentifierLogic;
    }

    protected final StatusChangeManager getStatusChangeManager() {
        return statusChangeManager;
    }

    protected final CounterManager getCounterManager() {
        return counterManager;
    }
}
