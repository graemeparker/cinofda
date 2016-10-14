package com.byyd.middleware.device.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Model;
import com.adfonic.domain.Vendor;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.device.dao.DeviceGroupDao;
import com.byyd.middleware.device.dao.ModelDao;
import com.byyd.middleware.device.service.jpa.DeviceManagerJpaImpl;

public class TestDeviceManager extends AbstractAdfonicTest {

    private DeviceManagerJpaImpl devicesManagerJpaImpl;

    @Before
    public void setup() {
        devicesManagerJpaImpl = new DeviceManagerJpaImpl();
    }
    
    @Test
    public void testGetDeviceGroupBySystemName(){
        final String systemName = randomAlphaNumericString(30);
        final DeviceGroupDao deviceGroupDao = mock(DeviceGroupDao.class,"deviceGroupDao");
        final DeviceGroup deviceGroup = mock(DeviceGroup.class,"deviceGroup");
        
        inject(devicesManagerJpaImpl, "deviceGroupDao", deviceGroupDao);
        expect(new Expectations() {{
            oneOf (deviceGroupDao).getBySystemName(systemName);
                will(returnValue(deviceGroup));
        }});
        
        DeviceGroup returnValue = devicesManagerJpaImpl.getDeviceGroupBySystemName(systemName);
        assertEquals(deviceGroup, returnValue);
    }
    
    @Test(expected=NullPointerException.class)
    public void testnewModel_VendorIsNull(){
        final String modelName = randomAlphaNumericString(30);
        final String externalID = UUID.randomUUID().toString();
        final ModelDao modelDao = mock(ModelDao.class,"modelDao");
        final DeviceGroup deviceGroup = mock(DeviceGroup.class,"deviceGroup");
        final Vendor vendor = null;
        
        inject(devicesManagerJpaImpl, "modelDao", modelDao);
        expect(new Expectations() {{
        }});
        
        devicesManagerJpaImpl.newModel(vendor, modelName, externalID, deviceGroup);
        fail("Test case failed,should ahve thrown null pointer exception");
    }

    @Test
    public void testnewModel_AllValidValues_null_externalID(){
        final String modelName = randomAlphaNumericString(30);
        final String externalID = null;
        final ModelDao modelDao = mock(ModelDao.class,"modelDao");
        final DeviceGroup deviceGroup = mock(DeviceGroup.class,"deviceGroup");
        final Vendor vendor = mock(Vendor.class,"vendor");
        final long vendorId = randomLong();
        final Model model = mock(Model.class,"model");
        
        inject(devicesManagerJpaImpl, "modelDao", modelDao);
        expect(new Expectations() {{
            allowing (vendor).getId(); will(returnValue(vendorId));
            oneOf (vendor).newModel(modelName, deviceGroup); will(returnValue(model));
            oneOf (modelDao).create(model); will(returnValue(model));
        }});
        
        Model returnValue = devicesManagerJpaImpl.newModel(vendor, modelName, externalID, deviceGroup);
        assertEquals(model, returnValue);
    }

    @Test
    public void testnewModel_AllValidValues_non_null_externalID(){
        final String modelName = randomAlphaNumericString(30);
        final String externalID = UUID.randomUUID().toString();
        final ModelDao modelDao = mock(ModelDao.class,"modelDao");
        final DeviceGroup deviceGroup = mock(DeviceGroup.class,"deviceGroup");
        final Vendor vendor = mock(Vendor.class,"vendor");
        final long vendorId = randomLong();
        final Model model = mock(Model.class,"model");
        
        inject(devicesManagerJpaImpl, "modelDao", modelDao);
        expect(new Expectations() {{
            allowing (vendor).getId(); will(returnValue(vendorId));
            oneOf (vendor).newModel(modelName, deviceGroup);
                will(returnValue(model));
            oneOf (model).setExternalID(externalID);
            oneOf (modelDao).create(model);
                will(returnValue(model));
        }});
        
        Model returnValue = devicesManagerJpaImpl.newModel(vendor, modelName, externalID, deviceGroup);
        assertEquals(model, returnValue);
    }
}
