package com.adfonic.adserver.impl;

import java.awt.Dimension;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.tools.generic.EscapeTool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TemplateBuilder;
import com.adfonic.adserver.truste.AESNoSaltService;
import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Gender;
import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.geo.Coordinates;
import com.adfonic.util.Range;
import com.adfonic.util.StringUtils;

@Component(MarkupGeneratorImpl.BEAN_NAME)
public class MarkupGeneratorImpl implements MarkupGenerator {

    public static final String BEAN_NAME = "STANDARD";

    private static final transient Logger LOG = LoggerFactory.getLogger(MarkupGeneratorImpl.class.getName());

    static final EscapeTool ESCAPE_TOOL = new EscapeTool();

    private final ConcurrentMap<String, Template> templateCache = new ConcurrentHashMap<String, Template>();
    private final VelocityEngine velocityEngine;
    private final DisplayTypeUtils displayTypeUtils;
    private final TemplateBuilder templateBuilderFromFile;
    private final TemplateBuilder templateBuilderFromString;

    private final DynamicProperties dcProperties;

    private final Set<String> deviceIdentifiers = new TreeSet<String>();

    private final AESNoSaltService aesNoSaltService;

    private final Set<Long> weveCompanyIds;

    @Autowired
    public MarkupGeneratorImpl(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dcProperties) {
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_HIFA); // d.hifa
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ATID); // AdTruth ID
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID); // d.adid
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID_MD5); // d.adid_md5
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1); // d.adid_sha1
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_IFA); // d.ifa
        deviceIdentifiers.add(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5); // d.idfa_md5		

        Objects.requireNonNull(velocityEngine);
        this.velocityEngine = velocityEngine;

        Objects.requireNonNull(displayTypeUtils);
        this.displayTypeUtils = displayTypeUtils;

        Objects.requireNonNull(dcProperties);
        this.dcProperties = dcProperties;

        this.aesNoSaltService = new AESNoSaltService(dcProperties.getProperty(DcProperty.TrusteDefaultAeskey));

        this.weveCompanyIds = StringUtils.toSetOfLongs(dcProperties.getProperty(DcProperty.WeveAdvertisers), ",");

        this.templateBuilderFromFile = new TemplateBuilder() {
            @Override
            public Template build(String templateName, String templateText) {
                return MarkupGeneratorImpl.this.velocityEngine.getTemplate(templateName);
            }
        };

        this.templateBuilderFromString = new TemplateBuilder() {
            @Override
            public Template build(String templateName, String templateText) {
                //to do caching
                //to do look for a better solution using ResourceLoader

                RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
                StringReader reader = new StringReader(templateText);
                SimpleNode node = null;
                try {
                    node = runtimeServices.parse(reader, templateName);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Template template = new Template();
                template.setRuntimeServices(runtimeServices);
                template.setData(node);
                template.setName(templateName);
                template.initDocument();

                return template;
            }
        };
    }

    /**
     * Generate the ad mark up
     * return String
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public String generateMarkup(AdComponents adComponents, TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression, boolean renderBeacons)
            throws java.io.IOException {

        // Defensive validation
        if (creative == null) {
            return null;
        }

        Template template = getTemplate(adComponents, context, creative, impression);

        // Populate properties with everything the template needs in order to 
        // generate the ad content
        Properties templateProps = generateTemplateProperties(adComponents, context, adSpace, creative, impression, renderBeacons);
        //adtruth_prefs_js_url - http://as.byyd.net/adtruth/prefs.js
        boolean sslRequired = context.isSslRequired();
        String adtruthUrl = dcProperties.getProperty(DcProperty.AdtruthPrefsJsUrl);
        templateProps.put("adtruth_prefs_js_url", AdResponseLogicImpl.toHttpsIfRequired(sslRequired, adtruthUrl));

        boolean notCreativeAudit = !context.isFlagTrue(TargetingContext.CREATIVE_AUDIT);
        // Better than excluding VAST would be something like include all HTML based creatives (uff and but what about native ads...)
        boolean notVastXml = context.getAttribute(TargetingContext.RENDERED_TRANSFORM) != ContentForm.VAST_2_0;
        boolean isTrusteEligible = creative.getCampaign().isBehavioural() && notVastXml && notCreativeAudit;
        // Truste (adchoices) optout markup - Piece of HTML appended at the end of the original creative markup 
        if (isTrusteEligible) {

            // Publisher id, same for Byyd and Weve.
            String pid = dcProperties.getProperty(DcProperty.TrusteWevePid);

            // Campaign id, used to store devices for Weve.
            StringBuilder cid = new StringBuilder();

            // App id, always the same for Weve, but different between app and  web for Byyd.
            String aid = dcProperties.getProperty(DcProperty.TrusteDefaultWebAid);

            // Encrypted device id
            String sid = "";

            IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
            boolean isTrusteFeature = integrationType != null && integrationType.getSupportedFeatures().contains(Feature.TRUSTE_ICON);
            Long pubType = adSpace.getPublication().getPublicationTypeId();

            // MAX-168: AppNexus MarkupGenerator Publication Medium (hack)
            Medium medium = Medium.UNKNOWN;
            if (context.getDomainCache() != null && pubType != null && context.getDomainCache().getPublicationTypeById(pubType) != null
                    && context.getDomainCache().getPublicationTypeById(pubType).getMedium() != null) {
                medium = context.getDomainCache().getPublicationTypeById(pubType).getMedium();
            }

            Long companyId = creative.getCampaign().getAdvertiser().getCompany().getId();

            boolean isWeve = weveCompanyIds.contains(companyId);

            // Ignore Feature.TRUSTE_WEVE_ICON as we must add Truste for Weve.
            if (isWeve) {

                // Always the same for app and web for Weve.
                aid = dcProperties.getProperty(DcProperty.TrusteWeveWebAid);

                if (medium.equals(Medium.APPLICATION) || medium.equals(Medium.UNKNOWN)) {

                    // Ignore encrypted for weve, stuff the ids in the campaign
                    sid = "0";

                    Map<Long, String> devices = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, Map.class);
                    if (!devices.isEmpty()) {
                        SortedSet<DeviceIdentifierTypeDto> deviceIds = context.getDomainCache().getAllDeviceIdentifierTypes();
                        int i = 0;
                        for (DeviceIdentifierTypeDto deviceType : deviceIds) {
                            if (devices.containsKey(deviceType.getId()) && deviceIdentifiers.contains(deviceType.getSystemName())) {
                                if (i > 0) {
                                    cid.append("^");
                                }
                                cid.append("d." + deviceType.getSystemName() + "~" + devices.get(deviceType.getId()));
                                i++;
                            }
                        }
                    }
                }
            }

            // Now pay attention to where Feature.TRUSTE_ICON is supported
            if (!isWeve && isTrusteFeature) {
                cid.append(creative.getCampaign().getExternalID());
                if (medium.equals(Medium.SITE)) {
                    aid = dcProperties.getProperty(DcProperty.TrusteDefaultWebAid);
                } else {
                    aid = dcProperties.getProperty(DcProperty.TrusteDefaultAppAid);

                    Map<Long, String> devices = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, Map.class);
                    if (!devices.isEmpty()) {
                        SortedSet<DeviceIdentifierTypeDto> deviceIds = context.getDomainCache().getAllDeviceIdentifierTypes();
                        JSONArray jsonArray = new JSONArray();
                        for (DeviceIdentifierTypeDto deviceIdentifierTypeDto : deviceIds) {
                            if (devices.containsKey(deviceIdentifierTypeDto.getId()) && deviceIdentifiers.contains(deviceIdentifierTypeDto.getSystemName())) {
                                JSONObject jobject = new JSONObject();
                                jobject.put("idName", deviceIdentifierTypeDto.getSystemName());
                                jobject.put("idValue", devices.get(deviceIdentifierTypeDto.getId()));
                                jsonArray.add(jobject);
                            }
                        }

                        JSONObject json = new JSONObject();
                        json.put("additionalID", jsonArray);
                        json.put("requestedDate", System.currentTimeMillis());

                        sid = aesNoSaltService.encrypt(json.toString());
                    }
                }
            }

            // For both Weve and anything with truste explicitly enabled. 
            if (isWeve || isTrusteFeature) {
                templateProps.put("truste_pid", pid);
                templateProps.put("truste_cid", cid);
                templateProps.put("truste_aid", aid);
                templateProps.put("truste_sid", sid);

                //parametrize truste_choices_url - http://choices.truste.com/ca
                String trusteUrl = dcProperties.getProperty(DcProperty.TrusteChoicesUrl);
                templateProps.put("truste_choices_url", AdResponseLogicImpl.toHttpsIfRequired(sslRequired, trusteUrl));

                // Extended creative
                if (creative.hasDynamicExtendedTemplate() || creative.getExtendedCreativeTypeId() != null) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Velocity merge extended creative template with properties: " + templateProps);
                    }
                    try {
                        java.io.StringWriter swri = new java.io.StringWriter();
                        template.merge(new VelocityContext(templateProps), swri);
                        templateProps.put("extendeCreativeContent", swri.toString());
                        if (medium.equals(Medium.SITE)) {
                            template = getAndCacheTemplateInternal("truste/truste-mobile-web-container-extended.vtl", "", templateBuilderFromFile);
                        } else {
                            template = getAndCacheTemplateInternal("truste/truste-in-app-container-extended.vtl", "", templateBuilderFromFile);
                        }
                    } catch (RuntimeException e) {
                        LOG.warn("Failed to merge extended template \"" + template.getName() + "\"", e);
                        throw e;
                    }
                } else {
                    // Normal VTL templates
                    templateProps.put("trusteRegularVtl", template.getName());
                    if (medium.equals(Medium.SITE)) {
                        template = getAndCacheTemplateInternal("truste/truste-mobile-web-container.vtl", "", templateBuilderFromFile);
                    } else {
                        template = getAndCacheTemplateInternal("truste/truste-in-app-container.vtl", "", templateBuilderFromFile);
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Velocity merge template with properties: " + templateProps);
        }
        StringWriter swri = new StringWriter();
        try {
            template.merge(new VelocityContext(templateProps), swri);
        } catch (RuntimeException e) {
            LOG.warn("Failed to merge template \"" + template.getName() + "\"", e);
            throw e;
        }
        return swri.toString();
    }

    static Properties generateTemplateProperties(AdComponents adComponents, TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression,
            boolean renderBeacons) {
        Properties props = new Properties();
        props.put("adComponents", adComponents);
        props.put("renderBeacons", renderBeacons);

        // Expose the color scheme to the template
        ColorScheme colorScheme;
        try {
            colorScheme = ColorScheme.valueOf(context.getAttribute(Parameters.COLOR_SCHEME, String.class));
        } catch (Exception e) {
            colorScheme = DEFAULT_COLOR_SCHEME;
        }
        props.put("colorScheme", colorScheme.name());
        props.put("textColor", colorScheme.getTextColor());

        // Make sure our templates have access to the device properties,
        // which will be in the context as "device.*"
        Map<String, String> deviceProps = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (deviceProps == null) {
            // This is known to happen at RTB win notice time
            //if (LOG.isLoggable(Level.INFO)) {
            //LOG.info("Could not make device properties available to VTL");
            //}
            // At least put an empty map in there, so that any calls to
            // ${device.get("...")} can resolve.
            props.put("device", Collections.emptyMap());
        } else {
            props.put("device", deviceProps);
        }

        // While we're at it, let's just shove the whole TargetingContext
        // attributes map into the Velocity context.  That way the templates
        // can access things like operator, platform, gender, age, etc.
        // They'll be accessible like this: ${targetingContext.get("...")}
        props.put("targetingContext", context.getAttributes());

        // Some templates require access to the derived Platform, so slap that
        // in the properties if it's available
        PlatformDto platform = context.getAttribute(TargetingContext.PLATFORM);
        if (platform != null) {
            props.put("platform", platform);
        }

        // Set the "escape" property, which is actually the velocity tool
        // object that supports methods like $escape.html(...) and what not.
        props.put("escape", ESCAPE_TOOL);

        props.put("macros", MarkupGenerator.getVelocityMacroProps(context, adSpace, creative, impression));

        Range<Integer> ageRange = context.getAttribute(TargetingContext.AGE_RANGE);
        if (ageRange != null) {
            props.put("ageLow", ageRange.getStart());
            props.put("ageHigh", ageRange.getEnd());
        }

        Gender gender = context.getAttribute(TargetingContext.GENDER);
        if (gender != null) {
            props.put("gender", gender.name());
        }

        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null) {
            props.put("country", country);
        }

        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        if (coordinates != null) {
            props.put("latitude", coordinates.getLatitude());
            props.put("longitude", coordinates.getLongitude());
        }

        return props;
    }

    protected Template getTemplate(AdComponents adComponents, TargetingContext context, CreativeDto creative, Impression impression) {

        if (creative != null && creative.hasDynamicExtendedTemplate()) {
            // Extended capabilities creatives have their own set of templates
            return getDynamicExtendedTemplate(context, creative);
        }

        if (creative != null && creative.getExtendedCreativeTypeId() != null) {
            // Extended capabilities creatives have their own set of templates
            return getExtendedCapabilitiesTemplate(context, creative.getExtendedCreativeTypeId());
        }

        // Conventional way of deriving the template (i.e. for banner or text)
        FormatDto format;
        if (creative != null) {
            // Just grab the format straight from the Creative
            format = context.getDomainCache().getFormatById(creative.getFormatId());
        } else if (adComponents.getFormat() != null) {
            // No creative...most likely a test ad
            // Grab the format name from the AdComponents object instead.
            format = context.getDomainCache().getFormatBySystemName(adComponents.getFormat());
        } else {
            LOG.warn("No way to make HTML with no Creative and no Format specified");
            return null;
        }

        // MAD-614
        Boolean isNative = context.getAttribute(TargetingContext.IS_NATIVE);
        if (isNative != null && isNative.booleanValue()) {
            // Mopub 2.1 native response. Standard OpenRTB 2.3 is sorted in AdMarkupRenderer
            return getAndCacheTemplateInternal("native/native-template.vtl", "", templateBuilderFromFile);
        }

        DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context);

        // Use the publisher's template size, if specified
        Dimension templateSize = context.getAttribute(TargetingContext.TEMPLATE_SIZE);

        return getHtmlTemplate(format.getSystemName(), displayType == null ? null : displayType.getSystemName(), templateSize);
    }

    protected Template getHtmlTemplate(String formatSystemName, String displayTypeSystemName, Dimension templateSize) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHtmlTemplate " + formatSystemName + ", " + displayTypeSystemName + ", " + templateSize);
        }
        if (displayTypeSystemName == null) {
            // Fall back on generic if the DisplayTypeDto isn't known
            displayTypeSystemName = "generic";
        }

        // Build the name of the Velocity template that we'll use.  The naming
        // convention is: "<format.systemName>_<displayType.systemName>[_<templateSize>].vtl"
        // We'll look for that specific template first...
        StringBuilder bld = new StringBuilder();
        bld.append("html/").append(formatSystemName).append('_').append(displayTypeSystemName);

        // TODO: possibly allow other template sizes in the future, for now just allow 320x50 override
        if (templateSize != null && templateSize.getWidth() == 320 && templateSize.getHeight() == 50) {
            bld.append('_').append(String.valueOf((int) templateSize.getWidth())).append('x').append(String.valueOf((int) templateSize.getHeight()));
        }

        bld.append(".vtl");
        String templateName = bld.toString();

        try {
            return getAndCacheTemplate(templateName);
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            if ("generic".equals(displayTypeSystemName)) {
                throw e; // We've already tried "generic", nothing else we can do
            } else {
                // The DisplayType-specific template doesn't exist, so fall back on the
                // "_generic" template.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Template not found: " + templateName + ", falling back on generic");
                }
                return getHtmlTemplate(formatSystemName, "generic", templateSize);
            }
        }
    }

    Template getExtendedCapabilitiesTemplate(TargetingContext context, long extendedCreativeTypeId) {
        ExtendedCreativeTypeDto extendedCreativeType = context.getDomainCache().getExtendedCreativeTypeById(extendedCreativeTypeId);
        IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
        if (integrationType == null) {
            throw new IllegalStateException("How did we get to the markup phase with no IntegrationType?");
        }

        @SuppressWarnings("unchecked")
        String templateFileBaseName = ExtendedCapabilitiesUtils.getTransformTemplate(extendedCreativeType, integrationType,
                context.getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET, Set.class), context);
        if (templateFileBaseName == null) {
            throw new IllegalStateException("No transform template available for \"" + extendedCreativeType.getName() + "\" on \"" + integrationType.getSystemName() + "\"");
        }

        final String templateName = new StringBuilder("extended/").append(templateFileBaseName).append(".vtl").toString();

        return getAndCacheTemplate(templateName);
    }

    Template getDynamicExtendedTemplate(TargetingContext context, CreativeDto creative) {
        ExtendedCreativeTypeDto extendedCreativeType = context.getDomainCache().getExtendedCreativeTypeById(creative.getExtendedCreativeTypeId());

        String externalID = creative.getExternalID();
        if (!extendedCreativeType.getUseDynamicTemplates()) {
            throw new IllegalStateException("Creative with dynamic templates but incompatible extended creative type! " + extendedCreativeType.getName() + " creative externalId: "
                    + externalID);
        }

        IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
        if (integrationType == null) {
            throw new IllegalStateException("How did we get to the markup phase with no IntegrationType?");
        }

        @SuppressWarnings("unchecked")
        Set<ContentForm> bidContentForms = context.getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET, Set.class);
        String preprocessedTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creative.getExtendedCreativeTemplates(), extendedCreativeType, integrationType, bidContentForms,
                context);
        if (preprocessedTemplate == null) {
            throw new IllegalStateException("No extended template for \"" + extendedCreativeType.getName() + "\" on Creative \"" + externalID + "\", Allowed ContentForms: "
                    + bidContentForms);
        }

        String templateKey = "Dynamic@" + externalID + "_" + DigestUtils.md5Hex(preprocessedTemplate);
        return getAndCacheTemplateInternal(templateKey, preprocessedTemplate, templateBuilderFromString);
    }

    protected Template getAndCacheTemplate(final String templateName) {
        return getAndCacheTemplateInternal(templateName, "", templateBuilderFromFile);
    }

    private Template getAndCacheTemplateInternal(String templateName, String templateText, TemplateBuilder templateBuilder) {
        // First, see if we already loaded it
        Template template = templateCache.get(templateName);
        if (template != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using cached Template: " + templateName);
            }
            return template;
        }

        // Load it on the fly
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading template: " + templateName);
        }
        template = templateBuilder.build(templateName, templateText);

        if (template != null) {
            // Cache it for next time
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded and caching template: " + templateName);
            }
            // Cache it, but only if somebody else hasn't gotten there first
            Template alreadySet = templateCache.putIfAbsent(templateName, template);
            if (alreadySet != null) {
                template = alreadySet; // use the one that got there first
            }
        }
        return template;
    }

}
