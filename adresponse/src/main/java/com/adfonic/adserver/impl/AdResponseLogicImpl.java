package com.adfonic.adserver.impl;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.adserver.AbstractAdComponents;
import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.IconManager;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.BidType;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

// Avoid the class name collision with "Component" in our domain
@org.springframework.stereotype.Component
public class AdResponseLogicImpl implements AdResponseLogic {

    private static final transient Logger LOG = Logger.getLogger(AdResponseLogicImpl.class.getName());

    private static final Set<String> deviceIdentifiers = new TreeSet<String>();

    static {
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_DPID); // d.dpid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_HIFA); // d.hifa
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ODIN_1); // d.odin-1
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_OPENUDID); // d.openudid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ATID); // AdTruth ID
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID); // d.adid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID_MD5); // d.adid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1); //
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_GOUID); // d.gouid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_IDFA); // d.idfa_md5
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5); // d.idfa_md5
    }

    private DisplayTypeUtils displayTypeUtils;
    private IconManager iconMgr;
    private VhostManager vhostManager;
    private DynamicProperties dProperties;

    @Value("${testad.destination.url:}")
    String testAdDestinationUrl;

    @Autowired
    public AdResponseLogicImpl(DisplayTypeUtils displayTypeUtils, IconManager iconMgr, VhostManager vhostManager, DynamicProperties dProperties) {
        this.displayTypeUtils = displayTypeUtils;
        this.iconMgr = iconMgr;
        this.vhostManager = vhostManager;
        this.dProperties = dProperties;
    }

    private final ThreadLocal<DecimalFormat> threadLocalNetAmountFmt = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("0.00########");
        }
    };

    /** {@inheritDoc} */
    @Override
    public void addBackgroundImageAsNeeded(AdComponents adComponents, TargetingContext context) {
        // We only include the mock "backgroundImage" component for text ads.
        // Also, don't bother with the backgroundImage for t.pretty=0 requests.
        if (!"text".equals(adComponents.getFormat()) || "0".equals(context.getAttribute(Parameters.PRETTY))) {
            return;
        }

        // Determine the color scheme based on the parameter, if specified
        ColorScheme colorScheme;
        try {
            colorScheme = ColorScheme.valueOf(context.getAttribute(Parameters.COLOR_SCHEME, String.class));
        } catch (Exception e) {
            colorScheme = MarkupGenerator.DEFAULT_COLOR_SCHEME;
        }

        // Determine the background image size
        int bgImageWidth;
        int bgImageHeight;

        // See if the publisher specified a template size...
        Dimension templateSize = context.getAttribute(TargetingContext.TEMPLATE_SIZE);
        if (templateSize != null) {
            bgImageWidth = (int) templateSize.getWidth();
            bgImageHeight = (int) templateSize.getHeight();
            // TODO: validate these?  easier/faster not to for now, since the
            // IconManager will throw below if it's passed junk width/height.
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Using template size as background image size: " + bgImageWidth + "x" + bgImageHeight);
            }
        } else {
            // By default we'll use a 300x50 background image
            bgImageWidth = 300;
            bgImageHeight = 50;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Using default background image size: " + bgImageWidth + "x" + bgImageHeight);
            }
        }

        // We'll provide the "url" of the backgroundImage data, which
        // is inline base64-encoded data.
        try {
            Map<String, String> attributes = new LinkedHashMap<String, String>();
            attributes.put("url", "data:image/gif;base64," + iconMgr.getBase64EncodedImageData(adComponents.getDestinationType(), colorScheme, bgImageWidth, bgImageHeight));

            // Add the "backgroundImage" component
            adComponents.getComponents().put("backgroundImage", attributes);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to getBase64EncodedImageData(" + adComponents.getDestinationType() + "," + colorScheme + "," + bgImageWidth + "," + bgImageHeight + ")",
                    e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addBeaconsAsNeeded(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, String priceUrlMacro, TargetingContext context, Impression impression,
            HttpServletRequest request) {
        boolean useBeacons = context.getAttribute(TargetingContext.USE_BEACONS, Boolean.class);
        if (useBeacons) {
            addBeacons(adComponents, creative, adSpace, priceUrlMacro, context, impression, request);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addBeacons(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, String priceUrlMacro, TargetingContext context, Impression impression,
            HttpServletRequest httpRequest) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("addBeacons: " + adComponents);
        }

        boolean sslRequired = context.isSslRequired();
        boolean isCreativeAudit = context.isFlagTrue(TargetingContext.CREATIVE_AUDIT);
        // Byyd impression tracker is always added
        String byydBeaconUrl = getByydBeaconUrl(creative, adSpace, impression.getExternalID(), httpRequest, isCreativeAudit, sslRequired, priceUrlMacro);
        addBeacon(adComponents, byydBeaconUrl, context);

        // Weve Beacon SC-509 if applicable
        OperatorDto operatorDto = context.getAttribute(TargetingContext.OPERATOR, OperatorDto.class);
        if (operatorDto != null && operatorDto.isWeveEnabled()) {
            String weveBeaconUrl = getWeveBeaconUrl(creative, adSpace, context, impression, sslRequired);
            addBeacon(adComponents, weveBeaconUrl, context);
        }

        // Additional creative's beacons if present
        if (creative != null && creative.getDestination().getBeaconUrls() != null) {
            //Apply all the beacons
            for (String beacon : creative.getDestination().getBeaconUrls()) {
                // If the Creative's Destination has its own beaconUrls, post-process any
                // %style% variables in it and include it in the response.
                String postProcessedUrl = MarkupGenerator.resolveMacros(beacon, adSpace, creative, impression, context, null, true, null);
                addBeacon(adComponents, postProcessedUrl, context);
            }
        }

        // Some DMP vendors are using itrackers for usage/billing purposses (Factual)
        List<String> dynamicTrackers = context.getAttribute(TargetingContext.DYNAMIC_IMP_TRACKERS);
        if (dynamicTrackers != null) {
            for (String trackerUrl : dynamicTrackers) {
                addBeacon(adComponents, trackerUrl, context);
            }
        }

    }

    private String getWeveBeaconUrl(CreativeDto creative, AdSpaceDto adSpace, TargetingContext context, Impression impression, boolean sslRequired) {
        String weveBeaconUrl = toHttpsIfRequired(sslRequired, dProperties.getProperty(DcProperty.WeveBeaconUrl));
        StringBuilder weveBeaconBldr = new StringBuilder().append(weveBeaconUrl).append(adSpace.getExternalID()).append("/").append(impression.getExternalID()).append("/")
                .append(creative.getExternalID()).append(".gif");

        Map devices = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, Map.class);
        Map<Long, String> devices2 = impression.getDeviceIdentifiers();
        Map<Long, String> alldevices = mergeMaps(devices2, devices);

        if (!alldevices.isEmpty()) {
            SortedSet<DeviceIdentifierTypeDto> deviceIds = context.getDomainCache().getAllDeviceIdentifierTypes();
            int i = 0;
            for (DeviceIdentifierTypeDto deviceIdentifierTypeDto : deviceIds) {
                if (alldevices.containsKey(deviceIdentifierTypeDto.getId()) && deviceIdentifiers.contains(deviceIdentifierTypeDto.getSystemName())) {
                    if (i == 0) {
                        weveBeaconBldr.append("?");
                    } else {
                        weveBeaconBldr.append("&");
                    }
                    weveBeaconBldr.append("d." + deviceIdentifierTypeDto.getSystemName() + "=" + alldevices.get(deviceIdentifierTypeDto.getId()));
                    i++;
                }
            }
        }
        return weveBeaconBldr.toString();
    }

    private String getByydBeaconUrl(CreativeDto creative, AdSpaceDto adSpace, String impressionId, HttpServletRequest httpRequest, boolean isCreativeAudit, boolean sslRequired,
            String priceUrlMacro) {
        // We must derive our (byyd) beacon url from incoming http request (even for RTB)  
        StringBuilder byydBeaconBldr = vhostManager.getBeaconBaseUrl(httpRequest, sslRequired);

        if (AdServerFeatureFlag.EXCHANGE_IN_URL.isEnabled()) {
            byydBeaconBldr.append('/').append(adSpace.getPublication().getPublisher().getExternalId());
        }

        byydBeaconBldr.append('/').append(adSpace.getExternalID()).append('/').append(impressionId).append(".gif");

        if (isCreativeAudit) {
            byydBeaconBldr = addAuditUrlParams(byydBeaconBldr, creative, adSpace.getPublication().getPublisher());
        }

        // AF-1342 - if this is RTB and winNoticeMode is BEACON, then we need to make sure 
        // we add a substitution macro that the RTB exchange will populate with the settlement price.  
        // That's the only way we find out about the actual RTB settlement price in that win notice mode.
        if (priceUrlMacro != null) {
            char c = (byydBeaconBldr.indexOf("?") == -1) ? '?' : '&';
            byydBeaconBldr.append(c).append("sp=").append(priceUrlMacro);
        }

        return byydBeaconBldr.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void addBeacon(AdComponents adComponents, String beaconUrl, TargetingContext context) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("addBeacon: " + beaconUrl);
        }
        Map<String, String> beaconsComponent = adComponents.getComponents().get("beacons");
        int numBeacons = 0;
        if (beaconsComponent != null) {
            numBeacons = Integer.parseInt(beaconsComponent.get("numBeacons"));
        } else {
            beaconsComponent = new LinkedHashMap<String, String>();
            adComponents.getComponents().put("beacons", beaconsComponent);
        }

        ++numBeacons;
        //MAD-767
        //There could be a better way to do this by using a java.util.List to store the beacons, 
        //but that would require change in the basic component used across the adserver i.e. 'adComponents'
        beaconsComponent.put("beacon" + numBeacons, beaconUrl);
        beaconsComponent.put("numBeacons", String.valueOf(numBeacons));

        // Also put it into context as some exchanges has special treatment for beacons 
        List<String> impTrackList = context.getAttribute(TargetingContext.IMP_TRACK_LIST);
        if (impTrackList == null) {
            impTrackList = new LinkedList<String>();
            context.setAttribute(TargetingContext.IMP_TRACK_LIST, impTrackList);
        }
        impTrackList.add(beaconUrl);
    }

    /** {@inheritDoc} */
    @Override
    public void addExtendedDataAsNeeded(AdComponents adComponents, CreativeDto creative) {
        // Add Creative.extendedData if the creative has extended capabilities
        Map<String, String> extendedData = null;
        for (Map.Entry<String, String> entry : creative.getExtendedData().entrySet()) {
            if (extendedData == null) {
                extendedData = new HashMap<String, String>();
            }
            extendedData.put(entry.getKey(), entry.getValue());
        }
        if (extendedData != null) {
            adComponents.getComponents().put("extendedData", extendedData);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addPricingAsNeeded(AdComponents adComponents, CreativeDto creative, AdSpaceDto adSpace, TargetingContext context) {
        if (creative == null) {
            return;
        } else if (!context.getAdserverDomainCache().mayPublicationViewPricing(adSpace.getPublication().getId())) {
            return;
        }

        CampaignBidDto currentBid = creative.getCampaign().getCurrentBid();
        if (currentBid == null) {
            return;
        }

        /**
         * Bugzilla 1278
         * bid.type = CPC | CPM
         * bid.amount = net amount, i.e. (currentBid * (1 - advertiserDiscount)) * publisherRevenueShare
         * bid.currency = USD
         */
        //Here we dont return ECPM, its applicable to non RTB and we want to show the bid price(after counting discount and rev share in) and not ECPM
        double netAmount = currentBid.getAmount() * (1 - creative.getCampaign().getAgencyDiscount()) * adSpace.getPublication().getPublisher().getCurrentRevShare();
        //double netAmount = context.getAdserverDomainCache().getEcpm(adSpace, creative);;
        Map<String, String> bidAttributes = new LinkedHashMap<String, String>();
        BidType bidType = currentBid.getBidType();
        bidAttributes.put("type", (bidType == BidType.CPI ? BidType.CPA : bidType).name());
        bidAttributes.put("amount", threadLocalNetAmountFmt.get().format(netAmount));
        bidAttributes.put("currency", "USD");
        adComponents.getComponents().put("bid", bidAttributes);
    }

    /** {@inheritDoc} */
    @Override
    public AdComponents generateFullAdComponents(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, ProxiedDestination pd, Impression impression,
            HttpServletRequest request) {
        if (creative == null) {
            return null;
        }
        AdComponents adComponents = generateBareAdComponents(context, adSpace, creative, pd, impression, request);
        addExtendedDataAsNeeded(adComponents, creative);
        addBackgroundImageAsNeeded(adComponents, context);
        addBeaconsAsNeeded(adComponents, creative, adSpace, null, context, impression, request);
        addPricingAsNeeded(adComponents, creative, adSpace, context);
        return adComponents;
    }

    /**
     * For auditing there is no real AdSpace and Impression, but we know which Exchange is auditing which Creative
     * see adserver's AppNexusRoutingController.java adfonic-api's PublisherAuditController.java
     */
    public static StringBuilder addAuditUrlParams(StringBuilder url, CreativeDto creative, PublisherDto publisher) {
        if (url.indexOf("?") == -1) {
            url.append("?");
        } else {
            url.append("&");
        }
        return url.append("pubr=").append(publisher.getExternalId()).append("&crid=").append(creative.getExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public AdComponents generateBareAdComponents(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, ProxiedDestination pd, Impression impression,
            HttpServletRequest httpRequest) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("generateBareAdComponents: " + VhostManager.makeBaseUrl(httpRequest) + ", ProxiedDestination: " + pd);
        }

        FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());

        // Create an AdComponents impl to hold the key components.
        // Pass the ProxiedDestination to the constructor, whether we have one or not.
        // That way its components will get copied if we do.
        //Ravi : ProxiedDestniation can be null at this point so create AdComponenetImpl
        //with or without ProxiedDestniation
        // Dan: Check adserver-api code and you'll see that the constructor
        // in AbstractAdComponents null checks, which is why I didn't bother
        // doing that here before
        AdComponents adComponents = null;
        if (pd == null) {
            adComponents = new AdComponentsImpl();
        } else {
            adComponents = new AdComponentsImpl(pd);
        }

        boolean sslRequired = context.isSslRequired() || httpRequest.isSecure();
        context.setSslRequired(sslRequired);

        HashMap<String, String> urls = new HashMap<String, String>();
        String assetBaseUrl = toHttpsIfRequired(sslRequired, dProperties.getProperty(DcProperty.AssetBaseUrl));
        urls.put("asset", assetBaseUrl);
        String assetPixelUrl = toHttpsIfRequired(sslRequired, dProperties.getProperty(DcProperty.AssetPixelUrl));
        urls.put("pixel", assetPixelUrl);
        adComponents.getComponents().put("baseUrls", urls);

        adComponents.setFormat(format.getSystemName());

        boolean clickRedirectRequired = false;
        if (creative.getExtendedCreativeTypeId() != null) {
            clickRedirectRequired = context.getDomainCache().getExtendedCreativeTypeById(creative.getExtendedCreativeTypeId()).isClickRedirectRequired();
        }
        StringBuilder clickUrlBldr = new StringBuilder();
        if (clickRedirectRequired) {
            clickUrlBldr = vhostManager.getClickRedirectBaseUrl(httpRequest);
        } else {
            clickUrlBldr = vhostManager.getClickBaseUrl(httpRequest);
        }

        if (AdServerFeatureFlag.EXCHANGE_IN_URL.isEnabled()) {
            clickUrlBldr.append('/').append(adSpace.getPublication().getPublisher().getExternalId());
        }

        clickUrlBldr.append("/").append(adSpace.getExternalID()).append("/").append(impression.getExternalID());

        if (context.isFlagTrue(TargetingContext.CREATIVE_AUDIT)) {
            addAuditUrlParams(clickUrlBldr, creative, adSpace.getPublication().getPublisher());
        }
        adComponents.setDestinationUrl(clickUrlBldr.toString());

        adComponents.setDestinationType(creative.getDestination().getDestinationType());

        if (pd != null) {
            // Use the ProxiedDestination's DestinationType if set
            if (pd.getDestinationType() != null) {
                adComponents.setDestinationType(pd.getDestinationType());
            }

            // Just return the AdComponents like that...no need to add Asset components
            return adComponents;
        }

        // Derive the correct DisplayTypeDto for the format/device combo, falling back on first available
        DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context, true);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("FormatDto is " + format.getSystemName() + ", DisplayTypeDto is " + displayType.getSystemName());
        }

        // Grab text, URLs, and size info from the assets & their components
        for (ComponentDto component : format.getComponents()) {
            AssetDto asset = null;
            for (DisplayTypeDto displayTypeDto : displayTypeUtils.getAllDisplayTypes(format, context)) {
                asset = creative.getAsset(displayTypeDto.getId(), component.getId());
                if (asset != null) {
                    displayType = displayTypeDto;
                    break;
                }
            }

            //try to fall back
            if (asset == null) {
                asset = creative.getAsset(displayType.getId(), component.getId());
                if (asset == null) {
                    if ("image".equals(component.getSystemName())) {
                        // This is NOT ok!
                        throw new IllegalStateException("No \"" + component.getSystemName() + "\" Asset for Creative id=" + creative.getId() + ", DisplayType id="
                                + displayType.getId());
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("No \"" + component.getSystemName() + "\" Asset for Creative id=" + creative.getId() + ", DisplayType id=" + displayType.getId()
                                    + ", this is ok");
                        }
                        continue;
                    }
                }
            }

            Map<String, String> attributes = new LinkedHashMap<String, String>();
            assetBaseUrl = toHttpsIfRequired(sslRequired, dProperties.getProperty(DcProperty.AssetBaseUrl));
            if ("image".equals(component.getSystemName()) || "icon".equals(component.getSystemName()) || "app_icon".equals(component.getSystemName())) {
                // This is the most central spot to omit the icon component
                // if the caller passed t.pretty=0 (no icon or bg image).
                if ("0".equals(context.getAttribute(Parameters.PRETTY)) && "icon".equals(component.getSystemName())) {
                    continue;
                }

                attributes.put("url", assetBaseUrl + "/" + asset.getExternalID());
                // Also set the image or icon width & height attributes based
                // on the given ContentSpec
                ContentSpecDto contentSpec = component.getContentSpec(displayType);
                if (contentSpec != null) {
                    Map<String, String> props = contentSpec.getManifestProperties();
                    if (props.containsKey("width")) {
                        attributes.put("width", props.get("width"));
                    }
                    if (props.containsKey("height")) {
                        attributes.put("height", props.get("height"));
                    }
                }
            } else if ("text".equals(component.getSystemName())) {
                // For banner ads, text=tagline.  For text ads, text=text.
                try {
                    attributes.put("content", new String(asset.getData(), "utf-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    throw new UnsupportedOperationException("Man, if you don't know utf-8 then what DO you know?", e);
                }
            } else if ("video".equals(component.getSystemName())) {
                // nothing to do for video, just do not write "not handled" warning
            } else {
                LOG.warning("ComponentDto not handled: " + component.getSystemName());
                continue;
            }

            // Store the component attributes on the AdComponents object
            // using Component.systemName as the key
            adComponents.getComponents().put(component.getSystemName(), attributes);
        }

        return adComponents;
    }

    /** {@inheritDoc} */
    @Override
    public AdComponents generateTestAdComponents(TargetingContext context, AdSpaceDto adSpace, HttpServletRequest request) throws java.io.IOException {
        // First, determine the test ad format for the given AdSpace
        FormatDto format = null;
        for (Long availableFormatId : adSpace.getFormatIds()) {
            format = context.getDomainCache().getFormatById(availableFormatId);
            if ("banner".equals(format.getSystemName())) {
                break; // Always use banner whenever it's available
            }
            // TODO: allow image formats other than "banner" in test ad slots
        }

        // Build a set of ad components that will render a test ad
        AdComponents adComponents = new AdComponentsImpl();
        adComponents.setDestinationType(DestinationType.URL);
        adComponents.setDestinationUrl(testAdDestinationUrl);

        if (format == null) {
            // AF-1165 - AdSpace.formats was empty in one weird case
            // In this case, we just default to a text response
            adComponents.setFormat("text");
            adComponents.getComponents().put("text", Collections.singletonMap("content", "Congratulations! Ad slot verified."));
        } else {
            // Look up the correct DisplayTypeDto index for the given device
            DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context);
            if (displayType == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("No DisplayTypeDto detected...probably a desktop browser?");
                }
            }

            adComponents.setFormat(format.getSystemName());
            for (ComponentDto component : format.getComponents()) {
                Map<String, String> attributes = new LinkedHashMap<String, String>();

                if ("image".equals(component.getSystemName())) {
                    // TODO: properties for the test ad image path(s)
                    attributes.put("url",
                            VhostManager.makeBaseUrl(request) + "/images/verified_" + format.getSystemName() + "_"
                                    + (displayType == null ? "generic" : displayType.getSystemName()) + ".gif");
                    if (displayType != null) {
                        // Also set the image width & height attributes based on
                        // the given ContentSpec
                        ContentSpecDto contentSpec = component.getContentSpec(displayType);
                        if (contentSpec != null) {
                            Map<String, String> props = contentSpec.getManifestProperties();
                            if (props.containsKey("width")) {
                                attributes.put("width", props.get("width"));
                            }
                            if (props.containsKey("height")) {
                                attributes.put("height", props.get("height"));
                            }
                        }
                    }
                } else if ("text".equals(component.getSystemName())) {
                    attributes.put("content", "Congratulations! Ad slot verified.");
                } else {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("ComponentDto not included in test ad: " + component.getSystemName());
                    }
                    continue;
                }

                // Store the component attributes on the AdComponents object
                // using Component.systemName as the key
                adComponents.getComponents().put(component.getSystemName(), attributes);
            }
        }

        // NOTE: we don't add pricing info for test ads
        // NOTE: we don't add beacons for test ads since there's no "impression" object per se
        addBackgroundImageAsNeeded(adComponents, context);
        return adComponents;
    }

    public static String toHttpsIfRequired(boolean sslRequired, String urlString) {
        if (sslRequired && urlString != null && urlString.startsWith("http:")) {
            return "https:" + urlString.substring(5);
        }
        return urlString;
    }

    /** {@inheritDoc} */
    @Override
    public void postProcessAdComponents(AdComponents adComponents, TargetingContext context) {
        if (adComponents == null) {
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : adComponents.getComponents().entrySet()) {
            String componentName = entry.getKey();
            if (!"text".equals(componentName)) {
                continue;
            }
            Map<String, String> component = entry.getValue();
            String text = component.get("content");
            if (text == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("No text to post-process, skipping");
                }
                continue;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Post-processing " + componentName + ".content: " + text);
            }
            String man = "";
            String phn = "";
            ModelDto model = context.getAttribute(TargetingContext.MODEL);
            if (model != null) {
                man = model.getVendor().getName();
                phn = model.getName();
            }
            text = StringUtils.replace(text, "%man%", man);
            text = StringUtils.replace(text, "%phn%", phn);
            component.put("content", text);
        }
    }

    private static final class AdComponentsImpl extends AbstractAdComponents {
        private static final long serialVersionUID = 1L;

        private AdComponentsImpl() {
        }

        private AdComponentsImpl(AdComponents other) {
            super(other);
        }
    }

    @SuppressWarnings("rawtypes")
    private Map<Long, String> mergeMaps(Map map1, Map map2) {
        Map<Long, String> result = new HashMap<>();
        for (Object key : map1.keySet()) {
            Long longKey = (Long) key;
            String stringValue = (String) map1.get(key);
            result.put(longKey, stringValue);
        }
        for (Object key : map2.keySet()) {
            Long longKey = (Long) key;
            String stringValue = (String) map2.get(key);
            result.put(longKey, stringValue);
        }
        return result;
    }
}
