package com.byyd.middleware.creative.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.Component;
import com.adfonic.domain.Component_;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AssetManagerIT extends AbstractAdfonicTest {

    @Autowired
    private AssetManager assetManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private DeviceManager deviceManager;
    
    @Autowired
    private CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAssetWithInvalidId() {
        assertNull(assetManager.getAssetById(0L));
    }

    @Test
    public void testAsset() {
        Creative creative = creativeManager.getCreativeById(3L);
        ContentType contentType = commonManager.getContentTypeByName("PNG");
        Asset asset = null;
        byte[] data = "Testing".getBytes();
        try {
            asset = assetManager.newAsset(creative, contentType, data);
            assertNotNull(asset);
            long id = asset.getId();
            assertTrue(id > 0L);
            asset = assetManager.getAssetById(id);
            assertEquals(new String(data), new String(asset.getData()));

            assertEquals(asset,assetManager.getAssetById(id));
            assertEquals(asset,assetManager.getAssetById(Long.toString(id)));
            assertEquals(asset,assetManager.getAssetByExternalId(asset.getExternalID()));

            List<Asset> assets = assetManager.getAllAssetsForCreative(creative);
            assertTrue(assets.contains(asset));

            assets = assetManager.getAllAssetsForCreativeAndContentType(creative, contentType);
            assertTrue(assets.contains(asset));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            assetManager.delete(asset);
            assertNull(assetManager.getAssetById(asset.getId()));
        }
    }

    @Test
    public void testAssetDelete() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(AssetBundle.class, "assetMap", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Creative.class, "assetBundleMap", JoinType.LEFT);
        Creative creative = creativeManager.getCreativeById(3L, fs);
        DisplayType displayType = deviceManager.getDisplayTypeById(1L);
        commonManager.getFormatById(1L);
        Component component = assetManager.getComponentById(1L);
        AssetBundle bundle = null;
        ContentType contentType = commonManager.getContentTypeByName("PNG");
        byte[] data = "Testing".getBytes();
        try {
            bundle = assetManager.newAssetBundle(creative, displayType);
            assertNotNull(bundle);
            long id = bundle.getId();
            assertTrue(id > 0L);

            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertEquals(creative.getAssetBundle(displayType), bundle);

            bundle = assetManager.getAssetBundleById(bundle.getId(), fs);
            Asset asset = assetManager.newAsset(creative, contentType, data);
            bundle.putAsset(component, asset);
            bundle = assetManager.update(bundle);
            bundle = assetManager.getAssetBundleById(bundle.getId(), fs);
            Map<Component,Asset> assetMap = bundle.getAssetMap();
            assertTrue(assetMap.containsValue(asset));

            assetManager.delete(asset);
            assertNull(assetManager.getAssetById(asset.getId()));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            bundle = assetManager.getAssetBundleById(bundle.getId());
            assetManager.delete(bundle);
            assertNull(assetManager.getAssetBundleById(bundle.getId()));
            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertNull(creative.getAssetBundle(displayType));
        }
    }

    @Test
    public void testGetAssetBySystemNameForCreative() {
        Creative creative = creativeManager.getCreativeById(3L);
        String displayTypeSystemName = "generic";
        String componentSystemName = "image";
        try {
            Asset asset = assetManager.getAssetBySystemNameForCreative(creative, displayTypeSystemName, componentSystemName);
            if(asset == null) {
                System.out.println("Not found");
            } else {
                System.out.println(asset.getId());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAssetBundleWithInvalidId() {
        assertNull(assetManager.getAssetBundleById(0L));
    }

    @Test
    public void testAssetBundle() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(AssetBundle.class, "assetMap", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Creative.class, "assetBundleMap", JoinType.LEFT);
        Creative creative = creativeManager.getCreativeById(3L, fs);
        DisplayType displayType = deviceManager.getDisplayTypeById(1L);
        Format format = commonManager.getFormatById(1L);
        Component component = assetManager.getComponentById(1L);
        AssetBundle bundle = null;
        try {
            bundle = assetManager.newAssetBundle(creative, displayType);
            assertNotNull(bundle);
            long id = bundle.getId();
            assertTrue(id > 0L);

            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertEquals(creative.getAssetBundle(displayType), bundle);

            bundle = assetManager.getAssetBundleById(id);
            assertNotNull(bundle);
            assertEquals(id, bundle.getId());

            bundle = assetManager.getAssetBundleById(Long.toString(id));
            assertNotNull(bundle);
            assertEquals(id, bundle.getId());

            bundle = assetManager.getAssetBundleById(bundle.getId(), fs);
            Asset asset = assetManager.getAssetById(3L);
            bundle.putAsset(component, asset);
            bundle = assetManager.update(bundle);
            bundle = assetManager.getAssetBundleById(bundle.getId(), fs);
            Map<Component,Asset> assetMap = bundle.getAssetMap();
            assertTrue(assetMap.containsValue(asset));

            List<AssetBundle> assetBundles = assetManager.getAllAssetBundlesForCreativeAndFormat(creative, format);
            assertTrue(assetBundles.contains(bundle));

            assetBundles = assetManager.getAllAssetBundlesForCreativeAndNotFormat(creative, format);
            assertFalse(assetBundles.contains(bundle));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            assetManager.delete(bundle);
            assertNull(assetManager.getAssetBundleById(bundle.getId()));
            creative = creativeManager.getCreativeById(creative.getId(), fs);
            assertNull(creative.getAssetBundle(displayType));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetComponentWithInvalidId() {
        assertNull(assetManager.getComponentById(0L));
    }

    @Test
    public void testComponent() {
        Component component = null;
        String name = "Name Testing";
        String systemName = "SystemName Testing";
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Format.class, "components", JoinType.LEFT);
        Format format = commonManager.getFormatById(1L, fs);
        try {
            component = assetManager.newComponent(format, systemName, name);
            assertNotNull(component);
            long id = component.getId();
            assertEquals(component, assetManager.getComponentById(id));
            assertEquals(component, assetManager.getComponentById(Long.toString(id)));

            String newName = name + " Changed";
            component.setName(newName);
            component = assetManager.update(component);
            component = assetManager.getComponentById(component.getId());
            assertEquals(newName, component.getName());

             format = commonManager.getFormatById(1L, fs);
            List<Component> components = format.getComponents();
            assertNotNull(components);
            assertTrue(components.size() > 0);
            assertTrue(components.contains(component));

            format = commonManager.getFormatById(8L);
            if(format != null) {
                int nbComponents = 10;
                List<Component> list = new ArrayList<Component>();
                for(int i = 0;i < nbComponents;i++) {
                    list.add(assetManager.newComponent(format, systemName + System.currentTimeMillis(), name + System.currentTimeMillis()));
                }
                format = commonManager.getFormatById(format.getId(), fs);
                components = format.getComponents();
                assertTrue(components.size() > 0);
                for(Component c: list) {
                    assertTrue(components.contains(c));
                }
                for(Component c: list) {
                    assetManager.delete(c);
                    assertNull(assetManager.getComponentById(c.getId()));
                }
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
         } finally {
            assetManager.delete(component);
            assertNull(assetManager.getComponentById(component.getId()));
        }
    }

    @Test
    public void testComponentsDeux() {
        try {
            FetchStrategy componentFs = new FetchStrategyBuilder()
                                            .addLeft(Component_.contentSpecMap)
                                            .build();
            List<Format> formats = commonManager.getAllFormats();
            System.out.println("Using the FS");
            for(Format format : formats) {
                System.out.println("Format: " + format.getName());
                List<Component> components = assetManager.findAllComponentsForFormat(format, componentFs);
                for(Component component : components) {
                    System.out.println("Component: " + component.getName());
                }
            }
            System.out.println("Without the FS");
            for(Format format : formats) {
                System.out.println("Format: " + format.getName());
                List<Component> components = assetManager.findAllComponentsForFormat(format);
                for(Component component : components) {
                    System.out.println("Component: " + component.getName());
                }
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        }
    }

}
