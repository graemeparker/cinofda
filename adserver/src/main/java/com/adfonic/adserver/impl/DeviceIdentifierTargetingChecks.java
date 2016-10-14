package com.adfonic.adserver.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.retargeting.redis.DeviceData;

/**
 * Device identifier related checks 
 * Dissected from massive BasicTargetingEngineImpl
 * 
 * @author mvanek
 *
 */
public class DeviceIdentifierTargetingChecks {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    /**
     * TargetingContext attribute for Truste
     */
    static final String TRUSTE_ICON = "BasicTargetingEngineImpl.trusteIcon";
    static final String TRUSTE_WEVE_ICON = "BasicTargetingEngineImpl.trusteWeveIcon";

    /*
     * Testing campaign for live testing usually has audience limited to individual device id to ensure
     * that test creatives are not delivered to real end user devices. 
     * This however makes impossible to debug request locally with live adserver cache because 
     * for audience checks, local MUID and DMP is required to work, which is impossible.
     * This switch allows to disable such request and it is NEVER supposed to be live  
     */
    private final boolean audienceCheckDisabled = "true".equals(System.getProperty("audience.check.disabled"));

    private final boolean fineLogging = LOG.isLoggable(Level.FINE);

    private final Set<Long> weveCompanyIds;

    public DeviceIdentifierTargetingChecks(Set<Long> weveCompanyIds) {
        Objects.requireNonNull(weveCompanyIds);
        this.weveCompanyIds = weveCompanyIds;
    }

    public CreativeEliminatedReason checkTrusteWeveIcon(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("creative.getCampaign().isBehavioural() = " + creative.getCampaign().isBehavioural() + " CAMPAIGN ID = " + creative.getCampaign().getId());
        }

        if (creative.getCampaign().isBehavioural()) {
            if (fineLogging) {
                LOG.fine("weveAdvertisers.contains(creative.getCampaign().getAdvertiser().getCompany().getId()) = "
                        + weveCompanyIds.contains(creative.getCampaign().getAdvertiser().getCompany().getId()) + " CompanyId = "
                        + creative.getCampaign().getAdvertiser().getCompany().getId());
            }

            if (weveCompanyIds.contains(creative.getCampaign().getAdvertiser().getCompany().getId().toString())) {
                if (fineLogging) {
                    LOG.fine("!isTrusteWeveIconOk(context) = " + !isTrusteWeveIconOk(context));
                }

                if (!isTrusteWeveIconOk(context)) {
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.TrusteWeveIcon, "not TrusteWeveIconOk");
                    }
                    return CreativeEliminatedReason.TrusteWeveIcon;
                }
            } else {
                /*
                 * Commented as part of MAD-718, need to uncomment later
                 if (fineLogging) {
                 LOG.fine("!isTrusteIconOk(context) = " + !isTrusteIconOk(context));
                 }
                 if (!isTrusteIconOk(context)){
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative,CreativeEliminatedReason.TrusteWeveIcon, "not TrusteIconOk");
                    }
                        return false;
                }
                */
            }
        }
        return null;
    }

    /**
     * Determine whether or not the integration type supports TRUSTE WEVE ICON
     * This method "caches" the result so we don't calculate it more than
     * once per ad request.
     */
    boolean isTrusteWeveIconOk(TargetingContext context) {
        Boolean trusteWeveIconOk = context.getAttribute(TRUSTE_WEVE_ICON, Boolean.class);
        if (trusteWeveIconOk == null) {
            IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("isTrusteWeveIconOk INTEGRATION TYPE NAME : " + integrationType.getName());
            }
            trusteWeveIconOk = integrationType != null && integrationType.getSupportedFeatures().contains(Feature.TRUSTE_WEVE_ICON);
            context.setAttribute(TRUSTE_WEVE_ICON, trusteWeveIconOk);
        }
        return trusteWeveIconOk;
    }

    // SC-524
    /**
     * Determine whether or not the integration type supports TRUSTE ICON
     * This method "caches" the result so we don't calculate it more than
     * once per ad request.
     
    boolean isTrusteIconOk(TargetingContext context) {
        Boolean trusteIconOk = context.getAttribute(TRUSTE_ICON, Boolean.class);
        if (trusteIconOk == null) {
            IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("isTrusteIconOk INTEGRATION TYPE NAME : " + integrationType.getName());
            }
            trusteIconOk = integrationType != null && integrationType.getSupportedFeatures().contains(Feature.TRUSTE_ICON);
            context.setAttribute(TRUSTE_ICON, trusteIconOk);
        }
        return trusteIconOk;
    }
    */

    public static CreativeEliminatedReason checkDeviceIdentifiers(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        // SC-12 - we used to do these checks only if the campaign had install
        // tracking enabled.  Now we do it regardless, since some conversion
        // trackable campaigns require device identifiers now (and who knows
        // what else in the near future).

        CampaignDto campaign = creative.getCampaign();
        Set<Long> campaignDidTypes = campaign.getDeviceIdentifierTypeIds();

        if (campaignDidTypes.isEmpty()) {
            // The campaign doesn't care.  The only reason we'd need to check
            // anything further is if install tracking is enabled.
            if (campaign.isInstallTrackingEnabled()) {
                // Anything goes.  This is probably an old campaign that was
                // set up prior to the point where we started requiring that
                // campaigns specify the device identifier type(s) they'll use.
                // All we need to do is make sure at least one device identifier
                // was specified.
                Map<Long, String> deviceIdMap = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                if (deviceIdMap.isEmpty()) {
                    // No good...can't serve this campaign
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoDeviceIdentifier, "No device identifiers in bid");
                    }
                    return CreativeEliminatedReason.NoDeviceIdentifier;
                }
            }
        } else {
            // The campaign has a discrete set of device identifier type(s)
            // that have been designated as required, so make sure at least
            // one of them has been passed to us by the publisher. The quickest
            // way for us to do that is to check for an intersection between
            // the publisher-supplied device identifier types and those allowed
            // by the campaign.
            Map<Long, String> deviceIdMap = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
            if (!CollectionUtils.containsAny(deviceIdMap.keySet(), campaignDidTypes)) {
                // No good...can't serve this campaign
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentDeviceIdentifier, "Device identifier types: " + deviceIdMap.keySet()
                            + " vs Campaing required types: " + campaignDidTypes);
                }
                return CreativeEliminatedReason.differentDeviceIdentifier;
            }
        }

        return null;
    }

    CreativeEliminatedReason checkDeviceIdAudienceTargeting(AdSpaceDto adSpace, CreativeDto creative, TargetingContext context, TargetingEventListener listener) {

        if (!creative.getCampaign().hasAudience()) {
            // if not behavioural campaign (no audience) that means its eligible
            return null;
        }

        if (audienceCheckDisabled) {
            // For development only (without redis)
            LOG.warning("Audience check is DISABLED using System property");
            return null;
        }

        Set<OptOutType> optOuts = context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class);
        if (optOuts != null && optOuts.contains(OptOutType.global)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OptedOut, "global optout");
            }
            return CreativeEliminatedReason.OptedOut;
        }

        Long advertiserCompanyId = creative.getCampaign().getAdvertiser().getCompany().getId();
        if (weveCompanyIds.contains(advertiserCompanyId)) {
            if (optOuts != null && optOuts.contains(OptOutType.weve)) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OptedOut, "weve optout");
                }
                return CreativeEliminatedReason.OptedOut;
            }
        }

        Set<CampaignAudienceDto> campaignAudience = creative.getCampaign().getDeviceIdAudiences();
        if (campaignAudience == null || campaignAudience.isEmpty()) {
            // No audience specified -> accept anything
            return null;
        }

        Set<Long> deviceAudienceIds = context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class);
        if (deviceAudienceIds == null) {
            deviceAudienceIds = Collections.emptySet();
        }

        boolean isTargeted = false;
        Set<DeviceData> deviceDataSet = context.getAttribute(TargetingContext.DEVICE_DATA, Set.class); // DeviceData from Redis
        if (deviceDataSet != null) {
            for (DeviceData deviceData : deviceDataSet) {
                isTargeted = isAudienceTargeted(campaignAudience, deviceAudienceIds, deviceData.getRecencyByAudience());
                if (isTargeted) {
                    break;
                }
            }

            if (!isTargeted) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceRedisMismatch, "Device Id audiences " + deviceAudienceIds + " vs "
                            + campaignAudience);
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Audience Targeted Campaign So checking Eligibilty " + creative.getCampaign().getId());
                LOG.fine("deviceAudienceIds=" + deviceAudienceIds);
                LOG.fine("creative.getCampaign().getId()=" + creative.getCampaign().getId());
                LOG.fine("deviceids=" + context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS));
            }
        }
        return isTargeted ? null : CreativeEliminatedReason.DeviceRedisMismatch;
    }

    boolean isAudienceTargeted(Set<CampaignAudienceDto> campaignAudience, Set<Long> deviceAudienceIds, Map<Long, Instant> recencyByAudience) {

        if (campaignAudience.isEmpty()) {
            return true;
        }

        // check excluded first
        for (CampaignAudienceDto audienceDto : campaignAudience) {
            if (audienceDto.isInclude()) {
                continue;
            }

            boolean contains = deviceAudienceIds.contains(audienceDto.getAudienceId());
            if (contains && isWithinRecencyInterval(audienceDto, recencyByAudience)) {
                return false;
            }
        }

        // check included
        boolean hasIncludes = false;
        for (CampaignAudienceDto audienceDto : campaignAudience) {
            if (!audienceDto.isInclude()) {
                continue;
            }
            hasIncludes = true;

            long audienceId = audienceDto.getAudienceId();
            boolean contains = deviceAudienceIds.contains(audienceId);
            if (contains && isWithinRecencyInterval(audienceDto, recencyByAudience)) {
                return true;
            }
        }

        return !hasIncludes;
    }

    boolean isWithinRecencyInterval(CampaignAudienceDto audienceDto, Map<Long, Instant> recencyByAudience) {

        long audienceId = audienceDto.getAudienceId();
        final Instant timeStamp = recencyByAudience.get(audienceId);

        if (audienceDto.recencyInterval != null) {
            boolean isWithin = audienceDto.recencyInterval.contains(timeStamp);
            return isWithin;
        }

        if (audienceDto.getNumDaysAgoFrom() != null && audienceDto.getNumDaysAgoTo() != null) {
            Duration hoursFrom = Duration.standardHours(24 * audienceDto.getNumDaysAgoFrom());
            Duration hoursTo = Duration.standardHours(24 * audienceDto.getNumDaysAgoTo());
            Instant now = new Instant();
            Instant from = now.minus(hoursFrom);
            Instant to = now.minus(hoursTo);

            Interval recencyInterval;
            if (from.isAfter(to)) {
                recencyInterval = new Interval(to, from);
            } else {
                recencyInterval = new Interval(from, to);
            }

            boolean isWithin = recencyInterval.contains(timeStamp);
            return isWithin;
        }

        // no restrictions
        return true;
    }
}
