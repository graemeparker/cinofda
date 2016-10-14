package com.adfonic.domain.cache;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections.map.MultiKeyMap;

import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.BrowserDto;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentTypeDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.domain.cache.dto.adserver.MobileIpAddressRangeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.geo.GeoUtils;
import com.adfonic.util.IpAddressUtils;
import com.adfonic.util.Range;
import com.adfonic.util.stats.CounterManager;

/** Cached representation of the domain as the adserver needs access to it */
public class DomainCacheImpl implements DomainCache {

    private static final long serialVersionUID = 31L;

    // These fields are package access so that the DomainSerializer can
    // populate them most easily.

    public final Map<String, ModelDto> modelsByExternalID = new HashMap<String, ModelDto>();
    public final Map<Long, ModelDto> modelsById = new HashMap<Long, ModelDto>();
    public final Map<Long, CountryDto> countriesById = new HashMap<Long, CountryDto>();
    public final Map<String, CountryDto> countriesByIsoCode = new TreeMap<String, CountryDto>();
    public final Map<String, CountryDto> countriesByIsoAlpha3 = new TreeMap<String, CountryDto>();
    public final Map<String, CountryDto> countriesByLowerCaseName = new TreeMap<String, CountryDto>();
    public final Map<String, SortedSet<MobileIpAddressRangeDto>> mobileIpAddressRangesByCountryIsoCode = new HashMap<String, SortedSet<MobileIpAddressRangeDto>>();
    public final Map<Long, OperatorDto> operatorsById = new HashMap<Long, OperatorDto>();
    public final List<PlatformDto> platforms = new ArrayList<PlatformDto>();
    public final Map<Long, PlatformDto> platformsById = new HashMap<Long, PlatformDto>();
    public final Map<String, PlatformDto> platformsBySystemName = new HashMap<String, PlatformDto>();
    public final List<CapabilityDto> capabilities = new ArrayList<CapabilityDto>();
    public final Map<Long, LanguageDto> languagesById = new HashMap<Long, LanguageDto>();
    public final Map<String, LanguageDto> languagesByIsoCode = new TreeMap<String, LanguageDto>();
    public final Map<Long, FormatDto> formatsById = new TreeMap<Long, FormatDto>();
    public final Map<String, FormatDto> formatsBySystemName = new TreeMap<String, FormatDto>();
    public final MultiKeyMap componentsByFormatAndSystemName = new MultiKeyMap();
    public final Map<String, DisplayTypeDto> displayTypesBySystemName = new HashMap<String, DisplayTypeDto>();
    // This maps DisplayType by Format.systemName + ContentSpec.manifest
    public final Map<String, Map<String, DisplayTypeDto>> displayTypeMap = new TreeMap<String, Map<String, DisplayTypeDto>>();
    // Cache of polygon data by GeotargetDto id
    public final Map<Long, Path2D.Double> polygonsByGeotargetId = new HashMap<Long, Path2D.Double>();
    // All available Geotargets
    public final List<GeotargetDto> geotargets = new ArrayList<GeotargetDto>();
    public final Map<Long, GeotargetDto> geotargetsById = new HashMap<Long, GeotargetDto>();

    //public transient LocationTargetDto[] locationTargetsArr = new LocationTargetDto[0];
    //public final List<LocationTargetDto> locationTargets = new ArrayList<LocationTargetDto>();
    //public final Map<Long, LocationTargetDto> locationTargetsById = new HashMap<Long, LocationTargetDto>();

    // IntegrationType mappings
    public final Map<Long, IntegrationTypeDto> integrationTypesById = new HashMap<Long, IntegrationTypeDto>();
    public final Map<String, IntegrationTypeDto> integrationTypesBySystemName = new HashMap<String, IntegrationTypeDto>();
    public final Map<String, Map<Range<Integer>, IntegrationTypeDto>> integrationTypeVersionRangeMapsByPrefix = new HashMap<String, Map<Range<Integer>, IntegrationTypeDto>>();

    // MCC+MNC to OperatorDto mappings
    public final Map<String, Long> operatorIdsByMccMnc = new HashMap<String, Long>();
    // CountryDto + Quova alias to OperatorDto mappings (OperatorAlias
    // type=QUOVA)
    public final MultiKeyMap operatorIdsByCountryIdAndQuovaAlias = new MultiKeyMap();

    public final Map<String, AdserverPluginDto> adserverPluginsBySystemName = new HashMap<String, AdserverPluginDto>();

    // This map allows us to look up an image Format by <width>x<height>
    public final MultiKeyMap boxableFormatSizeMap = new MultiKeyMap();

    public final BidiMap categoryIdsByIabId = new DualHashBidiMap();

    // This is a map from a given Category id to the complete set of
    // Category ids that it represents. That includes itself, and
    // all descendants (children, children of their children, etc.).
    public final Map<Long, Set<Long>> expandedCategoryIdsByCategoryId = new HashMap<Long, Set<Long>>();

    public final Map<Long, ExtendedCreativeTypeDto> extendedCreativeTypesById = new HashMap<Long, ExtendedCreativeTypeDto>();

    public final Map<Long, PublicationTypeDto> publicationTypesById = new HashMap<Long, PublicationTypeDto>();

    public final Map<Long, BrowserDto> browsersById = new HashMap<Long, BrowserDto>();

    public final SortedSet<DeviceIdentifierTypeDto> deviceIdentifierTypes = new TreeSet<DeviceIdentifierTypeDto>();
    public final Map<Long, DeviceIdentifierTypeDto> deviceIdentifierTypesById = new HashMap<Long, DeviceIdentifierTypeDto>();
    public final Map<String, DeviceIdentifierTypeDto> deviceIdentifierTypesBySystemName = new HashMap<String, DeviceIdentifierTypeDto>();
    public final Map<String, Long> deviceIdentifierTypeIdsBySystemName = new HashMap<String, Long>();

    public final Map<Long, Set<String>> blacklistedDeviceIdentifiersByType = new HashMap<Long, Set<String>>();

    public final Map<String, ContentTypeDto> animatedContentTypes = new HashMap<String, ContentTypeDto>();
    public final Map<String, ContentTypeDto> normalContentTypes = new HashMap<String, ContentTypeDto>();

    private Date populationStartedAt;
    private Date serializationStartedAt;

    private transient Date deserializationStartedAt;
    private transient Date deserializationFinishedAt;

    public DomainCacheImpl() {
        // default
    }

    public DomainCacheImpl(Date populationStartedAt) {
        this.populationStartedAt = populationStartedAt;
    }

    @Override
    public ContentTypeDto getAnimatedContentTypeByMime(String mimeType) {
        return animatedContentTypes.get(mimeType);
    }

    @Override
    public ContentTypeDto getNormalContentTypeByMime(String mimeType) {
        return normalContentTypes.get(mimeType);
    }

    @Override
    public ModelDto getModelByExternalID(String externalID) {
        return modelsByExternalID.get(externalID);
    }

    @Override
    public ModelDto getModelById(long id) {
        return modelsById.get(id);
    }

    @Override
    public CountryDto getCountryById(long id) {
        return countriesById.get(id);
    }

    @Override
    public CountryDto getCountryByIsoCode(String isoCode) {
        return countriesByIsoCode.get(isoCode);
    }

    @Override
    public CountryDto getCountryByIsoAlpha3(String isoAlpha3) {
        return countriesByIsoAlpha3.get(isoAlpha3);
    }

    @Override
    public CountryDto getCountryByName(String name) {
        return countriesByLowerCaseName.get(name.toLowerCase());
    }

    @Override
    public Map<String, CountryDto> getCountriesByIsoCode() {
        return Collections.unmodifiableMap(countriesByIsoCode);
    }

    @Override
    public Map<String, CountryDto> getCountriesByIsoAlpha3() {
        return Collections.unmodifiableMap(countriesByIsoAlpha3);
    }

    @Override
    public MobileIpAddressRangeDto getMobileIpAddressRange(String ipAddress) {
        // Convert the IP address to its numeric value
        long value;
        try {
            value = IpAddressUtils.ipAddressToLong(ipAddress);
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress, e);
        }
        for (Map.Entry<String, SortedSet<MobileIpAddressRangeDto>> entry : mobileIpAddressRangesByCountryIsoCode.entrySet()) {
            for (MobileIpAddressRangeDto range : entry.getValue()) {
                if (range.isInRange(value)) {
                    return range;
                }
            }
        }
        return null;
    }

    @Override
    public OperatorDto getOperator(String ipAddress, CountryDto country) {
        if (country == null) {
            return null; // We can't do anything without the country
        }

        // Convert the IP address to its numeric value
        long value;
        try {
            value = IpAddressUtils.ipAddressToLong(ipAddress);
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress, e);
        }

        // Grab the respective sorted set of mobile IP address ranges
        // for the given country
        SortedSet<MobileIpAddressRangeDto> sortedSet = mobileIpAddressRangesByCountryIsoCode.get(country.getIsoCode());
        if (sortedSet == null) {
            return null;
        }

        // The ranges are already sorted by priority and then by start point,
        // so we can just iterate through until we find a matching range.
        // This most certainly is NOT the most efficient way to do this, but
        // since the ranges are divied up by country it should be really
        // friggin fast...fast enough.
        for (MobileIpAddressRangeDto range : sortedSet) {
            if (range.isInRange(value)) {
                if (range.getOperatorId() != null) {
                    return operatorsById.get(range.getOperatorId());
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public OperatorDto getOperatorById(long id) {
        return operatorsById.get(id);
    }

    @Override
    public OperatorDto getOperatorByMccMnc(String mccmnc) {
        Long operatorId = operatorIdsByMccMnc.get(mccmnc);
        return operatorId == null ? null : getOperatorById(operatorId);
    }

    @Override
    public OperatorDto getOperatorByCountryAndQuovaAlias(CountryDto country, String quovaAlias) {
        Long operatorId = (Long) operatorIdsByCountryIdAndQuovaAlias.get(country.getId(), quovaAlias);
        return operatorId == null ? null : getOperatorById(operatorId);
    }

    @Override
    public List<PlatformDto> getPlatforms() {
        return platforms;
    }

    @Override
    public PlatformDto getPlatformById(long id) {
        return platformsById.get(id);
    }

    @Override
    public PlatformDto getPlatformBySystemName(String systemName) {
        return platformsBySystemName.get(systemName);
    }

    @Override
    public List<CapabilityDto> getCapabilities() {
        return capabilities;
    }

    @Override
    public LanguageDto getLanguageById(long id) {
        return languagesById.get(id);
    }

    @Override
    public LanguageDto getLanguageByIsoCode(String isoCode) {
        return languagesByIsoCode.get(isoCode);
    }

    @Override
    public Map<String, LanguageDto> getLanguagesByIsoCode() {
        return Collections.unmodifiableMap(languagesByIsoCode);
    }

    @Override
    public AdserverPluginDto getAdserverPluginBySystemName(String systemName) {
        return adserverPluginsBySystemName.get(systemName);
    }

    @Override
    public Collection<FormatDto> getAllFormats() {
        return formatsBySystemName.values();
    }

    @Override
    public FormatDto getFormatById(long id) {
        return formatsById.get(id);
    }

    @Override
    public FormatDto getFormatBySystemName(String systemName) {
        return formatsBySystemName.get(systemName);
    }

    @Override
    public Set<FormatDto> getBoxableFormatsBySize(int width, int height) {
        @SuppressWarnings("unchecked")
        Set<FormatDto> formats = (Set<FormatDto>) boxableFormatSizeMap.get(width, height);
        return formats;
    }

    @Override
    public ComponentDto getComponentByFormatAndSystemName(FormatDto format, String systemName) {
        return (ComponentDto) componentsByFormatAndSystemName.get(format.getSystemName(), systemName);
    }

    @Override
    public DisplayTypeDto getDisplayType(FormatDto format, String contentSpecManifest) {
        Map<String, DisplayTypeDto> byManifest = displayTypeMap.get(format.getSystemName());
        if (byManifest == null) {
            return null;
        } else {
            return byManifest.get(contentSpecManifest);
        }
    }

    @Override
    public DisplayTypeDto getDisplayTypeBySystemName(String systemName) {
        return displayTypesBySystemName.get(systemName);
    }

    @Override
    public Path2D.Double getPolygon(GeotargetDto geotarget) {
        return polygonsByGeotargetId.get(geotarget.getId());
    }

    @Override
    public Collection<GeotargetDto> getAllGeotargets() {
        return geotargets;
    }

    @Override
    public GeotargetDto getGeotargetById(Long id) {
        return geotargetsById.get(id);
    }

    @Override
    public Collection<IntegrationTypeDto> getAllIntegrationTypes() {
        return integrationTypesById.values();
    }

    @Override
    public IntegrationTypeDto getIntegrationTypeById(Long id) {
        return integrationTypesById.get(id);
    }

    @Override
    public IntegrationTypeDto getIntegrationTypeBySystemName(String systemName) {
        return integrationTypesBySystemName.get(systemName);
    }

    @Override
    public Map<Range<Integer>, IntegrationTypeDto> getIntegrationTypeVersionRangeMapByPrefix(String prefix) {
        return integrationTypeVersionRangeMapsByPrefix.get(prefix);
    }

    @Override
    public Long getCategoryIdByIabId(String iabId) {
        return (Long) categoryIdsByIabId.get(iabId);
    }

    @Override
    public String getIabIdByCategoryId(Long categoryId) {
        return (String) categoryIdsByIabId.getKey(categoryId);
    }

    @Override
    public Set<Long> getExpandedCategoryIds(long categoryId) {
        // This will never be null unless somebody passed in a junk categoryId,
        // in which case they deserve to get null back...muuahahahaha.
        return expandedCategoryIdsByCategoryId.get(categoryId);
    }

    @Override
    public ExtendedCreativeTypeDto getExtendedCreativeTypeById(long id) {
        return extendedCreativeTypesById.get(id);
    }

    @Override
    public PublicationTypeDto getPublicationTypeById(long id) {
        return publicationTypesById.get(id);
    }

    @Override
    public BrowserDto getBrowserById(long id) {
        return browsersById.get(id);
    }

    @Override
    public Set<BrowserDto> getAllBrowsers() {
        return new HashSet<BrowserDto>(browsersById.values());
    }

    @Override
    public SortedSet<DeviceIdentifierTypeDto> getAllDeviceIdentifierTypes() {
        return deviceIdentifierTypes;
    }

    @Override
    public DeviceIdentifierTypeDto getDeviceIdentifierTypeById(long id) {
        return deviceIdentifierTypesById.get(id);
    }

    @Override
    public DeviceIdentifierTypeDto getDeviceIdentifierTypeBySystemName(String systemName) {
        return deviceIdentifierTypesBySystemName.get(systemName);
    }

    @Override
    public Map<String, Long> getDeviceIdentifierTypeIdsBySystemName() {
        return Collections.unmodifiableMap(deviceIdentifierTypeIdsBySystemName);
    }

    @Override
    public Long getDeviceIdentifierTypeIdBySystemName(String systemName) {
        return deviceIdentifierTypeIdsBySystemName.get(systemName);
    }

    @Override
    public boolean isDeviceIdentifierBlacklisted(long deviceIdentifierTypeId, String deviceIdentifier) {
        Set<String> blacklistedDeviceIdentifiers = blacklistedDeviceIdentifiersByType.get(deviceIdentifierTypeId);
        return blacklistedDeviceIdentifiers != null && blacklistedDeviceIdentifiers.contains(deviceIdentifier);
    }

    @Override
    @Deprecated
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, getStatsString());
        }
    }

    public String getStatsString() {
        return "Cache counts:" + " Models=" + modelsById.size() + ", Countries=" + countriesById.size() + ", Operators=" + operatorsById.size() + ", Platforms=" + platforms.size()
                + ", Capabilities=" + capabilities.size() + ", Languages=" + languagesByIsoCode.size() + ", Formats=" + formatsBySystemName.size() + ", Geotargets="
                + geotargets.size() + ", IntegrationTypes=" + integrationTypesById.size() + ", MCC+MNCs=" + operatorIdsByMccMnc.size() + ", Country+Quova="
                + operatorIdsByCountryIdAndQuovaAlias.size() + ", AdserverPlugins=" + adserverPluginsBySystemName.size() + ", IAB Categories=" + categoryIdsByIabId.size()
                + ", ExtendedCreativeTypes=" + extendedCreativeTypesById.size() + ", PublicationTypes=" + publicationTypesById.size() + ", Browsers=" + browsersById.size()
                + ", DeviceIdentifierTypes=" + deviceIdentifierTypes.size();
    }

    @Override
    public void afterDeserialize() {
        this.deserializationFinishedAt = new Date();
    }

    @Override
    public List<LocationTargetDto> retrieveMatchingLocations(double latitude, double longitude, Collection<LocationTargetDto> whitelistDtos, boolean stopAtFirst, CounterManager cm) {
        List<LocationTargetDto> retval = new ArrayList<>(1);

        cm.incrementCounter("LocationEnter");

        if (whitelistDtos != null && !whitelistDtos.isEmpty()) {
            for (LocationTargetDto l : whitelistDtos) {
                if (l.isPossiblyInReach(latitude, longitude) && GeoUtils.fastWithinDistance(latitude, longitude, l.getLatitude(), l.getLongitude(), l.getRadius())) {
                    cm.incrementCounter("LocationVerifyWhitelistHit");
                    retval.add(l);
                    if (stopAtFirst) {
                        break;
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public Date getSerializationStartedAt() {
        return serializationStartedAt;
    }

    public void setSerializationStartedAt(Date serializationStartedAt) {
        this.serializationStartedAt = serializationStartedAt;
    }

    @Override
    public Date getDeserializationStartedAt() {
        return deserializationStartedAt;
    }

    public void setDeserializationStartedAt(Date deserializationStartedAt) {
        this.deserializationStartedAt = deserializationStartedAt;
    }

    @Override
    public Date getPopulationStartedAt() {
        return populationStartedAt;
    }

    @Override
    public Date getDeserializationFinishedAt() {
        return deserializationFinishedAt;
    }

}
