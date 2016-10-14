package com.adfonic.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Model;
import com.adfonic.domain.Vendor;
import com.adfonic.email.EmailService;
import com.adfonic.tasks.deviceatlas.Device;
import com.adfonic.tasks.deviceatlas.Deviceatlas;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-jpa-context.xml"})
public class TestSyncDevices extends AbstractAdfonicTest {
    private Unmarshaller unmarshaller;
    private SyncDevices syncDevices;
    private DeviceManager deviceManager;
    private EmailService emailService;
    private final String summaryEmailFromAddress = randomEmailAddress();
    private final String[] summaryEmailToAddresses = new String[] { randomEmailAddress(), randomEmailAddress() };
    private final String deviceAtlasLicenseKey = randomAlphaNumericString(10);

    @Before
    public void beforeEachTest() {
        unmarshaller = mock(Unmarshaller.class);
        deviceManager = mock(DeviceManager.class);
        emailService = mock(EmailService.class);
        
        syncDevices = new SyncDevices(unmarshaller);
        
        inject(syncDevices, "deviceManager", deviceManager);
        inject(syncDevices, "emailService", emailService);
        inject(syncDevices, "summaryEmailFromAddress", summaryEmailFromAddress);
        inject(syncDevices, "summaryEmailToAddresses", summaryEmailToAddresses);
        inject(syncDevices, "deviceAtlasLicenseKey", deviceAtlasLicenseKey);
    }

    @Test
    public void testReadDeviceAtlas_non_200() throws Exception {
        final HttpClient httpClient = mock(HttpClient.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final StatusLine statusLine = mock(StatusLine.class);
        final HttpEntity httpEntity = mock(HttpEntity.class);
        final InputStream content = mock(InputStream.class);
        final Deviceatlas deviceatlas = mock(Deviceatlas.class);
        final List<Device> deviceList = new ArrayList<Device>();
        final int notFoundCode = 404;

        expect(new Expectations() {{
            allowing (httpClient).execute(with(any(HttpRequestBase.class))); will(returnValue(httpResponse));
            allowing (httpResponse).getStatusLine(); will(returnValue(statusLine));
            allowing (httpResponse).getEntity(); will(returnValue(httpEntity));
            allowing (httpEntity).isStreaming(); will(returnValue(false));
            oneOf (statusLine).getStatusCode(); will(returnValue(notFoundCode));
            // building the exception
            oneOf (statusLine).getStatusCode(); will(returnValue(notFoundCode));
            oneOf (statusLine).getReasonPhrase(); will(returnValue("Not Found"));
        }});
        
        try {
            syncDevices.readDeviceAtlas(httpClient);
            fail("Should have thrown");
        } catch (HttpResponseException e) {
            assertEquals(notFoundCode, e.getStatusCode());
        }
    }

    @Test
    public void testSyncDevices01_empty() throws Exception {
        syncDevices.syncDevices(new ArrayList<Device>());
    }

    @Ignore
    @Test
    public void testSyncDevices02() throws Exception {
        final Device device1 = mock(Device.class, "device1");
        final String device1Nid = randomAlphaNumericString(10);
        final String device1Model = randomAlphaNumericString(10);
        final String device1Title = randomAlphaNumericString(10);
        final String device1Vendor = uniqueAlphaNumericString(10, "vendor");
        
        final Device device2 = mock(Device.class, "device2");
        final String device2Nid = randomAlphaNumericString(10);
        final String device2Model = randomAlphaNumericString(10);
        final String device2Title = randomAlphaNumericString(10);
        final String device2Vendor = uniqueAlphaNumericString(10, "vendor");
        
        final Device device3 = mock(Device.class, "device3");
        final String device3Nid = randomAlphaNumericString(10);
        final String device3Model = randomAlphaNumericString(10);
        final String device3Title = randomAlphaNumericString(10);
        final String device3Vendor = uniqueAlphaNumericString(10, "vendor");
        
        final Vendor newVendor1 = mock(Vendor.class, "newVendor1");
        final long newVendor1Id = randomLong();
        final String newVendor1Name = device1Vendor;

        final Vendor vendor2 = mock(Vendor.class, "vendor2");
        final long vendor2Id = randomLong();
        final String vendor2Name = randomAlphaNumericString(10);
        final String vendor2Alias1 = uniqueAlphaNumericString(10, "vendor");
        final String vendor2Alias2 = device2Vendor; // alias matches
        final Set<String> vendor2Aliases = new HashSet<String>() {{
                add(vendor2Alias1);
                add(vendor2Alias2);
            }};
        
        final Vendor vendor3 = mock(Vendor.class, "vendor3");
        final long vendor3Id = randomLong();
        final String vendor3Name = device3Vendor;
        final Set<String> vendor3Aliases = Collections.emptySet();

        final List<Vendor> allVendors = new ArrayList<Vendor>() {{
                add(vendor2);
                add(vendor3);
            }};

        final Model newModel1 = mock(Model.class, "newModel1");
        final long newModel1Id = randomLong();
        final String newModel1Name = device1Model;
        final String newModel1ExternalID = device1Nid;
        
        final Model model2 = mock(Model.class, "model2");
        final long model2Id = randomLong();
        final String model2Name = device2Model;
        final boolean model2Deleted = false;
        final String model2ExternalID = device2Nid;

        final Model model3 = mock(Model.class, "model3");
        final long model3Id = randomLong();
        final String model3Name = randomAlphaNumericString(10); // mismatch, update
        final boolean model3Deleted = true; // deleted...we'll revive it
        final String model3ExternalID = device3Nid;

        final Model modelToDelete = mock(Model.class, "modelToDelete");
        final long modelToDeleteId = randomLong();
        final String modelToDeleteName = randomAlphaNumericString(10);
        final String modelToDeleteExternalID = randomAlphaNumericString(10);

        final List<Model> allModelsAtTheEnd = new ArrayList<Model>() {{
                add(newModel1);
                add(model2);
                add(model3);
                add(modelToDelete); // this one will need to be deleted
            }};

        final List<Device> deviceList = new ArrayList<Device>() {{
                add(device1);
                add(device2);
                add(device3);
            }};
        
        expect(new Expectations() {{
            allowing (device1).getNid(); will(returnValue(device1Nid));
            allowing (device1).getModel(); will(returnValue(device1Model));
            allowing (device1).getTitle(); will(returnValue(device1Title));
            allowing (device1).getVendor(); will(returnValue(device1Vendor));
            
            allowing (device2).getNid(); will(returnValue(device2Nid));
            allowing (device2).getModel(); will(returnValue(device2Model));
            allowing (device2).getTitle(); will(returnValue(device2Title));
            allowing (device2).getVendor(); will(returnValue(device2Vendor));
            
            allowing (device3).getNid(); will(returnValue(device3Nid));
            allowing (device3).getModel(); will(returnValue(device3Model));
            allowing (device3).getTitle(); will(returnValue(device3Title));
            allowing (device3).getVendor(); will(returnValue(device3Vendor));

            allowing (newVendor1).getId(); will(returnValue(newVendor1Id));
            allowing (newVendor1).getName(); will(returnValue(newVendor1Name));

            allowing (vendor2).getId(); will(returnValue(vendor2Id));
            allowing (vendor2).getName(); will(returnValue(vendor2Name));
            allowing (vendor2).getAliases(); will(returnValue(vendor2Aliases));
            
            allowing (vendor3).getId(); will(returnValue(vendor3Id));
            allowing (vendor3).getName(); will(returnValue(vendor3Name));
            allowing (vendor3).getAliases(); will(returnValue(vendor3Aliases));

            allowing (newModel1).getId(); will(returnValue(newModel1Id));
            allowing (newModel1).getName(); will(returnValue(newModel1Name));
            allowing (newModel1).getExternalID(); will(returnValue(newModel1ExternalID));
            allowing (newModel1).getVendor(); will(returnValue(newVendor1));

            allowing (model2).getId(); will(returnValue(model2Id));
            allowing (model2).getName(); will(returnValue(model2Name));
            allowing (model2).isDeleted(); will(returnValue(model2Deleted));
            allowing (model2).getExternalID(); will(returnValue(model2ExternalID));
            allowing (model2).getVendor(); will(returnValue(vendor2));
            
            allowing (model3).getId(); will(returnValue(model3Id));
            allowing (model3).getName(); will(returnValue(model3Name));
            allowing (model3).isDeleted(); will(returnValue(model3Deleted));
            allowing (model3).getExternalID(); will(returnValue(model3ExternalID));
            allowing (model3).getVendor(); will(returnValue(vendor3));

            allowing (modelToDelete).getId(); will(returnValue(modelToDeleteId));
            allowing (modelToDelete).getName(); will(returnValue(modelToDeleteName));
            allowing (modelToDelete).getExternalID(); will(returnValue(modelToDeleteExternalID));
            allowing (modelToDelete).getVendor(); will(returnValue(vendor3));
            
            oneOf (deviceManager).getAllVendors(with(any(FetchStrategy.class))); will(returnValue(allVendors));

            // device1's model and vendor don't exist in our system yet
            oneOf (deviceManager).getModelByExternalId(device1Nid); will(returnValue(null));
            oneOf (deviceManager).create(with(any(Vendor.class))); will(returnValue(newVendor1));
            oneOf (deviceManager).newModel(newVendor1, device1Model, device1Nid, null); will(returnValue(newModel1));

            // device2 exists in our system as model2
            oneOf (deviceManager).getModelByExternalId(device2Nid); will(returnValue(model2));

            // device3 exists in our system as model3, which was previously deleted
            oneOf (deviceManager).getModelByExternalId(device3Nid); will(returnValue(model3));
            oneOf (model3).setDeleted(false);
            oneOf (model3).setName(device3Model);
            oneOf (deviceManager).update(model3); will(returnValue(model3));

            oneOf (deviceManager).getAllModels(with(any(ModelFilter.class)), with(any(FetchStrategy.class))); will(returnValue(allModelsAtTheEnd));

            // modelToDelete gets marked deleted
            oneOf (modelToDelete).setDeleted(true);
            oneOf (deviceManager).update(modelToDelete); will(returnValue(modelToDelete));

            // Email summary gets built
            oneOf (deviceManager).getModelById(with(model3Id), with(any(FetchStrategy.class))); will(returnValue(model3));
            oneOf (deviceManager).getModelById(with(newModel1Id), with(any(FetchStrategy.class))); will(returnValue(newModel1));
            oneOf (deviceManager).getModelById(with(modelToDeleteId), with(any(FetchStrategy.class))); will(returnValue(modelToDelete));
            
            // Email summary gets sent
            oneOf (emailService).sendEmail(with(summaryEmailFromAddress), with(summaryEmailFromAddress), with(any(List.class)), with((List)null), with((List)null), with((Map)null), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        }});
        
        syncDevices.syncDevices(deviceList);
    }

    @Test
    public void testEstablishVendor01_found() {
        final Device device = mock(Device.class);
        final Map<String,Vendor> vendorsByNameOrAlias = new HashMap<String,Vendor>();
        final Set<Vendor> vendorsAdded = new LinkedHashSet<Vendor>();
        final String deviceModel = randomAlphaNumericString(10);
        final String deviceVendor = uniqueAlphaNumericString(10, "vendor");
        final Vendor vendor = mock(Vendor.class);

        expect(new Expectations() {{
            allowing (device).getNid(); will(returnValue(randomAlphaNumericString(10)));
            allowing (device).getModel(); will(returnValue(deviceModel));
            allowing (device).getVendor(); will(returnValue(deviceVendor));
            allowing (vendor).getId(); will(returnValue(randomLong()));
            allowing (vendor).getName(); will(returnValue(deviceVendor));
        }});

        vendorsByNameOrAlias.put(deviceVendor.toLowerCase(), vendor);
        assertEquals(vendor, syncDevices.establishVendor(device, vendorsByNameOrAlias, vendorsAdded));
    }

    @Test
    public void testEstablishVendor02_notFound() {
        final Device device = mock(Device.class);
        final Map<String,Vendor> vendorsByNameOrAlias = new HashMap<String,Vendor>();
        final Set<Vendor> vendorsAdded = new LinkedHashSet<Vendor>();
        final String deviceModel = randomAlphaNumericString(10);
        final String deviceVendor = uniqueAlphaNumericString(10, "vendor");
        final Vendor vendor = mock(Vendor.class);

        expect(new Expectations() {{
            allowing (device).getNid(); will(returnValue(randomAlphaNumericString(10)));
            allowing (device).getModel(); will(returnValue(deviceModel));
            allowing (device).getVendor(); will(returnValue(deviceVendor));
            allowing (vendor).getName(); will(returnValue(deviceVendor));
            allowing (vendor).getId(); will(returnValue(randomLong()));

            oneOf (deviceManager).create(with(any(Vendor.class))); will(returnValue(vendor));
        }});

        assertEquals(vendor, syncDevices.establishVendor(device, vendorsByNameOrAlias, vendorsAdded));
        assertTrue(vendorsAdded.contains(vendor));
        assertEquals(vendor, vendorsByNameOrAlias.get(deviceVendor.toLowerCase()));
    }

    @Test
    public void testGetModelName() {
        final Device device = mock(Device.class);
        final Map<String,Vendor> vendorsByNameOrAlias = new HashMap<String,Vendor>();
        final String deviceModel = randomAlphaNumericString(10);
        final String empty = "";
        final String token1 = randomAlphaNumericString(10);
        final String token2 = randomAlphaNumericString(10);
        final String token3 = randomAlphaNumericString(10);
        final String token4 = randomAlphaNumericString(10);
        final String title = token1 + "_" + token2 + "/" + token3 + " " + token4;
        final String foundVendorAlias = token1 + " " + token2;
        final String foundModelName = token3 + " " + token4;

        expect(new Expectations() {{
            allowing (device).getNid(); will(returnValue(randomAlphaNumericString(10)));
            
            // Test 1: device.model is not empty
            exactly(2).of (device).getModel(); will(returnValue(deviceModel));

            // Test 2: device.model and device.title are both empty
            oneOf (device).getModel(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(empty)); // empty check
            oneOf (device).getModel(); will(returnValue(empty));
            
            // Test 3: device.model is empty, device.title has tokens to match
            oneOf (device).getModel(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(title)); // empty check
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getTitle(); will(returnValue(title)); // tokenization
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getModel(); will(returnValue(empty)); // return
            
            // Test 4: device.model is empty, device.title has tokens to match
            oneOf (device).getModel(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(title)); // empty check
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getTitle(); will(returnValue(title)); // tokenization
        }});

        // Test 1
        assertEquals(deviceModel, SyncDevices.getModelName(device, vendorsByNameOrAlias));

        // Test 2
        assertEquals(empty, SyncDevices.getModelName(device, vendorsByNameOrAlias));

        // Test 3
        assertEquals(empty, SyncDevices.getModelName(device, vendorsByNameOrAlias));

        // Test 4
        vendorsByNameOrAlias.put(foundVendorAlias.toLowerCase(), mock(Vendor.class)); // value not important
        assertEquals(foundModelName, SyncDevices.getModelName(device, vendorsByNameOrAlias));
    }

    @Test
    public void testGetVendorName() {
        final Device device = mock(Device.class);
        final Map<String,Vendor> vendorsByNameOrAlias = new HashMap<String,Vendor>();
        final String vn1 = randomAlphaNumericString(10);
        final String empty = "";
        final String token1 = randomAlphaNumericString(10);
        final String token2 = randomAlphaNumericString(10);
        final String token3 = randomAlphaNumericString(10);
        final String token4 = randomAlphaNumericString(10);
        final String title = token1 + "_" + token2 + "/" + token3 + " " + token4;
        final String foundVendorName = token1 + " " + token2;

        expect(new Expectations() {{
            allowing (device).getNid(); will(returnValue(randomAlphaNumericString(10)));

            // Test 1: device.vendor is not empty
            exactly(2).of (device).getVendor(); will(returnValue(vn1));

            // Test 2: device.vendor and device.title are both empty
            oneOf (device).getVendor(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(empty)); // empty check
            oneOf (device).getVendor(); will(returnValue(empty));

            // Test 3: device.vendor is empty, device.title is a multi-token string
            // Tokenized form not found in map
            oneOf (device).getVendor(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(title)); // empty check
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getTitle(); will(returnValue(title)); // tokenization
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getVendor(); will(returnValue(empty)); // return

            // Test 4: device.vendor is empty, device.title is a multi-token string
            oneOf (device).getVendor(); will(returnValue(empty));
            oneOf (device).getTitle(); will(returnValue(title)); // empty check
            oneOf (device).getTitle(); will(returnValue(title)); // logging
            oneOf (device).getTitle(); will(returnValue(title)); // tokenization
        }});

        // Test 1
        assertEquals(vn1, SyncDevices.getVendorName(device, vendorsByNameOrAlias));

        // Test 2
        assertEquals(empty, SyncDevices.getVendorName(device, vendorsByNameOrAlias));

        // Test 3
        vendorsByNameOrAlias.clear();
        assertEquals(empty, SyncDevices.getVendorName(device, vendorsByNameOrAlias));

        // Test 4
        vendorsByNameOrAlias.clear();
        vendorsByNameOrAlias.put(foundVendorName.toLowerCase(), mock(Vendor.class)); // value not important
        assertEquals(foundVendorName, SyncDevices.getVendorName(device, vendorsByNameOrAlias));
    }
}
