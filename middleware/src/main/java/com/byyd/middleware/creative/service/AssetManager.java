package com.byyd.middleware.creative.service;

import java.util.List;

import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.byyd.middleware.creative.filter.AssetBundleFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface AssetManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // Asset
    //------------------------------------------------------------------------------------------
    Asset newAsset(Creative creative, ContentType contentType, FetchStrategy... fetchStrategy);
    Asset newAsset(Creative creative, ContentType contentType, byte[] data, FetchStrategy... fetchStrategy);

    Asset getAssetById(String id, FetchStrategy... fetchStrategy);
    Asset getAssetById(Long id, FetchStrategy... fetchStrategy);
    Asset update(Asset asset);
    void delete(Asset asset);
    void deleteAssets(List<Asset> list);

    Asset getAssetByExternalId(String externalID, FetchStrategy... fetchStrategy);

    Long countAllAssetsForCreative(Creative creative);
    List<Asset> getAllAssetsForCreative(Creative creative, FetchStrategy... fetchStrategy);
    List<Asset> getAllAssetsForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy);
    List<Asset> getAllAssetsForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllAssetsForCreativeAndContentType(Creative creative, ContentType contentType);
    List<Asset> getAllAssetsForCreativeAndContentType(Creative creative, ContentType contentType, FetchStrategy... fetchStrategy);
    List<Asset> getAllAssetsForCreativeAndContentType(Creative creative, ContentType contentType, Sorting sort, FetchStrategy... fetchStrategy);
    List<Asset> getAllAssetsForCreativeAndContentType(Creative creative, ContentType contentType, Pagination page, FetchStrategy... fetchStrategy);
    
    Asset getAssetBySystemNameForCreative(Creative creative, String displayTypeSystemName, String componentSystemName);
    String getAssetExternalIdForCreative(Creative creative, String displayTypeSystemName, String componentSystemName);
    String getAssetTextForCreative(Creative creative, String displayTypeSystemName, String componentSystemName);
    
    //------------------------------------------------------------------------------------------
    // AssetBundle
    //------------------------------------------------------------------------------------------
    AssetBundle newAssetBundle(Creative creative, DisplayType displayType, FetchStrategy... fetchStrategy);

    AssetBundle getAssetBundleById(String id, FetchStrategy... fetchStrategy);
    AssetBundle getAssetBundleById(Long id, FetchStrategy... fetchStrategy);
    AssetBundle create(AssetBundle assetBundle);
    AssetBundle update(AssetBundle assetBundle);
    void delete(AssetBundle assetBundle);
    void deleteAssetBundles(List<AssetBundle> list);

    Long countAllAssetBundles(AssetBundleFilter filter);
    List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllAssetBundlesForCreative(Creative creative);
    List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllAssetBundlesForCreativeAndFormat(Creative creative, Format format);
    List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, Sorting sort, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format);
    List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, Sorting sort, FetchStrategy... fetchStrategy);
    List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // Component
    //------------------------------------------------------------------------------------------
    Component newComponent(Format format, String systemName, String name, FetchStrategy... fetchStrategy);

    Component getComponentById(String id, FetchStrategy... fetchStrategy);
    Component getComponentById(Long id, FetchStrategy... fetchStrategy);
    Component update(Component component);
    void delete(Component component);
    void deleteComponents(List<Component> list);

    Long countAllComponentsForFormat(Format format);
    List<Component> findAllComponentsForFormat(Format format, FetchStrategy... fetchStrategy);
    List<Component> findAllComponentsForFormat(Format format, Sorting sort, FetchStrategy... fetchStrategy);
    List<Component> findAllComponentsForFormat(Format format, Pagination page, FetchStrategy... fetchStrategy);
    
}
