package com.byyd.middleware.common.service;

import java.util.Collection;
import java.util.List;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Format;
import com.adfonic.domain.Language;
import com.adfonic.domain.MobileIpAddressRange;
import com.adfonic.domain.Region;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.Segment;
import com.adfonic.domain.ThirdPartyVendor;
import com.adfonic.domain.ThirdPartyVendorType;
import com.adfonic.domain.UploadedContent;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.common.filter.ThirdPartyVendorFilter;
import com.byyd.middleware.common.filter.ThirdPartyVendorTypeFilter;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface CommonManager extends BaseManager {

    //------------------------------------------------------------------------------------------
    // UploadedContent
    //------------------------------------------------------------------------------------------
    UploadedContent newUploadedContent(ContentType contentType, FetchStrategy... fetchStrategy);
    UploadedContent newUploadedContent(ContentType contentType, byte[] data, FetchStrategy... fetchStrategy);

    UploadedContent getUploadedContentById(String id, FetchStrategy... fetchStrategy);
    UploadedContent getUploadedContentById(Long id, FetchStrategy... fetchStrategy);
    UploadedContent create(UploadedContent uploadedContent);
    UploadedContent update(UploadedContent uploadedContent);
    void delete(UploadedContent uploadedContent);
    void deleteUploadedContent(List<UploadedContent> list);

    UploadedContent getUploadedContentByExternalId(String externalId, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // MobileIpAddressRange
    //------------------------------------------------------------------------------------------

    MobileIpAddressRange getMobileIpAddressRangeById(String id, FetchStrategy... fetchStrategy);
    MobileIpAddressRange getMobileIpAddressRangeById(Long id, FetchStrategy... fetchStrategy);
    MobileIpAddressRange create(MobileIpAddressRange mobileIpAddressRange);
    MobileIpAddressRange update(MobileIpAddressRange mobileIpAddressRange);
    void delete(MobileIpAddressRange mobileIpAddressRange);
    void deleteMobileIpAddressRanges(List<MobileIpAddressRange> list);

    Long countAllMobileIpAddressRanges();
    List<MobileIpAddressRange> getAllMobileIpAddressRanges(FetchStrategy... fetchStrategy);
    List<MobileIpAddressRange> getAllMobileIpAddressRanges(Sorting sort, FetchStrategy... fetchStrategy);
    List<MobileIpAddressRange> getAllMobileIpAddressRanges(Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Category
    //------------------------------------------------------------------------------------------
    Category getCategoryById(String id, FetchStrategy... fetchStrategy);
    Category getCategoryById(Long id, FetchStrategy... fetchStrategy);
    Category create(Category category);
    Category update(Category category);
    Category getCategoryByName(String name, FetchStrategy... fetchStrategy);
    Category getCategoryByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Category getCategoryByIabId(String iabId, FetchStrategy ... fetchStrategy);

    Long countAllCategories();
    List<Category> getAllCategories(FetchStrategy... fetchStrategy);
    List<Category> getAllCategories(Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getAllCategories(Pagination page, FetchStrategy... fetchStrategy);

    Long countAllCategoriesForParent(Category parent);
    List<Category> getAllCategoriesForParent(Category parent, FetchStrategy... fetchStrategy);
    List<Category> getAllCategoriesForParent(Category parent, Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getAllCategoriesForParent(Category parent, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllTopLevelCategories();
    List<Category> getAllTopLevelCategories(FetchStrategy... fetchStrategy);
    List<Category> getAllTopLevelCategories(Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getAllTopLevelCategories(Pagination page, FetchStrategy... fetchStrategy);

    Long countCategoriesByName(String name, LikeSpec like, boolean caseSensitive);
    List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    Category getParentCategoryForCategory(Category category, FetchStrategy... fetchStrategy);

    String getExcludedCategoryNamesBySegment(Segment s);

    Category getDefaultCategory(FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Channel
    //------------------------------------------------------------------------------------------
    Channel newChannel(String name, FetchStrategy... fetchStrategy);

    Channel getChannelById(String id, FetchStrategy... fetchStrategy);
    Channel getChannelById(Long id, FetchStrategy... fetchStrategy);
    Channel create(Channel channel);
    Channel update(Channel channel);
    void delete(Channel channel);

    Channel getChannelByName(String name, FetchStrategy... fetchStrategy);

    Number countAllChannels();
    List<Channel> getAllChannels(FetchStrategy... fetchStrategy);
    List<Channel> getAllChannels(Sorting sort, FetchStrategy... fetchStrategy);
    List<Channel> getAllChannels(Pagination page, FetchStrategy... fetchStrategy);

    Long countAllChannels(ChannelFilter filter);
    List<Channel> getAllChannels(ChannelFilter filter, FetchStrategy... fetchStrategy);
    List<Channel> getAllChannels(ChannelFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Channel> getAllChannels(ChannelFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    
    //------------------------------------------------------------------------------------------
    // Country
    //------------------------------------------------------------------------------------------
    Country getCountryById(String id, FetchStrategy... fetchStrategy);
    Country getCountryById(Long id, FetchStrategy... fetchStrategy);
    Country create(Country country);
    Country update(Country country);

    Country getCountryByName(String name, FetchStrategy... fetchStrategy);
    Country getCountryByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);
    Country getCountryByIsoCode(String isoCode, FetchStrategy... fetchStrategy);

    Long countAllCountries();
    List<Country> getAllCountries(FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(Pagination page, FetchStrategy... fetchStrategy);
    
    Long countAllCountries(boolean includeHidden);
    List<Country> getAllCountries(boolean includeHidden, FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(boolean includeHidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getAllCountries(boolean includeHidden, Pagination page, FetchStrategy... fetchStrategy);

    Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy);

    String getCountryNamesBySegment(Segment s);
    
    //------------------------------------------------------------------------------------------
    // Region
    //------------------------------------------------------------------------------------------

    Region getRegionById(String id, FetchStrategy... fetchStrategy);
    Region getRegionById(Long id, FetchStrategy... fetchStrategy);
    Region create(Region region);
    Region update(Region region);

    Region getRegionByName(String name, FetchStrategy... fetchStrategy);
    Region getRegionByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Long countAllRegions();
    List<Region> getAllRegions(FetchStrategy... fetchStrategy);
    List<Region> getAllRegions(Sorting sort, FetchStrategy... fetchStrategy);
    List<Region> getAllRegions(Pagination page, FetchStrategy... fetchStrategy);

    Long countRegionsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Language
    //------------------------------------------------------------------------------------------

    Language getLanguageById(String id, FetchStrategy... fetchStrategy);
    Language getLanguageById(Long id, FetchStrategy... fetchStrategy);
    Language create(Language language);
    Language update(Language language);

    Language getLanguageByIsoCode(String isoCode, FetchStrategy... fetchStrategy);
    Language getLanguageByName(String name, FetchStrategy... fetchStrategy);
    Language getLanguageByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Long countAllLanguages();
    List<Language> getAllLanguages(FetchStrategy... fetchStrategy);
    List<Language> getAllLanguages(Sorting sort, FetchStrategy... fetchStrategy);
    List<Language> getAllLanguages(Pagination page, FetchStrategy... fetchStrategy);

    Long countLanguagesByName(String name, LikeSpec like, boolean caseSensitive);
    List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // RemovalInfo
    //------------------------------------------------------------------------------------------
    RemovalInfo getRemovalInfoById(String id, FetchStrategy... fetchStrategy);
    RemovalInfo getRemovalInfoById(Long id, FetchStrategy... fetchStrategy);
    RemovalInfo create(RemovalInfo removalInfo);
    RemovalInfo update(RemovalInfo removalInfo);
    void delete(RemovalInfo removalInfo);
    void deleteRemovalInfos(List<RemovalInfo> list);
    
    //------------------------------------------------------------------------------------------
    // ContentType
    //------------------------------------------------------------------------------------------
    ContentType newContentType(String name, String mimeType, boolean animated, FetchStrategy... fetchStrategy);

    ContentType getContentTypeById(String id, FetchStrategy... fetchStrategy);
    ContentType getContentTypeById(Long id, FetchStrategy... fetchStrategy);
    ContentType update(ContentType contentType);
    void delete(ContentType contentType);
    void deleteContentTypes(List<ContentType> list);

    ContentType getContentTypeByName(String name, FetchStrategy... fetchStrategy);

    Long countContentTypesForMimeType(String mimeType);
    List<ContentType> getAllContentTypesForMimeType(String mimeType, FetchStrategy... fetchStrategy);
    List<ContentType> getAllContentTypesForMimeType(String mimeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ContentType> getAllContentTypesForMimeType(String mimeType, Pagination page, FetchStrategy... fetchStrategy);

    Long countContentTypesForMimeTypeLike(String mimeType);
    List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType, FetchStrategy... fetchStrategy);
    List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType, Pagination page, FetchStrategy... fetchStrategy);

    ContentType getContentTypeForMimeType(String mimeType, FetchStrategy... fetchStrategy);
    ContentType getContentTypeForMimeType(String mimeType, boolean animated, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Format
    //------------------------------------------------------------------------------------------
    Format getFormatById(String id, FetchStrategy... fetchStrategy);
    Format getFormatById(Long id, FetchStrategy... fetchStrategy);
    Format create(Format format);
    Format update(Format format);

    Format getFormatByName(String name, FetchStrategy... fetchStrategy);
    Format getFormatByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);
    Format getFormatBySystemName(String systemName, FetchStrategy... fetchStrategy);

    Long countAllFormats();
    List<Format> getAllFormats(FetchStrategy... fetchStrategy);
    List<Format> getAllFormats(Sorting sort, FetchStrategy... fetchStrategy);
    List<Format> getAllFormats(Pagination page, FetchStrategy... fetchStrategy);

    List<Format> getSupportedFormats(Collection<AdSpace> adSpaces, FetchStrategy... fetchStrategy);
    List<Format> getSupportedFormats(Campaign campaign, FetchStrategy... fetchStrategy);

    Long countFormatsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // ContentSpec
    //------------------------------------------------------------------------------
    ContentSpec newContentSpec(String name, String manifest, FetchStrategy... fetchStrategy);
    ContentSpec getContentSpecById(String id, FetchStrategy... fetchStrategy);
    ContentSpec getContentSpecById(Long id, FetchStrategy... fetchStrategy);
    ContentSpec create(ContentSpec contentSpec);
    ContentSpec update(ContentSpec contentSpec);
    void delete(ContentSpec contentSpec);
    void deleteContentSpecs(List<ContentSpec> list);

    Long countAllContentSpecs();
    List<ContentSpec> getAllContentSpecs(FetchStrategy ... fetchStrategy);
    List<ContentSpec> getAllContentSpecs(Sorting sort, FetchStrategy ... fetchStrategy);
    List<ContentSpec> getAllContentSpecs(Pagination page, FetchStrategy ... fetchStrategy);

    ContentSpec getContentSpecByName(String name, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // CurrencyExchangeRate
    //------------------------------------------------------------------------------------------
    CurrencyExchangeRate getCurrencyExchangeRateById(String id, FetchStrategy... fetchStrategy);
    CurrencyExchangeRate getCurrencyExchangeRateById(Long id, FetchStrategy... fetchStrategy);
    CurrencyExchangeRate update(CurrencyExchangeRate currencyExchangeRate);

    Long countAllCurrencyExchangeRate();
    List<CurrencyExchangeRate> getAllCurrencyExchangeRate(FetchStrategy... fetchStrategy);
    List<CurrencyExchangeRate> getAllCurrencyExchangeRate(Sorting sort, FetchStrategy... fetchStrategy);
    List<CurrencyExchangeRate> getAllCurrencyExchangeRate(Pagination page, FetchStrategy... fetchStrategy);
    
    Long countCurrencyExchangeRates(CurrencyExchangeRatesFilter filter);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, FetchStrategy ... fetchStrategy);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Sorting sort, FetchStrategy ... fetchStrategy);
    List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Pagination page, FetchStrategy ... fetchStrategy);
    
	// ------------------------------------------------------------------------------
	// ThirdPartyVendor
	// ------------------------------------------------------------------------------
	ThirdPartyVendor getThirdPartyVendorById(Long id, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Pagination page, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
	
	// ------------------------------------------------------------------------------
	// ThirdPartyVendorType
	// ------------------------------------------------------------------------------
	ThirdPartyVendorType getThirdPartyVendorTypeById(Long id, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy);
	List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

}
