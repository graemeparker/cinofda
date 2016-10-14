package com.adfonic.presentation.campaign.creative.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Asset_;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Component;
import com.adfonic.domain.Component_;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentSpec_;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Country_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeAttribute;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Destination;
import com.adfonic.domain.Destination_;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeTemplate_;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeType_;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.adfonic.domain.Segment;
import com.adfonic.dto.campaign.creative.AssetDto;
import com.adfonic.dto.campaign.creative.AssetInfoDto;
import com.adfonic.dto.campaign.creative.BeaconUrlDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.creative.ContentTypeDto;
import com.adfonic.dto.campaign.creative.CreativeAttributeDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.creative.DestinationDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTemplateDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.VastVideoCreative;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.VastWarning;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.Warning;
import com.adfonic.dto.campaign.creative.NativeAdInfoDto;
import com.adfonic.dto.campaign.enums.ContentSpecKeyEnum;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.campaign.enums.ExtendedCreativeDataEnum;
import com.adfonic.dto.format.ContentSpecDto;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.language.LanguageDto;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.presentation.exceptions.BigFileException;
import com.adfonic.presentation.exceptions.FileExtensionNotSupportedException;
import com.adfonic.presentation.exceptions.NotContentTypeException;
import com.adfonic.presentation.exceptions.SizeNotSupportedException;
import com.adfonic.presentation.format.FormatService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.adfonic.util.MediaUtils;
import com.adfonic.util.MediaUtils.ImageInfo;
import com.adfonic.util.VastWorker;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter.VisibilityEnum;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.creative.service.ExtendedCreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.vast2.VAST;
import com.byyd.vast2.VAST.Ad.InLine.Creatives.Creative.Linear.MediaFiles.MediaFile;
import com.byyd.vast2.VideoClicksType.ClickThrough;

@Service("creativeService")
public class CreativeServiceImpl extends GenericServiceImpl implements CreativeService {

    private static final String NO_HTTPS_FOUND_IN_MARKUP = "No https found in markup";

	private static final String DEFAULT_CLICK_TO_ACTION = "Install";

    @Autowired
    private CreativeManager creativeManager;

    @Autowired
    private ExtendedCreativeManager extendedCreativeManager;

    @Autowired
    private AssetManager assetManager;

    @Autowired
    private CampaignManager campaignManager;

    @Autowired
    private CommonManager commonManager;

    @Autowired
    private FormatService fService;

    @Autowired
    private org.dozer.Mapper mapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(CreativeServiceImpl.class);

    private FetchStrategy creativeFs = new FetchStrategyBuilder().addLeft(Creative_.format).addLeft(Creative_.destination).addLeft(Destination_.beaconUrls)
            .addLeft(Creative_.language).addLeft(Creative_.assetBundleMap).addLeft(Creative_.status).addLeft(Creative_.extendedCreativeType)
            .addLeft(Creative_.extendedCreativeTemplates).addLeft(AssetBundle_.assetMap).build();

    private FetchStrategy contentSpecFs = new FetchStrategyBuilder().addLeft(ContentSpec_.contentTypes).build();

    private FetchStrategy formatFs = new FetchStrategyBuilder().addLeft(Format_.displayTypes).build();

    private FetchStrategy assetFs = new FetchStrategyBuilder().addLeft(Asset_.contentType).addLeft(Asset_.creative).build();

    private FetchStrategy campaignFs = new FetchStrategyBuilder().addLeft(Campaign_.advertiser).addLeft(Campaign_.segments).addLeft(Campaign_.timePeriods)
            .addLeft(Campaign_.transparentNetworks).addLeft(Campaign_.defaultLanguage).addLeft(Campaign_.currentBid).addLeft(Campaign_.deviceIdentifierTypes)
            .addLeft(Advertiser_.company).addLeft(Country_.operators).addLeft(Country_.region).build();

    private FetchStrategy extendedCreativeFs = new FetchStrategyBuilder().addLeft(ExtendedCreativeType_.templateMap).addLeft(ExtendedCreativeTemplate_.contentForm).build();

    @Override
    public CreativeDto getCreativeById(Long id) {
        LOGGER.debug("getCreativeById-->");
        Creative creative = creativeManager.getCreativeById(id, creativeFs);
        CreativeDto creativeDto = mapper.map(creative, CreativeDto.class);
        creativeDto = loadCreativeInfo(creative, creativeDto);
        LOGGER.debug("getCreativeById<--");
        return creativeDto;
    }

    private CreativeDto loadCreativeInfo(Creative creative, CreativeDto dto) {
        //load assets
        Map<Long, AssetDto> assets = loadAssets(dto);
        dto.setAssets(assets);

        //ContentSpec
        dto.setContentSpecs(new ArrayList<ContentSpecDto>());
        Iterator<Long> it = assets.keySet().iterator();
        while (it.hasNext()) {
            ContentSpec cs = commonManager.getContentSpecById(it.next());
            ContentSpecDto contentSpecDto = mapper.map(cs, ContentSpecDto.class);
            dto.getContentSpecs().add(contentSpecDto);
        }

        //Third party
        if (dto.isThirdPartyTag()) {
            List<ExtendedCreativeTemplateDto> templates = getTemplates(dto.getExtendedCreativeType());
            for (ExtendedCreativeTemplateDto ectemp : templates) {
                for (ExtendedCreativeTemplateDto creativeTemplate : dto.getExtendedCreativeTemplates()) {
                    if (ectemp.getContentForm().equals(creativeTemplate.getContentForm())) {
                        ectemp.setTemplateOriginal(creativeTemplate.getTemplateOriginal());
                        ectemp.setId(creativeTemplate.getId());
                        ectemp.setTemplatePreprocessed(creativeTemplate.getTemplatePreprocessed());
                    }
                }
            }
            dto.getExtendedCreativeTemplates().clear();
            dto.getExtendedCreativeTemplates().addAll(templates);

            //IsRichMedia property
            Set<Feature> features = creative.getExtendedCreativeType().getFeatures();
            if (features != null) {
                dto.setRichMedia(features.contains(Feature.RICH_MEDIA));
            }
        }

        if (isNativeAd(creative)) {
            dto.setNativeAd(true);
            dto.setNativeAdInfo(loadNativeAdInfo(dto));
        }

        DestinationDto destination = dto.getDestination();
        if (destination != null) {
            destination.setBeaconUrls(removeRepeatedBeacons(destination.getBeaconUrls()));
        } else {
            // MAX-2681 - explicitly set empty destination to be able to set on the UI after copy to all destination bug
            dto.setDestination(new DestinationDto());
        }

        return dto;
    }

    private NativeAdInfoDto loadNativeAdInfo(CreativeDto dto) {
        NativeAdInfoDto nativeAdDto = new NativeAdInfoDto();
        nativeAdDto.setTitle(getExtendedData(dto, ExtendedCreativeDataEnum.NA_TITLE));
        nativeAdDto.setDescription(getExtendedData(dto, ExtendedCreativeDataEnum.NA_DESCRIPTION));
        nativeAdDto.setClickToAction(getExtendedData(dto, ExtendedCreativeDataEnum.NA_CLICK_TO_ACTION));
        byte[] dataIcon = getAssetBytes(dto, ContentSpecKeyEnum.CS_NATIVE_AD_ICON);
        if (dataIcon != null) {
            AssetInfoDto iconAsset = new AssetInfoDto(dataIcon, getAssetContentType(dto, ContentSpecKeyEnum.CS_NATIVE_AD_ICON));
            nativeAdDto.setIcon(iconAsset);
        }
        byte[] dataImage = getAssetBytes(dto, ContentSpecKeyEnum.CS_NATIVE_AD_IMAGE);
        if (dataImage != null) {
            AssetInfoDto imageAsset = new AssetInfoDto(dataImage, getAssetContentType(dto, ContentSpecKeyEnum.CS_NATIVE_AD_IMAGE));
            nativeAdDto.setImage(imageAsset);
        }
        return nativeAdDto;
    }

    private String getExtendedData(CreativeDto dto, ExtendedCreativeDataEnum extendedCreativeData) {
        String value = "";
        if (MapUtils.isNotEmpty(dto.getExtendedData())) {
            value = dto.getExtendedData().get(extendedCreativeData.key());
        }
        return value;
    }

    private byte[] getAssetBytes(CreativeDto dto, ContentSpecKeyEnum contentSpecKeyEnum) {
        ContentSpecDto cs = dto.getContentSpec(contentSpecKeyEnum.key());
        if (cs != null) {
            Map<Long, AssetDto> assets = dto.getAssets();
            if (!assets.isEmpty() && assets.containsKey(cs.getId())) {
                return assets.get(cs.getId()).getData();
            }
        }
        return null;
    }

    private ContentTypeDto getAssetContentType(CreativeDto dto, ContentSpecKeyEnum contentSpecKeyEnum) {
        ContentSpecDto cs = dto.getContentSpec(contentSpecKeyEnum.key());
        if (cs != null) {
            Map<Long, AssetDto> assets = dto.getAssets();
            if (!assets.isEmpty() && assets.containsKey(cs.getId())) {
                return assets.get(cs.getId()).getContentType();
            }
        }
        return new ContentTypeDto();
    }

    private List<BeaconUrlDto> removeRepeatedBeacons(List<BeaconUrlDto> beacons) {
        List<String> urls = new ArrayList<String>();
        List<BeaconUrlDto> result = new ArrayList<BeaconUrlDto>();
        if (CollectionUtils.isNotEmpty(beacons)) {
            for (BeaconUrlDto beacon : beacons) {
                if (!urls.contains(beacon.getUrl())) {
                    result.add(beacon);
                    urls.add(beacon.getUrl());
                }
            }
        }
        return result;
    }

    @Override
    public CreativeDto newImageForCreative(CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException,
            NotContentTypeException, IOException, SizeNotSupportedException, BigFileException {
        LOGGER.debug("newImageForCreative-->");
        if (creativeDto == null) {
            creativeDto = new CreativeDto();
            creativeDto.setDestination(new DestinationDto());
            creativeDto.setLanguage(new LanguageDto());
            creativeDto.setExtendedCreativeType(null);
        }

        ContentTypeDto cont = getContentTypeForMime(ct, false);
        if (cont == null || "Text".equals(cont.getName())) {
            LOGGER.debug("newImageForCreative<-- File Extension not supported");
            throw new FileExtensionNotSupportedException();
        }

        ImageInfo imageInfo = null;

        imageInfo = MediaUtils.getImageInfo(is);

        //Get content spec and content type from the image
        ContentSpecDto contentSpec = getContentSpec(cont, imageInfo.getWidth(), imageInfo.getHeight());
        if (contentSpec == null) {
            throw new SizeNotSupportedException();
        }

        ContentTypeDto contentType = getContentTypeForMime(ct, imageInfo.isAnimated());

        //Contentspec must contain the content type
        if ((contentSpec != null && contentType != null) && (!contentSpec.getContentTypes().contains(contentType))) {
            LOGGER.debug("newImageForCreative<-- File Extension not supported");
            throw new NotContentTypeException();
        }

        if (!isValidSize(contentSpec, size)) {
            throw new BigFileException();
        }

        creativeDto.setContentSpecs(new ArrayList<ContentSpecDto>());
        creativeDto.getContentSpecs().add(contentSpec);
        creativeDto.setFormat(fService.getFormat(imageInfo.getWidth(), imageInfo.getHeight()));
        if (creativeDto.getFormat() == null) {
            throw new SizeNotSupportedException();
        }

        //Create the new asset
        AssetDto asset = new AssetDto();
        asset.setContentType(contentType);
        asset.setData(data);

        creativeDto.setAssets(new HashMap<Long, AssetDto>());
        creativeDto.getAssets().put(contentSpec.getId(), asset);
        LOGGER.debug("newImageForCreative<--");
        return creativeDto;
    }

    @Override
    @Transactional(readOnly = false)
    public CreativeDto newCreativeVast() {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setDestination(new DestinationDto());
        creativeDto.setLanguage(new LanguageDto());
        creativeDto.setAssets(null);
        creativeDto.setClosedMode(true);
        creativeDto.setAllowExternalAudit(false);

        for (ExtendedCreativeTypeDto extendedType : getExtendedCreativeTypes(null, true)) {
            //better find by MEDIA_TYPE 
            if (extendedType.getName().toLowerCase().startsWith("vast")) {
                creativeDto.setExtendedCreativeType(extendedType);
            }
        }
        if (creativeDto.getExtendedCreativeType() == null) {
            throw new IllegalStateException("VAST ExtendedCreativeType not found");
        }
        //prepare template for VAST xml
        ExtendedCreativeTemplateDto template = new ExtendedCreativeTemplateDto();
        template.setContentForm(ContentForm.VAST_2_0);
        template.setTemplateOriginal("");
        creativeDto.setExtendedCreativeTemplates(Arrays.asList(template));
        return creativeDto;
    }

    /**
     * Parse VAST and derive Creative's 
     * - Format and ContentType
     * - Destination web link
     */
    @Override
    public MobileAdVastMetadataDto processVastTag(CreativeDto creativeDto) {
        ExtendedCreativeTemplateDto template = creativeDto.getExtendedCreativeTemplates().get(0);
        String templateOriginal = template.getTemplateOriginal();
        return processVastTag(templateOriginal);
    }

    @Override
    public MobileAdVastMetadataDto processVastTag(String templateOriginal) {
        MobileAdVastMetadataDto vastMetaData = null;
        if (StringUtils.isNotEmpty(templateOriginal)) {

            //catch most silly mistakes here before doing real parsing where generated messages are not so nice 
            if (templateOriginal.length() < 10 || (!templateOriginal.startsWith("<VAST") && !templateOriginal.startsWith("<?xml"))) {
                vastMetaData = new MobileAdVastMetadataDto();
                vastMetaData.addWarning(VastWarning.BROKEN_FILE, "Not a VAST");
                return vastMetaData;
            }

            try {
                VAST vast = VastWorker.instance().read(new StringReader(templateOriginal));
                MobileAdVastAnalyser analyser = new MobileAdVastAnalyser();
                VastWorker.instance().visit(vast, null, analyser);
                vastMetaData = analyser.getMetaData();
                //To save some space (our DB column is limited) we can try to remove things that inflate VAST and are not playable
                //like AdParameters and Extensions 
                //vast.getAd().get(0).getInLine().getCreatives().getCreative().get(0).getLinear().setAdParameters(null);
                //vast.getAd().get(0).getInLine().getExtensions().getExtension().clear();
            } catch (com.adfonic.util.VastWorker.VastParsingException vpx) {
                LOGGER.debug("VAST parsing failed " + vpx);
                vastMetaData = new MobileAdVastMetadataDto();
                if (vpx.getFormatErrors() != null) {
                    List<ValidationEvent> formatErrors = vpx.getFormatErrors();
                    for (ValidationEvent validationEvent : formatErrors) {
                        vastMetaData.addWarning(VastWarning.BROKEN_FILE, validationEvent.getMessage());
                    }
                } else {
                    vastMetaData.addWarning(VastWarning.BROKEN_FILE, vpx.getMessage());
                }
                return vastMetaData;
            }

            //DB column is actually 10000, but we still need to add tracker/beacon
            if (templateOriginal.length() > 29000) {
                vastMetaData = new MobileAdVastMetadataDto();
                vastMetaData.addWarning(VastWarning.BROKEN_FILE, "VAST is too big (" + templateOriginal.length() + "). Limit is 29000 characters");
                return vastMetaData;
            }

            VastVideoCreative videoCreative = vastMetaData.getVideoCreative();
            if (videoCreative == null) {
                vastMetaData.addWarning(VastWarning.MISSING_MEDIA_FILES, "No Linear Video Creative in VAST was found");
            } else {

                boolean supportedFound = false;
                List<MobileAdVastMetadataDto.Warning> warnings = new ArrayList<MobileAdVastMetadataDto.Warning>();

                //Check all mediafiles if there is a at least one with supported mimeType and dimensions
                for (MediaFile mediaFile : videoCreative.getMediaFiles()) {
                    int mWidth = mediaFile.getWidth().intValue();
                    int mHeight = mediaFile.getHeight().intValue();

                    // Checking content type
                    String mimeType = mediaFile.getType();
                    ContentTypeDto contentType = getContentTypeForMime(mimeType, false);
                    if (contentType == null) {
                        warnings.add(new Warning(VastWarning.CONTENT_TYPE_NOT_FOUND, String.valueOf(mimeType)));
                    }

                    // Checking supported sizes
                    FormatDto format = fService.getVideoFormat(mWidth, mHeight);
                    if (format == null) {
                        warnings.add(new Warning(VastWarning.UNSUPPORTED_FORMAT, String.valueOf(mWidth), String.valueOf(mHeight), String.valueOf(mimeType)));
                    }

                    // Checking existing content spec
                    ContentSpecDto contentSpec = getVideoContentSpec(contentType, mWidth, mHeight);
                    if (contentSpec == null) {
                        warnings.add(new Warning(VastWarning.MEDIAFILE_SIZE_NOT_ALLOWED, String.valueOf(mWidth), String.valueOf(mHeight)));
                    }

                    LOGGER.debug("MediaFile " + mediaFile.getType() + " " + mWidth + "x" + mHeight + " -> format: " + format + ", contentType: " + contentType + ", contentSpec: "
                            + contentSpec);

                    if (contentType != null && format != null && contentSpec != null) {
                        supportedFound = true;
                    }
                }

                if (!supportedFound) {
                    //if no supported mediafile found -> print all collected warnings
                    for (Warning warning : warnings) {
                        vastMetaData.addWarning(warning.getType(), warning.getValuesAsArray());
                    }

                }
            }

            LOGGER.debug("VAST parsed. Warnings: " + vastMetaData.getWarnings().size());
        }

        return vastMetaData;
    }

    @Override
    public CreativeDto newTextCreative(String data) {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setDestination(new DestinationDto());
        //Mapper mapper = new DozerBeanMapper();
        creativeDto.setLanguage(mapper.map(commonManager.getLanguageByName("English"), LanguageDto.class));

        ContentSpec contentSpec = commonManager.getContentSpecByName("MMA Text Link", contentSpecFs);
        ContentSpecDto cs = mapper.map(contentSpec, ContentSpecDto.class);
        ContentType contentType = commonManager.getContentTypeByName("Text");
        ContentTypeDto ct = getObjectDto(ContentTypeDto.class, contentType);

        creativeDto.setContentSpecs(new ArrayList<ContentSpecDto>());
        creativeDto.getContentSpecs().add(cs);
        creativeDto.setFormat(fService.getFormatBySystemName("text"));
        creativeDto.setLanguage(new LanguageDto());

        creativeDto.setExtendedCreativeType(null);

        //Create the new asset
        AssetDto asset = new AssetDto();
        asset.setContentType(ct);
        asset.setData(data.getBytes());

        creativeDto.setAssets(new HashMap<Long, AssetDto>());
        creativeDto.getAssets().put(cs.getId(), asset);
        return creativeDto;
    }

    @Override
    public CreativeDto newNativeCreative(String title, String description) {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setDestination(new DestinationDto());
        creativeDto.setNativeAd(true);

        creativeDto.setLanguage(mapper.map(commonManager.getLanguageByName("English"), LanguageDto.class));
        creativeDto.setContentSpecs(new ArrayList<ContentSpecDto>());
        creativeDto.setAssets(new HashMap<Long, AssetDto>());

        creativeDto.setFormat(fService.getFormatBySystemName("native_app_install"));
        creativeDto.setLanguage(new LanguageDto());

        creativeDto.setExtendedCreativeType(null);
        creativeDto.setNativeAdInfo(new NativeAdInfoDto());

        //Icon
        ContentSpec contentSpec = commonManager.getContentSpecByName(ContentSpecKeyEnum.CS_NATIVE_AD_ICON.key(), contentSpecFs);
        ContentSpecDto cs = mapper.map(contentSpec, ContentSpecDto.class);
        creativeDto.getContentSpecs().add(cs);

        //Image
        contentSpec = commonManager.getContentSpecByName(ContentSpecKeyEnum.CS_NATIVE_AD_IMAGE.key(), contentSpecFs);
        cs = mapper.map(contentSpec, ContentSpecDto.class);
        creativeDto.getContentSpecs().add(cs);

        return creativeDto;
    }

    @Override
    public CreativeDto uploadIconForNativeAd(CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException,
            NotContentTypeException, IOException, SizeNotSupportedException, BigFileException {
        LOGGER.debug("uploadIconForNativeAd-->");
        ContentSpecDto cs = getContentSpecByName(ContentSpecKeyEnum.CS_NATIVE_AD_ICON.key());
        ContentTypeDto cont = validateImageForCreative(creativeDto, is, ct, size, cs);
        creativeDto.getNativeAdInfo().setIcon(new AssetInfoDto(data, cont));
        LOGGER.debug("uploadIconForNativeAd<--");
        return creativeDto;
    }

    @Override
    public CreativeDto uploadImageForNativeAd(CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException,
            NotContentTypeException, IOException, SizeNotSupportedException, BigFileException {
        LOGGER.debug("uploadImageForNativeAd-->");
        ContentSpecDto cs = getContentSpecByName(ContentSpecKeyEnum.CS_NATIVE_AD_IMAGE.key());
        ContentTypeDto cont = validateImageForCreative(creativeDto, is, ct, size, cs);
        creativeDto.getNativeAdInfo().setImage(new AssetInfoDto(data, cont));
        LOGGER.debug("uploadImageForNativeAd<--");
        return creativeDto;
    }

    private ContentTypeDto validateImageForCreative(CreativeDto creativeDto, InputStream is, String ct, long size, ContentSpecDto cs) throws FileExtensionNotSupportedException,
            NotContentTypeException, IOException, SizeNotSupportedException, BigFileException {
        ContentTypeDto cont = getContentTypeForMime(ct, false);
        if (cont == null || "Text".equals(cont.getName())) {
            LOGGER.debug("newCreative<-- File Extension not supported");
            throw new FileExtensionNotSupportedException();
        }

        ImageInfo imageInfo = null;

        imageInfo = MediaUtils.getImageInfo(is);

        //Get content spec and content type from the image
        ContentSpecDto contentSpec = getContentSpec(cont, imageInfo.getWidth(), imageInfo.getHeight());
        if (contentSpec == null) {
            throw new SizeNotSupportedException();
        }

        //check the image is the correct required
        if (!contentSpec.getId().equals(cs.getId())) {
            LOGGER.debug("newCreative<-- Tried to upload different image than accepted");
            throw new SizeNotSupportedException();
        }
        ContentTypeDto contentType = getContentTypeForMime(ct, imageInfo.isAnimated());

        //Contentspec must contain the content type
        if ((contentSpec != null && contentType != null) && (!contentSpec.getContentTypes().contains(contentType))) {
            LOGGER.debug("newCreative<-- File Extension not supported");
            throw new NotContentTypeException();
        }

        if (!isValidSize(contentSpec, size)) {
            throw new BigFileException();
        }

        if (creativeDto.getFormat() == null) {
            throw new SizeNotSupportedException();
        }

        return contentType;
    }

    @Override
    public CreativeDto newCreative3rdParty(String data, boolean isRichMedia) {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setDestination(new DestinationDto());
        creativeDto.setRichMedia(isRichMedia);
        creativeDto.setLanguage(new LanguageDto());
        creativeDto.setExtendedCreativeType(new ExtendedCreativeTypeDto());
        creativeDto.setAssets(null);
        creativeDto.setClosedMode(true);
        creativeDto.setAllowExternalAudit(false);
        return creativeDto;
    }

    @Override
    public List<ContentSpecDto> getAllContentSpecs() {
        List<ContentSpecDto> contentSpecs = new ArrayList<ContentSpecDto>();

        for (ContentSpec cs : commonManager.getAllContentSpecs(contentSpecFs)) {
            ContentSpecDto dto = mapper.map(cs, ContentSpecDto.class);
            contentSpecs.add(dto);
        }

        return contentSpecs;
    }

    @Override
    public ContentSpecDto getContentSpecByName(String name) {
        ContentSpec contentSpec = commonManager.getContentSpecByName(name, contentSpecFs);
        return mapper.map(contentSpec, ContentSpecDto.class);
    }

    @Override
    public List<ContentSpecDto> getContentSpecsForImages() {
        List<ContentSpecDto> contentSpecs = new ArrayList<ContentSpecDto>();

        //Only cotent specs with with and height in the manifest
        for (ContentSpecDto cs : getAllContentSpecs()) {
            if (cs.getManifest().contains("width") && !"Icon for X-Large Text".equalsIgnoreCase(cs.getName())) {
                contentSpecs.add(cs);
            }
        }

        return contentSpecs;
    }

    @Override
    public ContentTypeDto getContentTypeForMime(String mimeType, boolean animated) {
        ContentType ct = commonManager.getContentTypeForMimeType(mimeType, animated);
        return getObjectDto(ContentTypeDto.class, ct);
    }

    @Override
    public CreativeDto save(CreativeDto dto, CampaignCreativeDto campaignDto, boolean isReApprovalNeeded) {

        // Update Creative
        Map<Long, AssetDto> assets = dto.getAssets();
        Format f = commonManager.getFormatById(dto.getFormat().getId(), formatFs);
        List<ContentSpecDto> contentSpecs = dto.getContentSpecs();
        boolean isRichmedia = dto.isRichMedia();

        Campaign campaign = mapper.map(campaignDto, Campaign.class);

        campaign = campaignManager.getCampaignById(campaignDto.getId(), campaignFs);

        Creative creative = null;
        if (dto.getId() != null && dto.getId().longValue() > 0) {
            // already persisted
            creative = creativeManager.getCreativeById(dto.getId(), creativeFs);

            // Saving native-ad assets information
            saveNativeAdInfo(dto);

            // Updating Assets            
            creative = deleteAssetBundles(creative);
            Map<Long, AssetDto> resultAssets = new HashMap<Long, AssetDto>();
            for (ContentSpecDto cs : contentSpecs) {
                AssetDto assetDto = saveAsset(assets.get(cs.getId()), creative);
                resultAssets.put(cs.getId(), assetDto);
            }
            createAssetBundles(creative, f, contentSpecs, resultAssets);

            // Update creative properties
            creative.setLanguage(commonManager.getLanguageById(dto.getLanguage().getId()));
            creative.setEnglishTranslation(dto.getEnglishTranslation());
            creative.setName(dto.getName());
            creative.setStatus(dto.getStatus());
            creative.setLastUpdated(new Date());
            creative.setFormat(f);

            // Save destination
            DestinationDto destination = dto.getDestination();
            if (destination != null && destination.getDestinationType() != null) {
                creative.setDestination(saveDestination(destination, campaign.getAdvertiser()));
            }

            // SSL Compliance
            creative.setSslCompliant(BooleanUtils.toBoolean(dto.isSslCompliant()));
            creative.setSslOverride(BooleanUtils.toBoolean(dto.getSslOverride()));

            // Creative attributes
            creative.getCreativeAttributes().clear();
            for (CreativeAttributeDto attrib : dto.getCreativeAttributes()) {
                CreativeAttribute at = creativeManager.getCreativeAttributeById(attrib.getId());
                creative.getCreativeAttributes().add(at);
            }

            // Saving native-ad extended data
            Map<String, String> extendedData = creative.getExtendedData();
            extendedData.putAll(dto.getExtendedData());

            creative = creativeManager.update(creative);
            creative = creativeManager.getCreativeById(dto.getId(), creativeFs);

            if (dto.isThirdPartyTag()) {
                creative = saveThirdParty(dto, isReApprovalNeeded);
            }

            creative.setFormat(f);

            int status = dto.getState();
            String clas = dto.getHiddenClass();
            List<ExtendedCreativeTemplateDto> templates = new ArrayList<ExtendedCreativeTemplateDto>();
            if (dto.isThirdPartyTag()) {
                templates.addAll(dto.getExtendedCreativeTemplates());
            }
            dto = mapper.map(creative, CreativeDto.class);
            dto.setAssets(resultAssets);
            dto.setContentSpecs(contentSpecs);
            dto.setState(status);
            dto.setHiddenClass(clas);
            dto.setRichMedia(isRichmedia);
            dto.setExtendedCreativeTemplates(templates);
            if (destination != null) {
                destination.setBeaconUrls(removeRepeatedBeacons(destination.getBeaconUrls()));
                dto.setDestination(destination);
            }

        } else {
            creative = mapper.map(dto, Creative.class);

            Segment segment = campaign.getSegments().get(0);

            creative = creativeManager.newCreative(campaign, segment, f, dto.getName(), creativeFs);

            // Save destination
            DestinationDto destination = dto.getDestination();
            if (destination != null && destination.getDestinationType() != null) {
                creative.setDestination(saveDestination(destination, campaign.getAdvertiser()));
            }
            
            // SSL Compliance
            creative.setSslCompliant(BooleanUtils.toBoolean(dto.isSslCompliant()));
            creative.setSslOverride(BooleanUtils.toBoolean(dto.getSslOverride()));

            creative.setLanguage(commonManager.getLanguageById(dto.getLanguage().getId()));
            creative.setEnglishTranslation(dto.getEnglishTranslation());
            creative.setStatus(dto.getStatus());
            if (dto.isThirdPartyTag()) {
                creative.setClosedMode(true);
                creative.setAllowExternalAudit(false);
            }

            creative.getCreativeAttributes().clear();

            for (CreativeAttributeDto attrib : dto.getCreativeAttributes()) {
                CreativeAttribute at = creativeManager.getCreativeAttributeById(attrib.getId());
                creative.getCreativeAttributes().add(at);
            }

            creative = creativeManager.update(creative);

            creative = creativeManager.getCreativeById(creative.getId(), creativeFs);

            // Saving native-ad assets information
            saveNativeAdInfo(dto);

            Map<Long, AssetDto> resultAssets = new HashMap<Long, AssetDto>();
            for (ContentSpecDto cs : contentSpecs) {
                AssetDto assetDto = saveAsset(assets.get(cs.getId()), creative);
                resultAssets.put(cs.getId(), assetDto);
            }
            createAssetBundles(creative, f, contentSpecs, resultAssets);

            // Saving native-ad extended data
            Map<String, String> extendedData = creative.getExtendedData();
            extendedData.putAll(dto.getExtendedData());

            // transation problems here if we convert entity with mapper.
            creative = creativeManager.update(creative);

            creative = creativeManager.getCreativeById(creative.getId(), creativeFs);

            dto.setId(creative.getId());

            if (dto.isThirdPartyTag()) {
                creative = saveThirdParty(dto, isReApprovalNeeded);
            }

            int status = dto.getState();
            String clas = dto.getHiddenClass();
            List<ExtendedCreativeTemplateDto> templates = new ArrayList<ExtendedCreativeTemplateDto>();
            if (dto.isThirdPartyTag()) {
                templates.addAll(dto.getExtendedCreativeTemplates());
            }
            dto = mapper.map(creative, CreativeDto.class);
            dto.setAssets(resultAssets);
            dto.setContentSpecs(contentSpecs);
            dto.setState(status);
            dto.setHiddenClass(clas);
            dto.setRichMedia(isRichmedia);
            dto.setExtendedCreativeTemplates(templates);
            if (destination != null) {
                destination.setBeaconUrls(removeRepeatedBeacons(destination.getBeaconUrls()));
            }
        }

        if (isNativeAd(creative)) {
            dto.setNativeAd(true);
            dto.setNativeAdInfo(loadNativeAdInfo(dto));
        }

        return dto;

    }

    private void saveNativeAdInfo(CreativeDto dto) {
        if (dto.isNativeAd()) {
            NativeAdInfoDto nativeInfoDto = dto.getNativeAdInfo();
            if (dto.getNativeAdInfo() != null) {
                Map<String, String> extendedData = dto.getExtendedData();

                // Setting title
                extendedData.put(ExtendedCreativeDataEnum.NA_TITLE.key(), nativeInfoDto.getTitle());

                // Setting description
                extendedData.put(ExtendedCreativeDataEnum.NA_DESCRIPTION.key(), nativeInfoDto.getDescription());

                // Setting click_to_action
                String naClickToAction = nativeInfoDto.getClickToAction();
                if (StringUtils.isEmpty(naClickToAction)) {
                    naClickToAction = DEFAULT_CLICK_TO_ACTION;
                }
                extendedData.put(ExtendedCreativeDataEnum.NA_CLICK_TO_ACTION.key(), naClickToAction);

                // Setting icon
                if (nativeInfoDto.getIcon() != null) {
                    ContentSpecDto csIcon = dto.getContentSpec(ContentSpecKeyEnum.CS_NATIVE_AD_ICON.key());
                    if (csIcon != null) {
                        // Setting Asset information
                        setAssetInformation(dto, nativeInfoDto.getIcon().getData(), nativeInfoDto.getIcon().getContentType(), csIcon);
                    }
                }

                // Setting image
                if (nativeInfoDto.getImage() != null) {
                    ContentSpecDto csImage = dto.getContentSpec(ContentSpecKeyEnum.CS_NATIVE_AD_IMAGE.key());
                    if (csImage != null) {
                        // Setting Asset information
                        setAssetInformation(dto, nativeInfoDto.getImage().getData(), nativeInfoDto.getImage().getContentType(), csImage);
                    }
                }
            }
        }
    }

    private MobileAdVastMetadataDto parseVastMetadata(CreativeDto creativeDto) {
        MobileAdVastMetadataDto vastMetaData = null;

        vastMetaData = processVastTag(creativeDto);
        if (vastMetaData != null && vastMetaData.getWarnings().isEmpty()) {
            VastVideoCreative videoCreative = vastMetaData.getVideoCreative();

            creativeDto.getExtendedData().put(ExtendedCreativeDataEnum.VAST_DURATION.key(), String.valueOf(videoCreative.getDuration()));

            ClickThrough clickThrough = videoCreative.getClickThrough();
            if (clickThrough != null) {
                DestinationDto destinationDto = creativeDto.getDestination();
                destinationDto.setDestinationType(DestinationType.URL);
                destinationDto.setData(clickThrough.getValue().trim());
            }

            //creating asset for each media files contained in VAST tag
            for (MediaFile mediaFile : videoCreative.getMediaFiles()) {
                int mWidth = mediaFile.getWidth().intValue();
                int mHeight = mediaFile.getHeight().intValue();

                // Getting format, content-spec and content-type. 
                // Do NOT need to check possible NULL values because we already did parsing the VAST tag
                FormatDto format = fService.getVideoFormat(mWidth, mHeight);
                ContentTypeDto contentType = getContentTypeForMime(mediaFile.getType(), false);
                ContentSpecDto contentSpec = getVideoContentSpec(contentType, mWidth, mHeight);
                if (format == null || contentType == null || contentSpec == null) {
                    //Ignore incompatible mediafiles 
                    continue;
                }

                creativeDto.setContentSpecs(Arrays.asList(contentSpec));
                creativeDto.setFormat(format);

                AssetDto asset = new AssetDto();
                asset.setContentType(contentType);
                asset.setData(null); //no real data for vast videos

                if (creativeDto.getAssets() == null) {
                    creativeDto.setAssets(new HashMap<Long, AssetDto>());
                }
                creativeDto.getAssets().put(contentSpec.getId(), asset);
            }
            if (creativeDto.getAssets().isEmpty()) {
                throw new IllegalStateException("No Creative Assets created from VAST");
            }
        }
        return vastMetaData;
    }

    private AssetDto setAssetInformation(CreativeDto dto, byte[] data, ContentTypeDto ct, ContentSpecDto cs) {
        Map<Long, AssetDto> assets = dto.getAssets();
        if (assets == null) {
            assets = new HashMap<Long, AssetDto>();
            assets.put(cs.getId(), new AssetDto());
        } else if (assets.containsKey(cs.getId())) {
            if (assets.get(cs.getId()) == null) {
                assets.put(cs.getId(), new AssetDto());
            }
        } else {
            assets.put(cs.getId(), new AssetDto());
        }
        AssetDto imageAsset = assets.get(cs.getId());
        imageAsset.setData(data);
        imageAsset.setContentType(ct);
        return imageAsset;
    }

    private boolean isNativeAd(Creative creative) {
        return "native_app_install".equals(creative.getFormat().getSystemName());
    }

    private Creative saveThirdParty(CreativeDto dto, boolean isReApprovalNeeded) {
        Creative creative = creativeManager.getCreativeById(dto.getId(), creativeFs);

        // First set the extended creative type
        ExtendedCreativeType exType = extendedCreativeManager.getExtendedCreativeTypeById(dto.getExtendedCreativeType().getId());
        creative.setExtendedCreativeType(exType);

        // Set closed mode to true for third party tags in case of re-approval.
        if (isReApprovalNeeded) {
            creative.setClosedMode(true);
        }
        creative.setAllowExternalAudit(false);
        creative = creativeManager.update(creative);
        creative = creativeManager.getCreativeById(dto.getId(), creativeFs);

        // Delete old extended creative templates (after update due to MAX-2934) 
        for (ExtendedCreativeTemplate temp : creative.getExtendedCreativeTemplates()) {
            extendedCreativeManager.delete(temp);
        }
        creative.getExtendedCreativeTemplates().clear();

        // Then the new extended creative templates
        if (dto.getExtendedCreativeTemplates() != null) {
            for (ExtendedCreativeTemplateDto ect : dto.getExtendedCreativeTemplates()) {
                if (!StringUtils.isEmpty(ect.getTemplateOriginal())) {
                    ExtendedCreativeTemplate template = extendedCreativeManager.newExtendedCreativeTemplate(creative, ect.getContentForm(), ect.getTemplateOriginal());
                    creative.getExtendedCreativeTemplates().add(template);
                    ect.setId(template.getId());
                }
            }
        }

        return creative;
    }

    private Destination saveDestination(DestinationDto dto, Advertiser advertiser) {
        List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
        if (!CollectionUtils.isEmpty(dto.getBeaconUrls())) {
            for (BeaconUrlDto beacon : removeRepeatedBeacons(dto.getBeaconUrls())) {
                //Only save filled beacons
                if (StringUtils.isNotEmpty(beacon.getUrl())) {
                    beacons.add(new BeaconUrl(beacon.getUrl()));
                }
            }
        }
        // Look for a duplicate destination, but if it doesn't exist create it.
        return creativeManager.getDestinationForAdvertiserAndDestinationTypeAndData(advertiser, dto.getDestinationType().getDestinationType(), dto.getData(), true, beacons,
                dto.isDataIsFinalDestination(), dto.getFinalDestination());
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCreative(CreativeDto dto) {
        Creative c = creativeManager.getCreativeById(dto.getId());
        c.getAssetBundleMap();
        Iterator<Entry<DisplayType, AssetBundle>> it = c.getAssetBundleMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DisplayType, AssetBundle> pair = it.next();
            pair.getValue().getAssetMap().clear();
            assetManager.update(pair.getValue());
        }

        c.getAssetBundleMap().clear();
        c = creativeManager.update(c);

        for (ContentSpecDto cs : dto.getContentSpecs()) {
            if (dto.getAssets().containsKey(dto.getAssets().get(cs.getId()))) {
                deleteAsset(cs.getId());
            }
        }

        creativeManager.delete(c);
    }

    private void deleteAsset(long assetId) {
        Asset asset = assetManager.getAssetById(assetId);
        assetManager.delete(asset);
    }

    @Override
    public CreativeDto copyCreative(CreativeDto oldCreative, CampaignCreativeDto campaignDto) {
        CreativeDto newCreative = new CreativeDto();

        //Assets
        newCreative.setAssets(new HashMap<Long, AssetDto>());
        Iterator<Entry<Long, AssetDto>> it = oldCreative.getAssets().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, AssetDto> pair = it.next();
            AssetDto asset = new AssetDto();
            asset.setContentType(pair.getValue().getContentType());
            asset.setData(pair.getValue().getData());
            newCreative.getAssets().put(pair.getKey(), pair.getValue());
        }

        //ContentSpec
        newCreative.setContentSpecs(oldCreative.getContentSpecs());
        //Destination
        DestinationDto dest = oldCreative.getDestination();
        dest.setId(null);
        newCreative.setDestination(dest);

        newCreative.setLanguage(oldCreative.getLanguage());
        newCreative.setFormat(oldCreative.getFormat());
        newCreative.setEnglishTranslation(oldCreative.getEnglishTranslation());
        newCreative.setName(oldCreative.getName());
        newCreative.setCreativeAttributes(new ArrayList<CreativeAttributeDto>());
        if (CollectionUtils.isNotEmpty(oldCreative.getCreativeAttributes())) {
            newCreative.getCreativeAttributes().addAll(oldCreative.getCreativeAttributes());
        }

        return save(newCreative, campaignDto, true);
    }

    @Override
    public Creative getCreativeForSubmission(Creative creative) {
        // Hydrate a local creative for JTrac processing
        FetchStrategy fs = new FetchStrategyBuilder().addInner(Creative_.campaign).addInner(Campaign_.advertiser).addInner(Advertiser_.company).addInner(Company_.accountManager)
                .addInner(Creative_.segment).addInner(Creative_.destination).build();
        return creativeManager.getCreativeById(creative.getId(), fs);
    }

    @Override
    @Transactional(readOnly = false)
    public CreativeDto submitCreative(CreativeDto creative, boolean directApproval) {
        FetchStrategy fs = new FetchStrategyBuilder().addInner(Creative_.campaign).addInner(Campaign_.advertiser).addInner(Advertiser_.company).addInner(Company_.accountManager)
                .addInner(Creative_.segment).addInner(Creative_.destination).build();

        Creative c = creativeManager.getCreativeById(creative.getId(), fs);

        if (!creative.isPending()) {
            if (directApproval) {
                if (creative.isPaused()) {
                    c.setStatus(Creative.Status.PAUSED);
                    creative.setStatus(Creative.Status.PAUSED);
                } else {
                    c.setStatus(Creative.Status.ACTIVE);
                    creative.setStatus(Creative.Status.ACTIVE);
                }
            } else {
                if (creative.isPaused()) {
                    c.setStatus(Creative.Status.PENDING_PAUSED);
                    creative.setStatus(Creative.Status.PENDING_PAUSED);
                } else {
                    c.setStatus(Creative.Status.PENDING);
                    creative.setStatus(Creative.Status.PENDING);
                }
            }

            c.setSubmissionTime(new Date());
            creativeManager.update(c);
            creativeManager.getCreativeById(creative.getId(), fs);
        }

        return creative;
    }

    @Override
    public List<DestinationType> getDestinationTypes(boolean isAndroidOnly, boolean isIosOnly) {
        List<DestinationType> destinations = new ArrayList<DestinationType>();

        destinations.add(DestinationType.URL);
        if (isAndroidOnly) {
            destinations.add(DestinationType.ANDROID);
        } else if (isIosOnly) {
            destinations.add(DestinationType.IPHONE_APP_STORE);
        }
        if (isAndroidOnly || isIosOnly) {
            destinations.add(DestinationType.AUDIO);
            destinations.add(DestinationType.VIDEO);
        }
        if (isIosOnly) {
            destinations.add(DestinationType.ITUNES_STORE);
        }

        return destinations;
    }

    @Override
    public List<ExtendedCreativeTypeDto> getExtendedCreativeTypes(Boolean isRichMedia, Boolean alsoShowHidden) {
        List<ExtendedCreativeTypeDto> result = new ArrayList<ExtendedCreativeTypeDto>();

        VisibilityEnum visibility = (alsoShowHidden == null || alsoShowHidden) ? null : VisibilityEnum.NOT_HIDDEN;
        ExtendedCreativeTypeFilter filter = new ExtendedCreativeTypeFilter().setVisibility(visibility);

        if (isRichMedia != null) {
            if (isRichMedia) {
                filter.setRichMedia(true);
            } else {
                filter.setThirdParyStandard(true);
            }
        }

        List<ExtendedCreativeType> extendedCreativeTypes = extendedCreativeManager.getAllExtendedCreativeTypes(filter);
        for (ExtendedCreativeType ect : extendedCreativeTypes) {
            result.add(mapper.map(ect, ExtendedCreativeTypeDto.class));
        }

        return result;
    }

    @Override
    public ExtendedCreativeTypeDto getExtendedCreativeTypeById(Long id) {
        ExtendedCreativeType ect = extendedCreativeManager.getExtendedCreativeTypeById(id);

        return mapper.map(ect, ExtendedCreativeTypeDto.class);
    }

    @Override
    public List<ExtendedCreativeTemplateDto> getTemplates(ExtendedCreativeTypeDto type) {
        ExtendedCreativeType ect = extendedCreativeManager.getExtendedCreativeTypeById(type.getId(), extendedCreativeFs);
        List<ExtendedCreativeTemplateDto> result = new ArrayList<ExtendedCreativeTemplateDto>();
        for (ContentForm cf : ect.getContentForms()) {
            ExtendedCreativeTemplateDto ectmp = new ExtendedCreativeTemplateDto();
            ectmp.setContentForm(cf);
            result.add(ectmp);
        }
        return result;
    }

    @Override
    public CreativeAttributeDto getCreativeAttributeByName(String name) {
        CreativeAttribute a = creativeManager.getCreativeAttributeByName(name);
        return mapper.map(a, CreativeAttributeDto.class);
    }

    @Override
    public CreativeAttributeDto getCreativeAttributeById(Long id) {
        CreativeAttribute a = creativeManager.getCreativeAttributeById(id);
        return mapper.map(a, CreativeAttributeDto.class);
    }

    @Override
    public List<CreativeAttributeDto> getAllCreativeAttributes() {
        List<CreativeAttributeDto> attrs = new ArrayList<CreativeAttributeDto>();
        for (CreativeAttribute a : creativeManager.getAllCreativeAttributes()) {
            CreativeAttributeDto dto = mapper.map(a, CreativeAttributeDto.class);
            attrs.add(dto);
        }

        return attrs;
    }

    /******PRIVATE METHODS*******/

    private ContentSpecDto getContentSpec(ContentTypeDto contentTypeDto, int width, int height) {

        //Look for the content spec which fits exactly with width and height
        for (ContentSpecDto cs : getContentSpecsForImages()) {
            if (cs.getContentTypes().contains(contentTypeDto) && cs.getWidth() == width && cs.getHeight() == height) {
                return cs;
            }
        }

        return null;
    }

    private ContentSpecDto getVideoContentSpec(ContentTypeDto contentTypeDto, int width, int height) {
        ContentSpecDto smallestCs = null;
        for (ContentSpecDto cs : getContentSpecsForImages()) {
            int csWidth = cs.getWidth();
            int csHeight = cs.getHeight();
            boolean orienationOk = (csWidth > csHeight && width > height) || (csWidth <= csHeight && width <= height);
            if (cs.getContentTypes().contains(contentTypeDto) && orienationOk && csWidth >= width && csHeight >= height) {
                if (smallestCs == null || smallestCs.getWidth() > csWidth || smallestCs.getHeight() > csHeight) {
                    smallestCs = cs;
                }
            }
        }
        return smallestCs;
    }

    private AssetDto saveAsset(AssetDto dto, Creative creative) {
        Asset asset;
        ContentType ct = commonManager.getContentTypeById(dto.getContentType().getId());
        if (dto.getId() != null && dto.getId().longValue() > 0) {
            // already persisted
            asset = assetManager.getAssetById(dto.getId());

            asset.setData(dto.getData());
            asset.setContentType(ct);

            asset = assetManager.update(asset);

            asset = assetManager.getAssetById(asset.getId(), assetFs);
        } else {
            asset = assetManager.newAsset(creative, ct);

            asset.setData(dto.getData());

            asset = assetManager.update(asset);

            asset = assetManager.getAssetById(asset.getId(), assetFs);

        }

        //Mapper mapper = new DozerBeanMapper();
        return mapper.map(asset, AssetDto.class);
    }

    // Returns a map with the contentSpec ids and the assets
    private Map<Long, AssetDto> loadAssets(CreativeDto dto) {
        Creative creative = creativeManager.getCreativeById(dto.getId(), creativeFs);
        Map<Long, AssetDto> result = new HashMap<Long, AssetDto>();

        // this is hardcoded for t2 behavior with banners, only look at required components. 
        // To be deleted when we update database structure around creatives 
        List<Component> components = assetManager.findAllComponentsForFormat(creative.getFormat());
        List<Component> requiredComponents = new ArrayList<Component>();
        for (Component c : components) {
            if (c.isRequired()) {
                requiredComponents.add(c);
            }
        }

        for (DisplayType dt : creative.getAssetBundleMap().keySet()) {
            AssetBundle ab = creative.getAssetBundleMap().get(dt);
            for (Component comp : requiredComponents) {
                if (ab.getAsset(comp) != null && comp.getContentSpec(dt) != null) {
                    Asset asset = ab.getAsset(comp);
                    if (asset != null) {
                        //Mapper mapper = new DozerBeanMapper();
                        AssetDto assetDto = mapper.map(asset, AssetDto.class);
                        result.put(comp.getContentSpec(dt).getId(), assetDto);
                    } else {
                        result.put(comp.getContentSpec(dt).getId(), new AssetDto());
                    }
                }
            }
        }

        // this is hardcoded for t2 behavior with banners, only return one size. 
        // To be deleted when we update database structure around creatives 
        if ((!isNativeAd(creative)) && (result.size() > 1)) {
            Map<Long, AssetDto> only1result = new HashMap<Long, AssetDto>();
            Long key = result.keySet().iterator().next();
            only1result.put(key, result.get(key));
            result = only1result;
        }

        return result;
    }

    private void createAssetBundles(Creative creative, Format format, List<ContentSpecDto> contentSpecs, Map<Long, AssetDto> assets) {
        //Create an assetbundle for each displaytype which fits
        FetchStrategy componentsFs = new FetchStrategyBuilder().addLeft(Component_.contentSpecMap).build();
        DisplayType displayType = null;
        List<Component> components = assetManager.findAllComponentsForFormat(format, componentsFs);
        Map<Long, Component> mComponents = new HashMap<Long, Component>();
        for (DisplayType dt : format.getDisplayTypes()) {
            for (Component c : components) {
                for (ContentSpecDto cs : contentSpecs) {
                    if (sizeFits(cs, dt) && c.getContentSpec(dt) != null && c.getContentSpec(dt).getId() == cs.getId()) {
                        displayType = dt;
                        mComponents.put(cs.getId(), c);
                    }
                }
            }
        }
        if (displayType != null) {
            AssetBundle ab = assetManager.newAssetBundle(creative, displayType);
            for (ContentSpecDto cs : contentSpecs) {
                Asset a = assetManager.getAssetById(assets.get(cs.getId()).getId());
                ab.getAssetMap().put(mComponents.get(cs.getId()), a);
                assetManager.update(ab);
            }
        }
    }

    private boolean sizeFits(ContentSpecDto cs, DisplayType dt) {
        Pattern p = Pattern.compile("\\w+>(\\d+);\\w+<(\\d+)");
        Matcher search = p.matcher(dt.getConstraints());
        Pattern p2 = Pattern.compile("\\w+>(\\d+)");
        Matcher search2 = p2.matcher(dt.getConstraints());
        Pattern p3 = Pattern.compile("\\w+<(\\d+)");
        Matcher search3 = p3.matcher(dt.getConstraints());
        if (search.find()) {
            return cs.getWidth() > Integer.parseInt(search.group(1)) && cs.getWidth() < Integer.parseInt(search.group(2));
        } else if (search2.find()) {
            return cs.getWidth() > Integer.parseInt(search2.group(1));
        } else if (search3.find()) {
            return cs.getWidth() < Integer.parseInt(search3.group(1));
        } else {
            return true;
        }
    }

    private Creative deleteAssetBundles(Creative c) {
        for (AssetBundle ab : c.getAssetBundleMap().values()) {
            assetManager.delete(ab);
        }
        return creativeManager.getCreativeById(c.getId(), creativeFs);
    }

    private boolean isValidSize(ContentSpecDto cs, long size) {
        if (cs.getMaxBytes() == 0) {
            return true;
        }
        return size <= cs.getMaxBytes();
    }
    
    @Override
    public Boolean isSslCompliant(CreativeDto dto) {
		Boolean sslCompliant = Boolean.TRUE; // we are sure by default

		// Always Check Beacons
		DestinationDto destination;
		List<BeaconUrlDto> beaconUrls;
		// First party ads are SSL compliant unless non-secure beacon is attached
		if ((destination = dto.getDestination()) != null && (beaconUrls = destination.getBeaconUrls()) != null) {
			for (BeaconUrlDto beacon : beaconUrls) {
				String beaconUrl = beacon.getUrl();
				if (StringUtils.isNotEmpty(beaconUrl) && !beaconUrl.startsWith("https")) {
					LOGGER.debug("Nonsecure beacon: " + beacon.getUrl());
					return Boolean.FALSE;
				}
			}
		}

		// VAST check
		if (dto.isVastVideo()) {
			MobileAdVastMetadataDto vastMetaData;
			if ((vastMetaData = parseVastMetadata(dto)) != null && !vastMetaData.isSslCompliant()){
				return Boolean.FALSE;
			}
			
		// Third-party check
		} else if (dto.isThirdPartyTag()) {
			List<ExtendedCreativeTemplateDto> extTemplates = (extTemplates = dto.getExtendedCreativeTemplates()) != null ? extTemplates : Collections.emptyList();
			// It is very hard to determine ssl compliance from ThirdPartyTag markup (neither Rich Media nor Simple)
			for (ExtendedCreativeTemplateDto extTemplate : extTemplates) {
				String templateOriginal = extTemplate.getTemplateOriginal();
				if (StringUtils.isNotEmpty(templateOriginal)) {
					return isTagSslCompliant(templateOriginal);
				}
			}
		}
		return sslCompliant;
    }

    /**
     * Whether the TAG is SSL compliant
     * @return true false or unsure (null)
     */
    public static Boolean isTagSslCompliant(String templateOriginal) {
        List<String> checkHtmlSecure = checkHtmlSecure(templateOriginal);
		return (checkHtmlSecure.contains(NO_HTTPS_FOUND_IN_MARKUP)) ? null : checkHtmlSecure.isEmpty();
    }

    public static List<String> checkHtmlSecure(String html) {
        Document jsoupDocument = Jsoup.parseBodyFragment(html);
        List<String> nonsecure = new ArrayList<String>();
        boolean seenHttps = false;
        Elements scripts = jsoupDocument.select("script");
        for (int i = 0; i < scripts.size(); ++i) {
            Element element = scripts.get(i);
            String url = element.attr("src");
            if (url.startsWith("https:")) {
                seenHttps = true;
            } else if (url.startsWith("http:")) {
                LOGGER.debug("Non https script found: " + url);
                nonsecure.add("script " + url);
            } else if ("mraid.js".equals(url)) {
                // Dummy mraid.js declaration - ignore
            } else {
                // search for javascript dynamic protocol setup 
                String javascript = element.data();
                if (isJsCodeHttps(javascript)) {
                    seenHttps = true;
                } else {
                    LOGGER.debug("Non https script found: " + javascript);
                    nonsecure.add("script embedded");
                }
            }
        }

        Elements mediaLinks = jsoupDocument.select("img[src]"); // img
        Elements linkHrefs = jsoupDocument.select("link[href]"); // css
        mediaLinks.addAll(linkHrefs);

        for (int i = 0; i < mediaLinks.size(); ++i) {
            Element element = mediaLinks.get(i);
            String tagName = element.tagName();
            String url;
            if (element.hasAttr("src")) {
                url = element.attr("src");
            } else {
                url = element.attr("href");
            }

            if (url.startsWith("https:")) { //normal links and anchors
                seenHttps = true;
            } else if (url.startsWith("data:")) { //celtra img
                boolean jsCodeHttps = isJsCodeHttps(element.attr("onerror"));
                if (jsCodeHttps) {
                    seenHttps = true;
                } else {
                    nonsecure.add("script onerror=...");
                }
            } else {
                LOGGER.debug("Non https element found: " + tagName + " " + url);
                nonsecure.add(tagName + " " + url);
            }
        }
        LOGGER.debug("Any https found: " + seenHttps);
        if (!seenHttps) {
            nonsecure.add(NO_HTTPS_FOUND_IN_MARKUP);
        }

        return nonsecure;
    }

    /**
     * Best effort to guess if javascript code is manipulating urls according location.protocol to make itself secure
     * This will never be 100% accurate as javascript code is not really executed.
     */
    private static boolean isJsCodeHttps(String javascript) {
        boolean domUpdates = javascript.contains("document.write") || javascript.contains("document.createElement");
        boolean locationProtocolHttps = javascript.contains("location.protocol") && javascript.contains("https");
        // Some justad tags go directly for https
        boolean directHttpsUrl = javascript.contains("https:");
        return domUpdates && (directHttpsUrl || locationProtocolHttps);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("One parameter expected: file with tag to be verified");
        }
        File file = new File(args[0]);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Cannot read: " + file);
        }
        try {
            String creativeMarkup = FileUtils.readFileToString(file);
            List<String> nonsecures;
            if (creativeMarkup.contains("<VAST")) {
                VAST vast = VastWorker.instance().read(new StringReader(creativeMarkup));
                MobileAdVastAnalyser analyser = new MobileAdVastAnalyser();
                VastWorker.instance().visit(vast, null, analyser);
                nonsecures = analyser.getMetaData().getNonSecureAssets();
            } else {
                nonsecures = CreativeServiceImpl.checkHtmlSecure(creativeMarkup);
            }
            System.out.println(file + " SSL compliant: " + nonsecures.isEmpty());
            for (String one : nonsecures) {
                System.out.println(one);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}