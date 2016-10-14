package com.adfonic.adserver.simulation.impl;

import java.awt.geom.Path2D;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.map.MultiKeyMap;

import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheImpl;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.BrowserDto;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;
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

public class SimulationDomainCacheManager extends DomainCacheManager {

	DomainCache dc = new SimulationDomainCacheImpl();
	
	public SimulationDomainCacheManager(File rootDir, String label,
			boolean useMemory) {
		super(rootDir, label, useMemory);
	}

	@PostConstruct
    public void initialize() throws java.io.IOException, java.lang.ClassNotFoundException {
	}
	
	@Override
	public DomainCache getCache() {
		return dc;
	}
	
	@SuppressWarnings("serial")
	public class SimulationDomainCacheImpl extends DomainCacheImpl {

		public Map<String, ModelDto> getModelsByExternalID() {
			return modelsByExternalID;
		}
		public Map<Long, ModelDto> getModelsById() {
			return modelsById;
		}
		public Map<Long, CountryDto> getCountriesById() {
			return countriesById;
		}
		public Map<String, CountryDto> getCountriesByIsoCode() {
			return countriesByIsoCode;
		}
		public Map<String, CountryDto> getCountriesByIsoAlpha3() {
			return countriesByIsoAlpha3;
		}
		public Map<String, CountryDto> getCountriesByLowerCaseName() {
			return countriesByLowerCaseName;
		}
		public Map<String, SortedSet<MobileIpAddressRangeDto>> getMobileIpAddressRangesByCountryIsoCode() {
			return mobileIpAddressRangesByCountryIsoCode;
		}
		public Map<Long, OperatorDto> getOperatorsById() {
			return operatorsById;
		}
		public List<PlatformDto> getPlatforms() {
			return platforms;
		}
		public Map<Long, PlatformDto> getPlatformsById() {
			return platformsById;
		}
		public Map<String, PlatformDto> getPlatformsBySystemName() {
			return platformsBySystemName;
		}
		public List<CapabilityDto> getCapabilities() {
			return capabilities;
		}
		public Map<Long, LanguageDto> getLanguagesById() {
			return languagesById;
		}
		public Map<String, LanguageDto> getLanguagesByIsoCode() {
			return languagesByIsoCode;
		}
		public Map<Long, FormatDto> getFormatsById() {
			return formatsById;
		}
		public Map<String, FormatDto> getFormatsBySystemName() {
			return formatsBySystemName;
		}
		public MultiKeyMap getComponentsByFormatAndSystemName() {
			return componentsByFormatAndSystemName;
		}
		public Map<String, DisplayTypeDto> getDisplayTypesBySystemName() {
			return displayTypesBySystemName;
		}
		public Map<String, Map<String, DisplayTypeDto>> getDisplayTypeMap() {
			return displayTypeMap;
		}
		public Map<Long, Path2D.Double> getPolygonsByGeotargetId() {
			return polygonsByGeotargetId;
		}
		public List<GeotargetDto> getGeotargets() {
			return geotargets;
		}
		public Map<Long, GeotargetDto> getGeotargetsById() {
			return geotargetsById;
		}
		public List<LocationTargetDto> getLocationTargets() {
			return locationTargets;
		}
		public Map<Long, LocationTargetDto> getLocationTargetsById() {
			return locationTargetsById;
		}
		public Map<Long, IntegrationTypeDto> getIntegrationTypesById() {
			return integrationTypesById;
		}
		public Map<String, IntegrationTypeDto> getIntegrationTypesBySystemName() {
			return integrationTypesBySystemName;
		}
		public Map<String, Map<Range<Integer>, IntegrationTypeDto>> getIntegrationTypeVersionRangeMapsByPrefix() {
			return integrationTypeVersionRangeMapsByPrefix;
		}
		public Map<String, Long> getOperatorIdsByMccMnc() {
			return operatorIdsByMccMnc;
		}
		public MultiKeyMap getOperatorIdsByCountryIdAndQuovaAlias() {
			return operatorIdsByCountryIdAndQuovaAlias;
		}
		public Map<String, AdserverPluginDto> getAdserverPluginsBySystemName() {
			return adserverPluginsBySystemName;
		}
		public MultiKeyMap getBoxableFormatSizeMap() {
			return boxableFormatSizeMap;
		}
		public BidiMap getCategoryIdsByIabId() {
			return categoryIdsByIabId;
		}
		public Map<Long, Set<Long>> getExpandedCategoryIdsByCategoryId() {
			return expandedCategoryIdsByCategoryId;
		}
		public Map<Long, ExtendedCreativeTypeDto> getExtendedCreativeTypesById() {
			return extendedCreativeTypesById;
		}
		public Map<Long, PublicationTypeDto> getPublicationTypesById() {
			return publicationTypesById;
		}
		public Map<Long, BrowserDto> getBrowsersById() {
			return browsersById;
		}
		public SortedSet<DeviceIdentifierTypeDto> getDeviceIdentifierTypes() {
			return deviceIdentifierTypes;
		}
		public Map<Long, DeviceIdentifierTypeDto> getDeviceIdentifierTypesById() {
			return deviceIdentifierTypesById;
		}
		public Map<String, DeviceIdentifierTypeDto> getDeviceIdentifierTypesBySystemName() {
			return deviceIdentifierTypesBySystemName;
		}
		public Map<String, Long> getDeviceIdentifierTypeIdsBySystemName() {
			return deviceIdentifierTypeIdsBySystemName;
		}
		public Map<Long, Set<String>> getBlacklistedDeviceIdentifiersByType() {
			return blacklistedDeviceIdentifiersByType;
		}
		public Map<String, ContentTypeDto> getAnimatedContentTypes() {
			return animatedContentTypes;
		}
		public Map<String, ContentTypeDto> getNormalContentTypes() {
			return normalContentTypes;
		}

	    
	}
}
