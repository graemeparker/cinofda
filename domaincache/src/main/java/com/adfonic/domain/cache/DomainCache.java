package com.adfonic.domain.cache;

import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

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
import com.adfonic.util.Range;
import com.adfonic.util.stats.CounterManager;

/** Cached representation of the domain as the adserver needs access to it */
public interface DomainCache extends SerializableCache {

    ModelDto getModelByExternalID(String externalID);

    ModelDto getModelById(long id);

    CountryDto getCountryById(long id);

    CountryDto getCountryByIsoCode(String isoCode);

    CountryDto getCountryByIsoAlpha3(String isoAlpha3);

    CountryDto getCountryByName(String name); // this is case-insensitive

    Map<String, CountryDto> getCountriesByIsoCode();

    Map<String, CountryDto> getCountriesByIsoAlpha3();

    MobileIpAddressRangeDto getMobileIpAddressRange(String ipAddress);

    OperatorDto getOperator(String ipAddress, CountryDto country);

    OperatorDto getOperatorById(long id);

    OperatorDto getOperatorByMccMnc(String mccmnc);

    OperatorDto getOperatorByCountryAndQuovaAlias(CountryDto country, String quovaAlias);

    List<PlatformDto> getPlatforms();

    PlatformDto getPlatformById(long id);

    PlatformDto getPlatformBySystemName(String systemName);

    List<CapabilityDto> getCapabilities();

    LanguageDto getLanguageById(long id);

    LanguageDto getLanguageByIsoCode(String isoCode);

    Map<String, LanguageDto> getLanguagesByIsoCode();

    DisplayTypeDto getDisplayType(FormatDto format, String contentSpecManifest);

    DisplayTypeDto getDisplayTypeBySystemName(String systemName);

    Path2D.Double getPolygon(GeotargetDto geotarget);

    Collection<GeotargetDto> getAllGeotargets();

    GeotargetDto getGeotargetById(Long id);

    //Collection<LocationTargetDto> getAllLocationTargets();

    //LocationTargetDto getLocationTargetById(Long id);

    Collection<IntegrationTypeDto> getAllIntegrationTypes();

    IntegrationTypeDto getIntegrationTypeById(Long id);

    IntegrationTypeDto getIntegrationTypeBySystemName(String systemName);

    Map<Range<Integer>, IntegrationTypeDto> getIntegrationTypeVersionRangeMapByPrefix(String prefix);

    Collection<FormatDto> getAllFormats();

    FormatDto getFormatById(long id);

    FormatDto getFormatBySystemName(String systemName);

    Set<FormatDto> getBoxableFormatsBySize(int width, int height);

    ComponentDto getComponentByFormatAndSystemName(FormatDto format, String systemName);

    AdserverPluginDto getAdserverPluginBySystemName(String systemName);

    Long getCategoryIdByIabId(String iabId);

    Set<Long> getExpandedCategoryIds(long categoryId);

    ExtendedCreativeTypeDto getExtendedCreativeTypeById(long id);

    PublicationTypeDto getPublicationTypeById(long id);

    BrowserDto getBrowserById(long id);

    Set<BrowserDto> getAllBrowsers();

    SortedSet<DeviceIdentifierTypeDto> getAllDeviceIdentifierTypes();

    DeviceIdentifierTypeDto getDeviceIdentifierTypeById(long id);

    DeviceIdentifierTypeDto getDeviceIdentifierTypeBySystemName(String systemName);

    Map<String, Long> getDeviceIdentifierTypeIdsBySystemName();

    Long getDeviceIdentifierTypeIdBySystemName(String systemName);

    boolean isDeviceIdentifierBlacklisted(long deviceIdentifierTypeId, String deviceIdentifier);

    ContentTypeDto getAnimatedContentTypeByMime(String mimeType);

    ContentTypeDto getNormalContentTypeByMime(String mimeType);

    public abstract String getIabIdByCategoryId(Long categoryId);

    @Deprecated
    List<LocationTargetDto> retrieveMatchingLocations(double latitude, double longitude, Collection<LocationTargetDto> whitelist, boolean stopAtFirstMatch, CounterManager cm);

    void afterDeserialize();

    public Date getPopulationStartedAt();

    public Date getSerializationStartedAt();

    public Date getDeserializationStartedAt();

    public Date getDeserializationFinishedAt();

}
