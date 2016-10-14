package com.adfonic.tasks;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Vendor;
import com.adfonic.domain.Vendor_;
import com.adfonic.email.EmailService;
import com.adfonic.tasks.deviceatlas.Device;
import com.adfonic.tasks.deviceatlas.Deviceatlas;
import com.adfonic.util.HostUtils;
import com.adfonic.util.HttpUtils;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class SyncDevices implements Runnable {
    private static final transient Logger LOG = LoggerFactory.getLogger(SyncDevices.class.getName());

    private static final String DEVICEATLAS_DOWNLOAD_URL = "http://deviceatlas.com/getDevicesBasic.php";

    // HTTP auto-retry settings
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_INTERVAL_MS = 30000;

    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private EmailService emailService;
    @Value("${SyncDevices.summaryEmail.from}")
    private String summaryEmailFromAddress;
    @Value("${SyncDevices.summaryEmail.to}")
    private String[] summaryEmailToAddresses;
    @Value("${deviceAtlas.licenseKey}")
    private String deviceAtlasLicenseKey;
    @Value("${SyncDevices.ghostMode:false}")
    private boolean ghostMode;

    private final Unmarshaller unmarshaller;

    public SyncDevices() throws javax.xml.bind.JAXBException {
        this(JAXBContext.newInstance(Deviceatlas.class.getPackage().getName()).createUnmarshaller());
    }

    SyncDevices(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    @Override
    public void run() {
        try {
            syncDevices(readDeviceAtlas());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to syncDevices", e);
        }
    }

    List<Device> readDeviceAtlas() throws java.io.IOException, javax.xml.bind.JAXBException {
        // SC-228 - retry, don't get hosed when DeviceAtlas is down briefly
        return readDeviceAtlas(new AutoRetryHttpClient(new DefaultServiceUnavailableRetryStrategy(MAX_RETRIES, RETRY_INTERVAL_MS)));
    }

    List<Device> readDeviceAtlas(HttpClient httpClient) throws java.io.IOException, javax.xml.bind.JAXBException {
        Map<String, String> params = new HashMap<>();
        params.put("type", "xml");
        params.put("format", "zip");
        params.put("licencekey", deviceAtlasLicenseKey);
        // SC-283 - now we need to pass "submitted=1" (DeviceAtlas sucks a butthole)
        params.put("submitted", "1");
        HttpGet httpGet = new HttpGet(DEVICEATLAS_DOWNLOAD_URL + "?" + HttpUtils.encodeParams(params));
        LOG.debug("{} {}", httpGet.getMethod(), httpGet.getURI());
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
            }
            try (ZipInputStream zipInputStream = new ZipInputStream(httpEntity.getContent())) {
                return readDeviceAtlasFromZip(zipInputStream);
            }
        } finally {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }

    List<Device> readDeviceAtlasFromZip(ZipInputStream zipInputStream) throws java.io.IOException, javax.xml.bind.JAXBException {
        // We expect just a single entry in the zip, the XML file, but just
        // to be sure, let's walk the zip until we find a .xml file entry.
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null && !zipEntry.getName().endsWith(".xml")) {
            LOG.info("Skipping zip entry: {}", zipEntry.getName());
            zipEntry = zipInputStream.getNextEntry();
        }
        if (zipEntry == null) {
            throw new IllegalStateException("No xml entry found in zip content");
        }
        LOG.info("Reading DeviceAtlas data from zip entry: {}", zipEntry.getName());
        return readDeviceAtlasFromXml(zipInputStream);
    }

    /**
     * This method is used to read the XML data and use JAXB to unmarshal
     * the XML into a list of Device objects.
     *
     * DeviceAtlas XML format looks like this:
     * <deviceatlas>
     *   <device>
     *     <nid>2516672</nid>
     *     <vendor>HTC</vendor>
     *     <model>ADR6350/Droid Incredible 2</model>
     *     <title>HTC ADR6350/Droid Incredible 2</title>
     *   </device>
     *   <device>
     *     <nid>2900754</nid>
     *     <vendor>HTC</vendor>
     *     <model>ADR6425</model>
     *     <title>HTC ADR6425LVW</title>
     *   </device>
     *   ...
     * </deviceatlas>
     *
     * @param xmlInputStream the InputStream to the XML content
     */
    List<Device> readDeviceAtlasFromXml(InputStream xmlInputStream) throws java.io.IOException, javax.xml.bind.JAXBException {
        // Use JAXB to unmarshal the XML document into a list of Device objects
        return ((Deviceatlas) unmarshaller.unmarshal(xmlInputStream)).getDevice();
    }

    /**
     * Takes a list of DeviceInfo objects.  For each device, if a corresponding
     * externalID is found in the database, no action is taken.  If not,
     * if a Vendor with the same name/alias does not exist, it is created, and then
     * a new Model is added to the database.  New Vendors added are marked with
     * reviewed=false in order to draw attention from our device review team.
     *
     * Note that this method will not update model and vendor names if
     * a record already exists.
     */
    void syncDevices(List<Device> deviceList) {
        // Before we go any further, make sure we haven't been given an empty
        // empty list of devices.  If the list is empty, that implies that
        // DeviceAtlas returned us empty XML or something wacky like that,
        // and we don't want to auto-delete all models as a result.
        if (deviceList.isEmpty()) {
            // This could probably just be a warning, but we may as well
            // let scavenger discover it as SEVERE so we get an email.
            LOG.error("Empty device list...bailing");
            return;
        }

        LOG.info("Processing {} devices", deviceList.size());

        FetchStrategy modelFs = new FetchStrategyBuilder().addInner(Model_.vendor).build();

        FetchStrategy vendorFs = new FetchStrategyBuilder().addLeft(Vendor_.models).addLeft(Vendor_.aliases).build();

        Map<String, Vendor> vendorsByNameOrAlias = new HashMap<String, Vendor>();
        for (Vendor vendor : deviceManager.getAllVendors(vendorFs)) {
            vendorsByNameOrAlias.put(vendor.getName().toLowerCase(), vendor);
            for (String alias : vendor.getAliases()) {
                vendorsByNameOrAlias.put(alias.toLowerCase(), vendor);
            }
        }

        Set<String> externalIDsFound = new HashSet<String>();
        Set<Model> undeletedModels = new LinkedHashSet<Model>();
        Map<Model, Vendor> modelVendorChanges = new LinkedHashMap<Model, Vendor>();
        Map<Model, String> modelNameChanges = new LinkedHashMap<Model, String>();
        Set<Vendor> vendorsAdded = new LinkedHashSet<Vendor>();
        Set<Model> modelsAdded = new LinkedHashSet<Model>();

        for (Device device : deviceList) {
            // Add it to our running hash for reverse checks later
            externalIDsFound.add(device.getNid());

            String deviceModelName = getModelName(device, vendorsByNameOrAlias);
            if (StringUtils.isEmpty(deviceModelName)) {
                LOG.warn("Empty model name for externalID={}", device.getNid());
            }

            // Check if it's in the DB already
            Model m = deviceManager.getModelByExternalId(device.getNid(), modelFs);
            if (m != null) {
                String displayVendor = displayVendor(m.getVendor());
                LOG.debug("Model found: id={}, vendor={}, name={}, externalID={}", m.getId(), displayVendor, m.getName(), m.getExternalID());
                // See if it had previously been deleted
                boolean isModelDeleted = m.isDeleted();
                Vendor oldModelVendor = null;
                String oldModelName = null;
                if (isModelDeleted) {
                    LOG.info("Undeleting Model id={}, vendor={}, name={}, externalID={}", m.getId(), displayVendor, m.getName(), m.getExternalID());
                }
                try {
                    // Establish the vendor
                    Vendor vendor = establishVendor(device, vendorsByNameOrAlias, vendorsAdded);

                    // It reappeared...wacky...but it happens.  Undelete it.
                    if (isModelDeleted) {
                        m.setDeleted(false);
                    }

                    // Update the vendor if necessary
                    Vendor modelVendor = m.getVendor();
                    if (!vendor.equals(modelVendor)) {
                        LOG.info("Updating Vendor on Model id={}, name={}, externalID={}, old vendor={}, new vendor={}", m.getId(), m.getName(), m.getExternalID(), displayVendor,
                                displayVendor(vendor));
                        oldModelVendor = modelVendor;
                        m.setVendor(vendor);
                    }

                    // Update the model name if necessary
                    String modelName = m.getName();
                    if (StringUtils.isNotBlank(deviceModelName) && !deviceModelName.equals(modelName)) {
                        LOG.info("Updating name on Model id={}, externalID={}, old name={}, new name={}", m.getId(), m.getExternalID(), modelName, deviceModelName);
                        oldModelName = modelName;
                        m.setName(deviceModelName);
                    }

                    if (!ghostMode) {
                        m = deviceManager.update(m); // persist the update
                    }

                    if (isModelDeleted) {
                        undeletedModels.add(m);
                    } else {
                        if (oldModelVendor != null) {
                            modelVendorChanges.put(m, oldModelVendor);
                        }
                        if (oldModelName != null) {
                            modelNameChanges.put(m, oldModelName);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Failed to " + (isModelDeleted ? "undelete" : "update") + " Model id={}, externalID={} {}", m.getId(), m.getExternalID(), e);
                }
            } else {
                // Create the Model
                try {
                    Vendor vendor = establishVendor(device, vendorsByNameOrAlias, vendorsAdded);

                    LOG.info("Creating Model: vendor={}, name={}, externalID={}", displayVendor(vendor), deviceModelName, device.getNid());
                    // Create new Model with null Device Group, Device Group will be assigned by Platform Mapper
                    if (!ghostMode) {
                        m = deviceManager.newModel(vendor, deviceModelName, device.getNid(), null);
                    } else {
                        m = vendor.newModel(deviceModelName, null);
                        m.setExternalID(device.getNid());
                    }

                    modelsAdded.add(m); // track that we added it
                } catch (Exception e) {
                    LOG.error("Failed to create Model: device=[nid={}, vendor={}, model={}, title={}] {}", device.getNid(), device.getVendor(), device.getModel(),
                            device.getTitle(), e);
                }
            }
        }

        // Now go through all models in the db that aren't marked deleted,
        // and make sure they still exist in DeviceAtlas.
        ModelFilter modelFilter = new ModelFilter().setDeleted(false);
        Set<Model> autoDeletedModels = new LinkedHashSet<Model>();
        for (Model model : deviceManager.getAllModels(modelFilter, modelFs)) {
            if (!externalIDsFound.contains(model.getExternalID())) {
                // Auto-delete it
                LOG.warn("Auto-deleting Model id={}, vendor={}, name={}, externalID={}", model.getId(), displayVendor(model.getVendor()), model.getName(), model.getExternalID());
                model.setDeleted(true);
                if (!ghostMode) {
                    model = deviceManager.update(model);
                }
                autoDeletedModels.add(model);
            }
        }

        // See if there's anything we need to whine about in a summary email
        if (!undeletedModels.isEmpty() || !vendorsAdded.isEmpty() || !modelsAdded.isEmpty() || !autoDeletedModels.isEmpty() || !modelVendorChanges.isEmpty()
                || !modelNameChanges.isEmpty()) {
            StringWriter swri = new StringWriter();
            PrintWriter out = new PrintWriter(swri);
            out.println("Host: " + HostUtils.getHostName());
            if (!undeletedModels.isEmpty()) {
                out.println("===================================================");
                out.println("Previously Deleted Models Reappeared (undeleted): " + undeletedModels.size());
                for (Model model : undeletedModels) {
                    model = deviceManager.getModelById(model.getId(), modelFs);
                    out.println(displayModel(model));
                }
            }
            if (!vendorsAdded.isEmpty()) {
                out.println("===================================================");
                out.println("Vendors Added: " + vendorsAdded.size());
                for (Vendor vendor : vendorsAdded) {
                    out.println(displayVendor(vendor));
                }
            }
            if (!modelsAdded.isEmpty()) {
                out.println("===================================================");
                out.println("Models Added: " + modelsAdded.size());
                for (Model model : modelsAdded) {
                    if (!ghostMode) {
                        model = deviceManager.getModelById(model.getId(), modelFs);
                    }
                    out.println(displayModel(model));
                }
            }
            if (!autoDeletedModels.isEmpty()) {
                out.println("===================================================");
                out.println("Models Deleted: " + autoDeletedModels.size());
                for (Model model : autoDeletedModels) {
                    model = deviceManager.getModelById(model.getId(), modelFs);
                    out.println(displayModel(model));
                }
            }
            if (!modelVendorChanges.isEmpty()) {
                out.println("===================================================");
                out.println("Model Vendor Changes: " + modelVendorChanges.size());
                Model model;
                Vendor oldModelVendor;
                for (Entry<Model, Vendor> entry : modelVendorChanges.entrySet()) {
                    model = entry.getKey();
                    oldModelVendor = entry.getValue();
                    if (!ghostMode) {
                        model = deviceManager.getModelById(model.getId(), modelFs);
                    }
                    out.print(displayModel(model));
                    out.println(" (" + displayVendor(oldModelVendor) + " -> " + displayVendor(model.getVendor()) + ")");
                }
            }
            if (!modelNameChanges.isEmpty()) {
                out.println("===================================================");
                out.println("Model Name Changes: " + modelNameChanges.size());
                Model model;
                String oldModelName;
                for (Entry<Model, String> entry : modelNameChanges.entrySet()) {
                    model = entry.getKey();
                    oldModelName = entry.getValue();
                    if (!ghostMode) {
                        model = deviceManager.getModelById(model.getId(), modelFs);
                    }
                    out.print(displayModel(model));
                    out.println(" (" + oldModelName + " -> " + model.getName() + ")");
                }
            }

            LOG.info("Sending summary info to {}", StringUtils.join(summaryEmailToAddresses, ", "));
            try {
                emailService.sendEmail(summaryEmailFromAddress, summaryEmailFromAddress, Arrays.asList(summaryEmailToAddresses), null, null, null, "SyncDevices Summary ("
                        + HostUtils.getHostName() + ")", swri.toString(), "text/plain");
            } catch (com.adfonic.email.EmailException e) {
                LOG.error("Failed to send summary email {}", e);
            }
        }
    }

    Vendor establishVendor(Device device, Map<String, Vendor> vendorsByNameOrAlias, Set<Vendor> vendorsAdded) {
        // Establish the vendor name
        String vendorName = getVendorName(device, vendorsByNameOrAlias);
        if (StringUtils.isEmpty(vendorName)) {
            LOG.warn("Empty vendor name for externalID={}", device.getNid());
        }

        // See if the vendor already exists
        Vendor vendor = vendorsByNameOrAlias.get(vendorName.toLowerCase());
        if (vendor == null) {
            // It doesn't exist...create it now
            LOG.info("Creating Vendor, name={}", vendorName);
            vendor = new Vendor(vendorName);
            vendor.setReviewed(false); // requires review
            if (!ghostMode) {
                vendor = deviceManager.create(vendor);
            }
            LOG.info("Created new Vendor id={}, name={}", vendor.getId(), vendor.getName());

            vendorsByNameOrAlias.put(vendor.getName().toLowerCase(), vendor); // save it for next time
            vendorsAdded.add(vendor); // track that we added it
        }

        LOG.debug("Vendor resolved: id={}, name={}", vendor.getId(), vendor.getName());
        return vendor;
    }

    static String getModelName(Device device, Map<String, Vendor> vendorsByNameOrAlias) {
        if (StringUtils.isNotEmpty(device.getModel())) {
            return device.getModel();
        } else if (StringUtils.isEmpty(device.getTitle())) {
            return device.getModel(); // not much else we can do
        }

        // There was no <model> provided.  Here's an example of when this would happen:
        //
        // <device>
        //   <nid>207603</nid>
        //   <vendor></vendor>
        //   <model></model>
        //   <title>Motorola V860</title>
        // </device>
        //
        // Fall back on parsing the <title> element text.  Wouldn't it be nice if we
        // could just take the first token of the <title> and assume that's the vendor?
        // Yeah right.  Some vendors have spaces in their names, so we have to sorta
        // walk the title token by token, appending as we go, and seeing if we can
        // recognize the vendor by name.  For example, if the <title> was "abc foo bar M123"
        // then we'd try "abc", then "abc foo", and so on, until we found a match.
        // Only if we find the vendor, *then* we can take the rest of the title to
        // represent the model.
        LOG.debug("No model provided for externalID={}, trying to detect vendor in title: {}", device.getNid(), device.getTitle());
        StringBuilder bld = new StringBuilder();
        String[] tokens = StringUtils.split(device.getTitle(), " /_"); // split on space, slash, or underscore
        // We'll iterate through all but the very last token...cuz *something* has to
        // represent the model in this title...can't just be a vendor name (hope not).
        for (int k = 0; k < tokens.length - 1; ++k) {
            if (bld.length() > 0) {
                bld.append(' ');
            }
            bld.append(tokens[k]);
            String possibleVendor = bld.toString();
            LOG.debug("Trying possible vendor: {}", possibleVendor);
            if (vendorsByNameOrAlias.containsKey(possibleVendor.toLowerCase())) {
                // Found a known vendor name...the rest of the tokens are the model
                StringBuilder bld2 = new StringBuilder();
                for (int p = k + 1; p < tokens.length; ++p) {
                    if (bld2.length() > 0) {
                        bld2.append(' ');
                    }
                    bld2.append(tokens[p]);
                }
                String modelName = bld2.toString();
                LOG.debug("For externalID={}, found model name in title: {}", device.getNid(), modelName);
                return modelName;
            }
        }

        // We didn't recognize a vendor name in the title, so we couldn't glean the model
        LOG.warn("No recognized vendor name found in title, so no model: {}", device.getTitle());
        return device.getModel(); // not much else we can do
    }

    static String getVendorName(Device device, Map<String, Vendor> vendorsByNameOrAlias) {
        if (StringUtils.isNotEmpty(device.getVendor())) {
            return device.getVendor();
        } else if (StringUtils.isEmpty(device.getTitle())) {
            return device.getVendor(); // not much else we can do
        }

        // There was no <vendor> provided.  Here's an example of when this would happen:
        //
        // <device>
        //   <nid>207603</nid>
        //   <vendor></vendor>
        //   <model></model>
        //   <title>Motorola V860</title>
        // </device>
        //
        // There are even cases where the <model> is specified but <vendor> is not, i.e.:
        //
        // <device>
        //   <nid>207208</nid>
        //   <vendor></vendor>
        //   <model>RAZR V3/RAZR (Vodafone)</model>
        //   <title>Motorola RAZR V3-Vodafone</title>
        // </device>
        //
        // Fall back on parsing the <title> element text.  Wouldn't it be nice if we
        // could just take the first token of the <title> and assume that's the vendor?
        // Yeah right.  Some vendors have spaces in their names, so we have to sorta
        // walk the title token by token, appending as we go, and seeing if we can
        // recognize the vendor by name.  For example, if the <title> was "abc foo bar M123"
        // then we'd try "abc", then "abc foo", and so on, until we found a match.
        LOG.debug("No vendor provided for externalID={}, trying to detect vendor in title: {}", device.getNid(), device.getTitle());
        StringBuilder bld = new StringBuilder();
        String[] tokens = StringUtils.split(device.getTitle(), " /_"); // split on space, slash, or underscore
        // We'll iterate through all but the very last token...cuz *something* has to
        // represent the model in this title...can't just be a vendor name (hope not).
        for (int k = 0; k < tokens.length - 1; ++k) {
            if (bld.length() > 0) {
                bld.append(' ');
            }
            bld.append(tokens[k]);
            String possibleVendor = bld.toString();
            LOG.debug("Trying possible vendor: {}", possibleVendor);
            if (vendorsByNameOrAlias.containsKey(possibleVendor.toLowerCase())) {
                LOG.debug("For externalID={}, found vendor name in title: {}", device.getNid(), possibleVendor);
                return possibleVendor;
            }
        }

        // We didn't recognize a vendor name in the title
        LOG.warn("No recognized vendor name found in title: {}", device.getTitle());
        return device.getVendor(); // not much else we can do
    }

    private String displayVendor(Vendor vendor) {
        StringBuilder sb = new StringBuilder();
        if (vendor != null) {
            sb.append(vendor.getName()).append(" (id=").append(vendor.getId()).append(")");
        }
        return sb.toString();
    }

    private String displayModel(Model model) {
        StringBuilder sb = new StringBuilder();
        if (model != null) {
            sb.append(model.getVendor().getName()).append(" ").append(model.getName());
            sb.append(" (id=").append(model.getId()).append(", externalID=").append(model.getExternalID()).append(")");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SpringTaskBase.runBean(SyncDevices.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml");
        } catch (Throwable e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}