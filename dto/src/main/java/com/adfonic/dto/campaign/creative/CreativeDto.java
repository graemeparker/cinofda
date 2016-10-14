package com.adfonic.dto.campaign.creative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative.Status;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.enums.CreativeStatus;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.format.ContentSpecDto;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.language.LanguageDto;

public class CreativeDto extends NameIdBusinessDto implements Cloneable {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "format")
    private FormatDto format;

    @DTOCascade
    @Source(value = "destination")
    private DestinationDto destination;

    @DTOCascade
    @Source(value = "externalID")
    private String externalID = null;

    @DTOCascade
    @Source(value = "language")
    private LanguageDto language;

    @Source(value = "englishTranslation")
    private String englishTranslation;

    @Source(value = "extendedCreativeType")
    private ExtendedCreativeTypeDto extendedCreativeType;

    @Source(value = "status")
    private com.adfonic.domain.Creative.Status status;

    @DTOCascade
    @Source(value = "extendedCreativeTemplates")
    private List<ExtendedCreativeTemplateDto> extendedCreativeTemplates;

    @DTOCascade
    @Source(value = "creativeAttributes")
    private List<CreativeAttributeDto> creativeAttributes = new ArrayList<CreativeAttributeDto>(0);

    private CreativeStatus creativeStatus;

    private Map<Long, AssetDto> assets;

    private List<ContentSpecDto> contentSpecs;

    private int index;

    private Integer width = null;

    private Integer height = null;

    private int state = 0;

    private CreativeDto oldCreative = null;

    private String hiddenClass = "";

    private String adText = "";

    private String activeStatus;

    private boolean opened;

    private String incompleteMessage;

    private boolean changedToCommit = false;

    private boolean richMedia;

    @Source(value = "closedMode")
    private boolean closedMode;

    @Source(value = "allowExternalAudit")
    private boolean allowExternalAudit;

    @Source(value = "extendedData")
    private Map<String, String> extendedData;

    private NativeAdInfoDto nativeAdInfo;
    private boolean isNativeAd = false;

    private Boolean isVastVideo = null;

    private Boolean sslCompliant;
    
    @Source(value = "sslOverride")
    private Boolean sslOverride;

    public FormatDto getFormat() {
        return format;
    }

    public void setFormat(FormatDto formatDto) {
        this.format = formatDto;
    }

    public DestinationDto getDestination() {
        return destination;
    }

    public void setDestination(DestinationDto destination) {
        this.destination = destination;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public Map<Long, AssetDto> getAssets() {
        return assets;
    }

    public void setAssets(Map<Long, AssetDto> assets) {
        this.assets = assets;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LanguageDto getLanguage() {
        return language;
    }

    public void setLanguage(LanguageDto language) {
        this.language = language;
    }

    public String getEnglishTranslation() {
        return englishTranslation;
    }

    public void setEnglishTranslation(String englishTranslation) {
        this.englishTranslation = englishTranslation;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CreativeDto getOldCreative() {
        return oldCreative;
    }

    public void setOldCreative(CreativeDto oldCreative) {
        this.oldCreative = oldCreative;
    }

    public String getHiddenClass() {
        return hiddenClass;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setHiddenClass(String hiddenClass) {
        this.hiddenClass = hiddenClass;
    }

    public String getHiddenHeader() {
        if ("none".equals(this.hiddenClass)) {
            return "";
        } else {
            return "none";
        }
    }

    public com.adfonic.domain.Creative.Status getStatus() {
        return status;
    }

    public void setStatus(com.adfonic.domain.Creative.Status status) {
        this.status = status;
    }

    public CreativeStatus getCreativeStatus() {
        this.creativeStatus = CreativeStatus.valueOf(this.status.toString());
        return creativeStatus;
    }

    public void setCreativeStatus(CreativeStatus creativeStatus) {
        this.creativeStatus = creativeStatus;
        this.status = this.creativeStatus.getStatus();
    }

    public String getIncompleteMessage() {
        return incompleteMessage;
    }

    public void setIncompleteMessage(String incompleteMessage) {
        this.incompleteMessage = incompleteMessage;
    }

    public boolean isChangedToCommit() {
        return changedToCommit;
    }

    public void setChangedToCommit(boolean changedToCommit) {
        this.changedToCommit = changedToCommit;
    }

    public ExtendedCreativeTypeDto getExtendedCreativeType() {
        return extendedCreativeType;
    }

    public void setExtendedCreativeType(ExtendedCreativeTypeDto extendedCreativeType) {
        this.extendedCreativeType = extendedCreativeType;
    }

    public List<ExtendedCreativeTemplateDto> getExtendedCreativeTemplates() {
        return extendedCreativeTemplates;
    }

    public void setExtendedCreativeTemplates(List<ExtendedCreativeTemplateDto> extendedCreativeTemplates) {
        this.extendedCreativeTemplates = extendedCreativeTemplates;
    }

    public List<CreativeAttributeDto> getCreativeAttributes() {
        return creativeAttributes;
    }

    public void setCreativeAttributes(List<CreativeAttributeDto> creativeAttributes) {
        this.creativeAttributes = creativeAttributes;
    }

    public boolean isThirdPartyTag() {
        return extendedCreativeType != null;
    }

    public boolean isRichMedia() {
        return richMedia;
    }
    
    public boolean isTagBased() {
        return isThirdPartyTag() || isRichMedia() || isVastVideo();
    }

    public void setRichMedia(boolean richMedia) {
        this.richMedia = richMedia;
    }

    public boolean isVastVideo() {
        if (isVastVideo == null) {
            isVastVideo = (getVastTag() != null);
        }
        return isVastVideo;
    }

    public String getVastTag() {
        ExtendedCreativeTemplateDto template = getVastTemplate();
        if (template != null) {
            return template.getTemplateOriginal();
        }
        return null;
    }

    private ExtendedCreativeTemplateDto getVastTemplate() {
        if (extendedCreativeTemplates != null) {
            for (ExtendedCreativeTemplateDto template : extendedCreativeTemplates) {
                if (ContentForm.VAST_2_0.equals(template.getContentForm())) {
                    return template;
                }
            }
        }
        return null;
    }

    public List<ContentSpecDto> getContentSpecs() {
        return contentSpecs;
    }

    public void setContentSpecs(List<ContentSpecDto> contentSpecs) {
        this.contentSpecs = contentSpecs;
    }

    public boolean isMissingImage() {
        boolean assetEmpty = false;
        if (isVastVideo()) {
            assetEmpty = true;
        } else if (CollectionUtils.isNotEmpty(this.contentSpecs) && assets != null && !assets.isEmpty()) {
            for (ContentSpecDto cs : this.contentSpecs) {
                if (!this.assets.containsKey(cs.getId())) {
                    assetEmpty = true;
                }
            }
        } else {
            assetEmpty = true;
        }
        return assetEmpty;
    }

    public String getActiveStatus() {
        if (activeStatus == null) {
            activeStatus = "ACTIVE";
        }
        if (/* status.equals(Creative.Status.NEW_PAUSED) || */status.equals(Status.PENDING_PAUSED) || status.equals(Status.PAUSED)) {
            activeStatus = "PAUSE";
        } else if (status.equals(Status.PENDING) || status.equals(Status.ACTIVE)) {
            activeStatus = "ACTIVE";
        }
        return activeStatus;
    }

    public boolean getDeletable() {
        return isNewCreative() || isRejected();
    }

    public void setActiveStatus(String activeStatus) {
        if (status.equals(Status.NEW) || status.equals(Status.REJECTED)) {
            this.activeStatus = activeStatus;
        } else if ("PAUSE".equals(activeStatus)) {
            /*
             * if(status.equals(Creative.Status.NEW)){ this.status =
             * Creative.Status.NEW_PAUSED; } else
             */if (status.equals(Status.PENDING)) {
                this.status = Status.PENDING_PAUSED;
            } else if (status.equals(Status.ACTIVE)) {
                this.status = Status.PAUSED;
            }
        } else {
            /*
             * if(status.equals(Creative.Status.NEW_PAUSED)){ this.status =
             * Creative.Status.NEW; } else
             */if (status.equals(Status.PENDING_PAUSED)) {
                this.status = Status.PENDING;
            } else if (status.equals(Status.PAUSED)) {
                this.status = Status.ACTIVE;
            }
        }
    }

    public boolean isPaused() {
        return this.getStatus().equals(Status.PAUSED) || this.getStatus().equals(Status.PENDING_PAUSED)/*
                                                                                                        * ||
                                                                                                        * this
                                                                                                        * .
                                                                                                        * getStatus
                                                                                                        * (
                                                                                                        * )
                                                                                                        * .
                                                                                                        * equals
                                                                                                        * (
                                                                                                        * Status
                                                                                                        * .
                                                                                                        * NEW_PAUSED
                                                                                                        * )
                                                                                                        */;
    }

    public boolean isPending() {
        return this.getStatus().equals(Status.PENDING) || this.getStatus().equals(Status.PENDING_PAUSED);
    }

    public boolean isRejected() {
        return this.getStatus().equals(Status.REJECTED);
    }

    public boolean isActive() {
        return this.getStatus().equals(Status.ACTIVE);
    }

    public boolean isNewCreative() {
        return this.getStatus().equals(Status.NEW) /*
                                                    * ||
                                                    * this.getStatus().equals(
                                                    * Status.NEW_PAUSED)
                                                    */;
    }

    public boolean isVendorSelected() {
        return extendedCreativeType != null && extendedCreativeType.getId() != null;
    }

    public boolean isImage() {

        return !isThirdPartyTag() && format != null && !"text".equals(format.getSystemName()) && !"native_app_install".equals(format.getSystemName());
    }

    public String getAdText(long contentSpecId) {
        if (format != null && !assets.isEmpty() && assets.containsKey(contentSpecId)) {
            adText = assets.get(contentSpecId).getDataAsString();
        }
        return adText;
    }

    public String getAdText() {
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            return getAdText(this.getContentSpec().getId());
        }
        return "";
    }

    public void setAdText(String adText) {
        this.adText = adText;
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            if (assets == null) {
                assets = new HashMap<Long, AssetDto>();
                assets.put(this.getContentSpec().getId(), new AssetDto());
            }
            assets.get(this.getContentSpec().getId()).setData(adText.getBytes());
        }
    }

    public void setAdText(String adText, long componentId) {
        this.adText = adText;
        if (assets == null) {
            assets = new HashMap<Long, AssetDto>();
            assets.put(componentId, new AssetDto());
        }
        assets.get(componentId).setData(adText.getBytes());
    }

    public boolean getEnglish() {
        if (language == null || language.getId() == null) {
            return true;
        }
        return "English".equals(language.getName());
    }

    public boolean getDestinated() {
        if (destination == null || destination.getData() == null || "".equals(destination.getData())) {
            return false;
        }
        return true;
    }

    public boolean getCall() {
        if (destination != null && destination.getDestinationType() != null) {
            return destination.getDestinationType().equals(DestinationType.CALL);
        }
        return false;
    }

    public boolean isEditable() {
        return status == Status.NEW || status == Status.REJECTED;
    }

    public int getWidth() {
        if (width == null) {
            if (CollectionUtils.isNotEmpty(this.contentSpecs) && contentSpecs.get(0) != null) {
                ContentSpecDto contentSpec = contentSpecs.get(0);
                if (contentSpec.getWidth() == 320 && contentSpec.getHeight() == 480) {
                    width = 160;
                } else if (contentSpec.getWidth() == 728 && contentSpec.getHeight() == 90) {
                    width = 364;
                } else if (contentSpec.getWidth() == 120 && contentSpec.getHeight() == 600) {
                    width = 50;
                } else {
                    width = contentSpec.getWidth();
                }
                return width;
            } else {
                return 0;
            }
        } else {
            return width;
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getSmallWidth() {
        int localWidth = 0;
        int localHeight = 0;
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            ContentSpecDto contentSpec = contentSpecs.get(0);
            localWidth = contentSpec.getWidth();
            localHeight = contentSpec.getHeight();
        }

        if (localWidth == 0) {
            return 0;
        } else if (localHeight == 600) {
            return 17;
        } else if (localHeight == 480) {
            return 59;
        }
        return 106;
    }

    public int getHeight() {
        if (height == null) {
            if (CollectionUtils.isNotEmpty(contentSpecs) && contentSpecs.get(0) != null) {
                ContentSpecDto contentSpec = contentSpecs.get(0);
                if (contentSpec.getWidth() == 320 && contentSpec.getHeight() == 480) {
                    height = 240;
                } else if (contentSpec.getWidth() == 728 && contentSpec.getHeight() == 90) {
                    height = 45;
                } else if (contentSpec.getWidth() == 120 && contentSpec.getHeight() == 600) {
                    height = 250;
                } else {
                    height = contentSpec.getHeight();
                }
                return height;
            } else {
                return 0;
            }
        } else {
            return height;
        }
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSmallHeight() {
        int localWidth = 0;
        int localHeight = 0;
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            ContentSpecDto contentSpec = contentSpecs.get(0);
            localWidth = contentSpec.getWidth();
            localHeight = contentSpec.getHeight();
        }
        if (localWidth == 0) {
            return 0;
        } else if (localHeight == 600 || localHeight == 480 || getWidth() == 0) {
            return 88;
        }
        return (106 * getHeight()) / getWidth();
    }

    public boolean isClosedMode() {
        return closedMode;
    }

    public void setClosedMode(boolean closedMode) {
        this.closedMode = closedMode;
    }

    public boolean isAllowExternalAudit() {
        return allowExternalAudit;
    }

    public void setAllowExternalAudit(boolean allowExternalAudit) {
        this.allowExternalAudit = allowExternalAudit;
    }

    public AssetDto getAsset() {
        if (this.assets.size() == 1) {
            List<AssetDto> lAssets = new ArrayList<AssetDto>(this.assets.values());
            return lAssets.get(0);
        }
        return null;
    }

    public ContentSpecDto getContentSpec() {
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            return this.contentSpecs.get(0);
        }
        return null;
    }

    public ContentSpecDto getContentSpec(String name) {
        if (CollectionUtils.isNotEmpty(this.contentSpecs)) {
            for (ContentSpecDto cs : this.contentSpecs) {
                if (cs.getName().equals(name)) {
                    return cs;
                }
            }
        }
        return null;
    }

    public Map<String, String> getExtendedData() {
        if (extendedData == null) {
            extendedData = new HashMap<String, String>();
        }
        return extendedData;
    }

    public void setExtendedData(Map<String, String> extendedData) {
        this.extendedData = extendedData;
    }

    public NativeAdInfoDto getNativeAdInfo() {
        if (nativeAdInfo == null) {
            this.nativeAdInfo = new NativeAdInfoDto();
        }
        return nativeAdInfo;
    }

    public void setNativeAdInfo(NativeAdInfoDto nativeAdInfo) {
        this.nativeAdInfo = nativeAdInfo;
    }

    public Boolean isNativeAd() {
        return isNativeAd;
    }

    public void setNativeAd(Boolean isNativeAd) {
        this.isNativeAd = isNativeAd;
    }

    public Boolean isSslCompliant() {
        return sslCompliant;
    }

    public void setSslCompliant(Boolean sslCompliant) {
        this.sslCompliant = sslCompliant;
    }

    public Boolean getSslOverride() {
		return sslOverride;
	}

	public void setSslOverride(Boolean sslOverride) {
		this.sslOverride = sslOverride;
	}

	public boolean isTextLink() {
        boolean isTextLink = false;

        if (getFormat() != null) {
            isTextLink = "MMA Text Link".equals(getFormat().getName());
        }

        return isTextLink;
    }

    @Override
    public CreativeDto clone() {
        CreativeDto newDto = new CreativeDto();
        newDto.setAssets(getAssets());
        newDto.setContentSpecs(getContentSpecs());
        DestinationDto localDestination = getDestination();
        if (getDestination() != null) {
            DestinationDto newDestination = new DestinationDto();
            newDestination.setDestinationType(localDestination.getDestinationType());
            newDestination.setId(localDestination.getId());
            newDestination.setData(localDestination.getData());
            newDestination.setBeaconUrls(localDestination.getBeaconUrls());
            newDestination.setDataIsFinalDestination(localDestination.isDataIsFinalDestination());
            newDestination.setFinalDestination(localDestination.getFinalDestination());
            newDto.setDestination(newDestination);
        }
        newDto.setEnglishTranslation(getEnglishTranslation());
        newDto.setFormat(getFormat());
        newDto.setHeight(getHeight());
        newDto.setId(getId());
        newDto.setLanguage(getLanguage());
        newDto.setName(getName());
        newDto.setState(getState());
        newDto.setStatus(getStatus());
        newDto.setWidth(getWidth());
        newDto.setState(getState());
        newDto.setIncompleteMessage(getIncompleteMessage());
        newDto.setExtendedCreativeType(getExtendedCreativeType());
        newDto.setExtendedCreativeTemplates(getExtendedCreativeTemplates());
        newDto.setRichMedia(isRichMedia());
        newDto.setCreativeAttributes(getCreativeAttributes());
        newDto.setExternalID(getExternalID());
        newDto.setSslCompliant(isSslCompliant());
        newDto.setExtendedData(new HashMap<String, String>());
        newDto.getExtendedData().putAll(getExtendedData());
        if (isNativeAd()) {
            newDto.setNativeAd(isNativeAd());
            NativeAdInfoDto oldNativeDto = getNativeAdInfo();
            NativeAdInfoDto newNativeDto = new NativeAdInfoDto();
            newNativeDto.setTitle(oldNativeDto.getTitle());
            newNativeDto.setDescription(oldNativeDto.getDescription());
            newNativeDto.setClickToAction(oldNativeDto.getClickToAction());
            if (oldNativeDto.getIcon() != null) {
                newNativeDto.setIcon(new AssetInfoDto(oldNativeDto.getIcon().getData(), oldNativeDto.getIcon().getContentType()));
            }
            if (oldNativeDto.getImage() != null) {
                newNativeDto.setImage(new AssetInfoDto(oldNativeDto.getImage().getData(), oldNativeDto.getImage().getContentType()));
            }
            newDto.setNativeAdInfo(newNativeDto);
        }

        if (isThirdPartyTag()) {
            newDto.setClosedMode(true);
        } else {
            newDto.setClosedMode(false);
        }

        newDto.setAllowExternalAudit(false);

        return newDto;
    }

}
