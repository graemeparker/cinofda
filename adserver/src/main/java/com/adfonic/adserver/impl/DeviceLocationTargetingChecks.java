package com.adfonic.adserver.impl;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.deriver.impl.AbstractGeoDeriver;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.PostalCode;
import com.adfonic.geo.USState;
import com.adfonic.util.stats.CounterManager;

/**
 * Geografic Country and Location related targetting
 * Dissected from massive BasicTargetingEngineImpl. 
 * Lots of things changed here as geolocation data are not shipped to adserver via cache anymore but ingested into GeoRedis as geohashes using GeoTasks  
 * 
 * @author mvanek
 *
 */
public class DeviceLocationTargetingChecks {

    private static final transient Logger LOG = Logger.getLogger(DeviceLocationTargetingChecks.class.getName());

    private final CounterManager counterManager;

    private final DataCacheProperties dcProperties;

    public DeviceLocationTargetingChecks(CounterManager counterManager, DataCacheProperties dcProperties) {
        this.counterManager = counterManager;
        this.dcProperties = dcProperties;
    }

    CreativeEliminatedReason checkGeoTargetting(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment,
            Map<Long, GeotargetDto> matchingGeotargetByCreativeId, TargetingEventListener listener) {

        if (segment.isExplicitGPSEnabled()) {
            Coordinates explicitCoordinates = context.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS);
            if (explicitCoordinates == null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.explicitGeoLocationExpected, "!explictGeoLocation Expected");
                }
                return CreativeEliminatedReason.explicitGeoLocationExpected;
            }
        }
        // Handle geo-targeting
        if (!segment.getGeotargetIds().isEmpty()) {
            // Make sure this is "geolocatable" traffic
            if (BooleanUtils.isNotTrue(context.getAttribute(TargetingContext.HAS_COORDINATES, Boolean.class))) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoGeolocations, "!geolocatable");
                }
                return CreativeEliminatedReason.NoGeolocations;
            }
            GeotargetDto matchingGeotarget = matchGeotarget(context, segment.getGeotargetIds(), context.getDomainCache());
            if (matchingGeotarget != null) {
                // Keep track of the matching GeotargetDto in case this
                // creative ends up getting selected.
                matchingGeotargetByCreativeId.put(creative.getId(), matchingGeotarget);
            } else {
                // The user isn't inside any of the geotarget areas
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notInGeotargetArea, "geotarget");
                }
                return CreativeEliminatedReason.notInGeotargetArea;
            }
        }

        return checkLatLonTargeting(context, adSpace, creative, segment, listener);
    }

    CreativeEliminatedReason checkLatLonTargeting(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, TargetingEventListener listener) {

        // Old style first
        if (!segment.getLocationTargets().isEmpty()) {
            // Make sure this is "geolocatable" traffic
            if (BooleanUtils.isNotTrue(context.getAttribute(TargetingContext.HAS_COORDINATES, Boolean.class))) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoGeolocations, "No coordinates in Bid");
                }
                return CreativeEliminatedReason.NoGeolocations;
            }
            LocationTargetDto matchingLocationTarget = matchLocationTarget(context, segment.getLocationTargets(), context.getDomainCache());
            if (matchingLocationTarget == null) {
                // The user isn't inside any of the geotarget areas
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NotInGeolocationArea, "locationtarget");
                }
                return CreativeEliminatedReason.NotInGeolocationArea;
            }
            return null;
        }

        // New style - only check redis IF the campaign has an LOCATION audience
        Set<CampaignAudienceDto> campaignAudiences = creative.getCampaign().getLocationAudiences();
        if (campaignAudiences != null && !campaignAudiences.isEmpty()) {
            return checkGeoRedisLatLon(context, adSpace, creative, listener);
        }

        return null;
    }

    /**
     * CampaignAudienceLoader in DS populates location audience ids in serialized campaign, but location data themselves (geohashes) are stored inside Geo Redis.
     */
    private CreativeEliminatedReason checkGeoRedisLatLon(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, TargetingEventListener listener) {
        Set<CampaignAudienceDto> campaignAudiences = creative.getCampaign().getLocationAudiences();
        // Set<CampaignAudienceDto> campaignAudiences = Sets.newHashSet(new CampaignAudienceDto(1, 13, CampaignAudienceDto.AudienceType.LOCATION, true, null, null, null)); //dev integration test only 
        if (campaignAudiences == null || campaignAudiences.isEmpty()) {
            // Usual path - most campaigns have no geo audiences
            return null;
        } else {
            Set<Long> audiencesIds = context.getAttribute(TargetingContext.LOCATION_AUDIENCES, Set.class); // Goes to LocationAudienceRedisDeriver
            if (audiencesIds == null || audiencesIds.isEmpty()) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoGeolocations, "No Bid location audiences vs " + campaignAudiences);
                }
                return CreativeEliminatedReason.NoGeolocations;
            }
            boolean onlyExclusions = true;
            for (CampaignAudienceDto campaignAudience : campaignAudiences) {
                boolean isInAudience = audiencesIds.contains(campaignAudience.getAudienceId());
                if (campaignAudience.isInclude()) {
                    onlyExclusions = false;
                    if (isInAudience) {
                        return null; // hurray !
                    }
                } else { // audience is excluded
                    // rigorously we should probably check all exclusions first and then try inclusions
                    // let's assume it is nonsense if campaign both includes and excludes same audience
                    if (isInAudience) {
                        if (listener != null) {
                            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NotInGeolocationArea, "Bid location audience: " + audiencesIds
                                    + " excluded by " + campaignAudience);
                        }
                        return CreativeEliminatedReason.NotInGeolocationArea;
                    }
                }
            }
            if (onlyExclusions) {
                // Not being excluded -> targeted
                return null;
            } else {
                // Sad so sad. No matching campaign audience found for bid location
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NotInGeolocationArea, "Bid location audience: " + audiencesIds
                            + " not match " + campaignAudiences);
                }
                return CreativeEliminatedReason.NotInGeolocationArea;
            }
        }
    }

    private static GeotargetDto matchGeotarget(TargetingContext context, Collection<Long> geotargetIds, DomainCache domainCache) {
        List<GeotargetDto> list = matchGeotargets(context, geotargetIds, domainCache, true);
        // Since we said "stopAtFirstMatch" the list will either be empty
        // or will only have one entry in it.
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    // This is public so internal/ip.jsp can use it
    public static List<GeotargetDto> matchGeotargets(TargetingContext context, Collection<Long> geotargetIds, DomainCache domainCache, boolean stopAtFirstMatch) {
        Dma dma = null;
        PostalCode postalCode = null;
        USState usState = null;
        CanadianProvince canadianProvince = null;
        ChineseProvince chineseProvince = null;
        AustrianProvince austrianProvince = null;
        String spanishProvince = null;
        Coordinates coordinates = null;

        List<GeotargetDto> matchingGeotargets = null; // lazy-construct
        for (Long geotargetId : geotargetIds) {
            GeotargetDto geotarget = context.getDomainCache().getGeotargetById(geotargetId);
            if (geotarget == null) {
                throw new RuntimeException("Impossible to retrieve geotarget with id " + geotargetId + "  geotargets size " + context.getDomainCache().getAllGeotargets().size());
            }
            switch (geotarget.getType()) {
            case DMA:
                if (dma == null) {
                    // It doesn't hurt to call this multiple times, since the
                    // deriver won't reattempt the same derivation.
                    dma = context.getAttribute(TargetingContext.DMA);
                }
                if (dma != null && dma.getName().equals(geotarget.getName())) {
                    if (matchingGeotargets == null) {
                        matchingGeotargets = new ArrayList<GeotargetDto>();
                    }
                    matchingGeotargets.add(geotarget);
                    if (stopAtFirstMatch) {
                        return matchingGeotargets;
                    }
                }
                break;
            case POSTAL_CODE:
                if (postalCode == null) {
                    // It doesn't hurt to call this multiple times, since the
                    // deriver won't reattempt the same derivation.
                    postalCode = context.getAttribute(TargetingContext.UK_POSTAL_CODE);
                }
                if (postalCode != null && postalCode.getName().equals(geotarget.getName())) {
                    if (matchingGeotargets == null) {
                        matchingGeotargets = new ArrayList<GeotargetDto>();
                    }
                    matchingGeotargets.add(geotarget);
                    if (stopAtFirstMatch) {
                        return matchingGeotargets;
                    }
                }
                break;
            case STATE:
                if ("US".equals(geotarget.getCountryIsoCode())) {
                    if (usState == null) {
                        // It doesn't hurt to call this multiple times, since the
                        // deriver won't reattempt the same derivation.
                        usState = context.getAttribute(TargetingContext.US_STATE);
                    }
                    if (usState != null && usState.getName().equals(geotarget.getName())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                } else if ("CA".equals(geotarget.getCountryIsoCode())) {
                    if (canadianProvince == null) {
                        // It doesn't hurt to call this multiple times, since the
                        // deriver won't reattempt the same derivation.
                        canadianProvince = context.getAttribute(TargetingContext.CANADIAN_PROVINCE);
                    }
                    if (canadianProvince != null && canadianProvince.getName().equals(geotarget.getName())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                } else if ("CN".equals(geotarget.getCountryIsoCode())) {
                    if (chineseProvince == null) {
                        chineseProvince = context.getAttribute(TargetingContext.CHINESE_PROVINCE);
                    }
                    if (chineseProvince != null && chineseProvince.getName().equals(geotarget.getName())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                } else if ("AT".equals(geotarget.getCountryIsoCode())) {
                    if (austrianProvince == null) {
                        austrianProvince = context.getAttribute(TargetingContext.AUSTRIAN_PROVINCE);
                    }
                    if (austrianProvince != null && austrianProvince.getName().equals(geotarget.getName())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                } else if ("ES".equals(geotarget.getCountryIsoCode())) {
                    if (spanishProvince == null) {
                        spanishProvince = context.getAttribute(TargetingContext.SPANISH_PROVINCE);
                    }
                    if (spanishProvince != null && spanishProvince.equals(geotarget.getName())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                } else {
                    LOG.warning("STATE geotargeting for country=" + geotarget.getCountryIsoCode() + " not supported");
                }
                break;
            case POLYGON:
                // Grab the pre-compiled polygon and see if the user
                // is inside the area
                if (coordinates == null) {
                    // It doesn't hurt to call this multiple times, since the
                    // deriver won't reattempt the same derivation.
                    coordinates = context.getAttribute(TargetingContext.COORDINATES);
                }
                if (coordinates != null) {
                    Path2D.Double polygon = domainCache.getPolygon(geotarget);
                    if (polygon != null && polygon.contains(coordinates.getLatitude(), coordinates.getLongitude())) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                }
                break;
            case RADIUS:
                LOG.warning("Deprecated Lat/Lon/Radius Geotargeting found: " + geotarget.getId());
                /*
                // See if the end user is within the given radius of
                // the specified coordinates
                if (coordinates == null) {
                    // It doesn't hurt to call this multiple times, since the
                    // deriver won't reattempt the same derivation.
                    coordinates = context.getAttribute(TargetingContext.COORDINATES);
                }
                if (coordinates != null) {
                    double dist = GeoUtils.distanceBetween(coordinates.getLatitude(), coordinates.getLongitude(), geotarget.getDisplayLatitude(), geotarget.getDisplayLongitude());
                    if (dist <= geotarget.getRadius().doubleValue()) {
                        if (matchingGeotargets == null) {
                            matchingGeotargets = new ArrayList<GeotargetDto>();
                        }
                        matchingGeotargets.add(geotarget);
                        if (stopAtFirstMatch) {
                            return matchingGeotargets;
                        }
                    }
                }*/
                break;

            }
        }

        return matchingGeotargets;
    }

    @Deprecated
    private LocationTargetDto matchLocationTarget(TargetingContext context, Collection<LocationTargetDto> targetLocations, DomainCache domainCache) {
        List<LocationTargetDto> list = targetLocations != null && !targetLocations.isEmpty() ? matchLocationTargets(context, targetLocations, domainCache, true) : null;
        // Since we said "stopAtFirstMatch" the list will either be empty
        // or will only have one entry in it.
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    // This is public so internal/ip.jsp can use it
    public List<LocationTargetDto> matchLocationTargets(TargetingContext context, Collection<LocationTargetDto> targetLocations, DomainCache domainCache, boolean stopAtFirstMatch) {
        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);

        List<LocationTargetDto> matchingLocationTargets = context.getDomainCache().retrieveMatchingLocations(coordinates.getLatitude(), coordinates.getLongitude(),
                targetLocations, stopAtFirstMatch, counterManager);

        return matchingLocationTargets;
    }

}
