package com.byyd.middleware.creative.service.jpa;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Asset_;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.dao.AssetBundleDao;
import com.byyd.middleware.creative.dao.AssetDao;
import com.byyd.middleware.creative.dao.ComponentDao;
import com.byyd.middleware.creative.exception.CreativeManagerException;
import com.byyd.middleware.creative.filter.AssetBundleFilter;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("assetManager")
public class AssetManagerJpaImpl extends BaseJpaManagerImpl implements AssetManager {
    
    private static final transient Logger LOG = Logger.getLogger(AssetManagerJpaImpl.class.getName());
    
    @Autowired(required = false)
    private AssetDao assetDao;
    
    @Autowired(required = false)
    private AssetBundleDao assetBundleDao;
    
    @Autowired(required = false)
    private ComponentDao componentDao;
    
    // ------------------------------------------------------------------------------------------
    // Asset
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Asset newAsset(Creative creative, ContentType contentType, FetchStrategy... fetchStrategy) {
        return this.newAsset(creative, contentType, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Asset newAsset(Creative creative, ContentType contentType, byte[] data, FetchStrategy... fetchStrategy) {
        Asset asset = creative.newAsset(contentType);
        if(data != null) {
            asset.setData(data);
        }
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(asset);
        } else {
            asset = create(asset);
            return getAssetById(asset.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Asset getAssetById(String id, FetchStrategy... fetchStrategy) {
        return getAssetById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Asset getAssetById(Long id, FetchStrategy... fetchStrategy) {
        return assetDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Asset create(Asset asset) {
        return assetDao.create(asset);
    }

    @Override
    @Transactional(readOnly = false)
    public Asset update(Asset asset) {
        return assetDao.update(asset);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Asset asset) {
        LOG.fine("Deleting asset " + asset.getId());
        FetchStrategy fs = new FetchStrategyBuilder()
                            .addInner(Asset_.creative)
                            .addLeft(Creative_.assetBundleMap)
                            .addLeft(AssetBundle_.assetMap)
                            .nonRecursive(AssetBundle_.assetMap)
                            .build();
        asset = this.getAssetById(asset.getId(), fs);
        LOG.fine("Asset was reloaded");
        
        for(AssetBundle bundle : asset.getCreative().getAssetBundleMap().values()) {
            LOG.fine("Examining bundle " + bundle.getId());
            boolean bundleModified = false;
            for(Entry<Component,Asset> entry : bundle.getAssetMap().entrySet()) {
                Asset a = entry.getValue();
                LOG.fine("Examining asset " + a.getId());
                if(a.equals(asset)) {
                    LOG.fine("Removing using key " + entry.getKey().getName());
                    bundle.getAssetMap().remove(entry.getKey());
                    bundleModified = true;
                }
            }
            if(bundleModified) {
                LOG.fine("Updating bundle " + bundle.getId());
                this.update(bundle);
            }
        }
        //asset = this.getAssetById(asset.getId());
        LOG.fine("Deleting asset " + asset.getId());
        assetDao.delete(asset);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAssets(List<Asset> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Asset entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Asset getAssetByExternalId(String externalID,
            FetchStrategy... fetchStrategy) {
        return assetDao.getByExternalId(externalID, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetsForCreative(Creative creative) {
        return assetDao.countAllForCreative(creative);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreative(Creative creative,
            FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreative(creative, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreative(Creative creative, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreative(creative, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreative(Creative creative,
            Pagination page, FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreative(creative, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetsForCreativeAndContentType(Creative creative,
            ContentType contentType) {
        return assetDao
                .countAllForCreativeAndContentType(creative, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreativeAndContentType(Creative creative,
            ContentType contentType, FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreativeAndContentType(creative, contentType,
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreativeAndContentType(Creative creative,
            ContentType contentType, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreativeAndContentType(creative, contentType,
                sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssetsForCreativeAndContentType(Creative creative,
            ContentType contentType, Pagination page,
            FetchStrategy... fetchStrategy) {
        return assetDao.findAllByCreativeAndContentType(creative, contentType,
                page, fetchStrategy);
    }


    @Override
    @Transactional(readOnly=true)
    public Asset getAssetBySystemNameForCreative(Creative creative, String displayTypeSystemName, String componentSystemName) {
        if ("".equals(displayTypeSystemName)) {
            displayTypeSystemName = null;
        }
        boolean assetBundleMapSet = false;
        try {
            creative.getAssetBundleMap().size();
            assetBundleMapSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!assetBundleMapSet) {
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Creative_.assetBundleMap)
                                   .addLeft(AssetBundle_.assetMap)
                                   .build();
            CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
            creative = creativeManager.getCreativeById(creative.getId(), fs);
        }
        if(creative.getAssetBundleMap() == null) {
            throw new CreativeManagerException("AssetBundleMap is null for creative " + creative.getId());
        }
        
        for (DisplayType dt : creative.getAssetBundleMap().keySet()) {
            if (displayTypeSystemName == null || dt.getSystemName().equals(displayTypeSystemName)) {
                AssetBundle ab = creative.getAssetBundleMap().get(dt);
                Asset asset = findAssetByComponentSystemName(componentSystemName, ab);
                if (asset!=null){
                    return asset;
                }
            }
        }
        return null;
    }

    private Asset findAssetByComponentSystemName(String componentSystemName, AssetBundle ab) {
        Asset asset = null;
        for (Component cp : ab.getAssetMap().keySet()) {
            if (cp.getSystemName().equals(componentSystemName)) {
                asset = ab.getAssetMap().get(cp);
                break;
            }
        }
        return asset;
    }

    @Override
    @Transactional(readOnly=true)
    public String getAssetExternalIdForCreative(Creative creative, String displayTypeSystemName, String componentSystemName) {
        Asset as = getAssetBySystemNameForCreative(creative, displayTypeSystemName, componentSystemName);
        if (as == null) { 
            return null; 
        }
        return as.getExternalID();
    }

    @Override
    @Transactional(readOnly=true)
    public String getAssetTextForCreative(Creative creative, String displayTypeSystemName, String componentSystemName) {
        Asset as = getAssetBySystemNameForCreative(creative, displayTypeSystemName, componentSystemName);
        if (as == null) { 
            return null; 
        }
        return as.getDataAsString();
    }

    // ------------------------------------------------------------------------------------------
    // AssetBundle
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public AssetBundle newAssetBundle(Creative creative, DisplayType displayType, FetchStrategy... fetchStrategy) {
        boolean assetBundleMapSet = false;
        try {
            creative.getAssetBundleMap().size();
            assetBundleMapSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!assetBundleMapSet) {
            CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Creative_.assetBundleMap)
                               .build();
            creative = creativeManager.getCreativeById(creative.getId(), fs);
        }
        AssetBundle bundle = creative.newAssetBundle(displayType);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(bundle);
        } else {
            bundle = create(bundle);
            return getAssetBundleById(bundle.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AssetBundle getAssetBundleById(String id, FetchStrategy... fetchStrategy) {
        return getAssetBundleById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetBundle getAssetBundleById(Long id, FetchStrategy... fetchStrategy) {
        return assetBundleDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public AssetBundle create(AssetBundle assetBundle) {
        return assetBundleDao.create(assetBundle);
    }

    @Override
    @Transactional(readOnly = false)
    public AssetBundle update(AssetBundle assetBundle) {
        return assetBundleDao.update(assetBundle);
    }

    /**
     * Note: this clears the mappings in ASSET_BUNDLE_ASSET_MAP automatically
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(AssetBundle assetBundle) {
        assetBundleDao.delete(assetBundle);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAssetBundles(List<AssetBundle> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AssetBundle entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetBundles(AssetBundleFilter filter) {
        return assetBundleDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, FetchStrategy... fetchStrategy) {
        return assetBundleDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return assetBundleDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundles(AssetBundleFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return assetBundleDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetBundlesForCreative(Creative creative) {
        return countAllAssetBundles(new AssetBundleFilter().setCreative(creative));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative), page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Transactional(readOnly=true)
    protected List<DisplayType> getDisplayTypesForFormat(Format format) {
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        try {
            format.getDisplayTypes().size();
            return format.getDisplayTypes();
        } catch(Exception e) {
            //do nothing
        }
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Format_.displayTypes)
                           .build();
        format = commonManager.getFormatById(format.getId(), fs);
        return format.getDisplayTypes();
    }


    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetBundlesForCreativeAndFormat(Creative creative, Format format) {
        return countAllAssetBundles(new AssetBundleFilter().setCreative(creative).setIncludeDisplayTypes(getDisplayTypesForFormat(format)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setIncludeDisplayTypes(getDisplayTypesForFormat(format)), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setIncludeDisplayTypes(getDisplayTypesForFormat(format)), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndFormat(Creative creative, Format format, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setIncludeDisplayTypes(getDisplayTypesForFormat(format)), page, fetchStrategy);
    }


    @Override
    @Transactional(readOnly = true)
    public Long countAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format) {
        return countAllAssetBundles(new AssetBundleFilter().setCreative(creative).setExcludeDisplayTypes(getDisplayTypesForFormat(format)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setExcludeDisplayTypes(getDisplayTypesForFormat(format)), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setExcludeDisplayTypes(getDisplayTypesForFormat(format)), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetBundle> getAllAssetBundlesForCreativeAndNotFormat(Creative creative, Format format, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllAssetBundles(new AssetBundleFilter().setCreative(creative).setExcludeDisplayTypes(getDisplayTypesForFormat(format)), page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // Component
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Component newComponent(Format format, String systemName, String name, FetchStrategy... fetchStrategy) {
        boolean componentsSet = false;
        try {
            format.getComponents().size();
            componentsSet = true;
        } catch (Exception e) {
            //do nothing    
        }
        if (!componentsSet) {
            CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
            FetchStrategy fs = new FetchStrategyBuilder().addLeft(Format_.components).build();
            format = commonManager.getFormatById(format.getId(), fs);
        }
        Component component = format.newComponent(systemName, name);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(component);
        } else {
            component = create(component);
            return getComponentById(component.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Component getComponentById(String id, FetchStrategy... fetchStrategy) {
        return getComponentById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Component getComponentById(Long id, FetchStrategy... fetchStrategy) {
        return componentDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Component create(Component component) {
        return componentDao.create(component);
    }

    @Override
    @Transactional(readOnly = false)
    public Component update(Component component) {
        return componentDao.update(component);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Component component) {
        componentDao.delete(component);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteComponents(List<Component> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Component entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllComponentsForFormat(Format format) {
        return componentDao.countAllForFormat(format);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> findAllComponentsForFormat(Format format, FetchStrategy... fetchStrategy) {
        return componentDao.findAllByFormat(format, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> findAllComponentsForFormat(Format format, Sorting sort, FetchStrategy... fetchStrategy) {
        return componentDao.findAllByFormat(format, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> findAllComponentsForFormat(Format format, Pagination page, FetchStrategy... fetchStrategy) {
        return componentDao.findAllByFormat(format, page, fetchStrategy);
    }
}
