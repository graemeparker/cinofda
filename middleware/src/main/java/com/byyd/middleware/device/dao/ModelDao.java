package com.byyd.middleware.device.dao;

import java.util.List;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Model;
import com.adfonic.domain.Platform;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ModelDao extends BusinessKeyDao<Model> {

    Model getModelByName(String name, boolean caseSensitive, Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy);

    Long countAll(ModelFilter filter);
    List<Model> findAll(ModelFilter filter, FetchStrategy... fetchStrategy);
    List<Model> findAll(ModelFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> findAll(ModelFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllModels(Boolean deleted, Boolean hidden);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy);
    List<Model> getAllModels(Boolean deleted, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy);

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

    //List<Model> getAllModelsJoinWithVendor(Boolean deleted, Boolean hidden, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
}
