package com.byyd.middleware.device.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Browser;
import com.adfonic.domain.Capability;
import com.adfonic.domain.Country;
import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Vendor;
import com.adfonic.domain.Vendor_;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class DeviceManagerIT {
    
    @Autowired
    DeviceManager devicesManager;
    
    @Autowired 
    CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testDeviceGroup() {
        String systemName = "Testing" + System.currentTimeMillis();
        String constraints = "Blah=blah";
        DeviceGroup deviceGroup = null;
        try {
            deviceGroup = devicesManager.newDeviceGroup(systemName, constraints);
            assertNotNull(deviceGroup);
            assertTrue(deviceGroup.getId() > 0);

            assertEquals(deviceGroup, devicesManager.getDeviceGroupById(deviceGroup.getId()));
            assertEquals(deviceGroup, devicesManager.getDeviceGroupById(Long.toString(deviceGroup.getId())));

            String newSystemName = systemName + "Changed";
            deviceGroup.setSystemName(newSystemName);
            deviceGroup = devicesManager.update(deviceGroup);

            deviceGroup = devicesManager.getDeviceGroupById(deviceGroup.getId());
            assertEquals(deviceGroup.getSystemName(), newSystemName);

            List<DeviceGroup> list = devicesManager.getAllDeviceGroups();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(deviceGroup));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            devicesManager.delete(deviceGroup);
            assertNull(devicesManager.getDeviceGroupById(deviceGroup.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testVendors() {
        try {
            Vendor vendor = devicesManager.getVendorByName("Apple");
            assertNotNull(vendor);
            assertEquals(vendor, devicesManager.getVendorByName("Apple".toUpperCase(), true));

            assertEquals(vendor, devicesManager.getVendorById(vendor.getId()));
            assertEquals(vendor, devicesManager.getVendorById(Long.toString(vendor.getId())));

            long count = devicesManager.countAllVendors();
            assertTrue(count > 0);

            List<Vendor> vendors = devicesManager.getAllVendors();
            assertNotNull(vendors);
            assertTrue(vendors.size() > 0);
            System.out.println("All Vendors:");
            for(Vendor p : vendors) {
                System.out.println("Vendor id=" + p.getId() + ", name=" + p.getName());
            }

            vendors = devicesManager.getAllVendors(new Sorting(asc("name")));
            assertNotNull(vendors);
            assertTrue(vendors.size() > 0);
            System.out.println("All Vendors, sorted by name:");
            for(Vendor p : vendors) {
                System.out.println(p.getName());
            }

            vendors = devicesManager.getVendorsByName("kddi", LikeSpec.CONTAINS, false);
            assertNotNull(vendors);
            assertTrue(vendors.size() > 0);
            System.out.println("All Vendors containing \"kddi\":");
            for(Vendor p : vendors) {
                System.out.println(p.getName());
            }

            vendors = devicesManager.getVendorsByName("kddi", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(vendors);
            assertTrue(vendors.size() > 0);
            System.out.println("All Vendors containing \"kddi\", sorted by name:");
            for(Vendor p : vendors) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }

    }
    
    //----------------------------------------------------------------------------------------

    @Test
    public void testModels() {
        try {
            Model model = devicesManager.getModelByName("Skypephone");
            assertNotNull(model);
            assertEquals(model, devicesManager.getModelByName("Skypephone".toUpperCase(), true, false, false));

            assertEquals(model, devicesManager.getModelById(model.getId()));
            assertEquals(model, devicesManager.getModelById(Long.toString(model.getId())));

            assertNotNull(devicesManager.getModelByName("device", false, true, false));
            assertNull(devicesManager.getModelByName("device", false, false, false));
            assertNotNull(devicesManager.getModelByName("device", false, null, null));

            long count = devicesManager.countAllModels();
            assertTrue(count > 0);

            List<Model> models = devicesManager.getAllModels();
            assertNotNull(models);
            assertTrue(models.size() > 0);
            System.out.println("All Models:");
            for(Model p : models) {
                System.out.println(p.getName());
            }

            models = devicesManager.getAllModels(new Sorting(asc("name")));
            assertNotNull(models);
            assertTrue(models.size() > 0);
            System.out.println("All Models, sorted by name:");
            for(Model p : models) {
                System.out.println(p.getName());
            }

            models = devicesManager.getModelsByName("one touch", LikeSpec.CONTAINS, false);
            assertNotNull(models);
            assertTrue(models.size() > 0);
            System.out.println("All Models containing \"one touch\":");
            for(Model p : models) {
                System.out.println(p.getName());
            }

            models = devicesManager.getModelsByName("one touch", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(models);
            assertTrue(models.size() > 0);
            System.out.println("All Models containing \"one touch\", sorted by name:");
            for(Model p : models) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }

    }

    @Test
    public void testModelsDeux() {
        try {
            String name = "Apple i";

            long count = devicesManager.countModelsByName(name, LikeSpec.CONTAINS, false, false, false, true);
            assertTrue(count > 0);
             System.out.println(count);

             List<Model> models = devicesManager.getModelsByName(name, LikeSpec.CONTAINS, false, false, false, true);
             assertTrue(models.size() > 0);
             for(Model model : models) {
                System.out.println(model.getName());
             }
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }

    @Test
    public void testModelsTrois() {
        try {
          String name = "Apple i";
          List<Platform> platforms = new ArrayList<Platform>();
          platforms.add(devicesManager.getPlatformByName("Android"));

          long count = devicesManager.countModelsByNameAndPlatform(name,
                  LikeSpec.CONTAINS, false, false, false, true, platforms);
          assertTrue(count == 0);
          System.out.println(count);

          List<Model> models = devicesManager.getModelsByNameAndPlatform(name,
                  LikeSpec.CONTAINS, false, false, false, true, platforms);
          assertTrue(models.size() == 0);

          platforms.add(devicesManager.getPlatformByName("iOS"));
          count = devicesManager.countModelsByNameAndPlatform(name,
                  LikeSpec.CONTAINS, false, false, false, true, platforms);
          assertTrue(count > 0);
          System.out.println(count);
          models = devicesManager.getModelsByNameAndPlatform(name,
                  LikeSpec.CONTAINS, false, false, false, true, platforms);
          assertTrue(models.size() > 0);
          for (Model model : models) {
              System.out.println(model.getName());
          }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }

    @Test
    public void testModelsQuatre() {
        try {

            // Will not work
            devicesManager.getAllModels(new Sorting(asc(Platform.class, "name")));
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }

     @Test
    public void testModelsSix() {
        Vendor vendor = null;
        Model model = null;
        try {
            String name = "Testing" + System.currentTimeMillis();
            String externalID = UUID.randomUUID().toString();
            FetchStrategy fs = new FetchStrategyBuilder()
                                   .addLeft(Vendor_.models)
                                   .build();
            vendor = devicesManager.getVendorById(0L, fs);
            DeviceGroup deviceGroupMobile = devicesManager.getDeviceGroupBySystemName(DeviceGroup.DEVICE_GROUP_MOBILE_SYSTEM_NAME);
            DeviceGroup deviceGroupTablet = devicesManager.getDeviceGroupBySystemName(DeviceGroup.DEVICE_GROUP_TABLET_SYSTEM_NAME);

            model = devicesManager.newModel(vendor, name, externalID, deviceGroupMobile);
            assertNotNull(model);
            assertTrue(model.getId() > 0);

            vendor = devicesManager.getVendorById(vendor.getId(), fs);
            assertTrue(vendor.getModels().contains(model));
            
            ModelFilter filter = new ModelFilter()
                                 .setName(name, null, true)
                                 .setDeviceGroup(deviceGroupMobile);
            assertTrue(devicesManager.getAllModels(filter).contains(model));
            filter.setDeviceGroup(deviceGroupTablet);
            assertFalse(devicesManager.getAllModels(filter).contains(model));

            // Test update() with vendor ID 0 set
            String newName = name + "Changed";
            model.setName(newName);
            model = devicesManager.update(model);

            // Reload without the Vendor set
            model = devicesManager.getModelById(model.getId());
            assertEquals(model.getName(), newName);

            // Test update again
            newName = name + "ChangedAgain";
            model.setName(newName);
            model = devicesManager.update(model);
            model = devicesManager.getModelById(model.getId());
            assertEquals(model.getName(), newName);
        } catch(Exception e) {
          String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
        } finally {
            if(model != null) {
                // Reload with Vendor set, for meaningful testing of delete()
                FetchStrategy fs = new FetchStrategyBuilder()
                                        .addLeft(Model_.vendor)
                                        .build();
                model = devicesManager.getModelById(model.getId(), fs);
                devicesManager.delete(model);
                assertNull(devicesManager.getModelById(model.getId()));
            }
        }
   }
     
     @Test
     public void testModelsSept() {
         try {
             // Will work
             List<Model> models = devicesManager.getAllModels(new Sorting(asc(Vendor.class, "name")));
             for (Model model : models) {
                 System.out.println(model.getName());
             }
         } catch(Exception e) {
                String stackTrace = ExceptionUtils.getStackTrace(e);
                System.out.println(stackTrace);
                fail(stackTrace);
         }
     }
     
     @Test
     public void testModelsHuit() {
         Vendor vendor = null;
         Model model = null;
         DeviceGroup deviceGroup = null;
         try {
             String name = "Testing" + System.currentTimeMillis();
             String externalID = UUID.randomUUID().toString();
             FetchStrategy fs = new FetchStrategyBuilder()
                                    .addLeft(Vendor_.models)
                                    .build();
             vendor = devicesManager.getVendorById(0L, fs);
             deviceGroup = devicesManager.getDeviceGroupBySystemName(DeviceGroup.DEVICE_GROUP_MOBILE_SYSTEM_NAME);

             model = devicesManager.newModel(vendor, name, externalID, deviceGroup);
             assertNotNull(model);
             assertTrue(model.getId() > 0);
             
             model.setHidden(true);
             model = devicesManager.update(model);
             
             assertNull(devicesManager.getModelByName(name, true, false, false));
             
             model.setHidden(false);
             model = devicesManager.update(model);
             
             assertNotNull(devicesManager.getModelByName(name, true, false, false));
             
         } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
         } finally {
             if(model != null) {
                 // Reload with Vendor set, for meaningful testing of delete()
                 FetchStrategy fs = new FetchStrategyBuilder()
                                         .addLeft(Model_.vendor)
                                         .build();
                 model = devicesManager.getModelById(model.getId(), fs);
                 devicesManager.delete(model);
                 assertNull(devicesManager.getModelById(model.getId()));
             }
         }
     }
     
     //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testOperators() {
        try {
            Operator operator = devicesManager.getOperatorByName("Mobiland");
            assertNotNull(operator);
            assertEquals(operator, devicesManager.getOperatorByName("Mobiland".toUpperCase(), true));

            assertEquals(operator, devicesManager.getOperatorById(operator.getId()));
            assertEquals(operator, devicesManager.getOperatorById(Long.toString(operator.getId())));

            long count = devicesManager.countAllOperators();
            assertTrue(count > 0);

            List<Operator> operators = devicesManager.getAllOperators();
            assertNotNull(operators);
            assertTrue(operators.size() > 0);
            System.out.println("All Operators:");
            for(Operator p : operators) {
                System.out.println(p.getName());
            }

            operators = devicesManager.getAllOperators(new Sorting(asc("name")));
            assertNotNull(operators);
            assertTrue(operators.size() > 0);
            System.out.println("All Operators, sorted by name:");
            for(Operator p : operators) {
                System.out.println(p.getName());
            }

            operators = devicesManager.getOperatorsByName("mobile", LikeSpec.CONTAINS, false);
            assertNotNull(operators);
            assertTrue(operators.size() > 0);
            System.out.println("All Operators containing \"mobile\":");
            for(Operator p : operators) {
                System.out.println(p.getName());
            }

            operators = devicesManager.getOperatorsByName("mobile", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(operators);
            assertTrue(operators.size() > 0);
            System.out.println("All Operators containing \"mobile\", sorted by name:");
            for(Operator p : operators) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }

    @Test
    public void testOperatorsDeux() {
        List<Country> countries = new ArrayList<Country>();
        //countries.add(devicesManager.getCountryByIsoCode("GB"));
        countries.add(commonManager.getCountryByIsoCode("US"));
        //countries.add(devicesManager.getCountryByIsoCode("BE"));

        String name = "verizon";
        boolean caseSensitive = false;
        boolean mandateQuova = false;
        long count = devicesManager.countOperatorsForNameAndCountries(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova, countries);
        assertTrue(count > 0);
        System.out.println("Count of Operators containing \"" + name + "\" without Quova mandated:");
        System.out.println(count);

        List<Operator> operators = devicesManager.getOperatorsForNameAndCountries(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova, countries);
        assertNotNull(operators);
        assertTrue(operators.size() > 0);

        System.out.println("Operators containing \"" + name + "\" without Quova mandated:");
        for(Operator operator : operators) {
            System.out.println(operator.getName());
        }

        mandateQuova = true;
        count = devicesManager.countOperatorsForNameAndCountries(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova, countries);
        System.out.println("Count of Operators containing \"" + name + "\" with Quova mandated:");
        System.out.println(count);

        operators = devicesManager.getOperatorsForNameAndCountries(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova, countries);

        System.out.println("Operators containing \"" + name + "\" with Quova mandated:");
        for(Operator operator : operators) {
            System.out.println(operator.getName());
        }
    }

    @Test
    public void testOperatorsTrois() {
        String name = "mobile";
        boolean caseSensitive = false;

        boolean mandateQuova = false;
        long count = devicesManager.countOperatorsForName(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova);
        assertTrue(count > 0);
        System.out.println(count);

        List<Operator> operators = devicesManager.getOperatorsForName(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova);
        assertNotNull(operators);
        assertTrue(operators.size() > 0);

        for(Operator operator : operators) {
            System.out.println(operator.getName());
        }

        mandateQuova = true;
        count = devicesManager.countOperatorsForName(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova);
        System.out.println(count);

        operators = devicesManager.getOperatorsForName(name, LikeSpec.CONTAINS, caseSensitive, mandateQuova);

        for(Operator operator : operators) {
            System.out.println(operator.getName());
        }

    }

    @Test
    public void testOperatorsQuatre() {
        String alias = "Personal";
        OperatorAlias.Type operatorAliasType = OperatorAlias.Type.MASSIVE;
        Country country = commonManager.getCountryById(133L);

        try {
            Operator operator = devicesManager.getOperatorForOperatorAliasAndCountry(operatorAliasType, country, alias);
            assertNotNull(operator);
            assertEquals(operator.getId(), 24L);
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }
    
    //----------------------------------------------------------------------------------------
    
    @Test
    public void testPlatforms() {
        try {
            Platform platform = devicesManager.getPlatformByName("iOS");
            assertNotNull(platform);
            assertEquals(platform, devicesManager.getPlatformByName("IOS", true));

            assertEquals(platform, devicesManager.getPlatformById(platform.getId()));
            assertEquals(platform, devicesManager.getPlatformById(Long.toString(platform.getId())));

            long count = devicesManager.countAllPlatforms();
            assertTrue(count > 0);

            List<Platform> platforms = devicesManager.getAllPlatforms();
            assertNotNull(platforms);
            assertTrue(platforms.size() > 0);
            System.out.println("All Platforms:");
            for(Platform p : platforms) {
                System.out.println(p.getName());
            }

            platforms = devicesManager.getAllPlatforms(new Sorting(asc("name")));
            assertNotNull(platforms);
            assertTrue(platforms.size() > 0);
            System.out.println("All Platforms, sorted by name:");
            for(Platform p : platforms) {
                System.out.println(p.getName());
            }

            platforms = devicesManager.getPlatformsByName("an", LikeSpec.CONTAINS, false);
            assertNotNull(platforms);
            assertTrue(platforms.size() > 0);
            System.out.println("All Platforms containing \"an\":");
            for(Platform p : platforms) {
                System.out.println(p.getName());
            }

            platforms = devicesManager.getPlatformsByName("an", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(platforms);
            assertTrue(platforms.size() > 0);
            System.out.println("All Platforms containing \"an\", sorted by name:");
            for(Platform p : platforms) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        }

    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetDisplayTypeWithInvalidId() {
        assertNull(devicesManager.getDisplayTypeById(0L));
    }

    @Test
    public void testDisplayType() {
        DisplayType displayType = null;
        String name = "Name Testing";
        String systemName = "SystemName Testing";
        String constraints = "usableDisplayWidth<200";
        try {
            displayType = devicesManager.newDisplayType(systemName, name, constraints);
            assertNotNull(displayType);
            long id = displayType.getId();
            assertEquals(displayType, devicesManager.getDisplayTypeById(id));
            assertEquals(displayType, devicesManager.getDisplayTypeById(Long.toString(id)));

            String newName = name + " Changed";
            displayType.setName(newName);
            displayType = devicesManager.update(displayType);
            displayType = devicesManager.getDisplayTypeById(displayType.getId());
            assertEquals(newName, displayType.getName());

            DisplayType d = devicesManager.getDisplayTypeBySystemName(systemName);
            assertNotNull(d);
            assertEquals(d.getId(), displayType.getId());

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            devicesManager.delete(displayType);
            assertNull(devicesManager.getDisplayTypeById(displayType.getId()));
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testDeviceIdentifierType() {
        Set<DeviceIdentifierType> dits = new HashSet<DeviceIdentifierType>();
        try {
            int order = 1;

            DeviceIdentifierType dit1 = new DeviceIdentifierType();
            dit1.setName(UUID.randomUUID().toString());
            dit1.setSystemName(dit1.getName().substring(0, 20));
            dit1.setPrecedenceOrder(order++);
            dit1.setHidden(false);
            dit1.setValidationRegex("^.*$");
            dits.add(devicesManager.create(dit1));

            DeviceIdentifierType dit2 = new DeviceIdentifierType();
            dit2.setName(UUID.randomUUID().toString());
            dit2.setSystemName(dit2.getName().substring(0, 20));
            dit2.setPrecedenceOrder(order++);
            dit2.setHidden(true);
            dits.add(devicesManager.create(dit2));

            DeviceIdentifierType dit3 = new DeviceIdentifierType();
            dit3.setName(UUID.randomUUID().toString());
            dit3.setSystemName(dit3.getName().substring(0, 20));
            dit3.setPrecedenceOrder(order++);
            dit3.setHidden(false);
            dits.add(devicesManager.create(dit3));

            assertTrue(devicesManager.getAllDeviceIdentifierTypes().size() >= 3);

            for (DeviceIdentifierType dit : dits) {
                assertEquals(dit, devicesManager.getDeviceIdentifierTypeBySystemName(dit.getSystemName()));
                assertTrue(devicesManager.getAllDeviceIdentifierTypes().contains(dit));
                if (dit.isHidden()) {
                    assertFalse(devicesManager.getAllNonHiddenDeviceIdentifierTypes().contains(dit));
                } else {
                    assertTrue(devicesManager.getAllNonHiddenDeviceIdentifierTypes().contains(dit));
                }
            }
        } finally {
            for (DeviceIdentifierType dit : dits) {
                devicesManager.delete(dit);
            }
        }
    }
    
    //----------------------------------------------------------------------------------------

    @Test
   public void testBrowsers() {
       try {
           Browser browser = devicesManager.getBrowserByName(DeviceManager.OPERA_BROWSER_NAME);
           assertNotNull(browser);
           assertEquals(browser, devicesManager.getBrowserByName(DeviceManager.OPERA_BROWSER_NAME.toUpperCase(), true));

           assertEquals(browser, devicesManager.getBrowserById(browser.getId()));
           assertEquals(browser, devicesManager.getBrowserById(Long.toString(browser.getId())));

           long count = devicesManager.countAllBrowsers();
           assertTrue(count > 0);

           List<Browser> browsers = devicesManager.getAllBrowsers();
           assertNotNull(browsers);
           assertTrue(browsers.size() > 0);
           System.out.println("All Browsers:");
           for(Browser p : browsers) {
               System.out.println(p.getName());
           }

           browsers = devicesManager.getAllBrowsers(new Sorting(asc("name")));
           assertNotNull(browsers);
           assertTrue(browsers.size() > 0);
           System.out.println("All Browsers, sorted by name:");
           for(Browser p : browsers) {
               System.out.println(p.getName());
           }

           browsers = devicesManager.getBrowsersByName("an", LikeSpec.CONTAINS, false);
           assertNotNull(browsers);
           assertTrue(browsers.size() > 0);
           System.out.println("All Browsers containing \"an\":");
           for(Browser p : browsers) {
               System.out.println(p.getName());
           }

           browsers = devicesManager.getBrowsersByName("an", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
           assertNotNull(browsers);
           assertTrue(browsers.size() > 0);
           System.out.println("All Browsers containing \"an\", sorted by name:");
           for(Browser p : browsers) {
               System.out.println(p.getName());
           }

       } catch(Exception e) {
              String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       }

   }

    //----------------------------------------------------------------------------------------

    @Test
   public void testCapabilities() {
       try {
           Capability capability = devicesManager.getCapabilityByName("Video");
           assertNotNull(capability);
           assertEquals(capability, devicesManager.getCapabilityByName("Video".toUpperCase(), true));

           assertEquals(capability, devicesManager.getCapabilityById(capability.getId()));
           assertEquals(capability, devicesManager.getCapabilityById(Long.toString(capability.getId())));

           long count = devicesManager.countAllCapabilities();
           assertTrue(count > 0);

           List<Capability> capabilities = devicesManager.getAllCapabilities();
           assertNotNull(capabilities);
           assertTrue(capabilities.size() > 0);
           System.out.println("All Capabilities:");
           for(Capability p : capabilities) {
               System.out.println(p.getName());
           }

           capabilities = devicesManager.getAllCapabilities(new Sorting(asc("name")));
           assertNotNull(capabilities);
           assertTrue(capabilities.size() > 0);
           System.out.println("All Capabilities, sorted by name:");
           for(Capability p : capabilities) {
               System.out.println(p.getName());
           }

           capabilities = devicesManager.getCapabilitiesByName("o", LikeSpec.CONTAINS, false);
           assertNotNull(capabilities);
           assertTrue(capabilities.size() > 0);
           System.out.println("All Capabilities containing \"o\":");
           for(Capability p : capabilities) {
               System.out.println(p.getName());
           }

           capabilities = devicesManager.getCapabilitiesByName("o", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
           assertNotNull(capabilities);
           assertTrue(capabilities.size() > 0);
           System.out.println("All Capabilities containing \"o\", sorted by name:");
           for(Capability p : capabilities) {
               System.out.println(p.getName());
           }

       } catch(Exception e) {
              String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       }

   }

}
