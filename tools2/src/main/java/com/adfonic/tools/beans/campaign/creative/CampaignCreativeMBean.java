package com.adfonic.tools.beans.campaign.creative;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Role;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.creative.AssetInfoDto;
import com.adfonic.dto.campaign.creative.BeaconUrlDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.creative.CreativeAttributeDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.creative.DestinationDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTemplateDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto;
import com.adfonic.dto.campaign.creative.NativeAdInfoDto;
import com.adfonic.dto.campaign.enums.CreativeStatus;
import com.adfonic.dto.campaign.enums.CreativeTrackedProperty;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.format.ContentSpecDto;
import com.adfonic.dto.language.LanguageDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.presentation.devicetarget.DeviceTargetService;
import com.adfonic.presentation.exceptions.BigFileException;
import com.adfonic.presentation.exceptions.FileExtensionNotSupportedException;
import com.adfonic.presentation.exceptions.NotContentTypeException;
import com.adfonic.presentation.exceptions.SizeNotSupportedException;
import com.adfonic.presentation.validator.ValidationResult;
import com.adfonic.presentation.validator.ValidationUtils;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.security.SecurityUtils;

@Component
@Scope("session")
public class CampaignCreativeMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignCreativeMBean.class);
    
    private static final String CREATIVES_CONTAINER = "creatives-container";
    private static final String SUBMIT_FORM = "submitForm";
    private static final String SAVE_BUTTON = ":saveButton";
    
    private static final String INDEX = "index";
    private static final String TEXT_FORMAT_SYSTEM_NAME = "text";

    private static final String PAGE_ERROR_COMMON_ERROR = "page.error.common.error";
    private static final String PAGE_CREATIVE_DESTINATIONTYPE_WATERMARK_URL = "page.creative.destinationtype.watermark.url";
    private static final String PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED = "page.campaign.creative.error.typenotrecognised";
    private static final String PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED = "page.campaign.creative.error.sizenotrecognised";
    private static final String PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND = "page.campaign.creative.error.sizenotfound";
    private static final String PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG = "page.campaign.creative.error.filebig";
    private static final String VAST_WARNING_MESSAGES_PREFIX = "page.campaign.creative.vast.tag.warninig.";

    private static final String CHANGE_FILE_SIZE_UNSUPPORTED = "changeFile<-- Not size supported exception";
    private static final String CHANGE_FILE_FORMAT_UNSUPPORTED = "changeFile<-- Not format supported exception";
    private static final String CHANGE_FILE_IO_EXCEPTION = "changeFile<-- IO exception";
    private static final String CHANGE_FILE_GENERAL_EXCEPTION = "changeFile<-- General exception";
    private static final String CHANGE_FILE_BIGGER_THAN_ALLOWED = "changeFile<-- File is bigger than allowed exception";
    private static final String CHANGE_FILE_EXT_NOT_RECOGNIZED = "changeFile<-- File extension not recognized exception";

    private static final String CREATIVE_ID = "Creative id: ";
    private static final String CANNOT_BE_COPIED = " is not defined and cannot be copied";
    private static final String IS_COMPLETED_NOW = " is completed now";
    private static final String CREATIVE = "Creative ";
    private static final String LANG_ENGLISH = "English";

    private static final String ACTIVE_STATUS_PAUSE = "PAUSE";
    private static final String SUBMIT_STATUS_CLOSED = "CLOSED";

    private static final int CREATIVE_STATE_2 = 2;
    private static final int TWO = 2;
    
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    
    private static CreativeAttributeDto DEFAULT_TEXT_ATTRIBUTE;
    private static CreativeAttributeDto DEFAULT_RICH_MEDIA_ATTRIBUTE;
    private static CreativeAttributeDto DEFAULT_VAST_VIDEO_ATTRIBUTE;
    
    private CampaignCreativeDto campaignDto;

    @Autowired
    private CreativeService crService;

    @Autowired
    private CampaignService cService;

    @Autowired
    private DeviceTargetService deviceTargetService;

    private boolean showCreativeDetails = false;

    private boolean showTextDetails = false;

    private List<LanguageDto> firstLanguages = null;
    private List<LanguageDto> secondLanguages = null;

    private String translation;

    private int currentCreative;

    private boolean editing = false;

    private String submitStatus = SUBMIT_STATUS_CLOSED;

    private int submitedIndex = 0;

    private Set<CreativeTrackedProperty> changedCreativeProperties = new HashSet<CreativeTrackedProperty>(); 
    
    private String oldText = null;
    private String oldUrl = null;
    private List<BeaconUrlDto> oldBeaconUrls = new ArrayList<BeaconUrlDto>();
    private String oldTranslation = null;
    private ExtendedCreativeTypeDto oldVendor = null;
    private List<ExtendedCreativeTemplateDto> oldTags = new ArrayList<ExtendedCreativeTemplateDto>();
    private String oldFinalDestinationUrl = null;
    private boolean oldDataIsFinalDestination = true;
    private List<DestinationType> destinationTypes = null;
    private List<CreativeAttributeDto> oldAttributes = new ArrayList<CreativeAttributeDto>();

    private CreativeDto oldDto;

    private List<CreativeDto> lOldCreatives;

    private boolean appliedToAll;

    private boolean applyDestination;
    private boolean applyFinalDestination;
    private boolean applyLanguage;

    private List<ExtendedCreativeTypeDto> extendedTypes;

    private List<ExtendedCreativeTypeDto> richMediaExtendedTypes;

    private List<ContentForm> contentForms;

    private MobileAdVastMetadataDto vastMetaData;
    
    private boolean isCreativeFirstSave;

    @Override
    @PostConstruct
    protected void init() {
        if (DEFAULT_TEXT_ATTRIBUTE == null) {
            DEFAULT_TEXT_ATTRIBUTE = crService.getCreativeAttributeByName(CreativeAttributeDto.DEFAULT_TEXT_NAME);
        }
        if (DEFAULT_RICH_MEDIA_ATTRIBUTE == null) {
            DEFAULT_RICH_MEDIA_ATTRIBUTE = crService.getCreativeAttributeByName(CreativeAttributeDto.DEFAULT_RICH_MEDIA_NAME);
        }
        if (DEFAULT_VAST_VIDEO_ATTRIBUTE == null) {
            DEFAULT_VAST_VIDEO_ATTRIBUTE = crService.getCreativeAttributeByName(CreativeAttributeDto.DEFAULT_VAST_VIDEO_AUTOPLAY);
        }
    }

    public void doSave(/*ActionEvent event*/) {
        LOGGER.debug("doSave-->");
        if (CollectionUtils.isEmpty(campaignDto.getCreatives())) {
            LOGGER.debug("Campaign list is empty");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, null, "page.campaign.creative.error.nocreatives");
            return;
        }
        if (getCNavigationBean().isTrackingDisabled()) {
            getCNavigationBean().setTrackingDisabled(false);
            getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), Constants.MENU_TRACKING);
        }
        if (getCampaignMBean().isNewCampaign() || getCNavigationBean().isCampaignBlocked()) {
            LOGGER.debug("New campaign");
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_TRACKING);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_tracking.xhtml");
        } else {
            LOGGER.debug("Exisiting campaign");
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        }
        submitStatus = SUBMIT_STATUS_CLOSED;
        LOGGER.debug("doSave<--");
    }

    public void addNew(ActionEvent event) {
        LOGGER.debug("addNew-->");
        CreativeDto creative;
        boolean isThirdParty = false;
        boolean isRichMedia = false;
        boolean isNative = false;
        boolean vastVideo = false;
        if ("thirdPartyButton".equals(event.getComponent().getId())) {
            LOGGER.debug("Third party tag");
            isThirdParty = true;
        } else if ("richmediaButton".equals(event.getComponent().getId())) {
            LOGGER.debug("Third party Rich media tag");
            isThirdParty = true;
            isRichMedia = true;
        } else if ("nativeadButton".equals(event.getComponent().getId())) {
            LOGGER.debug("Native ad");
            isNative = true;
        } else if ("vastButton".equals(event.getComponent().getId())) {
            LOGGER.debug("VAST ad");
            vastVideo = true;
        }
        if (isThirdParty) {
            creative = crService.newCreative3rdParty("", isRichMedia);
        } else if (vastVideo) {
            //vast is like 3rd party tag with predefined choices
            creative = crService.newCreativeVast();
        } else if (isNative) {
            creative = crService.newNativeCreative("", "");
        } else {
            creative = crService.newTextCreative("");
        }
        creative.setIncompleteMessage("page.campaign.creative.incomplete.noname");
        creative.setState(Constants.INCOMPLETE_STATE);
        creative.setCreativeStatus(CreativeStatus.NEW);
        creative.setOpened(true);
        DestinationDto destination = new DestinationDto();
        BeaconUrlDto beacon = new BeaconUrlDto();
        destination.getBeaconUrls().add(beacon);
        creative.setDestination(destination);

        setDefaultCreativeAttributes(creative);

        creative.setName("");
        campaignDto.getCreatives().add(creative);
        currentCreative = campaignDto.getCreatives().size() - 1;

        submitedIndex = campaignDto.getCreatives().size() - 1;
        openPanel();
        LOGGER.debug("addNew<--");
    }

    public void saveCreative(ActionEvent event) throws IOException {
        int index;
        if (event == null) {
            index = 0;
        } else {
            LOGGER.debug("saveCreative-->");
            index = getCreativeIndexFromParam();
        }
        LOGGER.debug("Save creative with index in list: " + index);
        CreativeDto creative = getCreativeByIndex(index);
        LOGGER.debug(CREATIVE_ID + creative.getId());

        // Get SSL override (based on dialog response, true, false or null)
        Boolean sslOverride = BooleanUtils.toBooleanObject(getSslOverrideFromParam());
        
        // Confirm SSL compliance in case of unsure or non-ssl compliance
        if (sslOverride == null && !BooleanUtils.isTrue(isCreativeSslCompliant(creative))) {
        	RequestContext.getCurrentInstance().execute("updateAndShowSslConfirmation()");
        	return;
        }
        
        for (CreativeAttributeDto c : creative.getCreativeAttributes()) {
            LOGGER.debug(c.getName());
        }
        // For native ads we use title as name
        if (creative.getFormat() != null && creative.isNativeAd()) {
            creative.setName(creative.getNativeAdInfo().getTitle());
        }
        if (isValidCreative(creative)) {
            if (isDuplicatedName(creative.getName(), index) == -1) {
                if (creative.getState() == CREATIVE_STATE_2) {
                    LOGGER.debug("No correct file");
                    creative.setHiddenClass("");
                    return;
                }
                if (creative.isTextLink()) {
                    LOGGER.debug("Creative is a text link");
                    creative.getAsset().setData(creative.getName().getBytes("UTF-8"));
                }
                // When admin user save a creative and the campaign is live the creative goes live
                if (getCampaignMBean().isLiveCampaign() && this.isAdminUserLoggedIn()) {
                    if (creative.getActiveStatus().equals(ACTIVE_STATUS_PAUSE)) {
                        LOGGER.debug("Campaign is not new so creative is activated as Pending paused");
                        creative.setCreativeStatus(CreativeStatus.PAUSED);
                    } else {
                        LOGGER.debug("Campaign is not new so creative is activated as Pending");
                        creative.setCreativeStatus(CreativeStatus.ACTIVE);
                    }
                    if (!oldDto.getStatus().equals(creative.getCreativeStatus().getStatus())) {
                        trackChangedCreativeProperty(CreativeTrackedProperty.STATUS);
                    }
                }
   
                // Store if this is the first save time
                isCreativeFirstSave = (creative.getId() == null) ? true : false;
                
                // Determine whether need a re-approval
                boolean isReApprovalNeeded = isReApprovalNeeded();
                
                // Save with SSL confirmation (non-SSL or unsure)
                if (sslOverride != null) {
                	creative.setSslCompliant(sslOverride);
                	creative.setSslOverride(sslOverride);
                	
                // Save without SSL confirmation (creative is SSL compliant)
                } else {
                	creative.setSslCompliant(Boolean.TRUE);
                	creative.setSslOverride(Boolean.FALSE);
                }
                
                campaignDto.getCreatives().set(index, crService.save(creative, campaignDto, isReApprovalNeeded));
                creative = getCreativeByIndex(index);

                LOGGER.debug("Saved creative with id: " + creative.getId());
                editing = false;
                creative.setState(Constants.COMPLETE_STATE);
                creative.setHiddenClass("none");
                creative.setOpened(false);

                if (appliedToAll) {
                    saveCreativesApplied(creative.getId());
                }

                if (!getCampaignMBean().isNewCampaign() && (isCreativeChanged() || creative.getStatus().equals(Creative.Status.NEW))) {
                    LOGGER.debug("Campaign is not new and creative has changed");
                    creative = crService.submitCreative(creative, isDirectApproval(creative));

                    // Save this change in creative history
                    if (!isCreativeFirstSave) {
                        trackCreativeHistory(creative, null);
                    }
                }

                closeCreative();
                appliedToAll = false;
            } else {
                LOGGER.debug("Duplicated name");
                String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":name";
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.error.validation.creative.duplicatednamme");
            }
        }
        LOGGER.debug("saveCreative<--");
    }

    public void cancelCreative(ActionEvent event) {
        LOGGER.debug("cancelCreative-->");
        int index = getCreativeIndex(event);
        if (appliedToAll) {
            this.campaignDto.setCreatives(copyCreatives(lOldCreatives));
            appliedToAll = false;
        }
        CreativeDto creative = getCreativeByIndex(index);
        if (creative.getOldCreative() == null) {
            LOGGER.debug("old creative hasn't been saved in the dto");
            CreativeDto oldCreative = this.oldDto;
            campaignDto.getCreatives().set(index, oldCreative);
            creative = getCreativeByIndex(index);
            creative.setOldCreative(null);
            creative.setHiddenClass("none");
            creative.setOpened(false);
        } else {
            LOGGER.debug("get the old creative from the dto");
            campaignDto.getCreatives().set(index, creative.getOldCreative());
            creative = getCreativeByIndex(index);
            creative.setOldCreative(null);
            creative.setHiddenClass("none");
            creative.setOpened(false);
        }
        closeCreative();
        LOGGER.debug("cancelCreative<--");
    }

    public void deleteCreative(ActionEvent event) {
        LOGGER.debug("deleteCreative-->");
        int index = getCreativeIndex(event);
        CreativeDto creative = getCreativeByIndex(index);
        LOGGER.debug(CREATIVE_ID + creative.getId());
        if (appliedToAll) {
            this.campaignDto.setCreatives(copyCreatives(lOldCreatives));
            appliedToAll = false;
        }
        campaignDto.getCreatives().remove(index);
        if (creative.getId() != null) {
            crService.deleteCreative(creative);
        }
        closeCreative();
        clearChangedCreativeProperties();
        LOGGER.debug("deleteCreative<--");
    }

    // Don't remove method, needed for processing fields when click on apply to all
    public void processApply(ActionEvent event) {
        int index = getCreativeIndex(event);
        RequestContext.getCurrentInstance().execute("confirmationApply" + index + ".show()");
        String applyTo = (String) event.getComponent().getAttributes().get("applyTo");
        if ("language".equals(applyTo)) {
            applyLanguage = true;
            applyFinalDestination = false;
            applyDestination = false;
            trackChangedCreativeProperty(CreativeTrackedProperty.LANGUAGE_COPY_TO_ALL);
        } else if ("destination".equals(applyTo)) {
            applyDestination = true;
            applyFinalDestination = false;
            applyLanguage = false;
            trackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION_COPY_TO_ALL);
        } else if ("finalDestination".equals(applyTo)) {
            applyDestination = false;
            applyFinalDestination = true;
            applyLanguage = false;
            trackChangedCreativeProperty(CreativeTrackedProperty.FINAL_DESTINATION_COPY_TO_ALL);
        }
    }

    public void applyToAll(ActionEvent event) {
        LOGGER.debug("applyDestToAll-->");
        int index = getCreativeIndex(event);
        if (applyDestination) {
            applyDestination(getCreativeByIndex(index));
        } else if (applyFinalDestination) {
            applyFinalDestination(getCreativeByIndex(index));
        } else if (applyLanguage) {
            applyLanguage(getCreativeByIndex(index));
        }

        appliedToAll = true;
        LOGGER.debug("applyDestToAll<--");
    }
    
    public void applyToAllCancelled(/*ActionEvent event*/) {
        LOGGER.debug("applyDestToAllCancelled-->");
        if (applyDestination) {
            untrackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION_COPY_TO_ALL);
        } else if (applyFinalDestination) {
            untrackChangedCreativeProperty(CreativeTrackedProperty.FINAL_DESTINATION_COPY_TO_ALL);
        } else if (applyLanguage) {
            untrackChangedCreativeProperty(CreativeTrackedProperty.LANGUAGE_COPY_TO_ALL);
        }
        appliedToAll = false;
        LOGGER.debug("applyDestToAllCancelled<--");
    }

    public void nativeAdAttributesChangeListener(AjaxBehaviorEvent event) {
        LOGGER.debug("nativeAdAttributesChangeListener-->");
        int index = getCreativeIndex(event);
        CreativeDto creativeDto = getCreativeByIndex(index);
        LOGGER.debug(CREATIVE_ID + creativeDto.getId());

        if (creativeDto.getId() != null) {// && !dto.isNew()){
            if ((!creativeDto.getNativeAdInfo().getTitle().equals(oldDto.getNativeAdInfo().getTitle()))) {
                trackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_TITLE);
            } else {
                untrackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_TITLE);
            }
            if ((!creativeDto.getNativeAdInfo().getDescription().equals(oldDto.getNativeAdInfo().getDescription()))) {
                trackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_DESCRIPTION);
            } else {
                untrackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_DESCRIPTION);
            }
            if ((!creativeDto.getNativeAdInfo().getClickToAction().equals(oldDto.getNativeAdInfo().getClickToAction()))) {
                trackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_CLICK_TO_ACTION);
            } else {
                untrackChangedCreativeProperty(CreativeTrackedProperty.NATIVE_AD_CLICK_TO_ACTION);
            }
        }
        LOGGER.debug("nativeAdAttributesChangeListener<--");
    }
    
    public void parseVast(ActionEvent event) {
    	parseVast(getCreativeByIndex(event));
    }
    
    private void parseVast(CreativeDto creativeDto) {
    	LOGGER.debug("parseVast-->");
        loadVastMetaData(creativeDto);
        if (this.vastMetaData != null && this.vastMetaData.getVideoCreative() != null) {
            creativeDto.getDestination().setDestinationType(DestinationType.URL);
            creativeDto.getDestination().setData(this.vastMetaData.getVideoCreative().getClickThrough().getValue().trim());
            LOGGER.debug("Clicktrough url" + creativeDto.getDestination().getData());
        }
        LOGGER.debug("<--parseVast");
    }

    public MobileAdVastMetadataDto loadVastMetaData(CreativeDto creativeDto) {
        if (creativeDto != null && creativeDto.isVastVideo()) {
            this.vastMetaData = crService.processVastTag(creativeDto);
            if (this.vastMetaData != null && CollectionUtils.isNotEmpty(this.vastMetaData.getWarnings())) {
                StringBuilder sb = new StringBuilder();
                for (MobileAdVastMetadataDto.Warning warning : this.vastMetaData.getWarnings()) {
                    sb.append(FacesUtils.getBundleMessage(VAST_WARNING_MESSAGES_PREFIX + warning.getType().toString().toLowerCase(), warning.getValuesAsArray()));
                }
                String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":vastInfo";
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, sb.toString());
            }
        }
        return this.vastMetaData;
    }

    public MobileAdVastMetadataDto getVastMetaData() {
        return vastMetaData;
    }
    
    public void templateChangeListener(AjaxBehaviorEvent event) {
    	ExtendedCreativeTemplateDto templateDto = getCreativeTemplateByIndex(event);
    	CreativeDto creativeDto = getCreativeByIndex(event);
    	int templateIndex = getTemplateIndex(event);
    	String tag = templateDto.getTemplateOriginal();
        LOGGER.debug(CREATIVE_ID + creativeDto.getId());
        if (oldTags.size() > templateIndex && ((oldTags.get(templateIndex).getId() == null && !StringUtils.isEmpty(tag))
                || (oldTags.get(templateIndex).getId() != null && !oldTags.get(templateIndex).getTemplateOriginal().equals(tag)))) {
            LOGGER.debug("T changed");
            trackChangedCreativeProperty(CreativeTrackedProperty.TAG);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.TAG);
        }
    }

    public void vendorChangeListener(AjaxBehaviorEvent event) {
        LOGGER.debug("vendorChangeListener-->");
        CreativeDto creativeDto = getCreativeByIndex(event);
        if (creativeDto != null) {
            LOGGER.debug(CREATIVE_ID + creativeDto.getId());
            creativeDto.setExtendedCreativeTemplates(crService.getTemplates(creativeDto.getExtendedCreativeType()));
            
            if (oldVendor != null && !oldVendor.getId().equals(creativeDto.getExtendedCreativeType().getId())) {
            	trackChangedCreativeProperty(CreativeTrackedProperty.VENDOR);
            } else {
            	untrackChangedCreativeProperty(CreativeTrackedProperty.VENDOR);
            }
        }
        LOGGER.debug("vendorChangeListener<--");
    }

    public void loadCampaignDto(CampaignDto campaignDto) {
        LOGGER.debug("loadCampaignDto-->");
        if (this.campaignDto == null) {
            LOGGER.debug("Dto is null");
            this.campaignDto = new CampaignCreativeDto();
        }
        if (campaignDto != null) {
            LOGGER.debug("Campaign id: " + campaignDto.getId());
            this.campaignDto.setId(campaignDto.getId());
            this.campaignDto.setName(campaignDto.getName());
            this.campaignDto.setAdvertiser(campaignDto.getAdvertiser());
        } else {
            LOGGER.debug("Dto is null");
            this.campaignDto = new CampaignCreativeDto();
            showTextDetails = false;
            showCreativeDetails = false;
            submitStatus = SUBMIT_STATUS_CLOSED;
            vastMetaData = null;
        }
        editing = false;
        LOGGER.debug("loadCampaignDto<--");
    }

    public boolean isAllCreativesCompleted() {
        if (CollectionUtils.isEmpty(campaignDto.getCreatives())) {
            LOGGER.debug("No creatives");
            return false;
        }
        for (CreativeDto creative : campaignDto.getCreatives()) {
            if (creative.getState() > 0) {
                return true;
            }
        }
        return false;
    }

    public String statusToClass(String status) {
        String statusStr = FacesUtils.getBundleMessage(status);
        if ("Pending Paused".equals(statusStr)) {
            statusStr = "PENDING_PAUSED";
        } else {
            statusStr = statusStr.toUpperCase();
        }
        return statusStr;
    }

    public void upload() {
        showCreativeDetails = true;
    }

    public void multipleFileUpload(FileUploadEvent event) throws IOException {
        LOGGER.debug("multipleFileUpload-->");

        if (event != null && event.getFile() != null) {
            LOGGER.debug("File has been uploaded");
            addCreative(event.getFile(), false);
        }
        LOGGER.debug("multipleFileUpload<--");
    }

    public void uploadRepresentative(FileUploadEvent event) throws IOException {
        LOGGER.debug("uploadRepresentative-->");
        int index = getCreativeIndex(event);
        CreativeDto dto = getCreativeByIndex(index);
        dto.setIncompleteMessage(null);
        
        trackImageChange(event);
        
        if (!dto.isMissingImage()) {
            upoadLinkFile(event);
        } else if (event.getFile() != null) {
            LOGGER.debug("File has been uploaded");
            addImageToCreative(event.getFile(), dto, true);
        }
        String message = getCreativeByIndex(index).getIncompleteMessage();
        if (!StringUtils.isEmpty(message)) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, message, message);
            campaignDto.getCreatives().set(index, dto);
        }
        LOGGER.debug("uploadRepresentative<--");
    }

    public void changeFile(FileUploadEvent event) throws IOException {
        int index = getCreativeIndex(event);
        CreativeDto dto = getCreativeByIndex(index);
        dto.setIncompleteMessage(null);
        
        // Track image changes
        trackImageChange(event);
        
        upoadLinkFile(event);
        String message = getCreativeByIndex(index).getIncompleteMessage();
        if (!StringUtils.isEmpty(message)) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, message, message);
            campaignDto.getCreatives().set(index, dto);
        }
    }

    public void changeIconForNativeAd(FileUploadEvent event) throws IOException {
        CreativeDto dto = getCreativeByIndex(event);
        dto.setIncompleteMessage(null);
        
        // Track icon changes
        trackNativeImageChange(event, CreativeTrackedProperty.REPRESENTATIVE_ICON);
        
        try {
            dto = crService
                    .uploadIconForNativeAd(dto, event.getFile().getInputstream(), event.getFile().getContentType(), event.getFile().getContents(), event.getFile().getSize());
        } catch (java.io.IOException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_IO_EXCEPTION);
            throw e;
        } catch (SizeNotSupportedException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND);
            LOGGER.debug(CHANGE_FILE_SIZE_UNSUPPORTED);
            return;
        } catch (NotContentTypeException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED);
            LOGGER.debug(CHANGE_FILE_FORMAT_UNSUPPORTED);
            return;
        } catch (FileExtensionNotSupportedException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED, PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED);
            LOGGER.debug(CHANGE_FILE_EXT_NOT_RECOGNIZED);
            return;
        } catch (BigFileException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG, PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG);
            LOGGER.debug(CHANGE_FILE_BIGGER_THAN_ALLOWED);
            return;
        } catch (Exception e) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_GENERAL_EXCEPTION);
            throw e;
        }
    }

    public void changeImageForNativeAd(FileUploadEvent event) throws IOException {
        CreativeDto dto = getCreativeByIndex(event);
        dto.setIncompleteMessage(null);
        
        // Track image changes
        trackNativeImageChange(event, CreativeTrackedProperty.REPRESENTATIVE_IMAGE);
        
        try {
            dto = crService.uploadImageForNativeAd(dto, event.getFile().getInputstream(), event.getFile().getContentType(), event.getFile().getContents(), event.getFile()
                    .getSize());
        } catch (java.io.IOException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_IO_EXCEPTION);
            throw e;
        } catch (SizeNotSupportedException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND);
            LOGGER.debug(CHANGE_FILE_SIZE_UNSUPPORTED);
            return;
        } catch (NotContentTypeException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED);
            LOGGER.debug(CHANGE_FILE_FORMAT_UNSUPPORTED);
            return;
        } catch (FileExtensionNotSupportedException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED, PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED);
            LOGGER.debug(CHANGE_FILE_EXT_NOT_RECOGNIZED);
            return;
        } catch (BigFileException e) {
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG, PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG);
            LOGGER.debug(CHANGE_FILE_BIGGER_THAN_ALLOWED);
            return;
        } catch (Exception e) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_GENERAL_EXCEPTION);
            throw e;
        }
    }

    private void upoadLinkFile(FileUploadEvent event) throws IOException {
        LOGGER.debug("changeFile-->");
        int index = getCreativeIndex(event);
        UploadedFile file = event.getFile();

        CreativeDto dto = getCreativeByIndex(index);

        try {
            CreativeDto newCreative = crService.newImageForCreative(null, file.getInputstream(), file.getContentType(), file.getContents(), file.getSize());
            LOGGER.debug("Creative saved id: " + newCreative.getId());
            newCreative.setName(dto.getName());
            newCreative.setLanguage(dto.getLanguage());
            newCreative.setDestination(dto.getDestination());
            newCreative.setId(dto.getId());
            newCreative.setIndex(index);
            newCreative.setExtendedCreativeTemplates(dto.getExtendedCreativeTemplates());
            newCreative.setExtendedCreativeType(dto.getExtendedCreativeType());
            newCreative.setRichMedia(dto.isRichMedia());
            if (dto.getAsset() != null) {
                newCreative.getAsset().setId(dto.getAsset().getId());
            }
            newCreative.setState(dto.getState());
            newCreative.setHiddenClass("");
            newCreative.setCreativeStatus(dto.getCreativeStatus());
            newCreative.setStatus(dto.getStatus());
            newCreative.setOpened(true);
            newCreative.setOldCreative(campaignDto.getCreatives().set(index, newCreative));
            newCreative.setState(1);

            if (dto.isThirdPartyTag()) {
                newCreative.setClosedMode(true);
            } else {
                newCreative.setClosedMode(false);
            }
            newCreative.setAllowExternalAudit(false);
        } catch (java.io.IOException e) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_IO_EXCEPTION);
            throw e;
        } catch (SizeNotSupportedException e) {
            CreativeDto errorDto = getErrorDto(PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND, dto);
            errorDto.setOldCreative(campaignDto.getCreatives().set(index, errorDto));
            LOGGER.debug(CHANGE_FILE_SIZE_UNSUPPORTED);
            return;
        } catch (NotContentTypeException e) {
            CreativeDto errorDto = getErrorDto(PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED, dto);
            errorDto.setOldCreative(campaignDto.getCreatives().set(index, errorDto));
            LOGGER.debug(CHANGE_FILE_FORMAT_UNSUPPORTED);
            return;
        } catch (FileExtensionNotSupportedException e) {
            CreativeDto errorDto = getErrorDto(PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED, dto);
            errorDto.setOldCreative(campaignDto.getCreatives().set(index, errorDto));
            LOGGER.debug(CHANGE_FILE_EXT_NOT_RECOGNIZED);
            return;
        } catch (BigFileException e) {
            CreativeDto errorDto = getErrorDto(PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG, dto);
            errorDto.setOldCreative(campaignDto.getCreatives().set(index, errorDto));
            LOGGER.debug(CHANGE_FILE_BIGGER_THAN_ALLOWED);
            return;
        } catch (Exception e) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, PAGE_ERROR_COMMON_ERROR, PAGE_ERROR_COMMON_ERROR);
            dto.setHiddenClass("");
            LOGGER.debug(CHANGE_FILE_GENERAL_EXCEPTION);
            throw e;
        }
        LOGGER.debug("changeFile<--");
    }

    public StreamedContent getExistingStream() {
        LOGGER.debug("getExistingStream-->");
        FacesContext context = FacesContext.getCurrentInstance();
        String id = context.getExternalContext().getRequestParameterMap().get(INDEX);

        if ((campaignDto != null) && (StringUtils.isNotBlank(id))) {
            // Browser is requesting the image. Get ID value from actual request
            // param.
            List<CreativeDto> creatives = campaignDto.getCreatives();
            if (creatives != null) {
                Integer index = Integer.valueOf(id);
                if (creatives.size() > index) {
                    CreativeDto creativeDto = creatives.get(index);
                    if ((creativeDto != null) && (creativeDto.getAsset() != null) && creativeDto.getAsset().getData() != null) {
                        LOGGER.debug("getExistingStream<-- content avalilable, asset id: " + (creativeDto.getAsset() == null ? null : creativeDto.getAsset().getId()));
                        return new DefaultStreamedContent(new ByteArrayInputStream(creativeDto.getAsset().getData()), creativeDto.getAsset().getContentType().getMimeType());
                    }
                }
            }
        }

        // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
        LOGGER.debug("getExistingStream<-- Rendering the view, not content available");
        return new DefaultStreamedContent(new ByteArrayInputStream(EMPTY_BYTE_ARRAY));
    }

    public StreamedContent getNativeAdIconStream() {
        LOGGER.debug("getIconStream-->");
        FacesContext context = FacesContext.getCurrentInstance();
        String id = context.getExternalContext().getRequestParameterMap().get(INDEX);

        if ((campaignDto != null) && (id != null) && (CollectionUtils.isNotEmpty(campaignDto.getCreatives()))) {
            // Browser is requesting the image. Get ID value from actual request param.
            CreativeDto creativeDto = getCreativeByIndex(Integer.parseInt(id));
            AssetInfoDto icon;
            if (creativeDto != null && (icon = creativeDto.getNativeAdInfo().getIcon()) != null) {
                LOGGER.debug("getIconStream<-- content avalilable");
                return new DefaultStreamedContent(new ByteArrayInputStream(icon.getData()), icon.getContentType().getMimeType());
            }
        }

        // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
        LOGGER.debug("getIconStream<-- Rendering the view, not content available");
        return new DefaultStreamedContent(new ByteArrayInputStream(EMPTY_BYTE_ARRAY));
    }

    public StreamedContent getNativeAdImageStream() {
        LOGGER.debug("getNativeAdImageStream-->");
        FacesContext context = FacesContext.getCurrentInstance();
        String id = context.getExternalContext().getRequestParameterMap().get(INDEX);

        if (id != null) {
            // Browser is requesting the image. Get ID value from actual request param.
            CreativeDto creativeDto = getCreativeByIndex(Integer.parseInt(id));
            AssetInfoDto image;
            if (creativeDto != null && (image = creativeDto.getNativeAdInfo().getImage()) != null) {
                LOGGER.debug("getNativeAdImageStream<-- content avalilable");
                return new DefaultStreamedContent(new ByteArrayInputStream(image.getData()), image.getContentType().getMimeType());
            }
        }

        // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
        LOGGER.debug("getNativeAdImageStream<-- Rendering the view, not content available");
        return new DefaultStreamedContent(new ByteArrayInputStream(EMPTY_BYTE_ARRAY));
    }

    public void testLink(ActionEvent event) {
        String url = getCreativeByIndex(event).getDestination().getData();
        if (StringUtils.isNotEmpty(url)) {
            RequestContext.getCurrentInstance().execute("window.open('" + url + "', '_blank')");
        }
    }

    public void testFinalLink(ActionEvent event) {
        String url = getCreativeByIndex(event).getDestination().getFinalDestination();
        if (StringUtils.isNotEmpty(url)) {
            RequestContext.getCurrentInstance().execute("window.open('" + url + "', '_blank')");
        }
    }

    public String calculateWatermark(String value, Boolean isVast) {
        // String value = (String)e.getNewValue();
        // default one is urldest type.
        LOGGER.debug("calculateWatermark-->");
        String res = PAGE_CREATIVE_DESTINATIONTYPE_WATERMARK_URL;
        boolean isAndroidOnly = deviceTargetService.isAndroidOnly(getCampaignTargetingBean().getCampaignDto());
        LOGGER.debug("is android only: " + isAndroidOnly);
        boolean isIosOnly = deviceTargetService.isIOSOnly(getCampaignTargetingBean().getCampaignDto());
        LOGGER.debug("is Ios only: " + isIosOnly);

        if (isVast) {
            res = "page.campaign.creative.vast.destination.watermark";
        } else if (!StringUtils.isEmpty(value)) {
            if (Constants.URL.equals(value) || Constants.CALL.equals(value)) {
                res = PAGE_CREATIVE_DESTINATIONTYPE_WATERMARK_URL;
            } else if (Constants.AUDIO.equals(value)) {
                res = "page.creative.destinationtype.watermark.audio";
            } else if (Constants.VIDEO.equals(value)) {
                res = "page.creative.destinationtype.watermark.video";
            } else {
                if (isAndroidOnly) {
                    if (Constants.ANDROID.equals(value)) {
                        res = "page.creative.destinationtype.watermark.android";
                    }
                } else if (isIosOnly) {
                    if (Constants.IPHONE_APP_STORE.equals(value)) {
                        res = "page.creative.destinationtype.watermark.appstore";
                    } else if (Constants.ITUNES_STORE.equals(value)) {
                        res = "page.creative.destinationtype.watermark.itunes";
                    } else {
                        res = PAGE_CREATIVE_DESTINATIONTYPE_WATERMARK_URL;
                    }
                }
            }
        }
        LOGGER.debug("calculateWatermark<--");
        return FacesUtils.getBundleMessage(res);
    }

    public void openPanel() {
        LOGGER.debug("openPanel-->");
        editing = true;
        clearChangedCreativeProperties();
        this.submitStatus = "OPENED";
        CreativeDto dto = getCreativeByIndex(submitedIndex);
        dto.setOpened(true);
        dto.setHiddenClass("");
        if (dto.isTextLink()) {
            dto.setHiddenClass("");
            LOGGER.debug("Text link");
        }
        oldText = dto.getAdText();
        if (dto.getState() != Constants.ERROR_STATE && dto.getState() != Constants.INCOMPLETE_STATE) {
        	DestinationDto destination = dto.getDestination();
            if (destination != null) {
            	// MAD-1665 - Handle CALL as URL
            	if (dto.getCall()) {
            		destination.setDestinationType(DestinationType.URL);
            	} 
                oldUrl = destination.getData();
                oldFinalDestinationUrl = destination.getFinalDestination();
                oldDataIsFinalDestination = destination.isDataIsFinalDestination();
            }
            oldTranslation = dto.getEnglishTranslation();
            if (dto.getExtendedCreativeType() != null) {
                oldVendor = dto.getExtendedCreativeType();
            }
            if (!CollectionUtils.isEmpty(dto.getExtendedCreativeTemplates())) {
            	oldTags = new ArrayList<ExtendedCreativeTemplateDto>();
            	for(ExtendedCreativeTemplateDto oldTemplate : dto.getExtendedCreativeTemplates()){
            		ExtendedCreativeTemplateDto template = new ExtendedCreativeTemplateDto();
            		template.setContentForm(oldTemplate.getContentForm());
            		template.setId(oldTemplate.getId());
            		template.setTemplatePreprocessed(oldTemplate.getTemplatePreprocessed());
            		template.setTemplateOriginal(oldTemplate.getTemplateOriginal());
            		oldTags.add(template);
            	}
            }
            
            if (!CollectionUtils.isEmpty(dto.getCreativeAttributes())) {
                oldAttributes = new ArrayList<CreativeAttributeDto>();
                oldAttributes.addAll(dto.getCreativeAttributes());
            }
            oldBeaconUrls = new ArrayList<BeaconUrlDto>();
            if (destination != null) {
                for(BeaconUrlDto oldBeacon : destination.getBeaconUrls()){
                    BeaconUrlDto beacon = new BeaconUrlDto();
                    beacon.setUrl(oldBeacon.getUrl());
                    oldBeaconUrls.add(beacon);
                }
            }

            oldDto = dto.clone();
        } else {
            oldDto = dto.clone();
        }
        lOldCreatives = copyCreatives(campaignDto.getCreatives());
        LOGGER.debug("openPanel<--");
    }

    public String getWarningMessage() {
        CreativeDto creativeDto = getCreativeByIndex(submitedIndex);
        if (creativeDto.isTextLink()) {
            return FacesUtils.getBundleMessage("page.campaign.creative.changes.warning.textAd.first");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.creative.changes.warning.image.first");
        }
    }

    public void nameChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldDto.getName(), getCreativeByIndex(event).getName())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.NAME);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.NAME);
        }
    }
    
    public void statusChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldDto.getActiveStatus(), getCreativeByIndex(event).getActiveStatus())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.STATUS);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.STATUS);
        }
    }
    
    public void languageChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldDto.getLanguage(), getCreativeByIndex(event).getLanguage())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.LANGUAGE);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.LANGUAGE);
        }
    }
    
    public void translationChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldTranslation, getCreativeByIndex(event).getEnglishTranslation())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.TRANSLATION);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.TRANSLATION);
        }
    }
    
    public void adTextChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldText, getCreativeByIndex(event).getAdText())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.AD_TEXT);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.AD_TEXT);
        }
    }
    
    public void destinationChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldUrl, getCreativeByIndex(event).getDestination().getData())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION);
        }
    }
    
    public void finalDestinationChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldFinalDestinationUrl, getCreativeByIndex(event).getDestination().getFinalDestination())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.FINAL_DESTINATION);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.FINAL_DESTINATION);
        }
    }
    
    public void dataIsFinalDestinationChangeListener(AjaxBehaviorEvent event) {
        DestinationDto destinationDto = getCreativeByIndex(event).getDestination();
        if (ObjectUtils.notEqual(oldDataIsFinalDestination, destinationDto.isDataIsFinalDestination())) {
            if (destinationDto.isDataIsFinalDestination()) {
                destinationDto.setFinalDestination(null);
            }
            trackChangedCreativeProperty(CreativeTrackedProperty.IS_FINAL_DESTINATION);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.IS_FINAL_DESTINATION);
        }
    }
    
    public void destinationTypeChangeListener(AjaxBehaviorEvent event) {
        DestinationDto destinationDto = getCreativeByIndex(event).getDestination();
        destinationDto.setData("");
        destinationDto.setDataIsFinalDestination(true);
        destinationDto.setFinalDestination(null);
        if (ObjectUtils.notEqual(oldDto.getDestination().getDestinationType(), destinationDto.getDestinationType())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION_TYPE);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.DESTINATION_TYPE);
        }
    }
    
    public void beaconChangeListener(AjaxBehaviorEvent event) {
        trackBeaconChange(event);
    }
    
    public void doAddBeacon(ActionEvent event) {
        getCreativeByIndex(event).getDestination().getBeaconUrls().add(new BeaconUrlDto());
    }

    public void doRemoveBeacon(ActionEvent event) {
        getCreativeByIndex(event).getDestination().getBeaconUrls().remove(getBeaconIndex(event));
        trackBeaconChange(event);
    }
    
    private void trackBeaconChange(FacesEvent event) {
        Set<String> oldUrls = getNonEmptyUrlSet(oldBeaconUrls);
        Set<String> newUrls = getNonEmptyUrlSet(getCreativeByIndex(event).getDestination().getBeaconUrls());
        if (oldUrls.size() == newUrls.size() && oldUrls.containsAll(newUrls)) {
            untrackChangedCreativeProperty(CreativeTrackedProperty.BEACON);
        } else {
            trackChangedCreativeProperty(CreativeTrackedProperty.BEACON);
        }
    }
    
	private void trackImageChange(FileUploadEvent fileUploadEvent) {
		List<ContentSpecDto> oldContents = oldDto.getContentSpecs();
		int oldContentLength = (oldDto.getAssets() == null || oldContents.isEmpty() || oldDto.getAssets().get(oldContents.get(0).getId()).getData() == null) ?
								-1 : oldDto.getAssets().get(oldContents.get(0).getId()).getData().length;
		int newContentLength = fileUploadEvent.getFile().getContents().length;
		if (ObjectUtils.notEqual(oldContentLength, newContentLength)) {
			trackChangedCreativeProperty(CreativeTrackedProperty.REPRESENTATIVE_IMAGE);
		} else {
			untrackChangedCreativeProperty(CreativeTrackedProperty.REPRESENTATIVE_IMAGE);
		}
	}
	
	private void trackNativeImageChange(FileUploadEvent fileUploadEvent, CreativeTrackedProperty trackedProperty) {
		NativeAdInfoDto oldNativeInfo = oldDto.getNativeAdInfo();
        int oldContentLength = (oldNativeInfo == null || oldNativeInfo.getImage() == null || oldNativeInfo.getImage().getData() == null) ?
        						-1 : oldNativeInfo.getImage().getData().length;
        int newContentLength = fileUploadEvent.getFile().getContents().length;
		if (ObjectUtils.notEqual(oldContentLength, newContentLength)) {
	        trackChangedCreativeProperty(trackedProperty);
	    } else {
	        untrackChangedCreativeProperty(trackedProperty);
	    }
	}
    
    private Set<String> getNonEmptyUrlSet(List<BeaconUrlDto> beaconUrlDtoList) {
        Set<String> urls = new HashSet<String>();
        for (BeaconUrlDto beaconUrlDto : beaconUrlDtoList) {
            String beaconUrl = beaconUrlDto.getUrl();
            if (!StringUtils.isEmpty(beaconUrl)) {
                urls.add(beaconUrl);
            }
        }
        return urls;
    }
        
    public void creativeAttributeChangeListener(AjaxBehaviorEvent event) {
        if (ObjectUtils.notEqual(oldAttributes, getCreativeByIndex(event).getCreativeAttributes())) {
            trackChangedCreativeProperty(CreativeTrackedProperty.ATTRIBUTE);
        } else {
            untrackChangedCreativeProperty(CreativeTrackedProperty.ATTRIBUTE);
        }
    }

    public boolean isCommitDisabled() {
        return !isCreativeChanged();
    }

    public void removeNotCommonDestinations() {
        for (CreativeDto creative : campaignDto.getCreatives()) {
            if (!creative.getDestination().getDestinationType().equals(DestinationType.URL) && !creative.getDestination().getDestinationType().equals(DestinationType.CALL)) {
                creative.getDestination().setDestinationType(DestinationType.URL);
                creative = crService.submitCreative(creative, isDirectApproval(creative));
            }
        }
    }

    public CampaignCreativeDto getCampaignDto() {
        if (campaignDto == null) {
            campaignDto = new CampaignCreativeDto();
        }
        return campaignDto;
    }

    public void openCreative(ActionEvent event) {
        LOGGER.debug("openCreative-->");
        if (submitStatus.equals(SUBMIT_STATUS_CLOSED)) {
            int index = getCreativeIndex(event);
            LOGGER.debug("Save creative with index in list: " + index);
            this.submitedIndex = index;
            openPanel();
        }
        LOGGER.debug("openCreative<--");
    }

    public boolean creativeEditable(int creativeIndex) {
        if (!CollectionUtils.isEmpty(campaignDto.getCreatives()) && creativeIndex < campaignDto.getCreatives().size()) {
            CreativeDto creative = getCreativeByIndex(creativeIndex);
            boolean thirdPartyAccess = isThirdPartyRole();

            if ((creative.isThirdPartyTag() || creative.isRichMedia()) && !thirdPartyAccess) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isThirdPartyRole() {
        List<String> roles = new ArrayList<String>(0);
        roles.add(Role.COMPANY_ROLE_THIRD_PARTY_TAGS);
        roles.add(Constants.LOGGED_IN_AS_ADMIN_ROLE);
        if (SecurityUtils.hasUserRoles(roles)) {
            return true;
        }
        return false;
    }
    
    public boolean hasMaxBeacons(int creativeId) {
        if (creativeId < campaignDto.getCreatives().size()) {
            return campaignDto.getCreatives().get(creativeId).getDestination().getBeaconUrls().size() >= Constants.MAX_BEACONS;
        } else {
            return false;
        }
    }

    public void setCampaignDto(CampaignCreativeDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public boolean getCreativeTableRendered() {
        return campaignDto != null && !campaignDto.getCreatives().isEmpty();
    }

    public boolean isShowCreativeDetails() {
        return showCreativeDetails;
    }

    public void setShowCreativeDetails(boolean showCreativeDetails) {
        this.showCreativeDetails = showCreativeDetails;
    }

    public List<LanguageDto> getFirstLanguages() {
        if (firstLanguages == null) {
            firstLanguages = cService.getFirstLanguages();
        }
        return firstLanguages;
    }

    public void setFirstLanguages(List<LanguageDto> languages) {
        this.firstLanguages = languages;
    }

    public List<LanguageDto> getSecondLanguages() {
        if (secondLanguages == null) {
            secondLanguages = cService.getSecondLanguages();
        }
        return secondLanguages;
    }

    public void setSecondLanguages(List<LanguageDto> secondLanguages) {
        this.secondLanguages = secondLanguages;
    }

    public boolean isShowTextDetails() {
        return showTextDetails;
    }

    public void setShowTextDetails(boolean showTextDetails) {
        this.showTextDetails = showTextDetails;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getCurrentCreative() {
        return currentCreative;
    }

    public void setCurrentCreative(int currentCreative) {
        this.currentCreative = currentCreative;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public boolean getNewCreative() {
        return getCreativeByIndex(currentCreative).getState() == 0;
    }

    public String getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(String submitStatus) {
        this.submitStatus = submitStatus;
    }

    public int getSubmitedIndex() {
        return submitedIndex;
    }

    public void setSubmitedIndex(int submitedIndex) {
        this.submitedIndex = submitedIndex;
    }

    public boolean isCreativeChanged() {
        return !changedCreativeProperties.isEmpty();
    }
    
    public boolean isReApprovalNeeded() {
        for (CreativeTrackedProperty creativeProperties : changedCreativeProperties) {
            if (creativeProperties.isReApprovalNeeded()) {
                return true;
            }
        }
        return false;
    }

    public List<ExtendedCreativeTypeDto> getExtendedTypes() {
        return crService.getExtendedCreativeTypes(false, isAdminUserLoggedIn());
    }

    public void setExtendedTypes(List<ExtendedCreativeTypeDto> extendedTypes) {
        this.extendedTypes = extendedTypes;
    }

    public List<ExtendedCreativeTypeDto> getRichMediaExtendedTypes() {
        return crService.getExtendedCreativeTypes(true, isAdminUserLoggedIn());
    }

    public void setRichMediaExtendedTypes(List<ExtendedCreativeTypeDto> richMediaExtendedTypes) {
        this.richMediaExtendedTypes = richMediaExtendedTypes;
    }

    public List<DestinationType> getDestinationTypes() {
        boolean isAndroidOnly = deviceTargetService.isAndroidOnly(getCampaignTargetingBean().getCampaignDto());
        boolean isIosOnly = deviceTargetService.isIOSOnly(getCampaignTargetingBean().getCampaignDto());
        destinationTypes = crService.getDestinationTypes(isAndroidOnly, isIosOnly);
        return destinationTypes;
    }

    public void setDestinationTypes(List<DestinationType> destinationTypes) {
        this.destinationTypes = destinationTypes;
    }

    public List<ContentForm> getContentForms() {
        return contentForms;
    }

    public void setContentForms(List<ContentForm> contentForms) {
        this.contentForms = contentForms;
    }

    public boolean isNewCampaign() {
        return getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.NEW)
                || getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.NEW_REVIEW);
    }

    public String getSaveButtonMessage() {
        if (isNewCampaign()) {
            return FacesUtils.getBundleMessage("page.campaign.creative.savecreative.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.creative.commit.label");
        }
    }

    public String getContinueButtonMessage() {
        if (isNewCampaign()) {
            return FacesUtils.getBundleMessage("page.campaign.setup.button.save.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.creative.done.label");
        }
    }

    public LanguageDto getEmptyLanguage() {
        return new LanguageDto();
    }

    public ExtendedCreativeTypeDto getEmptyExtendedType() {
        return new ExtendedCreativeTypeDto();
    }

    public String getCreativeSummaryFormat(Integer creativeIdx) {
        String format = "";

        CreativeDto creativeDto = this.getCreativeByIndex(creativeIdx);
        if (creativeDto != null) {
            if (creativeDto.isNativeAd()) {
                format = FacesUtils.getBundleMessage("page.campaign.creative.nativead.format.label");
            } else if (creativeDto.isVastVideo()) {
                format = FacesUtils.getBundleMessage("page.campaign.creative.vast.format.label");
            } else if (creativeDto.getContentSpec() != null) {
                format = creativeDto.getContentSpec().getName();
            }
        }

        return format;
    }

    /******
     * PRIVATE METHODS
     * 
     * @throws Exception
     * @throws IOException
     *******/
    private void addCreative(UploadedFile file, boolean single) throws IOException {
        LOGGER.debug("addCreative-->");
        CreativeDto creativeDto = addImageToCreative(file, null, single);
        if (creativeDto == null) {
            showCreativeDetails = false;
            submitStatus = SUBMIT_STATUS_CLOSED;
            return;
        }
        creativeDto.setName(getCheckedDuplicatedName(file.getFileName().split("\\.")[0], -1));
        creativeDto.setCreativeStatus(CreativeStatus.NEW);
        creativeDto.setIndex(campaignDto.getCreatives().size());
        creativeDto.setState(Constants.INCOMPLETE_STATE);
        DestinationDto destination = new DestinationDto();
        BeaconUrlDto beacon = new BeaconUrlDto();
        destination.getBeaconUrls().add(beacon);
        creativeDto.setDestination(destination);
        campaignDto.getCreatives().add(creativeDto);
        if (single) {
            creativeDto.setHiddenClass("");
            creativeDto.setOpened(true);
            creativeDto.setIncompleteMessage("page.campaign.creative.incomplete.nodestination");
            submitedIndex = campaignDto.getCreatives().size() - 1;
            openPanel();
        } else {
            creativeDto.setHiddenClass("none");
            creativeDto.setOpened(false);
            if (creativeDto.getDestination() == null || creativeDto.getDestination().getId() == null) {
                creativeDto.setIncompleteMessage("page.campaign.creative.incomplete.nodestination");
            } else if (creativeDto.getLanguage() == null) {
                creativeDto.setIncompleteMessage("page.campaign.creative.incomplete.nolanguage");
            } else if (!creativeDto.getEnglish() && creativeDto.getEnglishTranslation() == null) {
                creativeDto.setIncompleteMessage("page.campaign.creative.incomplete.notraslation");
            }
        }

        LOGGER.debug("addCreative<--");
    }

    private CreativeDto addImageToCreative(UploadedFile file, CreativeDto creativeDto, boolean single) throws IOException {
        try {
            creativeDto = crService.newImageForCreative(creativeDto, file.getInputstream(), file.getContentType(), file.getContents(), file.getSize());
        } catch (java.io.IOException e) {
            submitStatus = SUBMIT_STATUS_CLOSED;
            LOGGER.debug(CHANGE_FILE_IO_EXCEPTION);
            throw e;
        } catch (SizeNotSupportedException e) {
            if (single) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, null, PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTFOUND);
            } else {
                campaignDto.getCreatives().add(newErrorCreative(PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED, file.getFileName().split("\\.")[0]));
            }
            submitStatus = SUBMIT_STATUS_CLOSED;
            LOGGER.debug(CHANGE_FILE_SIZE_UNSUPPORTED);
            return null;
        } catch (NotContentTypeException e) {
            if (single) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, null, "page.campaign.creative.error.typedoesntmatch");
            } else {
                campaignDto.getCreatives().add(newErrorCreative(PAGE_CAMPAIGN_CREATIVE_ERROR_SIZENOTRECOGNISED, file.getFileName().split("\\.")[0]));
            }
            submitStatus = SUBMIT_STATUS_CLOSED;
            LOGGER.debug(CHANGE_FILE_FORMAT_UNSUPPORTED);
            return null;
        } catch (FileExtensionNotSupportedException e) {
            if (single) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, null, "page.campaign.creative.error.extension");
            } else {
                campaignDto.getCreatives().add(newErrorCreative(PAGE_CAMPAIGN_CREATIVE_ERROR_TYPENOTRECOGNISED, file.getFileName().split("\\.")[0]));
            }
            submitStatus = SUBMIT_STATUS_CLOSED;
            LOGGER.debug(CHANGE_FILE_EXT_NOT_RECOGNIZED);
            return null;
        } catch (BigFileException e) {
            if (single) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, SUBMIT_FORM, null, "page.campaign.creative.error.big");
            } else {
                campaignDto.getCreatives().add(newErrorCreative(PAGE_CAMPAIGN_CREATIVE_ERROR_FILEBIG, file.getFileName().split("\\.")[0]));
            }
            LOGGER.debug(CHANGE_FILE_BIGGER_THAN_ALLOWED);
            return null;
        } catch (Exception e) {
            submitStatus = SUBMIT_STATUS_CLOSED;
            LOGGER.debug(CHANGE_FILE_GENERAL_EXCEPTION);
            throw e;
        }
        return creativeDto;
    }

    private CreativeDto newErrorCreative(String errorMessage, String name) {
        CreativeDto creativeDto = new CreativeDto();
        creativeDto.setHiddenClass("none");
        creativeDto.setOpened(false);
        creativeDto.setState(Constants.ERROR_STATE);
        creativeDto.setIncompleteMessage(errorMessage);
        creativeDto.setStatus(CreativeStatus.NEW.getStatus());
        creativeDto.setName(name);
        creativeDto.setContentSpecs(new ArrayList<ContentSpecDto>());
        creativeDto.getContentSpecs().add(getErrorContentSpec());
        DestinationDto destination = new DestinationDto();
        BeaconUrlDto beacon = new BeaconUrlDto();
        destination.getBeaconUrls().add(beacon);
        creativeDto.setDestination(destination);
        creativeDto.setFormat(null);
        creativeDto.setAssets(null);
        creativeDto.setLanguage(new LanguageDto());
        return creativeDto;
    }

    private ContentSpecDto getErrorContentSpec() {
        List<ContentSpecDto> contents = crService.getAllContentSpecs();
        for (ContentSpecDto contentSpec : contents) {
            if ("MPU / 300 x 250".equals(contentSpec.getName())) {
                contentSpec.setName(FacesUtils.getBundleMessage("page.campaign.creative.error.nofile"));
                return contentSpec;
            }
        }
        return null;
    }

    private void closeCreative() {
        submitStatus = SUBMIT_STATUS_CLOSED;
        editing = false;
        clearChangedCreativeProperties();
        oldUrl = null;
        oldTags = new ArrayList<ExtendedCreativeTemplateDto>();
        oldBeaconUrls = new ArrayList<BeaconUrlDto>();
        oldFinalDestinationUrl = null;
        oldDataIsFinalDestination = true;
        oldText = null;
        oldTranslation = null;
        vastMetaData = null;
    }

    private List<CreativeDto> copyCreatives(List<CreativeDto> creatives) {
        List<CreativeDto> copyList = new ArrayList<CreativeDto>();

        for (CreativeDto creative : creatives) {
            CreativeDto copy = creative.clone();
            copy.setHiddenClass(creative.getHiddenClass());
            copy.setOpened(creative.isOpened());
            copyList.add(copy);
        }
        return copyList;
    }

    private void applyDestination(CreativeDto creativeToCopy) {
        DestinationDto destinationToCopy = creativeToCopy.getDestination();
        if (destinationToCopy == null || destinationToCopy.getData() == null || destinationToCopy.getDestinationType() == null || !isDestinationCopyable(creativeToCopy)) {
            LOGGER.debug("Destination for creative " + creativeToCopy.getName() + CANNOT_BE_COPIED);
            return;
        }
        for (CreativeDto creative : campaignDto.getCreatives()) {
            if (isDestinationCopyable(creative)) {
                boolean changed = false;
                if (creative.getCreativeStatus().equals(CreativeStatus.NEW) || creative.getCreativeStatus().equals(CreativeStatus.ACTIVE)) {
                    if (creative.getDestination() == null || creative.getDestination().getData() == null || creative.getDestination().getDestinationType() == null) {
                        creative.setDestination(new DestinationDto());
                        changed = true;
                    } else if (!creative.getDestination().getData().equals(destinationToCopy.getData())
                            || !creative.getDestination().getDestinationType().equals(destinationToCopy.getDestinationType())
                            || creative.getDestination().isDataIsFinalDestination() != destinationToCopy.isDataIsFinalDestination()
                            || (destinationToCopy.getFinalDestination() != null && creative.getDestination().getFinalDestination() == null)
                            || (destinationToCopy.getFinalDestination() == null && creative.getDestination().getFinalDestination() != null)
                            || (destinationToCopy.getFinalDestination() != null && creative.getDestination().getFinalDestination() != null && !creative.getDestination()
                                    .getFinalDestination().equals(destinationToCopy.getFinalDestination()))) {
                        changed = true;
                    }
                    LOGGER.debug("Changed: " + changed);
                    LOGGER.debug(creative.getDestination().isDataIsFinalDestination() + ":" + destinationToCopy.isDataIsFinalDestination());
                    creative.getDestination().setData(destinationToCopy.getData());
                    creative.getDestination().setFinalDestination(destinationToCopy.getFinalDestination());
                    creative.getDestination().setDataIsFinalDestination(destinationToCopy.isDataIsFinalDestination());
                    // AO-403 - do not copy beacon to all creatives. Only copy when a campaign copy.
                    // creative.getDestination().setBeaconUrl(destinationToCopy.getBeaconUrl());
                    creative.getDestination().setDestinationType(destinationToCopy.getDestinationType());
                    if (creative.getState() == Constants.INCOMPLETE_STATE && creative.getLanguage() != null && creative.getLanguage().getId() != null
                            && ((creative.getEnglish()) || (!creative.getEnglish() && !StringUtils.isEmpty(creative.getEnglishTranslation())))) {
                        LOGGER.debug(CREATIVE + creative.getName() + IS_COMPLETED_NOW);
                        creative.setState(Constants.COMPLETE_STATE);
                    }
                    creative.setChangedToCommit(changed);
                }
            }
        }
    }

    private boolean isDestinationCopyable(CreativeDto creative) {
        return !creative.isVastVideo();
    }

    private void applyFinalDestination(CreativeDto creativeToCopy) {
        String finalDestinationToCopy = creativeToCopy.getDestination().getFinalDestination();
        DestinationDto destinationToCopy = creativeToCopy.getDestination();
        if (finalDestinationToCopy == null || !isDestinationCopyable(creativeToCopy)) {
            LOGGER.debug("Final destination for creative " + creativeToCopy.getName() + CANNOT_BE_COPIED);
            return;
        }
        for (CreativeDto creative : campaignDto.getCreatives()) {
            if (isDestinationCopyable(creative)) {
                boolean changed = false;
                if (creative.getCreativeStatus().equals(CreativeStatus.NEW) || creative.getCreativeStatus().equals(CreativeStatus.ACTIVE)) {
                    if (creative.getDestination() == null || creative.getDestination().getData() == null || creative.getDestination().getDestinationType() == null) {
                        creative.setDestination(new DestinationDto());
                        creative.getDestination().setData(destinationToCopy.getData());

                        creative.getDestination().setDataIsFinalDestination(destinationToCopy.isDataIsFinalDestination());
                        creative.getDestination().setDestinationType(destinationToCopy.getDestinationType());
                        changed = true;
                    } else if (creative.getDestination().getFinalDestination() == null || !creative.getDestination().getFinalDestination().equals(finalDestinationToCopy)) {
                        creative.getDestination().setDataIsFinalDestination(destinationToCopy.isDataIsFinalDestination());
                        changed = true;
                    }

                    LOGGER.debug("Changed: " + changed);
                    creative.getDestination().setFinalDestination(destinationToCopy.getFinalDestination());

                    if (creative.getState() == Constants.INCOMPLETE_STATE && creative.getLanguage() != null && creative.getLanguage().getId() != null
                            && ((creative.getEnglish()) || (!creative.getEnglish() && !StringUtils.isEmpty(creative.getEnglishTranslation())))) {
                        LOGGER.debug(CREATIVE + creative.getName() + IS_COMPLETED_NOW);
                        creative.setState(Constants.COMPLETE_STATE);
                    }
                    creative.setChangedToCommit(changed);
                }
            }
        }
    }

    private void applyLanguage(CreativeDto creativeToCopy) {
        LanguageDto languageToCopy = creativeToCopy.getLanguage();
        if (languageToCopy == null || languageToCopy.getId() == null) {
            LOGGER.debug("Language for creative " + creativeToCopy.getName() + CANNOT_BE_COPIED);
            return;
        }
        for (CreativeDto creative : campaignDto.getCreatives()) {
            boolean changed = false;
            if (creative.getCreativeStatus().equals(CreativeStatus.NEW) || creative.getCreativeStatus().equals(CreativeStatus.ACTIVE)) {
                if (creative.getLanguage() == null) {
                    creative.setLanguage(new LanguageDto());
                    changed = true;
                } else if (!creative.getLanguage().equals(languageToCopy)) {
                    changed = true;
                }
                creative.setLanguage(languageToCopy);
                if (!languageToCopy.getName().equals(LANG_ENGLISH) && StringUtils.isEmpty(creative.getEnglishTranslation()) && creative.getState() != Constants.ERROR_STATE) {
                    creative.setState(Constants.INCOMPLETE_STATE);
                } else if (creative.getState() == Constants.INCOMPLETE_STATE && creative.getDestination() != null && !StringUtils.isEmpty(creative.getDestination().getData())
                        && ((creative.getEnglish()) || (!creative.getEnglish() && !StringUtils.isEmpty(creative.getEnglishTranslation())))) {
                    LOGGER.debug(CREATIVE + creative.getName() + IS_COMPLETED_NOW);
                    creative.setState(Constants.COMPLETE_STATE);
                }
            }
            creative.setChangedToCommit(changed);
        }
    }

    private void saveCreativesApplied(Long triggeredCreativeId) {
        for (int i = 0; i < campaignDto.getCreatives().size(); i++) {
            CreativeDto creative = getCreativeByIndex(i);

            boolean changed = creative.isChangedToCommit();
            if (creative.getState() == Constants.COMPLETE_STATE) {
            	// Re-check SSL compliance
            	creative.setSslCompliant(isCreativeSslCompliant(creative));
                creative = crService.save(creative, this.campaignDto, changed && isReApprovalNeeded());
                if (!getCampaignMBean().isNewCampaign() && changed) {
                    LOGGER.debug("Campaign is not new and creative has changed, so it is submitted");
                    creative = crService.submitCreative(creative, isDirectApproval(creative));
                    trackCreativeHistory(creative, "creative id " + String.format("%d", triggeredCreativeId));
                    campaignDto.getCreatives().set(i, creative);
                }
            }
        }
    }

    /**
     * Direct approval aka creative status won't go to pending.
     * 
     * So direct approval only if NOT {@link #isCreativeShouldGoToPending(CreativeDto)} AND the campaign is live.
     */
    public boolean isDirectApproval(CreativeDto creative) {
        return !isCreativeShouldGoToPending(creative) && getCampaignMBean().isLiveCampaign();
    }

    public boolean isCreativeGoToPendingWarningVisible(CreativeDto creative) {
        return !creative.isRejected() && !creative.isNewCreative() && isCreativeChanged() && isCreativeShouldGoToPending(creative);
    }

    /**
     * We know that creative goes to pending only in the following cases:
     *   1.) (User OR Admin AND creative is tag based) AND re-approval needed
     *   2.) User AND Creative is rejected AND changed
     */
    public boolean isCreativeShouldGoToPending(CreativeDto creative) {
        return (!isAdminUserLoggedIn() || isAdminUserLoggedIn() && creative.isTagBased()) && isReApprovalNeeded() ||
                !isAdminUserLoggedIn() && creative.isRejected() && isCreativeChanged(); 
    }
    
    public Boolean isCreativeSslCompliant(CreativeDto creativeDto) {
    	return crService.isSslCompliant(creativeDto);
    }

    private String getCheckedDuplicatedName(String name, int creativeIndex) {
        String returnName = name;
        if (isDuplicatedName(name, creativeIndex) != -1) {
            boolean foundName = false;
            int append = TWO;
            while (!foundName) {
                returnName = name + append;
                foundName = isDuplicatedName(returnName, creativeIndex) == -1;
                append++;
            }
        }
        return returnName;
    }

    private CreativeDto getErrorDto(String message, CreativeDto oldDto) {
        CreativeDto errorDto = newErrorCreative(message, oldDto.getName());
        errorDto.setOpened(false);
        errorDto.setHiddenClass("");
        errorDto.setLanguage(oldDto.getLanguage());
        errorDto.setDestination(oldDto.getDestination());
        errorDto.setCreativeStatus(oldDto.getCreativeStatus());
        errorDto.setStatus(oldDto.getStatus());

        return errorDto;
    }

    private int isDuplicatedName(String name, int creativeIndex) {
        for (int i = 0; i < campaignDto.getCreatives().size(); i++) {
            if (i != creativeIndex && !StringUtils.isEmpty(getCreativeByIndex(i).getName()) && getCreativeByIndex(i).getName().equals(name)) {
                LOGGER.debug("Duplicated name: " + getCreativeByIndex(i).getName() + " in creative with index " + i);
                return i;
            }
        }
        return -1;
    }

    private boolean isValidCreative(CreativeDto creative) {
        if (creative.isThirdPartyTag()) {
            if ((creative.getExtendedCreativeType() == null || creative.getExtendedCreativeType().getId() == null)) {
                LOGGER.debug("Invalid: ThirdPartyTag has no ExtendedCreativeType");
                String clientId;
                if (creative.isRichMedia()) {
                    clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":vendor2";
                } else {
                    clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":vendor";
                }
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.campaign.creative.error.novendor");
                return false;
            }
            if (!validThirdPartyTags(creative)) {
                LOGGER.debug("Invalid: ThirdPartyTag is invalid");
                String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":contentForms";
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.campaign.creative.error.emptytags");
                return false;
            }

            if (creative.isVastVideo()) {
                MobileAdVastMetadataDto vastMetadata = loadVastMetaData(creative);
                if (CollectionUtils.isNotEmpty(vastMetadata.getWarnings())) {
                    return false;
                }
            } else {
                if (creative.getFormat() == null || creative.isMissingImage()) {
                    LOGGER.debug("Invalid: Missing image or format");
                    String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId()
                            + ":representativeUpload";
                    addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.campaign.creative.error.noimage");
                    return false;
                }
            }
        }
        // Final destination
        ValidationResult validation = ValidationUtils.validateUrl(creative.getDestination().getFinalDestination());
        if (isAdminUserLoggedIn() && !creative.getDestination().isDataIsFinalDestination() && validation.isFailed()) {
            LOGGER.debug("Invalid final destination");
            String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + ":finalDestinationURL";
            addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, validation.getMessageKey());
            return false;
        }
        // Native ads
        if (creative.isNativeAd()) {
            if (creative.getNativeAdInfo().getIcon() == null) {
                LOGGER.debug("No icon");
                String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.campaign.creative.native.noicon.error");
                return false;
            }
            if (creative.getNativeAdInfo().getImage() == null) {
                LOGGER.debug("No image");
                String clientId = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATIVES_CONTAINER).getChildren().get(0).getClientId() + SAVE_BUTTON;
                addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, "page.campaign.creative.native.noimage.error");
                return false;
            }
        }

        return true;
    }

    private boolean validThirdPartyTags(CreativeDto creative) {
        // AO-418 content only required if isUseDynamicTemplates
        if (!creative.getExtendedCreativeType().isUseDynamicTemplates()) {
            return true;
        }

        boolean emptyTags = true;
        if (!CollectionUtils.isEmpty(creative.getExtendedCreativeTemplates())) {
            for (ExtendedCreativeTemplateDto ect : creative.getExtendedCreativeTemplates()) {
                if (!StringUtils.isEmpty(ect.getTemplateOriginal())) {
                    emptyTags = false;
                }
            }
        }
        return !emptyTags;
    }

    private void setDefaultCreativeAttributes(CreativeDto dto) {
        if (dto.getFormat() != null && TEXT_FORMAT_SYSTEM_NAME.equals(dto.getFormat().getSystemName()) && !dto.getCreativeAttributes().contains(DEFAULT_TEXT_ATTRIBUTE)) {
            dto.getCreativeAttributes().add(DEFAULT_TEXT_ATTRIBUTE);
        } else if (dto.isRichMedia() && !dto.getCreativeAttributes().contains(DEFAULT_RICH_MEDIA_ATTRIBUTE)) {
            dto.getCreativeAttributes().add(DEFAULT_RICH_MEDIA_ATTRIBUTE);
        } else if (dto.isVastVideo() && !dto.getCreativeAttributes().contains(DEFAULT_VAST_VIDEO_ATTRIBUTE)) {
            dto.getCreativeAttributes().add(DEFAULT_VAST_VIDEO_ATTRIBUTE);
        }
    }
    
    private void trackCreativeHistory(CreativeDto creative, String triggerBy) {
        StringBuilder historyComment = new StringBuilder(CREATIVE);
        historyComment.append(StringUtils.join(changedCreativeProperties, "/")).append(" changed by ");
        if (StringUtils.isBlank(triggerBy)) {
            historyComment.append((this.isAdminUserLoggedIn()) ? "admin" : getUser().getUser().getEmail());
        } else {
            historyComment.append(triggerBy);
        }
        cService.newCreativeHistory(creative, historyComment.toString(), getAdfonicUser());
    }
    
    private CreativeDto getCreativeByIndex(FacesEvent event) {
        return getCreativeByIndex(getCreativeIndex(event));
    }
    
    private CreativeDto getCreativeByIndex(int index) {
        return campaignDto.getCreatives().get(index);
    }
    
    private ExtendedCreativeTemplateDto getCreativeTemplateByIndex(FacesEvent event) {
    	return getCreativeTemplateByIndex(getCreativeIndex(event), getTemplateIndex(event));
    }
    
    private ExtendedCreativeTemplateDto getCreativeTemplateByIndex(int crativeIndex, int templateIndex) {
        return getCreativeByIndex(crativeIndex).getExtendedCreativeTemplates().get(templateIndex);
    }
    
    private int getCreativeIndex(FacesEvent event) {
        return ((Integer) event.getComponent().getAttributes().get(Constants.CREATIVE_INDEX)).intValue();
    }
    
    private int getTemplateIndex(FacesEvent event) {
        return ((Integer) event.getComponent().getAttributes().get(Constants.TEMPLATE_INDEX)).intValue();
    }
    
    private int getCreativeIndexFromParam() {
        return Integer.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(Constants.CREATIVE_INDEX));
    }
    
    private String getSslOverrideFromParam() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(Constants.SSL_OVERRIDE);
    }
    
    private int getBeaconIndex(FacesEvent event) {
        return ((Integer) event.getComponent().getAttributes().get(Constants.BEACON_INDEX)).intValue();
    }
    
    private void trackChangedCreativeProperty(CreativeTrackedProperty changedCreativeProperty) {
        changedCreativeProperties.add(changedCreativeProperty);
    }
    
    private void untrackChangedCreativeProperty(CreativeTrackedProperty changedCreativeProperty) {
        changedCreativeProperties.remove(changedCreativeProperty);
    }
    
    private void clearChangedCreativeProperties() {
        changedCreativeProperties.clear();
    }
    
}
