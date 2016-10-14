package com.byyd.middleware.common.service.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.adfonic.domain.Segment_;
import com.adfonic.domain.ThirdPartyVendor;
import com.adfonic.domain.ThirdPartyVendorType;
import com.adfonic.domain.UploadedContent;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.dao.CategoryDao;
import com.byyd.middleware.common.dao.ChannelDao;
import com.byyd.middleware.common.dao.ContentSpecDao;
import com.byyd.middleware.common.dao.ContentTypeDao;
import com.byyd.middleware.common.dao.CountryDao;
import com.byyd.middleware.common.dao.CurrencyExchangeRateDao;
import com.byyd.middleware.common.dao.FormatDao;
import com.byyd.middleware.common.dao.LanguageDao;
import com.byyd.middleware.common.dao.MobileIpAddressRangeDao;
import com.byyd.middleware.common.dao.RegionDao;
import com.byyd.middleware.common.dao.RemovalInfoDao;
import com.byyd.middleware.common.dao.ThirdPartyVendorDao;
import com.byyd.middleware.common.dao.ThirdPartyVendorTypeDao;
import com.byyd.middleware.common.dao.UploadedContentDao;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.common.filter.ThirdPartyVendorFilter;
import com.byyd.middleware.common.filter.ThirdPartyVendorTypeFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("commonManager")
public class CommonManagerJpaImpl extends BaseJpaManagerImpl implements CommonManager {
    
    @Autowired(required=false)
    private UploadedContentDao uploadedContentDao;
    
    @Autowired(required = false)
    private MobileIpAddressRangeDao mobileIpAddressRangeDao;
    
    @Autowired(required = false)
    private CategoryDao categoryDao;
    
    @Autowired(required = false)
    private CountryDao countryDao;
    
    @Autowired(required = false)
    private RegionDao regionDao;
    
    @Autowired(required = false)
    private LanguageDao languageDao;
    
    @Autowired(required=false)
    private RemovalInfoDao removalInfoDao;
    
    @Autowired(required = false)
    private ChannelDao channelDao;
    
    @Autowired(required = false)
    private ContentTypeDao contentTypeDao;
    
    @Autowired(required = false)
    private FormatDao formatDao;
    
    @Autowired(required = false)
    private ContentSpecDao contentSpecDao;
    
    @Autowired(required = false)
    private CurrencyExchangeRateDao currencyExchangeRateDao;
    
    @Autowired(required = false)
    private ThirdPartyVendorDao thirdPartyVendorDao;
    
    @Autowired(required = false)
    private ThirdPartyVendorTypeDao thirdPartyVendorTypeDao;
    
    //------------------------------------------------------------------------------------------
    // UploadedContent
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public UploadedContent newUploadedContent(ContentType contentType, FetchStrategy... fetchStrategy) {
        return newUploadedContent(contentType, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public UploadedContent newUploadedContent(ContentType contentType, byte[] data, FetchStrategy... fetchStrategy) {
        UploadedContent content = new UploadedContent(contentType);
        if(data != null) {
            content.setData(data);
        }
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(content);
        } else {
            content = create(content);
            return getUploadedContentById(content.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public UploadedContent getUploadedContentById(String id, FetchStrategy... fetchStrategy) {
        return getUploadedContentById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public UploadedContent getUploadedContentById(Long id, FetchStrategy... fetchStrategy) {
        return uploadedContentDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public UploadedContent create(UploadedContent uploadedContent) {
        return uploadedContentDao.create(uploadedContent);
    }

    @Override
    @Transactional(readOnly=false)
    public UploadedContent update(UploadedContent uploadedContent) {
        return uploadedContentDao.update(uploadedContent);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(UploadedContent uploadedContent) {
        uploadedContentDao.delete(uploadedContent);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteUploadedContent(List<UploadedContent> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(UploadedContent uploadedContent : list) {
            delete(uploadedContent);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public UploadedContent getUploadedContentByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return uploadedContentDao.getByExternalId(externalId, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // MobileIpAddressRange
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public MobileIpAddressRange getMobileIpAddressRangeById(String id, FetchStrategy... fetchStrategy) {
        return getMobileIpAddressRangeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public MobileIpAddressRange getMobileIpAddressRangeById(Long id, FetchStrategy... fetchStrategy) {
        return mobileIpAddressRangeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public MobileIpAddressRange create(MobileIpAddressRange mobileIpAddressRange) {
        return mobileIpAddressRangeDao.create(mobileIpAddressRange);
    }

    @Override
    @Transactional(readOnly = false)
    public MobileIpAddressRange update(MobileIpAddressRange mobileIpAddressRange) {
        return mobileIpAddressRangeDao.update(mobileIpAddressRange);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(MobileIpAddressRange mobileIpAddressRange) {
        mobileIpAddressRangeDao.delete(mobileIpAddressRange);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteMobileIpAddressRanges(List<MobileIpAddressRange> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (MobileIpAddressRange entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllMobileIpAddressRanges() {
        return mobileIpAddressRangeDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileIpAddressRange> getAllMobileIpAddressRanges(FetchStrategy... fetchStrategy) {
        return mobileIpAddressRangeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileIpAddressRange> getAllMobileIpAddressRanges(Sorting sort, FetchStrategy... fetchStrategy) {
        return mobileIpAddressRangeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileIpAddressRange> getAllMobileIpAddressRanges(Pagination page, FetchStrategy... fetchStrategy) {
        return mobileIpAddressRangeDao.getAll(page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Category
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(String id, FetchStrategy... fetchStrategy) {
        return getCategoryById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id, FetchStrategy... fetchStrategy) {
        return categoryDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Category create(Category category) {
        return categoryDao.create(category);
    }

    @Override
    @Transactional(readOnly = false)
    public Category update(Category category) {
        return categoryDao.update(category);
    }

    @Transactional(readOnly = false)
    public void delete(Category category) {
        categoryDao.delete(category);
    }

    @Transactional(readOnly = false)
    public void deleteCategories(List<Category> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Category entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name, FetchStrategy... fetchStrategy) {
        return categoryDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return categoryDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryByIabId(String iabId, FetchStrategy... fetchStrategy) {
        return categoryDao.getByIabId(iabId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCategories() {
        return categoryDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories(FetchStrategy... fetchStrategy) {
        return categoryDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories(Sorting sort, FetchStrategy... fetchStrategy) {
        return categoryDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories(Pagination page, FetchStrategy... fetchStrategy) {
        return categoryDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCategoriesForParent(Category parent) {
        if (parent == null) {
            return countAllTopLevelCategories();
        }
        return categoryDao.countAllForParent(parent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForParent(Category parent, FetchStrategy... fetchStrategy) {
        if (parent == null) {
            return getAllCategories(fetchStrategy);
        }
        return categoryDao.getAllForParent(parent, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForParent(Category parent, Sorting sort, FetchStrategy... fetchStrategy) {
        if (parent == null) {
            return getAllTopLevelCategories(sort, fetchStrategy);
        }
        return categoryDao.getAllForParent(parent, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForParent(Category parent, Pagination page, FetchStrategy... fetchStrategy) {
        if (parent == null) {
            return getAllTopLevelCategories(page, fetchStrategy);
        }
        return categoryDao.getAllForParent(parent, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllTopLevelCategories() {
        return categoryDao.countAllForParentIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllTopLevelCategories(FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForParentIsNull(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllTopLevelCategories(Sorting sort, FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForParentIsNull(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllTopLevelCategories(Pagination page, FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForParentIsNull(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getDefaultCategory(FetchStrategy... fetchStrategy) {
        return this.getCategoryByName(Category.NOT_CATEGORIZED_NAME, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCategoriesByName(String name, LikeSpec like, boolean caseSensitive) {
        return categoryDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return categoryDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getParentCategoryForCategory(Category category, FetchStrategy... fetchStrategy) {
        return categoryDao.getParentCategoryForCategory(category, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // Country
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Country getCountryById(String id, FetchStrategy... fetchStrategy) {
        return getCountryById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Country getCountryById(Long id, FetchStrategy... fetchStrategy) {
        return countryDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Country create(Country country) {
        return countryDao.create(country);
    }

    @Override
    @Transactional(readOnly = false)
    public Country update(Country country) {
        return countryDao.update(country);
    }

    @Transactional(readOnly = false)
    public void delete(Country country) {
        countryDao.delete(country);
    }

    @Transactional(readOnly = false)
    public void deleteCountries(List<Country> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Country entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Country getCountryByName(String name, FetchStrategy... fetchStrategy) {
        return countryDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Country getCountryByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return countryDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Country getCountryByIsoCode(String isoCode, FetchStrategy... fetchStrategy) {
        return countryDao.getByIsoCode(isoCode, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public String getCountryNamesBySegment(Segment s) {
        Set<Country> countries = s.getCountries();
        if (!countries.isEmpty()) {
            return StringUtils.join(countries, ',');
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getExcludedCategoryNamesBySegment(Segment segment) {
        TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        
        Segment localSegment = segment;
        try {
            localSegment.getExcludedCategories().size();
        } catch (Exception e) {
            FetchStrategy fs = new FetchStrategyBuilder().addLeft(Segment_.excludedCategories).build();
            localSegment = targetingManager.getSegmentById(localSegment.getId(), fs);
        }
        Set<Category> excludedCategories = localSegment.getExcludedCategories();
        if (!excludedCategories.isEmpty()) {
            return StringUtils.join(excludedCategories, ',');
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCountries() {
        return countryDao.countAllCountries(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(false, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(Sorting sort, FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(false, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(Pagination page, FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(false, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCountries(boolean includeHidden) {
        return countryDao.countAllCountries(includeHidden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(boolean includeHidden, FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(includeHidden, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(boolean includeHidden, Sorting sort, FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(includeHidden, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries(boolean includeHidden, Pagination page, FetchStrategy... fetchStrategy) {
        return countryDao.getAllCountries(includeHidden, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden) {
        return countryDao.countCountriesByName(name, like, caseSensitive, hidden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, FetchStrategy... fetchStrategy) {
        return countryDao.getCountriesByName(name, like, caseSensitive, hidden, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy) {
        return countryDao.getCountriesByName(name, like, caseSensitive, hidden, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy) {
        return countryDao.getCountriesByName(name, like, caseSensitive, hidden, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCountriesByName(String name, LikeSpec like, boolean caseSensitive) {
        return this.countCountriesByName(name, like, caseSensitive, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return this.getCountriesByName(name, like, caseSensitive, (Boolean) null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getCountriesByName(name, like, caseSensitive, (Boolean) null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> getCountriesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getCountriesByName(name, like, caseSensitive, (Boolean) null, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Region
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Region getRegionById(String id, FetchStrategy... fetchStrategy) {
        return getRegionById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Region getRegionById(Long id, FetchStrategy... fetchStrategy) {
        return regionDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Region create(Region region) {
        return regionDao.create(region);
    }

    @Override
    @Transactional(readOnly = false)
    public Region update(Region region) {
        return regionDao.update(region);
    }

    @Transactional(readOnly = false)
    public void delete(Region region) {
        regionDao.delete(region);
    }

    @Transactional(readOnly = false)
    public void deleteRegions(List<Region> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Region region : list) {
            delete(region);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Region getRegionByName(String name, FetchStrategy... fetchStrategy) {
        return regionDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Region getRegionByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return regionDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllRegions() {
        return regionDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getAllRegions(FetchStrategy... fetchStrategy) {
        return regionDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getAllRegions(Sorting sort, FetchStrategy... fetchStrategy) {
        return regionDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getAllRegions(Pagination page, FetchStrategy... fetchStrategy) {
        return regionDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countRegionsByName(String name, LikeSpec like, boolean caseSensitive) {
        return regionDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return regionDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return regionDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> getRegionsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return regionDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Language
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Language getLanguageById(String id, FetchStrategy... fetchStrategy) {
        return getLanguageById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Language getLanguageById(Long id, FetchStrategy... fetchStrategy) {
        return languageDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Language create(Language language) {
        return languageDao.create(language);
    }

    @Override
    @Transactional(readOnly = false)
    public Language update(Language language) {
        return languageDao.update(language);
    }

    @Transactional(readOnly = false)
    public void delete(Language language) {
        languageDao.delete(language);
    }

    @Transactional(readOnly = false)
    public void deleteLanguages(List<Language> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Language language : list) {
            delete(language);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Language getLanguageByIsoCode(String isoCode, FetchStrategy... fetchStrategy) {
        return languageDao.getByIsoCode(isoCode, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Language getLanguageByName(String name, FetchStrategy... fetchStrategy) {
        return languageDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Language getLanguageByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return languageDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllLanguages() {
        return languageDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getAllLanguages(FetchStrategy... fetchStrategy) {
        return languageDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getAllLanguages(Sorting sort, FetchStrategy... fetchStrategy) {
        return languageDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getAllLanguages(Pagination page, FetchStrategy... fetchStrategy) {
        return languageDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countLanguagesByName(String name, LikeSpec like, boolean caseSensitive) {
        return languageDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return languageDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return languageDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> getLanguagesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return languageDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // RemovalInfo
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public RemovalInfo getRemovalInfoById(String id, FetchStrategy... fetchStrategy) {
        return getRemovalInfoById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public RemovalInfo getRemovalInfoById(Long id, FetchStrategy... fetchStrategy) {
        return removalInfoDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public RemovalInfo create(RemovalInfo removalInfo) {
        return removalInfoDao.create(removalInfo);
    }

    @Override
    @Transactional(readOnly=false)
    public RemovalInfo update(RemovalInfo removalInfo) {
        return removalInfoDao.update(removalInfo);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(RemovalInfo removalInfo) {
        removalInfoDao.delete(removalInfo);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteRemovalInfos(List<RemovalInfo> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(RemovalInfo removalInfo : list) {
            delete(removalInfo);
        }
    }
    
    // ------------------------------------------------------------------------------------------
    // Channel
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Channel newChannel(String name, FetchStrategy... fetchStrategy) {
        Channel channel = new Channel(name);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(channel);
        } else {
            channel = create(channel);
            return getChannelById(channel.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannelById(String id, FetchStrategy... fetchStrategy) {
        return getChannelById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannelById(Long id, FetchStrategy... fetchStrategy) {
        return channelDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Channel create(Channel channel) {
        return channelDao.create(channel);
    }

    @Override
    @Transactional(readOnly = false)
    public Channel update(Channel channel) {
        return channelDao.update(channel);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Channel channel) {
        channelDao.delete(channel);
    }

    @Transactional(readOnly = false)
    public void deleteChannels(List<Channel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Channel entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannelByName(String name, FetchStrategy... fetchStrategy) {
        return channelDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllChannels() {
        return channelDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(FetchStrategy... fetchStrategy) {
        return channelDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(Sorting sort, FetchStrategy... fetchStrategy) {
        return channelDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(Pagination page, FetchStrategy... fetchStrategy) {
        return channelDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllChannels(ChannelFilter filter) {
        return channelDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(ChannelFilter filter, FetchStrategy... fetchStrategy) {
        return channelDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(ChannelFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return channelDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels(ChannelFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return channelDao.getAll(filter, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // ContentType
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public ContentType newContentType(String name, String mimeType, boolean animated, FetchStrategy... fetchStrategy) {
        ContentType contentType = new ContentType(name, mimeType, animated);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(contentType);
        } else {
            contentType = create(contentType);
            return getContentTypeById(contentType.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContentType getContentTypeById(String id,
            FetchStrategy... fetchStrategy) {
        return getContentTypeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentType getContentTypeById(Long id,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public ContentType create(ContentType contentType) {
        return contentTypeDao.create(contentType);
    }

    @Override
    @Transactional(readOnly = false)
    public ContentType update(ContentType contentType) {
        return contentTypeDao.update(contentType);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ContentType contentType) {
        contentTypeDao.delete(contentType);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteContentTypes(List<ContentType> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ContentType entry : list) {
            delete(entry);
        }
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countContentTypesForMimeType(String mimeType) {
        return contentTypeDao.countAllForMimeType(mimeType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeType(String mimeType,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeType(mimeType, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeType(String mimeType,
            Sorting sort, FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeType(mimeType, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeType(String mimeType,
            Pagination page, FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeType(mimeType, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countContentTypesForMimeTypeLike(String mimeType) {
        return contentTypeDao.countAllForMimeTypeLike("%" + mimeType + "%");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeTypeLike("%" + mimeType + "%",
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType,
            Sorting sort, FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeTypeLike("%" + mimeType + "%", sort,
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentType> getAllContentTypesForMimeTypeLike(String mimeType,
            Pagination page, FetchStrategy... fetchStrategy) {
        return contentTypeDao.getAllForMimeTypeLike("%" + mimeType + "%", page,
                fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ContentType getContentTypeByName(String name,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentType getContentTypeForMimeType(String mimeType,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getOneForMimeType(mimeType, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentType getContentTypeForMimeType(String mimeType, boolean animated,
            FetchStrategy... fetchStrategy) {
        return contentTypeDao.getOneForMimeType(mimeType, animated, fetchStrategy);
    }


    // ------------------------------------------------------------------------------------------
    // Format
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Format getFormatById(String id, FetchStrategy... fetchStrategy) {
        return getFormatById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Format getFormatById(Long id, FetchStrategy... fetchStrategy) {
        return formatDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Format create(Format format) {
        return formatDao.create(format);
    }

    @Override
    @Transactional(readOnly = false)
    public Format update(Format format) {
        return formatDao.update(format);
    }

    @Transactional(readOnly = false)
    public void delete(Format format) {
        formatDao.delete(format);
    }

    @Transactional(readOnly = false)
    public void deleteFormats(List<Format> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Format entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Format getFormatByName(String name, FetchStrategy... fetchStrategy) {
        return formatDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Format getFormatByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return formatDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Format getFormatBySystemName(String systemName, FetchStrategy... fetchStrategy) {
        return formatDao.getBySystemName(systemName, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllFormats() {
        return formatDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getAllFormats(FetchStrategy... fetchStrategy) {
        return formatDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getAllFormats(Sorting sort, FetchStrategy... fetchStrategy) {
        return formatDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getAllFormats(Pagination page, FetchStrategy... fetchStrategy) {
        return formatDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getSupportedFormats(Collection<AdSpace> adSpaces, FetchStrategy... fetchStrategy) {
        return formatDao.getSupportedFormats(adSpaces, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getSupportedFormats(Campaign campaign, FetchStrategy... fetchStrategy) {
        return formatDao.getSupportedFormats(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countFormatsByName(String name, LikeSpec like, boolean caseSensitive) {
        return formatDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return formatDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return formatDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Format> getFormatsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return formatDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------
    // ContentSpec
    // ------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public ContentSpec newContentSpec(String name, String manifest, FetchStrategy... fetchStrategy) {
        ContentSpec contentSpec = new ContentSpec(name, manifest);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(contentSpec);
        } else {
            contentSpec = create(contentSpec);
            return this.getContentSpecById(contentSpec.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContentSpec getContentSpecById(String id, FetchStrategy... fetchStrategy) {
        return this.getContentSpecById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentSpec getContentSpecById(Long id, FetchStrategy... fetchStrategy) {
        return contentSpecDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public ContentSpec create(ContentSpec contentSpec) {
        return contentSpecDao.create(contentSpec);
    }

    @Override
    @Transactional(readOnly = false)
    public ContentSpec update(ContentSpec contentSpec) {
        return contentSpecDao.update(contentSpec);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ContentSpec contentSpec) {
        contentSpecDao.delete(contentSpec);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteContentSpecs(List<ContentSpec> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ContentSpec entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllContentSpecs() {
        return contentSpecDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentSpec> getAllContentSpecs(FetchStrategy... fetchStrategy) {
        return contentSpecDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentSpec> getAllContentSpecs(Sorting sort, FetchStrategy... fetchStrategy) {
        return contentSpecDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentSpec> getAllContentSpecs(Pagination page, FetchStrategy... fetchStrategy) {
        return contentSpecDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentSpec getContentSpecByName(String name, FetchStrategy... fetchStrategy) {
        return contentSpecDao.getByName(name, fetchStrategy);
    }
    
    
    //------------------------------------------------------------------------------------------
    // CurrencyExchangeRate
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public CurrencyExchangeRate getCurrencyExchangeRateById(String id, FetchStrategy... fetchStrategy){
        return currencyExchangeRateDao.getById(makeLong(id), fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CurrencyExchangeRate getCurrencyExchangeRateById(Long id, FetchStrategy... fetchStrategy){
        return currencyExchangeRateDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = false)
    public CurrencyExchangeRate update(CurrencyExchangeRate currencyExchangeRate){
        return currencyExchangeRateDao.update(currencyExchangeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCurrencyExchangeRate(){
        return currencyExchangeRateDao.countAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getAllCurrencyExchangeRate(FetchStrategy... fetchStrategy){
        return currencyExchangeRateDao.getAll(fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getAllCurrencyExchangeRate(Sorting sort, FetchStrategy... fetchStrategy){
        return currencyExchangeRateDao.getAll(sort, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getAllCurrencyExchangeRate(Pagination page, FetchStrategy... fetchStrategy){
        return currencyExchangeRateDao.getAll(page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countCurrencyExchangeRates(CurrencyExchangeRatesFilter filter){
        return currencyExchangeRateDao.countCurrencyExchangeRates(filter);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, FetchStrategy ... fetchStrategy){
        return currencyExchangeRateDao.getCurrencyExchangeRates(filter, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Sorting sort, FetchStrategy ... fetchStrategy){
        return currencyExchangeRateDao.getCurrencyExchangeRates(filter, sort, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyExchangeRate> getCurrencyExchangeRates(CurrencyExchangeRatesFilter filter, Pagination page, FetchStrategy ... fetchStrategy){
        return currencyExchangeRateDao.getCurrencyExchangeRates(filter, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // ThirdPartyVendor
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ThirdPartyVendor getThirdPartyVendorById(Long id, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendor> getAllThirdPartyVendors(ThirdPartyVendorFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorDao.getAll(filter, page, sort, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // ThirdPartyVendor
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ThirdPartyVendorType getThirdPartyVendorTypeById(Long id, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorTypeDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorTypeDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorTypeDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorTypeDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyVendorType> getAllThirdPartyVendorTypes(ThirdPartyVendorTypeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return thirdPartyVendorTypeDao.getAll(filter, page, sort, fetchStrategy);
    }
}
