package com.adfonic.adserver.controller;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.ClickUtils;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.Publication;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Controller
public class StaticTagController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(StaticTagController.class.getName());

    static final String IMAGE_COMPONENT_SYSTEM_NAME = "image";

    @Autowired
    private ClickUtils clickUtils;
    @Autowired
    private AdserverDataCacheManager adserverDataCacheManager;

    protected AssetDto resolveImageAsset(CreativeDto creative, TargetingContext context) {
        FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());

        // Derive the correct DisplayTypeDto for the format/device combo.  Since this is for
        // static tags and we do have their fallback URL, don't make any assumptions if we
        // can't resolve the DisplayType.  One scenario in which we wouldn't be able to
        // resolve DisplayTypeDto is if a desktop browser is hitting us, not a mobile device.
        DisplayTypeDto displayType = getDisplayTypeUtils().getDisplayType(format, context);
        if (displayType == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Can't resolve Asset...DisplayType not resolved, Creative id=" + creative.getId() + ", Format " + format.getSystemName());
            }
            return null;
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Format is " + format.getSystemName() + ", DisplayType is " + displayType.getSystemName());
        }

        // Grab the Asset for the "image" Component
        ComponentDto imageComponent = context.getDomainCache().getComponentByFormatAndSystemName(format, IMAGE_COMPONENT_SYSTEM_NAME);
        if (imageComponent == null) {
            // This indicates something is more wrong than just "no X for Y" and we need
            // to whine loudly about it.  It's indicative that maybe a non-image creative
            // was selected, which should never happen.  It means the format filter
            // probably wasn't set up correctly when doing targeting.
            throw new IllegalStateException("No \"image\" ComponentDto for FormatDto " + format.getSystemName());
        }

        AssetDto asset = creative.getAsset(displayType.getId(), imageComponent.getId());
        if (asset == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No \"image\" Asset for DisplayType " + displayType.getSystemName() + " for Creative id=" + creative.getId());
            }

            // AF-1266 - need to do the DisplayType fallback to find the "best available" asset
            for (DisplayTypeDto displayTypeDto : getDisplayTypeUtils().getAllDisplayTypes(format, context)) {
                asset = creative.getAsset(displayTypeDto.getId(), imageComponent.getId());
                if (asset != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Found Asset id=" + asset.getId() + " for Creative id=" + creative.getId() + " using alternate DisplayType: " + displayTypeDto.getSystemName());
                    }
                    break;
                }
            }

            if (asset == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("No \"image\" Asset for any applicable DisplayType for Creative id=" + creative.getId());
                }
            }
        }
        return asset;
    }

    void redirectToFallbackImage(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        String fallbackUrl = request.getParameter(Parameters.STATIC_FALLBACK_URL);
        if (StringUtils.isNotEmpty(fallbackUrl)) {
            response.sendRedirect(fallbackUrl);
        } else {
            response.sendRedirect(getPixelImageUrl(request));
        }
    }

    void redirectToFallbackClick(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        String fallbackUrl = request.getParameter(Parameters.STATIC_FALLBACK_URL);
        if (StringUtils.isNotEmpty(fallbackUrl)) {
            response.sendRedirect(fallbackUrl);
        } else {
            clickUtils.redirectToFallbackUrl(request, response);
        }
    }

    protected String getHoldingAdImageUrl(HttpServletRequest request, AdSpaceDto adSpace, TargetingContext context) {
        // First, determine the test ad format for the given AdSpace
        FormatDto format = null;
        for (Long availableFormatId : adSpace.getFormatIds()) {
            FormatDto availableFormat = context.getDomainCache().getFormatById(availableFormatId);
            // Only allow formats with "image" components
            if (context.getDomainCache().getComponentByFormatAndSystemName(availableFormat, IMAGE_COMPONENT_SYSTEM_NAME) == null) {
                continue;
            }
            format = availableFormat;
            if ("banner".equals(availableFormat.getSystemName())) {
                break; // Always use banner whenever it's available
            }
            // TODO: allow image formats other than "banner" in test ad slots
        }

        // Look up the correct DisplayTypeDto index for the given device
        final DisplayTypeDto displayType = getDisplayTypeUtils().getDisplayType(format, context);
        if (displayType == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("No DisplayTypeDto detected...probably a desktop browser?");
            }
            return getPixelImageUrl(request);
        }

        // TODO: move these images into the asset service
        // https://tickets.adfonic.com/browse/BZ-1830
        return VhostManager.makeBaseUrl(request) + "/images/verified_" + format.getSystemName() + "_" + displayType.getSystemName() + ".gif";
    }

    @RequestMapping("/si/{adSpaceExternalID}")
    public void handleStaticImage(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID) throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Be sure to pass "true" as the 2nd arg here since the request
        // is coming directly from the end user, and we want to use their
        // request headers (i.e. headers are not proxied from a publisher)
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, true);
        } catch (InvalidIpAddressException e) {
            // We're specifically logging this exception this way so it
            // shows up in the logs but won't trip up the error scavenger.
            LOG.warning(e.getMessage());
            redirectToFallbackImage(request, response);
            return;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            redirectToFallbackImage(request, response);
            return;
        }

        // By pre-processing, the main thing we're doing here (other than
        // enforcing the blacklist) is munging the effective User-Agent
        // however needed.
        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied.
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            LOG.warning("Dropping blacklisted request (adSpaceExternalID=" + adSpaceExternalID + ") due to " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            // See if the AdSpace had been marked DORMANT
            if (context.getAdserverDomainCache().getDormantAdSpaceExternalIds().contains(adSpaceExternalID)) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Dormant adSpaceExternalID: " + adSpaceExternalID);
                }
                // Queue the AdSpace for reactivation
                getAdSpaceUtils().reactivateDormantAdSpace(adSpaceExternalID);
            } else {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid adSpaceExternalID: " + adSpaceExternalID);
                }
            }
            redirectToFallbackImage(request, response);
            return;
        }
        context.setAdSpace(adSpace);

        // Before we go any further, if the Publication.status==PENDING, we
        // should only serve the fallback URL, and we shouldn't log the AdEvent.
        PublicationDto pub = adSpace.getPublication();
        if (Publication.Status.PENDING.equals(getStatusChangeManager().getStatus(pub))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Publication id=" + pub.getId() + " has status=PENDING, serving a test ad");
            }
            switch (pub.getPublisher().getPendingAdType()) {
            case NO_AD:
                response.sendRedirect(getPixelImageUrl(request));
                break;
            case HOLDING_AD:
            default:
                response.sendRedirect(getHoldingAdImageUrl(request, adSpace, context));
                break;
            }

            // Mark the AdSpace verified if need be
            verifyAdSpace(adSpace);

            // Don't log the AdEvent in this case
            return;
        }

        // We know we need to serve an image, so build a set of only image formats.
        // The caller may have passed in Parameters.FORMATS, so try using that if
        // it was specified.
        Set<Long> allowedFormatIds = new HashSet<Long>();
        String formatSpec = context.getAttribute(Parameters.FORMATS);
        if (formatSpec != null) {
            for (String formatName : StringUtils.split(formatSpec, ',')) {
                formatName = formatName.trim();
                if (StringUtils.isEmpty(formatName)) {
                    continue;
                }
                FormatDto allowedFormat = context.getDomainCache().getFormatBySystemName(formatName);
                if (allowedFormat != null) {
                    // Make sure it has an "image" Component
                    if (context.getDomainCache().getComponentByFormatAndSystemName(allowedFormat, IMAGE_COMPONENT_SYSTEM_NAME) == null) {
                        LOG.warning(Parameters.FORMATS + " specified " + allowedFormat.getSystemName() + ", which has no \"" + IMAGE_COMPONENT_SYSTEM_NAME + "\" Component");
                    } else {
                        allowedFormatIds.add(allowedFormat.getId());
                    }
                } else {
                    LOG.warning("Invalid format supplied: " + formatName);
                }
            }

            if (allowedFormatIds.isEmpty()) {
                // This is basically acceptable, just log about it
                LOG.warning("All Formats specified in " + Parameters.FORMATS + " were invalid");
            }
        }

        // If the called didn't specify formats, we need to set up the format filter
        // ourselves before proceeding.
        if (allowedFormatIds.isEmpty()) {
            for (FormatDto format : context.getDomainCache().getAllFormats()) {
                // Any FormatDto with an "image" ComponentDto is allowed
                if (context.getDomainCache().getComponentByFormatAndSystemName(format, IMAGE_COMPONENT_SYSTEM_NAME) != null) {
                    allowedFormatIds.add(format.getId());
                }
            }
        }

        // Make sure the AdSpace.formats intersects
        if (!CollectionUtils.containsAny(adSpace.getFormatIds(), allowedFormatIds)) {
            // TODO: integrate this with the error checker framework (later)
            LOG.warning("AdSpace id=" + adSpace.getId() + " doesn't support any of FormatDto ids: " + allowedFormatIds);
            redirectToFallbackImage(request, response);
            return;
        }

        // Generate the Impression object
        Impression impression = new Impression();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Generated Impression externalID=" + impression.getExternalID());
        }
        impression.setAdSpaceId(adSpace.getId());

        // They made it past the front gate, so the request is legit.  We will
        // need to log the AdEvent at the end of all this, so try/finally.
        SelectedCreative selectedCreative = null;
        UnfilledReason unfilledReason = null;
        String staticImpressionId = null;
        try {
            // Make sure they passed us their unique static impression id
            staticImpressionId = context.getAttribute(Parameters.STATIC_IMPRESSION_ID);
            if (StringUtils.isEmpty(staticImpressionId)) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("No " + Parameters.STATIC_IMPRESSION_ID + " supplied");
                }
            } else {
                // Target an ad if possible.  This call will completely populate the
                // Impression object, regardless of whether or not a creative was selected.
                // This is necessary because we use the Impression to populate the AdEvent
                // later one, even for unfilled requests.
                selectedCreative = targetAd(adSpace, allowedFormatIds, impression, context);
            }

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to generate ad", e);
            unfilledReason = UnfilledReason.EXCEPTION;
        } finally {
            // Let's figure out where we need to redirect 'em to see the correct image
            String imageUrl = null;
            CreativeDto creative = null;
            ProxiedDestination pd = null;
            AdEvent event = null;
            if (selectedCreative != null) {
                creative = selectedCreative.getCreative();
                pd = selectedCreative.getProxiedDestination();

                // Resolve the image URL based on what was selected
                if (pd != null) {
                    // We need to use the 3rd party image url from the ProxiedDestination
                    imageUrl = pd.getComponents().get("image").get("url");
                } else {
                    // We need to use the asset URL for the selected Creative
                    AssetDto asset = resolveImageAsset(creative, context);
                    if (asset != null) {
                        String assetBaseUrl = adserverDataCacheManager.getCache().getProperties().getProperty("asset.base.url");
                        imageUrl = assetBaseUrl + "/" + asset.getExternalID();
                    } else {
                        LOG.warning("For AdSpace id=" + adSpace.getId() + ", Creative id=" + creative.getId() + " selected but Asset could not be resolved");
                        // Fallback logic will kick in
                    }
                }
            }

            if (imageUrl != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Request filled, saving Impression " + impression.getExternalID() + " for AdSpace id=" + adSpace.getId() + ", staticImpressionId="
                            + staticImpressionId);
                }

                // Save the Impression in cache...we'll need it at click time
                getImpressionService().saveImpression(impression);

                // Also save a mapping from "r.impid" to the Impression + clickthrough URL
                getImpressionService().saveStaticImpression(adSpace.getId(), staticImpressionId, impression);

                // Set up the AdEvent for AdAction.AD_SERVED_AND_IMPRESSION...this is key,
                // because there's no beacon to distinguish IMPRESSION from AD_SERVED.
                event = getAdEventFactory().newInstance(AdAction.AD_SERVED_AND_IMPRESSION);
            } else {
                // Unfilled request...no need to save anything in cache

                // AF-1265: CRITICAL: un-set the creative so it doesn't get saved with the UNFILLED_REQUEST
                creative = null;
                pd = null;
                impression.setCreativeId(0); // This looks funky but is correct

                // Do fallback logic
                String fallbackUrl = request.getParameter(Parameters.STATIC_FALLBACK_URL);
                if (StringUtils.isNotEmpty(fallbackUrl)) {
                    imageUrl = fallbackUrl;
                } else {
                    imageUrl = getPixelImageUrl(request);
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Unfilled request for AdSpace id=" + adSpace.getId() + ", falling back on: " + imageUrl);
                }

                // Set up the AdEvent for UNFILLED_REQUEST
                event = getAdEventFactory().newInstance(AdAction.UNFILLED_REQUEST);

                // If we haven't established an unfilled reason already, see if it
                // was set by the targeting engine.
                if (unfilledReason == null) {
                    unfilledReason = context.getAttribute(TargetingContext.UNFILLED_REASON);
                    if (unfilledReason == null) {
                        unfilledReason = UnfilledReason.UNKNOWN;
                    }
                }
                event.setUnfilledReason(unfilledReason);
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Redirecting to: " + imageUrl);
            }
            response.sendRedirect(imageUrl);

            // Finish populating the AdEvent and log it
            context.populateAdEvent(event, impression, creative);
            //TODO Clean up code to decomission JMS
//            getAdEventLogger().logAdEvent(event, context);
            
            //Log to json logger both adserved and impression if required
            if (event.getAdAction() == AdAction.AD_SERVED_AND_IMPRESSION){
                getBackupLogger().logAdServed(impression, event.getEventTime(), context);
                getBackupLogger().logImpression(impression, event.getEventTime(), context);
            }
            else{
                getBackupLogger().logUnfilledRequest(unfilledReason, event.getEventTime(), context);
            }

            // Mark the AdSpace verified if need be
            verifyAdSpace(adSpace);
        }
    }

    @RequestMapping("/sc/{adSpaceExternalID}")
    public void handleStaticClick(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID) throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // This is kinda funky, since we're not actually targeting anything
        // at this point, but the TargetingContext is really just a simple
        // container of attributes, and it allows us to interact with the
        // derivers most easily.  We also use it to grab the "actual" IP
        // address of the request, which has been derived intelligently
        // for us at this point.
        // We also need the context in order to pre-process, which does
        // User-Agent munging and blacklist blocking, etc.
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, true);
        } catch (InvalidIpAddressException e) {
            // We're specifically logging this exception this way so it
            // shows up in the logs but won't trip up the error scavenger.
            LOG.warning(e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String staticImpressionId = context.getAttribute(Parameters.STATIC_IMPRESSION_ID);
        if (StringUtils.isEmpty(staticImpressionId)) {
            redirectToFallbackClick(request, response);
            return;
        }

        // By pre-processing, the main thing we're doing here (other than
        // enforcing the blacklist) is munging the effective User-Agent
        // however needed.
        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied.
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            LOG.warning("Dropping blacklisted request (staticImpressionId=" + staticImpressionId + ") due to " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Invalid adSpaceExternalID: " + adSpaceExternalID);
            }
            redirectToFallbackClick(request, response);
            return;
        }
        context.setAdSpace(adSpace);

        Impression impression = getImpressionService().getStaticImpression(adSpace.getId(), staticImpressionId);
        if (impression == null) {
            LOG.warning("Impression not found by staticImpressionId: " + staticImpressionId);
            redirectToFallbackClick(request, response);
            return;
        }

        // - establish the Creative, otherwise fallback
        CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        if (creative == null) {
            // It's no longer active in cache...let's see if it stopped recently
            if ((creative = context.getAdserverDomainCache().getRecentlyStoppedCreativeById(impression.getCreativeId())) != null) {
                // Yup, there it is, it was stopped recently
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Allowing click on recently stopped Creative id=" + creative.getId());
                }
            } else {
                // It's no longer active, and it wasn't stopped recently.  This would happen
                // if somebody took a really long ass time to click after the impression, but
                // there's not much we can do about it now.  Toss 'em to the fallback URL.
                LOG.warning("Impression " + impression.getExternalID() + " references unknown Creative id=" + impression.getCreativeId());
                redirectToFallbackClick(request, response);
                // Technically we probably *could* log this as a click, since we have all the
                // key info handy at this point, but since we didn't send the end user to an
                // actual destination URL, it's not worth logging an event for this click.
                // TODO: maybe tie this condition into the Error Checker Framework, i.e. notify
                // us if we see clicks coming in well after the fact.
                return;
            }
        }

        // - establish the targetURL
        String targetURL = clickUtils.getTargetUrl(impression, creative);

        // Just double check that the target URL was set
        if (targetURL == null) {
            // Should never get here, but you never know!
            LOG.warning("Impression " + impression.getExternalID() + " yielded null targetURL");
            redirectToFallbackClick(request, response);
            // Same deal here as cases above...don't log anything about this click
            return;
        }

        // Set the click id cookie to enable conversion tracking
        clickUtils.setClickIdCookie(response, impression, creative);

        // Invoke the click tracking logic
        boolean isTracked = clickUtils.trackClick(adSpace, creative, impression, context, null);

        // - post-process the targetURL and redirect
        targetURL = clickUtils.processRedirectUrl(targetURL, isTracked, adSpace, creative, impression, context, true);
        response.sendRedirect(targetURL);

    }
}
