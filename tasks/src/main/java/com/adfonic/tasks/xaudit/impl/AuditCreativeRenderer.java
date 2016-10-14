package com.adfonic.tasks.xaudit.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MacroTractor;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.cst.AppNexusShared;
import com.adfonic.adserver.impl.SimpleTargetingContext;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Format;
import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.cache.DomainCacheImpl;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.ContentTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.tasks.xaudit.RenderingService;

public class AuditCreativeRenderer implements RenderingService {

    // XXX 7 - Other Application and 1 - Mobile website
    private static final Long XAUDIT_PUBLICATION_TYPE = Long.valueOf(7);

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(getClass());

    private AdMarkupRenderer renderer;

    public AuditCreativeRenderer(AdMarkupRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Prepare DTOs and TargetingContext as they are in AdServer during creative markup rendering
     * then use AdServer's AdMarkupRenderer component to get markup
     * 
     * Catch with creative audit is beacons (impression trackers) and click through/redirect links... 
     */
    @Override
    public RenderedCreative renderContent(Creative creative, Publisher publisher) throws IOException {
        TargetingContext context = buildTargetingContext(creative, publisher);
        Impression impression = new Impression();
        impression.setExternalID(Constant.XAUDIT_IMPRESSION_EXTERNAL_ID);
        impression.setDeviceIdentifiers(Collections.emptyMap());
        CreativeDto creativeDto = buildCreativeDto(creative);
        FormatDto formatDto = buildFormatDto(creative.getFormat());

        AdSpaceDto adSpaceDto = buildAdSpaceDto(publisher);
        RtbConfigDto rtbConfigDto = buildRtbConfig(publisher);
        HttpServletRequest httpRequest = new StaticHttpServletRequest();
        ProxiedDestination proxiedDestination = null; // Only AdServer plugins use ProxiedDestination. For RTB it is always null

        CreativeAssetInfo assetInfo = getCreativeAssetInfo(creative);
        DisplayTypeDto displayTypeDto = buildDisplayType(assetInfo.getDisplayType());

        String destinationUrl = getDestinationUrl(creative, impression);
        String[] rendered = renderer.createMarkup(context, impression, creativeDto, formatDto, adSpaceDto, displayTypeDto, httpRequest, proxiedDestination, rtbConfigDto);
        return new RenderedCreative(assetInfo, rendered[0], destinationUrl, context);
    }

    private String getDestinationUrl(Creative creative, Impression impression) {
        Destination destination = creative.getDestination();
        String destinationUrl;
        if (destination.isDataIsFinalDestination()) {
            destinationUrl = destination.getData();
        } else {
            destinationUrl = destination.getFinalDestination();
        }
        Campaign campaign = creative.getCampaign();
        return MacroTractor.resolveMacros(destinationUrl, "audit-publisher-id", "audit-adspace-id", creative.getExternalID(), campaign.getExternalID(), campaign.getAdvertiser()
                .getExternalID(), "audit-publication-id", impression, Collections.emptyMap(), Collections.emptyMap(), false);
    }

    private RtbConfigDto buildRtbConfig(Publisher publisher) {
        RtbConfig rtbConfig = publisher.getRtbConfig();
        RtbConfigDto rtbConfigDto = new RtbConfigDto();
        rtbConfigDto.setAdMode(rtbConfig.getAdMode());
        rtbConfigDto.setAdmProfile(rtbConfig.getAdmProfile());
        rtbConfigDto.setAuctionType(rtbConfig.getAuctionType());
        rtbConfigDto.setBidCurrency(rtbConfig.getBidCurrency());
        rtbConfigDto.setClickForwardValidationPattern(rtbConfig.getClickForwardValidationPattern());
        rtbConfigDto.setDecryptionScheme(rtbConfig.getDecryptionScheme());
        rtbConfigDto.setDpidFallback(rtbConfig.getDpidFallback());
        rtbConfigDto.setEscapedClickForwardURL(rtbConfig.getEscapedClickForwardUrl());
        rtbConfigDto.setId(rtbConfig.getId());
        rtbConfigDto.setIntegrationTypePrefix(rtbConfig.getIntegrationTypePrefix());
        rtbConfigDto.setPrefixonEscapedURLs(rtbConfig.getEscapedUrlPrefix());
        rtbConfigDto.setSecurityAlias(rtbConfig.getSecAlias());
        rtbConfigDto.setSpMacro(rtbConfig.getSettlementPriceMacro());
        rtbConfigDto.setRtbLostTimeDuration(rtbConfig.getRtbExpirySeconds());
        rtbConfigDto.setSslRequired(rtbConfig.isSslRequired());
        rtbConfigDto.setWinNoticeMode(rtbConfig.getWinNoticeMode());
        return rtbConfigDto;
    }

    private CreativeDto buildCreativeDto(Creative creative) {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setId(creative.getId());
        creativeDto.setExternalID(creative.getExternalID());
        creativeDto.setSslCompliant(creative.isSslCompliant());
        creativeDto.setFormatId(creative.getFormat().getId());

        DestinationDto destinationDto = buildDestinationDto(creative.getDestination());
        creativeDto.setDestination(destinationDto);

        CampaignDto campaignDto = new CampaignDto();
        Campaign campaign = creative.getCampaign();
        campaignDto.setId(campaign.getId());
        campaignDto.setExternalID(campaign.getExternalID());
        campaignDto.setAdvertiserDomain(campaign.getAdvertiserDomain());

        Advertiser advertiser = campaign.getAdvertiser();
        AdvertiserDto advertiserDto = new AdvertiserDto();
        advertiserDto.setId(advertiser.getId());
        advertiserDto.setExternalID(advertiser.getExternalID());
        campaignDto.setAdvertiser(advertiserDto);

        creativeDto.setCampaign(campaignDto);

        Map<DisplayType, AssetBundle> assetBundleMap = creative.getAssetBundleMap();
        for (Entry<DisplayType, AssetBundle> bundleEntry : assetBundleMap.entrySet()) {
            DisplayType displayType = bundleEntry.getKey();
            AssetBundle assetBundle = bundleEntry.getValue();
            Map<Component, Asset> assetMap = assetBundle.getAssetMap();
            for (Entry<Component, Asset> assetEntry : assetMap.entrySet()) {
                Component component = assetEntry.getKey();
                Asset asset = assetEntry.getValue();
                AssetDto assetDto = new AssetDto();
                assetDto.setId(asset.getId());
                assetDto.setExternalID(asset.getExternalID());
                assetDto.setData(asset.getData());
                creativeDto.setAsset(displayType.getId(), component.getId(), assetDto, asset.getContentType().getId());
            }
        }

        // Extended creative type and templates
        ExtendedCreativeType extendedCreativeType = creative.getExtendedCreativeType();
        if (extendedCreativeType != null) {
            creativeDto.setExtendedCreativeTypeId(extendedCreativeType.getId());
        }
        Set<ExtendedCreativeTemplate> extendedTemplates = creative.getExtendedCreativeTemplates();
        for (ExtendedCreativeTemplate extendedTemplate : extendedTemplates) {
            creativeDto.getExtendedCreativeTemplates().put(extendedTemplate.getContentForm(), extendedTemplate.getTemplatePreprocessed());
        }
        return creativeDto;
    }

    private DestinationDto buildDestinationDto(Destination destination) {
        DestinationDto destinationDto = new DestinationDto();
        destinationDto.setId(destination.getId());
        destinationDto.setData(destination.getData());
        destinationDto.setDataIsFinalDestination(destination.isDataIsFinalDestination());
        destinationDto.setDestinationType(destination.getDestinationType());
        destinationDto.setFinalDestination(destination.getFinalDestination());
        // Do not add custom beacon in audit
        // If it changes - beware that adding fetch for Destination.beaconUrls makes return 100 times more beacons - some hibernate mapping glitch
        /*
        List<BeaconUrl> beacons = destination.getBeaconUrls();
        if (!beacons.isEmpty()) {
            List<String> beaconUrls = new ArrayList<String>(beacons.size());
            for (BeaconUrl beacon : beacons) {
                beaconUrls.add(beacon.getUrl());
            }
            destinationDto.setBeaconUrls(beaconUrls);
        }
        */
        return destinationDto;
    }

    private FormatDto buildFormatDto(Format format) {
        FormatDto formatDto = new FormatDto();
        formatDto.setId(format.getId());
        formatDto.setSystemName(format.getSystemName());
        for (Component component : format.getComponents()) {
            ComponentDto componentDto = new ComponentDto();
            componentDto.setId(component.getId());
            componentDto.setSystemName(component.getSystemName());
            for (Entry<DisplayType, ContentSpec> contentSpecEntry : component.getContentSpecMap().entrySet()) {
                DisplayType displayType = contentSpecEntry.getKey();
                ContentSpec contentSpec = contentSpecEntry.getValue();
                DisplayTypeDto displayTypeDto = new DisplayTypeDto();
                displayTypeDto.setId(displayType.getId());
                displayTypeDto.setName(displayType.getName());
                displayTypeDto.setSystemName(displayType.getSystemName());
                displayTypeDto.setConstraints(displayType.getConstraints());
                ContentSpecDto contentSpecDto = new ContentSpecDto();
                contentSpecDto.setId(contentSpec.getId());
                contentSpecDto.setName(contentSpec.getName());
                contentSpecDto.getManifestProperties().putAll(contentSpec.getManifestProperties());
                for (ContentType contentType : contentSpec.getContentTypes()) {
                    ContentTypeDto contentTypeDto = ContentTypeDto.getContentType(contentType.getId(), contentType.getName(), contentType.getMIMEType(), contentType.isAnimated());
                    contentSpecDto.getContentTypes().add(contentTypeDto);
                }
                componentDto.getContentSpecMap().put(displayTypeDto, contentSpecDto);
            }
            formatDto.getComponents().add(componentDto);
            //Map<DisplayTypeDto, ContentSpecDto> contentSpecMap = 
        }
        for (DisplayType displayType : format.getDisplayTypes()) {
            DisplayTypeDto dto = buildDisplayType(displayType);
            formatDto.getDisplayTypes().add(dto);
        }
        return formatDto;
    }

    private DisplayTypeDto buildDisplayType(DisplayType displayType) {
        DisplayTypeDto dto = new DisplayTypeDto();
        dto.setId(displayType.getId());
        dto.setSystemName(displayType.getSystemName());
        dto.setConstraints(displayType.getConstraints());
        return dto;
    }

    private TargetingContext buildTargetingContext(Creative creative, Publisher publisher) {
        SimpleTargetingContext context = new SimpleTargetingContext();
        DomainCacheImpl domainCache = new DomainCacheImpl();

        FormatDto formatDto = buildFormatDto(creative.getFormat());
        domainCache.formatsById.put(formatDto.getId(), formatDto);

        ExtendedCreativeType extendedCreativeType = creative.getExtendedCreativeType();
        if (extendedCreativeType != null) {
            ExtendedCreativeTypeDto extendedDto = buildExtendedTypeDto(extendedCreativeType);
            domainCache.extendedCreativeTypesById.put(extendedCreativeType.getId(), extendedDto);

            Set<ExtendedCreativeTemplate> extendedTemplates = creative.getExtendedCreativeTemplates();
            // This is error and it should not be passed to us...
            if (extendedDto.getUseDynamicTemplates() && extendedTemplates.isEmpty()) {
                throw new IllegalStateException("Creative without own templates but extended type using them: " + extendedCreativeType);
            } else if (extendedDto.getUseDynamicTemplates() == false && extendedTemplates.isEmpty() == false) {
                throw new IllegalStateException("Creative with own templates but extended type not using them: " + extendedCreativeType);
            }

            if (!extendedTemplates.isEmpty()) {
                // Find corresponding integration for extended creative
                IntegrationTypeDto integrationDto = getIntegration(creative, publisher, context);
                context.setAttribute(TargetingContext.INTEGRATION_TYPE, integrationDto);
            }
        }

        context.setDomainCache(domainCache);
        // For AdX we should render and submit SSL version with different External Id
        context.setSslRequired(creative.isSslCompliant());
        context.setFlagTrue(TargetingContext.CREATIVE_AUDIT);
        return context;
    }

    private IntegrationTypeDto getIntegration(Creative creative, Publisher publisher, SimpleTargetingContext context) {
        Set<ExtendedCreativeTemplate> extendedTemplates = creative.getExtendedCreativeTemplates();
        ExtendedCreativeType extendedCreativeType = creative.getExtendedCreativeType();
        // If creative has more templates (usualy MRAID and MOBILE_WEB) then we should really build and submit for aproval multiple markups...
        // But until then we will simply prefer MRAID over MOBILE_WEB or any other...
        MediaType mediaType = extendedCreativeType.getMediaType();
        Map<PublicationType, IntegrationType> integrationMap = publisher.getDefaultIntegrationTypeMap();

        IntegrationType integrationType = null;
        // Try MRAID template first
        ExtendedCreativeTemplate extendedTemplate = findExtendedTemplate(extendedTemplates, ContentForm.MRAID_1_0);
        if (extendedTemplate != null) {
            integrationType = findIntegrationType(integrationMap, mediaType, extendedTemplate.getContentForm());
        }

        if (integrationType == null) {
            // No MRAID integration found - try any template with compatible integration
            for (ExtendedCreativeTemplate extendedTemplateLoop : extendedTemplates) {
                integrationType = findIntegrationType(integrationMap, mediaType, extendedTemplateLoop.getContentForm());
                if (integrationType != null) {
                    extendedTemplate = extendedTemplateLoop;
                    context.setAttribute(TargetingContext.RENDERED_TRANSFORM, extendedTemplate.getContentForm());
                    break; // leave on first match...
                }
            }
        }
        if (integrationType == null) {
            // Either DS eligibility passed to us something wrong or we are doing something wrong...
            throw new IllegalStateException("No matching integration found for Creative: " + creative.getId() + ", Extended Type: " + extendedCreativeType.getId() + ":'"
                    + extendedCreativeType.getName() + "', MediaType: " + mediaType + ", Templates: " + extendedTemplates);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("IntegrationType: " + integrationType.getId() + "/" + integrationType.getSystemName() + " chosen for ExtendedCreativeTemplate " + extendedTemplate.getId()
                    + " " + extendedTemplate.getContentForm() + " and MediaType: " + mediaType);
        }
        IntegrationTypeDto integrationDto = new IntegrationTypeDto();
        integrationDto.setId(integrationType.getId());
        integrationDto.setName(integrationType.getName());
        integrationDto.setSystemName(integrationType.getSystemName());
        // integrationDto.getBlockedExtendedCreativeTypes(); // DYNAMIC_INTEGRATION_TYPE_VENDOR_BLOCKING table
        integrationDto.getSupportedBeaconModes().addAll(integrationType.getSupportedBeaconModes());
        integrationDto.getSupportedFeatures().addAll(integrationType.getSupportedFeatures());
        integrationDto.addSupportedContentForm(mediaType, extendedTemplate.getContentForm());
        return integrationDto;
    }

    private ExtendedCreativeTemplate findExtendedTemplate(Set<ExtendedCreativeTemplate> extendedTemplates, ContentForm contentForm) {
        for (ExtendedCreativeTemplate extendedTemplate : extendedTemplates) {
            if (extendedTemplate.getContentForm() == contentForm) {
                return extendedTemplate;
            }
        }
        return null;
    }

    private IntegrationType findIntegrationType(Map<PublicationType, IntegrationType> integrationMap, MediaType mediaType, ContentForm contentForm) {
        for (Map.Entry<PublicationType, IntegrationType> entry : integrationMap.entrySet()) {
            //PublicationType publicationType = entry.getKey(); // We should set this into PublicationDto
            IntegrationType integrationType = entry.getValue();
            Set<ContentForm> contentForms = integrationType.getSupportedContentForms(mediaType);
            if (contentForms.contains(contentForm)) {
                return integrationType;
            }
        }
        return null;
    }

    private ExtendedCreativeTypeDto buildExtendedTypeDto(ExtendedCreativeType extendedCreativeType) {
        ExtendedCreativeTypeDto extendedDto = new ExtendedCreativeTypeDto();
        extendedDto.setId(extendedCreativeType.getId());
        extendedDto.setName(extendedCreativeType.getName());
        extendedDto.setMediaType(extendedCreativeType.getMediaType());
        extendedDto.setUseDynamicTemplates(extendedCreativeType.isUseDynamicTemplates());
        extendedDto.setClickRedirectRequired(extendedCreativeType.isClickRedirectRequired());
        for (Feature feature : extendedCreativeType.getFeatures()) {
            extendedDto.getFeatures().add(feature);
        }

        for (Entry<ContentForm, String> entry : extendedCreativeType.getTemplateMap().entrySet()) {
            extendedDto.getTemplateMap().put(entry.getKey(), entry.getValue());
        }
        return extendedDto;
    }

    private AdSpaceDto buildAdSpaceDto(Publisher publisher) {
        //adSpace.getPublication().getPublisher().getId();
        PublisherDto publisherDto = new PublisherDto();
        publisherDto.setId(publisher.getId());
        publisherDto.setExternalId(publisher.getExternalID());

        PublicationDto publicationDto = new PublicationDto();
        publicationDto.setId(0l);
        publicationDto.setExternalID(Constant.XAUDIT_PUBLICATION_EXTERNAL_ID);
        publicationDto.setPublisher(publisherDto);
        publicationDto.setPublicationTypeId(XAUDIT_PUBLICATION_TYPE);

        AdSpaceDto adSpaceDto = new AdSpaceDto();
        adSpaceDto.setId(0l);
        // We need to use this stupid AdSpace identifier because AppNexusRoutingController functionality depends on it
        adSpaceDto.setExternalID(AppNexusShared.ADSPACE_ID_MACRO);
        //adSpaceDto.setExternalID(Constant.XAUDIT_ADSPACE_EXTERNAL_ID);
        adSpaceDto.setPublication(publicationDto);

        return adSpaceDto;
    }

    /**
     * Usually Creative has only one AssetBundle and AssetBundles have only one Component
     * To make this more deterministic for exceptions with multiple values, the one with maximal width is returned 
     */
    public static CreativeAssetInfo getCreativeAssetInfo(Creative creative) {
        // Initialize DisplayType from Format. We actually can do similar thing with width/height
        DisplayType displayType = creative.getFormat().getDisplayType(0);
        Component component = creative.getFormat().getComponent(0);
        int width = 0;
        int height = 0;

        Map<DisplayType, AssetBundle> assetBundleMap = creative.getAssetBundleMap();
        for (Entry<DisplayType, AssetBundle> bundleEntry : assetBundleMap.entrySet()) {
            displayType = bundleEntry.getKey();
            AssetBundle assetBundle = bundleEntry.getValue();
            Map<Component, Asset> assetMap = assetBundle.getAssetMap();
            for (Entry<Component, Asset> assetEntry : assetMap.entrySet()) {
                component = assetEntry.getKey();
                String componentSystemName = component.getSystemName();
                // select "representative" Component for banner(image), video(video), text(adm)
                if (SystemName.COMPONENT_IMAGE.equals(componentSystemName) || SystemName.COMPONENT_VIDEO.equals(componentSystemName)
                        || SystemName.COMPONENT_ADM.equals(componentSystemName)) {
                    // ContentSpec for same DisplayType that AssetBundle has (important for banners xl/xxl/...)
                    ContentSpec contentSpec = component.getContentSpec(displayType);
                    Map<String, String> properties = contentSpec.getManifestProperties();
                    String swidth = properties.get(SystemName.CONTENT_SPEC_WIDTH);
                    String sheight = properties.get(SystemName.CONTENT_SPEC_HEIGHT);
                    if (swidth != null) {
                        int iwidth = Integer.parseInt(swidth);
                        if (iwidth > width) {
                            width = iwidth;
                            height = Integer.parseInt(sheight); //height just follows width
                        }
                    }

                }
            }
        }
        return new CreativeAssetInfo(width, height, component, displayType);
    }

    public static class CreativeAssetInfo {

        private final int width;
        private final int height;
        private final Component component;
        private final DisplayType displayType;

        public CreativeAssetInfo(int width, int height, Component component, DisplayType displayType) {
            this.width = width;
            this.height = height;
            this.component = component;
            this.displayType = displayType;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Component getComponent() {
            return component;
        }

        public DisplayType getDisplayType() {
            return displayType;
        }

        @Override
        public String toString() {
            return "CreativeAssetInfo [width=" + width + ", height=" + height + ", displayType=" + displayType + ", component=" + component + "]";
        }

    }

}
