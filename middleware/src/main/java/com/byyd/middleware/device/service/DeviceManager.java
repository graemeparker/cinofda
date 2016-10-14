package com.byyd.middleware.device.service;

import java.util.List;

import com.adfonic.domain.Browser;
import com.adfonic.domain.Capability;
import com.adfonic.domain.Country;
import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Model;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Vendor;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.device.filter.OperatorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface DeviceManager extends BaseManager {
    
    // Vendor constants
    static final String APPLE_VENDOR_NAME = "Apple";
    
    // Browser constants
    static final String OPERA_BROWSER_NAME = "Exclude Opera Mini";
    
    // Model constants
    static final String IPAD_MODEL_NAME   = "iPad";
    static final String IPOD_MODEL_NAME   = "iPod Touch";
    static final String IPHONE_MODEL_NAME = "iPhone";
    
    // Platform constants
    static final String APPLE_PLATFORM_SYSTEM_NAME   = "ios";
    static final String ANDROID_PLATFORM_SYSTEM_NAME = "android";
    static final String RIM_PLATFORM_SYSTEM_NAME     = "rim";
    static final String WINDOWS_PLATFORM_SYSTEM_NAME = "wp7";

    //------------------------------------------------------------------------------
    // DeviceGroup
    //------------------------------------------------------------------------------
    DeviceGroup newDeviceGroup(String systemName, String constraints, FetchStrategy... fetchStrategy);

    DeviceGroup getDeviceGroupById(String id, FetchStrategy... fetchStrategy);
    DeviceGroup getDeviceGroupById(Long id, FetchStrategy... fetchStrategy);
    DeviceGroup update(DeviceGroup deviceGroup);
    void delete(DeviceGroup deviceGroup);
    void deleteDeviceGroups(List<DeviceGroup> list);

    DeviceGroup getDeviceGroupBySystemName(String systemName);

    Long countAllDeviceGroups();
    List<DeviceGroup> getAllDeviceGroups(FetchStrategy ... fetchStrategy);
    List<DeviceGroup> getAllDeviceGroups(Sorting sort, FetchStrategy ... fetchStrategy);
    List<DeviceGroup> getAllDeviceGroups(Pagination page, Sorting sort, FetchStrategy ... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Vendor
    //------------------------------------------------------------------------------------------

    Vendor getVendorById(String id, FetchStrategy... fetchStrategy);
    Vendor getVendorById(Long id, FetchStrategy... fetchStrategy);
    Vendor create(Vendor vendor);
    Vendor update(Vendor vendor);
    void delete(Vendor vendor);
    void deleteVendors(List<Vendor> list);

    Vendor getVendorByName(String name, FetchStrategy... fetchStrategy);
    Vendor getVendorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Long countAllVendors();
    List<Vendor> getAllVendors(FetchStrategy... fetchStrategy);
    List<Vendor> getAllVendors(Sorting sort, FetchStrategy... fetchStrategy);
    List<Vendor> getAllVendors(Pagination page, FetchStrategy... fetchStrategy);

    Long countVendorsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);
    List<Vendor> getVendorsByPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Model
    //------------------------------------------------------------------------------------------
    Model newModel(Vendor vendor, String modelName, String externalID, DeviceGroup deviceGroup);

    Model getModelById(String id, FetchStrategy... fetchStrategy);
    Model getModelById(Long id, FetchStrategy... fetchStrategy);
    Model update(Model model);
    void delete(Model model);
    void deleteModels(List<Model> list);

    Model getModelByExternalId(String externalId, FetchStrategy... fetchStrategy);
    Model getModelByName(String name, FetchStrategy... fetchStrategy);
    Model getModelByName(String name, boolean caseSensitive, Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy);

    Long countAllModels();
    List<Model> getAllModels(FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Pagination page, FetchStrategy... fetchStrategy);

    Long countAllModels(ModelFilter filter);
    List<Model> getAllModels(ModelFilter filter, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(ModelFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(ModelFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllModels(Boolean deleted, Boolean hidden);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy);

    Long countModelsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    Long countModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, FetchStrategy... fetchStrategy);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Pagination page, FetchStrategy... fetchStrategy);

    Long countModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms);
    List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, FetchStrategy... fetchStrategy);
    List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, Pagination page, FetchStrategy... fetchStrategy);

    Long countModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup);
    List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy);
    List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, Pagination page, FetchStrategy... fetchStrategy);
    List<Model> getModelsByVendorNameAndPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Operator
    //------------------------------------------------------------------------------------------

    Operator getOperatorById(String id, FetchStrategy... fetchStrategy);
    Operator getOperatorById(Long id, FetchStrategy... fetchStrategy);
    Operator create(Operator operator);
    Operator update(Operator operator);

    Operator getOperatorByName(String name, FetchStrategy... fetchStrategy);
    Operator getOperatorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Long countAllOperators();
    List<Operator> getAllOperators(FetchStrategy... fetchStrategy);
    List<Operator> getAllOperators(Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getAllOperators(Pagination page, FetchStrategy... fetchStrategy);

    Long countOperatorsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    Long countOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova);
    List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, Pagination page, FetchStrategy... fetchStrategy);

    Long countOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries);
    List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries, Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries, Pagination page, FetchStrategy... fetchStrategy);
    
    Long countOperators(OperatorFilter filter);
    List<Operator> getOperators(OperatorFilter filter, FetchStrategy... fetchStrategy);
    List<Operator> getOperators(OperatorFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Operator> getOperators(OperatorFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Operator getOperatorForOperatorAliasAndCountry(OperatorAlias.Type operatorAliasType, Country country, String alias, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Platform
    //------------------------------------------------------------------------------------------

    Platform getPlatformById(String id, FetchStrategy... fetchStrategy);
    Platform getPlatformById(Long id, FetchStrategy... fetchStrategy);
    Platform create(Platform platform);
    Platform update(Platform platform);

    Platform getPlatformByName(String name, FetchStrategy... fetchStrategy);
    Platform getPlatformByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Platform getPlatformBySystemName(String name, FetchStrategy... fetchStrategy);

    Long countPlatformsByName(String name, LikeSpec like, boolean caseSensitive);
    List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllPlatforms();
    List<Platform> getAllPlatforms(FetchStrategy... fetchStrategy);
    List<Platform> getAllPlatforms(Sorting sort, FetchStrategy... fetchStrategy);
    List<Platform> getAllPlatforms(Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // DisplayType
    //------------------------------------------------------------------------------------------
    DisplayType newDisplayType(String systemName, String name, String constraints);

    DisplayType getDisplayTypeById(String id);
    DisplayType getDisplayTypeById(Long id);
    DisplayType update(DisplayType displayType);
    void delete(DisplayType displayType);
    void deleteDisplayTypes(List<DisplayType> list);

    DisplayType getDisplayTypeBySystemName(String systemName);

    List<DisplayType> getAllDisplayTypes(FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // DeviceIdentifierType
    //------------------------------------------------------------------------------
    DeviceIdentifierType create(DeviceIdentifierType deviceIdentifierType);
    DeviceIdentifierType update(DeviceIdentifierType deviceIdentifierType);
    void delete(DeviceIdentifierType deviceIdentifierType);

    DeviceIdentifierType getDeviceIdentifierTypeBySystemName(String systemName);
    List<DeviceIdentifierType> getAllDeviceIdentifierTypes();
    List<DeviceIdentifierType> getAllNonHiddenDeviceIdentifierTypes();
    List<DeviceIdentifierType> getUIDeviceIdentifierTypes();

    DeviceIdentifierType getDeviceIdentifierTypeForPromotion(DeviceIdentifierType type);
    
    //------------------------------------------------------------------------------------------
    // Browser
    //------------------------------------------------------------------------------------------
    Browser newBrowser(String name, FetchStrategy... fetchStrategy);

    Browser getBrowserById(String id, FetchStrategy... fetchStrategy);
    Browser getBrowserById(Long id, FetchStrategy... fetchStrategy);
    Browser create(Browser browser);
    Browser update(Browser browser);

    Browser getBrowserByName(String name, FetchStrategy... fetchStrategy);
    Browser getBrowserByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);
    Browser getOperaBrowser(FetchStrategy... fetchStrategy);

    Long countAllBrowsers();
    List<Browser> getAllBrowsers(FetchStrategy... fetchStrategy);
    List<Browser> getAllBrowsers(Sorting sort, FetchStrategy... fetchStrategy);
    List<Browser> getAllBrowsers(Pagination page, FetchStrategy... fetchStrategy);

    Long countBrowsersByName(String name, LikeSpec like, boolean caseSensitive);
    List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Capability
    //------------------------------------------------------------------------------------------
    Capability getCapabilityById(String id, FetchStrategy... fetchStrategy);
    Capability getCapabilityById(Long id, FetchStrategy... fetchStrategy);
    Capability create(Capability capability);
    Capability update(Capability capability);

    Capability getCapabilityByName(String name, FetchStrategy... fetchStrategy);
    Capability getCapabilityByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy);

    Long countAllCapabilities();
    List<Capability> getAllCapabilities(FetchStrategy... fetchStrategy);
    List<Capability> getAllCapabilities(Sorting sort, FetchStrategy... fetchStrategy);
    List<Capability> getAllCapabilities(Pagination page, FetchStrategy... fetchStrategy);

    Long countCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive);
    List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy);
    List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy);
    List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // DeviceTargetType
    //------------------------------------------------------------------------------------------

    public enum DeviceTargetType {
        IOS_ONLY("iOS"),
        ANDROID_ONLY("Android"),
        IOS_ANDROID_ONLY("iOS, Android"),
        OTHER_ONLY("Other"),
        OTHER_N_RIM_ONLY("Other BlackBerry"),
        OTHER_N_WINDOWS_ONLY("Other Windows"),
        MULTIPLE("Multiple"),
        ALL("All");

        private String description;

        private DeviceTargetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    };

    DeviceTargetType getPlatformDeviceTarget(Segment s);
    boolean isAppleTarget(Segment s);
    boolean isAndroidTarget(Segment s);
    String getPlatformDeviceTargetDescription(Segment s);
    boolean isAppleOnly(Segment segment);
    boolean isAndroidOnly(Segment segment);
    boolean isIOSAndroidOnly(Segment segment);
    boolean isMultiple(Segment segment);
    
}
