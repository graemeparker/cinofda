package com.adfonic.tools.beans.audience.source;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;
import static com.adfonic.presentation.audience.service.AudienceService.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.lang3.text.WordUtils;
import org.elasticsearch.ElasticsearchException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Audience;
import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.dto.advertiser.AdvertiserCloudInformationDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.CampaignUsingAudienceDto;
import com.adfonic.dto.audience.DMPAttributeDto;
import com.adfonic.dto.audience.DMPAudienceDto;
import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.dto.audience.DMPSelectorForDMPAudienceDto;
import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.dto.audience.FirstPartyAudienceCampaignDto;
import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.audience.enums.AudienceType;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.resultwrapper.DeviceIdsValidated;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.enums.FileType;
import com.adfonic.presentation.audience.service.AudienceFileService;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.FirstPartyAudienceDeviceIdsUploadHistorySortBy;
import com.adfonic.presentation.audienceengine.exception.AudienceEngineApiException;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;
import com.adfonic.presentation.user.UserService;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.util.AbstractLazyDataModelWrapper;
import com.byyd.middleware.account.exception.AdvertiserCloudManagerException;

@Component
@Scope("view")
public class AudienceSourceMBean extends GenericAbstractBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceSourceMBean.class);

    public static final String DASH = "-";
    
    // DMP Options
    public static final String DMP_DEFAULT_SEGMENTS_TYPE = "DEFAULT_SEGMENTS";
    public static final String DMP_CUSTOM_SEGMENT_TYPE = "CUSTOM_SEGMENT";
    
    // Factual Options
    public static final String DMP_FACTUAL_PROXIMITY_TYPE = "PROXIMITY";
    public static final String DMP_FACTUAL_AUDIENCE_TYPE = "AUDIENCE";
    
    public static final String FACTUAL_AUDIENCE_REPEAT_CLIENT_ID = "customFactualAudienceRepeat";
    public static final String FACTUAL_AUDIENCE_CLIENT_ID = "customFactualAudienceId";
    public static final String FACTUAL_PROXIMITY_CLIENT_ID = "customFactualProximityId";
    
    public static final String FACTUAL = "Factual";

    private static final char SUMMARY_SEPARATOR = ';';
    private static final char SPACE = ' ';
    private static final char COLON = ':';
    private static final char ITEM_SEPARATOR = ',';
    private static final String INSTALLERS_LABEL_KEY = "page.audience.source.label.installers";
    private static final String CONVERTERS_LABEL_KEY = "page.audience.source.label.converters";
    private static final String CLICKERS_LABEL_KEY = "page.audience.source.label.clickers";

    private static final String NO_CAMPAIGNS_MSG_KEY = "error.audience.source.nocampaigns";
    private static final String NO_SELECTORS_MSG_KEY = "error.audience.source.noselectors";
    private static final String NO_SELECTOR_FACTUAL_PROXIMITY_MSG_KEY = "error.audience.invalid.factual.proximity.customSegmentId";
    private static final String NO_SELECTORS_FACTUAL_AUDIENCE_MSG_KEY = "error.audience.source.factual.audience.noselectors";
    private static final String NO_SELECTOR_MSG_KEY = "error.audience.invalid.customSegmentId";

    private static final String DMP_VENDOR_PANEL_ID = "dmpVendorPanel";
    private static final String DMP_CUSTOM_SEGMENT_ID = "customSegmentId";
    private static final String CAMPAIGN_TYPE_AHEAD_ID = "campaignTypeAhead";
    
    private static final String CREATE_S3_BUTTON_ID  = "createS3Button";
    private static final String DELETE_S3_BUTTON_ID  = "deleteS3Button";
    private static final String ASSIGN_S3_FILE_BUTTON_ID  = "assignForUnassignedButton";

    private static final String UPLOAD_DEVICE_IDS_FORM_KEY = "uploadDeviceIds";

    private static final String CONTINUE_BUTTON_ID = "continueBtn";
    
    private static final String DEVICE_UPLOAD_SUCCESS_KEY = "page.audience.source.upload.success";
    private static final String DEVICE_UPLOAD_ERROR_GENERAL_KEY = "page.audience.source.upload.error.general";
    private static final String DEVICE_UPLOAD_ERROR_MAXSIZE_KEY = "page.audience.source.upload.error.maxsize";
    private static final String DEVICE_UPLOAD_WARNING_NOTFOUND_KEY = "page.audience.source.upload.warning.notfound";
    private static final String DEVICE_UPLOAD_ERROR_CONTENTTYPE_KEY = "page.audience.source.upload.error.contenttype";
    private static final String DEVICE_TYPE_REQUIRED_KEY = "page.audience.validation.type.required";

    private static final String DEVICE_HISTORY_TOTAL_RECORDS_KEY = "page.audience.confirmation.upload.history.header.total";
    private static final String DEVICE_HISTORY_VALIDATED_RECORDS_KEY = "page.audience.confirmation.upload.history.header.validated";
    private static final String DEVICE_HISTORY_INSERTED_RECORDS_KEY = "page.audience.confirmation.upload.history.header.inserted";
    private static final String FILEMOVER_CONNECTIVITY_ISSUE_KEY = "page.audience.source.common.s3.filemover.connectivity.issue";
    private static final String NO_FILE_DEVICES_ERROR_KEY = "page.audience.source.file.nodevices";
    private static final String NO_S3_ASSIGNED_FILES_ERROR_KEY = "page.audience.source.s3.nofiles";
    
    private static final String MESSAGE_KEY_FACTUAL_AUDIENCE_SELECTOR_UNIQUE_ERROR = "page.audience.source.customSegmentId.factual.unique";
    private static final String MESSAGE_KEY_FACTUAL_DESIGN_ID_EXCHANGE_MISMATCH_ERROR = "page.audience.source.customSegmentId.factual.exchange.mismatch";
    
    @SuppressWarnings("serial")
    private static final List<String> ALLOWED_CONTENT_TYPES = new ArrayList<String>() {
        {
            add(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLS);
            add(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLSX);
            add(DEVICE_IDS_UPLOAD_CONTENT_TYPE_CSV);
        }
    };

    @Autowired
    private AudienceService audienceService;
    
    @Autowired
    private AudienceFileService audienceFileService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DeviceIdentifierService deviceIdentifierService;
    
    @Autowired
    private UserService userService;

    @Value("${AudienceSourceMBean.deviceFileUploadMaxBytes:5242880}")
    private Integer deviceFileUploadMaxBytes;
    
    private AudienceDto audienceDto;

    // vendor items
    private List<DMPVendorDto> vendors = null;
    private String dmpType;
    private DMPVendorDto dmpVendor;
    private List<DMPAttributeDto> dmpAttributes;
    private DMPAudienceDto dmpAudience;
    private DMPSelectorDto customSelector;

    // the selected selectors
    Map<String, Boolean> selectedOptions = new HashMap<String, Boolean>(0);

    // campaign event
    private CampaignEventType campaignEventType = CampaignEventType.CLICKERS;
    private List<CampaignTypeAheadDto> campaigns = new ArrayList<CampaignTypeAheadDto>(0);
    private FirstPartyAudienceDto firstPartyAudience;

    private String confirmationBody = null;
    private String confirmationMessage = null;

    // devices
    private DeviceIdentifierTypeDto audienceDeviceIdentifierType = null;
    private LazyDataModel<FirstPartyAudienceDeviceIdsUploadHistoryDto> historyLazyDataModel = null;

    private boolean changesNotSaved = false;

    private DeviceIdsValidated deviceIdsValidated;
    private String fileName;

    private boolean fromUpload = false;

    private String audienceCollectionTag = null;

    BigDecimal currentAudiencePrice = null;
    
    private String selectedUploadType = null;
    private String fileMoverRootBucket = null;
    private AdvertiserCloudInformationDto advertiserCloudInformation = null;
    
    private Long audienceSize = null;
    
    @Autowired
    private AudienceSourceS3MBean audienceSourceS3MBean;

    // Factual UI [MAD-3232]
    
    private String dmpFactualType;
    private DMPSelectorDto customFactualProximitySelector = new DMPSelectorDto();
    private List<DMPSelectorDto> customFactualAudienceSelectors = new ArrayList<>();
    private boolean factualAudienceSelectorsAreUnique = true;
    private String factualAudienceSegmentsDuplicatedClientId;
    private String factualAudienceSegmentsDuplicatedErrorKey;
    
    @Override
    protected void init() throws Exception {
        // noop
    }

    public String getConfirmationBody() {
        return this.confirmationBody;
    }

    public String getConfirmationMessage() {
        return this.confirmationMessage;
    }

    // warning for changes to an active audience that has already been targeted
    // by campaigns
    public void checkContinue(ActionEvent event) {
        LOGGER.debug("checkContinue-->");
        String campaignsWithAudience = checkCampaignsUsingAudience();
        if (changesNotSaved && audienceDto.getStatus() == Audience.Status.ACTIVE && campaignsWithAudience != null) {
            if (priceHasChanged()) {
                confirmationBody = FacesUtils.getBundleMessage("page.audience.source.labels.confirmchangedialog.audiencecost");
            } else {
                confirmationBody = "";
            }
            confirmationMessage = FacesUtils.getBundleMessage("page.audience.source.labels.confirmchangedialog.linkedcampaigns", campaignsWithAudience);

            RequestContext.getCurrentInstance().execute("confirmationChanges.show()");
            return;
        }
        
        // Validate unique Factual Audience Selectors
        if (isFactualAudience() && !factualAudienceSelectorsAreUnique) {
        	addFacesMessage(FacesMessage.SEVERITY_ERROR, factualAudienceSegmentsDuplicatedClientId, null, factualAudienceSegmentsDuplicatedErrorKey);
        	return;
        }        
        
		doSave(event);

        LOGGER.debug("<--checkContinue");
    }

    private boolean priceHasChanged() {
        AudienceDto newAudienceDto = new AudienceDto();
        switch (getAudienceMBean().getAudienceSetupMBean().getType()) {                
            case CAMPAIGN_EVENT:
                FirstPartyAudienceDto fakeFirstPartyAudience = new FirstPartyAudienceDto();
                fakeFirstPartyAudience.setType(getFirstPartyAudienceType());
                fakeFirstPartyAudience.setCampaigns(getFirstPartyAudienceCampaigns());
                fakeFirstPartyAudience.setActive(audienceDto.getFirstPartyAudience().isActive());
                fakeFirstPartyAudience.setMuidSegmentId(audienceDto.getFirstPartyAudience().getMuidSegmentId());
                newAudienceDto.setFirstPartyAudience(fakeFirstPartyAudience);
                break;
                
            case DEVICE:
                FirstPartyAudienceDto fakeDeviceFirstPartyAudience = new FirstPartyAudienceDto();
                newAudienceDto.setFirstPartyAudience(fakeDeviceFirstPartyAudience);
                break;
            
            case LOCATION:
                FirstPartyAudienceDto fakeLocationFirstPartyAudience = new FirstPartyAudienceDto();
                newAudienceDto.setFirstPartyAudience(fakeLocationFirstPartyAudience);
                break;    
                
            case DMP:
                List<DMPSelectorForDMPAudienceDto> newSelections = getSelectedSelectors();
                DMPAudienceDto fakeDmpAudience = new DMPAudienceDto();
                fakeDmpAudience.setDmpVendor(dmpVendor);
                fakeDmpAudience.getDmpSelectors().clear();
                fakeDmpAudience.getDmpSelectors().addAll(newSelections);
                newAudienceDto.setDmpAudience(fakeDmpAudience);
                break;
        }

        BigDecimal newAudiencePrice = audienceService.calculateAudienceDataFee(newAudienceDto);

        return (currentAudiencePrice.compareTo(newAudiencePrice) != 0);
    }
    
    public void doCreateUserCredentials() throws AdvertiserCloudManagerException {
        try{
            AdvertiserDto advertiserDto = getUser().getAdvertiserDto();
            advertiserCloudInformation = userService.createAdvertiserCloudInformation(advertiserDto);
        }catch(AdvertiserCloudManagerException ex){
            addFacesMessage(FacesMessage.SEVERITY_ERROR, CREATE_S3_BUTTON_ID, null, Constants.KEY_MESSAGE_COMMON_ERROR);
        }
    }
    
    public void doDeleteUserCredentials() throws AdvertiserCloudManagerException {
        try{
            AdvertiserDto advertiserDto = getUser().getAdvertiserDto();
            userService.deleteAdvertiserCloudInformation(advertiserDto);
            advertiserCloudInformation = null;
        }catch(AdvertiserCloudManagerException ex){
            addFacesMessage(FacesMessage.SEVERITY_ERROR, DELETE_S3_BUTTON_ID, null, Constants.KEY_MESSAGE_COMMON_ERROR);
        }
    }

    public void doSave(ActionEvent event) {
        LOGGER.debug("doSave-->");

        switch (getAudienceMBean().getAudienceSetupMBean().getType()) {
        case CAMPAIGN_EVENT:
            if (CollectionUtils.isEmpty(campaigns)) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, CAMPAIGN_TYPE_AHEAD_ID, null, NO_CAMPAIGNS_MSG_KEY);
                return;
            }

            if (firstPartyAudience == null) {
                firstPartyAudience = new FirstPartyAudienceDto();
                firstPartyAudience.setActive(false);
            }

            firstPartyAudience.setType(getFirstPartyAudienceType());

            firstPartyAudience.getCampaigns().clear();
            firstPartyAudience.getCampaigns().addAll(getFirstPartyAudienceCampaigns());

            audienceDto.setFirstPartyAudience(firstPartyAudience);
            break;

        case DEVICE:
            // If the dialog comes from the upload method
            if (fromUpload) {
                fromUpload = false;
                saveDeviceIds(event);
                return;
            }
            // If s3 upload was selected
            if (Constants.S3_UPLOAD.equals(selectedUploadType)) {
                if (!processS3Files("device")) {
                    return;
                }
            // If file upload was selected
            } else {
                Long deviceCount = audienceService.getMuidSegmentSize(audienceDto.getFirstPartyAudience().getMuidSegmentId());
                if (deviceCount == null || deviceCount < 1) {
                    addFacesMessage(FacesMessage.SEVERITY_ERROR, UPLOAD_DEVICE_IDS_FORM_KEY, null, NO_FILE_DEVICES_ERROR_KEY);
                    return;
                }
            }
            break;
        case LOCATION:
            if (!processS3Files("location")) {
                return;
            }
            break;
        case DMP:
        	List<DMPSelectorForDMPAudienceDto> newSelectors = new ArrayList<>();
        	dmpAudience = (dmpAudience == null) ? new DMPAudienceDto() : dmpAudience;
        	dmpAudience.setDmpVendor(dmpVendor);
        	
        	// Prepare custom selectors for Custom Factual DMP or Custom DMP
        	boolean success = (isFactualAudience()) ? prepareFactualDMPSelectors(newSelectors) : prepareCustomDMPSelectors(newSelectors);
        	if (!success) return;
            
            LOGGER.debug("dmp type: " + ((isFactualAudience()) ? dmpFactualType : dmpType));
            LOGGER.debug(dmpAudience.getUserEnteredDMPSelectorExternalId());
            
            dmpAudience.getDmpSelectors().clear();
            dmpAudience.getDmpSelectors().addAll(newSelectors);
            audienceDto.setDmpAudience(dmpAudience);
        default:
            break;
        }

        prepareDto(audienceDto);

        audienceDto = audienceService.updateAudience(audienceDto);
        audienceDto = audienceService.getAudienceDtoById(audienceDto.getId());
        updateAudienceBeans(audienceDto);

        getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
        getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_CONFIRMATION_VIEW);
        setChangesNotSaved(false);
        LOGGER.debug("<--doSave");
    }
    
    private boolean prepareFactualDMPSelectors(List<DMPSelectorForDMPAudienceDto> newSelections) {
    	boolean hasDefinedSelector = false;
		
		// Factual Proximity Segment
		if (dmpFactualType.equals(DMP_FACTUAL_PROXIMITY_TYPE)) {
			
			String customFactualDesignId = customFactualProximitySelector.getExternalID();
			
			// Define Proximity Segment
			if(StringUtils.isNotBlank(customFactualDesignId)) {
				
				hasDefinedSelector = true;

				// Check for unlinked, but already existing Selector
				DMPSelectorDto existingCustomSelector = audienceService.getDMPSelectorByExternalIdAndDmpVendorId(customFactualDesignId, dmpVendor.getId());
				
				// Create Factual Selector
				if (existingCustomSelector == null) {
					audienceService.createFactualDMPSelector(customFactualDesignId, dmpVendor);
				}
				
				// Link the selector
				newSelections.add(audienceService.getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(customFactualDesignId, dmpVendor.getId()));
			}
			
			if (hasDefinedSelector) {
				dmpAudience.setUserEnteredDMPSelectorExternalId(customFactualDesignId);
				loadFactualDMPAudienceSelectors();
			} else {
				addFacesMessage(FacesMessage.SEVERITY_ERROR, FACTUAL_PROXIMITY_CLIENT_ID, null, NO_SELECTOR_FACTUAL_PROXIMITY_MSG_KEY);
                return false;
			}
			
		// Factual Audience Segment
		} else if (dmpFactualType.equals(DMP_FACTUAL_AUDIENCE_TYPE)) {
			
			int exchangeIndex = 0;
			for(DMPSelectorDto selector : customFactualAudienceSelectors) {
			
				String customFactualDesignId = selector.getExternalID();
				Long publisherId = selector.getPublisher().getId();
				
				// Define Audience Segment
				if(StringUtils.isNotBlank(customFactualDesignId)) {
					
					hasDefinedSelector = true;
					
					// Check for unlinked, but already existing Selector
					DMPSelectorDto existingCustomSelector = audienceService.getDMPSelectorByExternalIdAndDmpVendorId(customFactualDesignId, dmpVendor.getId());
					
					// Create New Factual Selector
					if (existingCustomSelector == null) {
						audienceService.createFactualDMPSelector(customFactualDesignId, dmpVendor, publisherId);
						
					// Check whether we try to set the selector for the same publisher
					} else if (existingCustomSelector.getPublisher() != null &&	!existingCustomSelector.getPublisher().getId().equals(publisherId)) {
						
						long existingPublisherId = existingCustomSelector.getPublisher().getId().longValue();
						String existingPublisherName = WordUtils.capitalize(existingCustomSelector.getPublisher().getName());
						
						StringBuilder sb = new StringBuilder("Existing Factual Design ID '");
						sb.append(customFactualDesignId).append("' tried to be reused for different Exchange ( Actual Publisher: '");
						sb.append(existingPublisherName).append("' (").append(existingPublisherId).append(") -> '");
						sb.append(selector.getPublisher().getName()).append("' (").append(publisherId).append(") ) !");
						
						StringBuilder clientId = new StringBuilder(
								FACTUAL_AUDIENCE_REPEAT_CLIENT_ID).append(COLON).append(exchangeIndex).append(COLON).append(FACTUAL_AUDIENCE_CLIENT_ID);
						addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId.toString(), null, MESSAGE_KEY_FACTUAL_DESIGN_ID_EXCHANGE_MISMATCH_ERROR, existingPublisherName);
						LOGGER.error(sb.toString());
						return false;
						
					// This selector was used by Proximity earlier as well
					} else if (existingCustomSelector.getPublisher() == null) {
						existingCustomSelector.setPublisher(selector.getPublisher());
						audienceService.updateFactualDMPSelector(existingCustomSelector);
					}
					
					// Link the selector
					newSelections.add(audienceService.getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(customFactualDesignId, dmpVendor.getId()));
				}
				exchangeIndex++;
			}
			
			if (hasDefinedSelector) {
				dmpAudience.setUserEnteredDMPSelectorExternalId(null);
				customFactualProximitySelector = new DMPSelectorDto();
			} else {
		        addFacesMessage(FacesMessage.SEVERITY_ERROR, FACTUAL_AUDIENCE_REPEAT_CLIENT_ID, null, NO_SELECTORS_FACTUAL_AUDIENCE_MSG_KEY);
		        return false;
			}
		}
    	
		return true;
	}

	private boolean prepareCustomDMPSelectors(List<DMPSelectorForDMPAudienceDto> newSelectors) {
    	newSelectors.addAll(getSelectedSelectors());
    	
        // segment type has a required on view but the check boxes don't
        if (dmpType.equals(DMP_DEFAULT_SEGMENTS_TYPE) && CollectionUtils.isEmpty(newSelectors)) {
            LOGGER.debug("no selectors");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, DMP_VENDOR_PANEL_ID, null, NO_SELECTORS_MSG_KEY);
            return false;
        }

        if (dmpType.equals(DMP_CUSTOM_SEGMENT_TYPE)) {
            // Auto complete can't have a required in the form
            if (customSelector == null) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, DMP_CUSTOM_SEGMENT_ID, null, NO_SELECTOR_MSG_KEY);
                return false;
            }
            dmpAudience.setUserEnteredDMPSelectorExternalId(customSelector.getExternalID());
            newSelectors.clear();
            newSelectors.add(audienceService.getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(customSelector.getExternalID(), customSelector.getDmpVendorId()));
        } else {
            dmpAudience.setUserEnteredDMPSelectorExternalId(null);
        }
        
		return true;
	}

	private boolean processS3Files(String id) {
        // Verify selected totals
        if (getAudienceSourceS3MBean().getTotalFilesAssigned() == 0) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, ASSIGN_S3_FILE_BUTTON_ID.concat(id), null, NO_S3_ASSIGNED_FILES_ERROR_KEY);
            return false;
        }

        // Notify file mover with new cloud files
        try {
            getAudienceSourceS3MBean().notifyAudienceEngineWithAudienceCloudFiles();
        } catch (AudienceEngineApiException fmae) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, CONTINUE_BUTTON_ID, null, FILEMOVER_CONNECTIVITY_ISSUE_KEY);
            return false;
        }
        return true;
    }

    public void cancel(/*ActionEvent event*/) {
        LOGGER.debug("cancel-->");
        loadAudienceDto(audienceDto);
        setChangesNotSaved(false);
        getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
        getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_CONFIRMATION_VIEW);
        LOGGER.debug("cancel<--");
    }

    public void loadAudienceDto(AudienceDto dto) {
        LOGGER.debug("loadAudienceDto-->");
        setAudienceDto(dto);
        this.dmpAudience = null;
        this.firstPartyAudience = null;

        if (CollectionUtils.isNotEmpty(this.campaigns)) {
            this.campaigns.clear();
        }

        if (dto != null) {
            LOGGER.debug("loading audience: " + dto.getId());
            if (audienceDto.getDmpAudience() != null) {
                setDmpAudience(audienceDto.getDmpAudience());
                setDmpVendor(audienceDto.getDmpAudience().getDmpVendor());

                // Prepare Factual or Common DMP audience
                if(isFactualAudience()) loadFactualDMPAudience();
                else loadCustomDMPAudience();
                                
            } else if (audienceDto.getFirstPartyAudience() == null
                    && (getAudienceSetupBean().getType() == AudienceType.DEVICE || getAudienceSetupBean().getType() == AudienceType.LOCATION)) {
                boolean isDeviceAudience = (getAudienceSetupBean().getType() == AudienceType.DEVICE) ? true : false;
                LOGGER.debug(isDeviceAudience ? "DEVICE" : "LOCATION" + " audience, no first party audience, setting up");
                firstPartyAudience = new FirstPartyAudienceDto();
                firstPartyAudience.setType(isDeviceAudience ? FirstPartyAudience.Type.UPLOAD : FirstPartyAudience.Type.LOCATION);
                audienceDto.setFirstPartyAudience(firstPartyAudience);
                audienceDto = audienceService.updateAudience(audienceDto);
                audienceDto = audienceService.getAudienceDtoById(audienceDto.getId());
                updateAudienceBeans(audienceDto);
                firstPartyAudience = audienceDto.getFirstPartyAudience();
            } else if (audienceDto.getFirstPartyAudience() != null) {
                this.firstPartyAudience = audienceDto.getFirstPartyAudience();
                if (dto.getFirstPartyAudience().getType() != null) {
                    switch (dto.getFirstPartyAudience().getType()) {
                    case UPLOAD:
                        if (firstPartyAudience.persisted()) {
                            createHistoryLazyDataModel();
                        }
                        break;
                    case LOCATION:
                        if (firstPartyAudience.persisted()) {
                            createHistoryLazyDataModel();
                        }
                        break;
                    case COLLECT:
                        break;
                    case INSTALL:
                        this.campaignEventType = CampaignEventType.INSTALLERS;
                        break;
                    case CONVERSION:
                        this.campaignEventType = CampaignEventType.CONVERTERS;
                        break;
                    case CLICK:
                    default:
                        this.campaignEventType = CampaignEventType.CLICKERS;
                        break;
                    }
                }

                if (CollectionUtils.isNotEmpty(dto.getFirstPartyAudience().getCampaigns())) {
                    for (FirstPartyAudienceCampaignDto f : dto.getFirstPartyAudience().getCampaigns()) {
                        CampaignTypeAheadDto c = new CampaignTypeAheadDto();
                        c.setId(f.getId());
                        c.setName(f.getName());
                        campaigns.add(c);
                        LOGGER.debug("loaded from dto campaign " + c.getId() + ":" + c.getName());
                    }
                }
            }
        }

        // Calculate the current audience price
        if (audienceDto != null) {
            currentAudiencePrice = audienceService.calculateAudienceDataFee(audienceDto);
        } else {
            currentAudiencePrice = null;
        }
        
        // Get cloud credentials information
        fileMoverRootBucket = userService.getFileMoverBucketName();
        AdvertiserDto advertiserDto = getUser().getAdvertiserDto();
        advertiserCloudInformation = userService.getAdvertiserCloudInformation(advertiserDto);

        LOGGER.debug("<--loadAudienceDto");
    }
    
    private void loadFactualDMPAudience() {
    	if (hasUserEnteredDMPSelectorExternalId()) {
    		customFactualProximitySelector = loadCustomDMPSelector();
    		setDmpFactualType(DMP_FACTUAL_PROXIMITY_TYPE);
    	} else {
    		setDmpFactualType(DMP_FACTUAL_AUDIENCE_TYPE);
    	}
    	// Audiences always needs to be prepared
    	loadFactualDMPAudienceSelectors();
    }

	private void loadCustomDMPAudience() {
		if (hasUserEnteredDMPSelectorExternalId()) {
			customSelector = loadCustomDMPSelector();
			setDmpType(DMP_CUSTOM_SEGMENT_TYPE);
		} else {
			setDmpType(DMP_DEFAULT_SEGMENTS_TYPE);
			this.selectedOptions.clear();
			if (CollectionUtils.isNotEmpty(audienceDto.getDmpAudience().getDmpSelectors())) {
				for (DMPSelectorDto selector : audienceDto.getDmpAudience().getDmpSelectors()) {
					this.selectedOptions.put(selector.getExternalID() + DASH + selector.getDmpVendorId(), Boolean.TRUE);
				}
			}
		}
	}

	private DMPSelectorDto loadCustomDMPSelector() {
        DMPAudienceDto dmpAudience = audienceDto.getDmpAudience();
        DMPSelectorDto s = audienceService.getDMPSelectorByExternalIdAndDmpVendorId(dmpAudience.getUserEnteredDMPSelectorExternalId(), dmpAudience.getDmpVendor().getId());
        return (s != null) ? s : null;
    }
    
    private void loadFactualDMPAudienceSelectors() {
    	customFactualAudienceSelectors.clear();
    	
    	// Initialize Publishers for DMP Vendor
    	List<PublisherDto> publishersWithoutSelector = new ArrayList<>(dmpAudience.getDmpVendor().getPublishers());
    	
    	// Populate all DMP Audience selectors
    	if (!hasUserEnteredDMPSelectorExternalId()) {
	    	for (DMPSelectorDto selector : dmpAudience.getDmpSelectors()) {
	    		selector.getPublisher().setName(WordUtils.capitalize(selector.getPublisher().getName()));
	    		customFactualAudienceSelectors.add(selector);
	    		publishersWithoutSelector.remove(selector.getPublisher());
	    	}
    	}
    	
    	// Initialize selector for missing Publishers
    	addSelectorsWithPublisher(publishersWithoutSelector);
    }

	public void addSelectorsWithPublisher(List<PublisherDto> publishers) {
		DMPSelectorDto newSelector;
		for (PublisherDto publisher : publishers) {
    		newSelector = new DMPSelectorDto();
    		publisher.setName(WordUtils.capitalize(publisher.getName()));
    		newSelector.setPublisher(publisher);
    		customFactualAudienceSelectors.add(newSelector);
    	}
	}
    
	private boolean hasUserEnteredDMPSelectorExternalId() {
		return audienceDto.getDmpAudience() != null && audienceDto.getDmpAudience().getUserEnteredDMPSelectorExternalId() != null;
	}

    public void changeFactualAudienceSegment(AjaxBehaviorEvent event) {
    	factualAudienceSelectorsAreUnique = true;
    	long numOfDefinedSelectors = 0l;
    	for(DMPSelectorDto selector : customFactualAudienceSelectors) {
    		if (StringUtils.isNotBlank(selector.getExternalID())) {
    			numOfDefinedSelectors++;
    		}
    	}
    	if (numOfDefinedSelectors > 1) {
	    	DMPSelectorDto changedSelector = getFactualAudienceSelectorByIndex(getFactualAudienceSelectorIndex(event));
    		checkFactualAudienceSelectorExternalIdUniqueness(event.getComponent().getClientId(), changedSelector.getExternalID());
    	}
    }
    
	public void checkFactualAudienceSelectorExternalIdUniqueness(String clientId, String changedExternalId) {
		String changedUnifiedExtId = changedExternalId.toLowerCase().trim();
		int numOfSameVendorNames = 0;
		for(DMPSelectorDto selector : customFactualAudienceSelectors) {
			if (StringUtils.isNotBlank(selector.getExternalID())){
				numOfSameVendorNames += (selector.getExternalID().toLowerCase().trim().equals(changedUnifiedExtId) ? 1 : 0); // +1 if same id exists
			}
		}
		if (numOfSameVendorNames > 1) {
			factualAudienceSelectorsAreUnique = false;
			factualAudienceSegmentsDuplicatedClientId = clientId;
			factualAudienceSegmentsDuplicatedErrorKey = MESSAGE_KEY_FACTUAL_AUDIENCE_SELECTOR_UNIQUE_ERROR;
			addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, MESSAGE_KEY_FACTUAL_AUDIENCE_SELECTOR_UNIQUE_ERROR);
		}
	}
    
    private DMPSelectorDto getFactualAudienceSelectorByIndex(int index) {
        return customFactualAudienceSelectors.get(index);
    }
    
    private int getFactualAudienceSelectorIndex(FacesEvent event) {
        return ((Integer) event.getComponent().getAttributes().get(Constants.EXCHANGE_INDEX)).intValue();
    }
	
    protected void createHistoryLazyDataModel() {
        LOGGER.debug("fpa is: " + (firstPartyAudience == null ? "" : "not") + " null");
        LOGGER.debug("fpa is: " + (firstPartyAudience.persisted() ? "" : "not") + " persisted");
        if (firstPartyAudience != null) {
            try {
                this.historyLazyDataModel = new AbstractLazyDataModelWrapper<FirstPartyAudienceDeviceIdsUploadHistoryDto>(
                                audienceService.createFirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel(firstPartyAudience));
                if (historyLazyDataModel != null) {
                    LOGGER.debug("row count: " + historyLazyDataModel.getRowCount());
                }
            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public AudienceDto prepareDto(AudienceDto dto) {
        LOGGER.debug("prepareDto-->");

        if (audienceDto.getStatus().equals(Audience.Status.NEW)) {
            audienceDto.setStatus(Audience.Status.NEW_REVIEW);
        }

        LOGGER.debug("prepareDto<--");
        return dto;
    }

    /** Event handlers **/
    public void onCampaignEventTypeChangedEvent(/*ValueChangeEvent event*/) {
        LOGGER.debug("onCampaignEventTypeChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("onCampaignEventTypeChangedEvent<--");
    }

    public void onVendorEvent(ValueChangeEvent event) {
        LOGGER.debug("onVendorEvent-->");
        DMPVendorDto selectedDmpVendor = (DMPVendorDto) event.getNewValue();
        if (selectedDmpVendor != null) {
            this.dmpAttributes = null;
            
            // Initialize selectors for Factual DMP with Publishers
            if (selectedDmpVendor.getName().equals(FACTUAL) && customFactualAudienceSelectors.isEmpty()) {
            	addSelectorsWithPublisher(selectedDmpVendor.getPublishers());
            }
        }
        setChangesNotSaved(true);
        LOGGER.debug("onVendorEvent<--");
    }

    public void onSourceCampaignChangedEvent(/*ValueChangeEvent event*/) {
        LOGGER.debug("onSourceCampaignChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("onSourceCampaignChangedEvent<--");
    }

    public void onDmpTypeChangedEvent() {
        LOGGER.debug("onDmpTypeChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("onDmpTypeChangedEvent<--");
    }
    
    public void onDmpFactualTypeChangedEvent() {
        LOGGER.debug("onDmpFactualTypeChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("onDmpFactualTypeChangedEvent<--");
    }

    public void customSegmentIdChangedEvent() {
        LOGGER.debug("customSegmentIdChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("customSegmentIdChangedEvent<--");
    }

    public void selectorChangedEvent() {
        LOGGER.debug("selectorChangedEvent-->");
        setChangesNotSaved(true);
        LOGGER.debug("selectorChangedEvent<--");
    }

    public List<DMPSelectorDto> completeSelector(String query) {
        // only show hidden selectors
        return audienceService.searchDMPSelectorByExternalIdForCompanyAndVendor(query, true, getUser().getCompany(), dmpVendor);
    }

    public void handleSelectedSelector(SelectEvent event) {
        DMPSelectorDto s = (DMPSelectorDto) event.getObject();
        this.customSelector = s;
    }

    public void handleUnSelectedSelector(/*UnselectEvent event*/) {
        this.customSelector = null;
    }

    public List<DMPVendorDto> getVendors() {
        if (vendors == null) {
        	CompanyDto companyDto = getUser().getCompany();
            vendors = (isAdminUserLoggedIn()) ? audienceService.getDMPVendorsForCompanyForAdmins(companyDto, false) : audienceService.getDMPVendorsForCompany(companyDto, false);
        }
        return vendors;
    }

    public List<DMPAttributeDto> getDMPAttributes() {
        if (dmpAttributes == null && dmpVendor != null) {
            dmpAttributes = audienceService.getDMPAttributesForDMPVendor(this.dmpVendor, false);
            LOGGER.debug("vendor: " + dmpVendor.getId() + " attributes size: " + dmpAttributes.size());
        }
        return dmpAttributes;
    }

    private List<DMPSelectorForDMPAudienceDto> getSelectedSelectors() {
        List<DMPSelectorForDMPAudienceDto> selections = new ArrayList<DMPSelectorForDMPAudienceDto>(0);
        for (Entry<String, Boolean> entry : selectedOptions.entrySet()) {
            Boolean add = Boolean.parseBoolean(String.valueOf(entry.getValue()));
            if (add) {
                LOGGER.debug("selected: " + entry.getKey());
                String[] extIdDashVendorId = entry.getKey().split(DASH);
                selections.add(audienceService.getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(extIdDashVendorId[0], Long.valueOf(extIdDashVendorId[1])));
            }
        }
        return selections;
    }

    public List<DMPAttributeDto> getSummaryAttributes() {
        if (audienceDto.getDmpAudience() != null) {
            return audienceService.getAttributesAndSelectorsForDMPAudience(audienceDto.getDmpAudience());
        } else {
            return new ArrayList<DMPAttributeDto>();
        }
    }

    public TimeZone getAdvertiserTimeZone() {
        return companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
    }

    public String getUploadHistorySummary(boolean spaces) {
        if (audienceDto != null && audienceDto.getFirstPartyAudience() != null
                && CollectionUtils.isNotEmpty(audienceDto.getFirstPartyAudience().getDeviceIdsUploadHistory())) {

            StringBuilder message = new StringBuilder();
            int lines = 0;
            String space = (spaces ? String.valueOf(SPACE) : StringUtils.EMPTY);

            List<FirstPartyAudienceDeviceIdsUploadHistoryDto> histories = new LinkedList<FirstPartyAudienceDeviceIdsUploadHistoryDto>(
                    audienceDto.getFirstPartyAudience().getDeviceIdsUploadHistory());
            Collections.sort(histories, new FirstPartyAudienceDeviceIdsUploadHistorySortBy(
                    FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.DATE_TIME_UPLOAD, false));

            for (FirstPartyAudienceDeviceIdsUploadHistoryDto history : histories) {
                if (lines > 0) {
                    message.append(SUMMARY_SEPARATOR).append(space);
                }
                lines++;
                FastDateFormat fdf = FastDateFormat.getInstance(DateUtils.getTimeStampFormat(),
                        companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto()), getLanguageSessionBean().getLocale());
                message.append(fdf.format(history.getDateTimeUploaded())).append(ITEM_SEPARATOR).append(space);
                message.append(history.getFilename()).append(ITEM_SEPARATOR).append(space);
                message.append(history.getDeviceIdentifierType().getName()).append(ITEM_SEPARATOR).append(space);
                message.append(FacesUtils.getBundleMessage(DEVICE_HISTORY_TOTAL_RECORDS_KEY)).append(COLON).append(space)
                        .append(history.getTotalNumRecords()).append(ITEM_SEPARATOR).append(space);
                message.append(FacesUtils.getBundleMessage(DEVICE_HISTORY_VALIDATED_RECORDS_KEY)).append(COLON).append(space)
                        .append(history.getNumValidatedRecords()).append(ITEM_SEPARATOR).append(space);
                message.append(FacesUtils.getBundleMessage(DEVICE_HISTORY_INSERTED_RECORDS_KEY)).append(COLON).append(space)
                        .append(history.getNumInsertedRecords());
            }
            return message.toString();
        }
        return notSet();
    }
    
    public String getUploadHistorySummaryFromAssignedFiles(boolean spaces) {
        List<String> fileNames = null;
        try{
            fileNames = audienceFileService.getFileNamesFromAssignedFiles(this.audienceDto.getId());
        }catch(ElasticsearchException ese){
            LOGGER.error("Found an exception accessing elasticsearch data.", ese);
        }
        if ((fileNames!=null)&&(CollectionUtils.isNotEmpty(fileNames))) {
            StringBuilder message = new StringBuilder();
            int lines = 0;
            String space = (spaces ? String.valueOf(SPACE) : StringUtils.EMPTY);

            for (String assignedFileName : fileNames) {
                if (lines > 0) {
                    message.append(SUMMARY_SEPARATOR).append(space);
                }
                lines++;
                message.append(assignedFileName);
            }
            return message.toString();
        }
        return notSet();
    }

    public String getCampaignSummary(boolean spaces) {
        if (audienceDto != null && audienceDto.getFirstPartyAudience() != null
                && CollectionUtils.isNotEmpty(audienceDto.getFirstPartyAudience().getCampaigns())) {
            StringBuilder message = new StringBuilder();
            int lines = 0;
            for (FirstPartyAudienceCampaignDto f : audienceDto.getFirstPartyAudience().getCampaigns()) {
                if (lines > 0) {
                    message.append(ITEM_SEPARATOR);
                    if (spaces) {
                        message.append(SPACE);
                    }
                }
                lines++;
                message.append(f.getName());
            }
            return message.toString();
        }
        return notSet();
    }

    public String getAttributeSummary(boolean spaces) {
        if (audienceDto != null && audienceDto.getDmpAudience() != null) {
            List<DMPAttributeDto> attributes = audienceService.getAttributesAndSelectorsForDMPAudience(audienceDto.getDmpAudience());
            StringBuilder message = new StringBuilder();
            int lines = 0;

            for (DMPAttributeDto attribute : attributes) {
                if (lines > 0) {
                    message.append(SUMMARY_SEPARATOR);
                    if (spaces) {
                        message.append(SPACE);
                    }
                }
                lines++;

                message.append(attribute.getName()).append(SPACE);
                int selectors = 0;
                for (DMPSelectorDto selector : attribute.getDMPSelectors()) {
                    if (selectors > 0) {
                        message.append(ITEM_SEPARATOR);
                        if (spaces) {
                            message.append(SPACE);
                        }
                    }
                    selectors++;
                    message.append(selector.getName()).append(SPACE);
                }
            }
            return message.toString();
        }
        return notSet();
    }
    
    public String getFactualAudiencesSummary() {
    	 if (!customFactualAudienceSelectors.isEmpty()) {
             StringBuilder message = new StringBuilder();

             for (DMPSelectorDto selector : customFactualAudienceSelectors) {
                 if (StringUtils.isNotBlank(selector.getExternalID())) {
                	 message.append(selector.getPublisher().getName()).append(SUMMARY_SEPARATOR);
                 }
             }
             return message.length() > 0 ? message.toString() : notSet();
         }
         return notSet();
    }

    public List<CampaignTypeAheadDto> completeCampaigns(String query) {
        CampaignSearchDto campaignSearchDto = new CampaignSearchDto();
        campaignSearchDto.setName(query);
        campaignSearchDto.setAdvertiser(getUser().getAdvertiserDto());
        campaignSearchDto = campaignService.getCampaignsThatHaveEverBeenActive(campaignSearchDto);
        return (List<CampaignTypeAheadDto>) campaignSearchDto.getCampaigns();
    }

    public String getDeviceIdentifierTypeName(String systemName) {
        if (StringUtils.isNotBlank(systemName)) {
            DeviceIdentifierTypeDto dto = deviceIdentifierService.getDeviceIdentifierTypeBySystemName(systemName);
            return dto.getName();
        }
        return null;
    }

    public void uploadDeviceIds(FileUploadEvent event) throws Exception {
        LOGGER.debug("uploadDeviceIds-->");
        if (audienceDeviceIdentifierType == null) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, UPLOAD_DEVICE_IDS_FORM_KEY, DEVICE_TYPE_REQUIRED_KEY, DEVICE_TYPE_REQUIRED_KEY);
        } else if (!ALLOWED_CONTENT_TYPES.contains(event.getFile().getContentType())) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, UPLOAD_DEVICE_IDS_FORM_KEY, DEVICE_UPLOAD_ERROR_CONTENTTYPE_KEY, DEVICE_UPLOAD_ERROR_CONTENTTYPE_KEY);
        } else if (event == null || event.getFile() == null) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, UPLOAD_DEVICE_IDS_FORM_KEY, DEVICE_UPLOAD_ERROR_GENERAL_KEY, DEVICE_UPLOAD_ERROR_GENERAL_KEY);
        } else if (event.getFile().getSize() > deviceFileUploadMaxBytes) {
            FacesMessage msg = new FacesMessage(FacesUtils.getBundleMessage(DEVICE_UPLOAD_ERROR_MAXSIZE_KEY,
                    String.valueOf(getToolsApplicationBean().bytesToMegabytes(deviceFileUploadMaxBytes))));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(UPLOAD_DEVICE_IDS_FORM_KEY, msg);
        } else {
            deviceIdsValidated = audienceService.validateDeviceIdsFileUpload(audienceDto.getName(),
                                                                             audienceDeviceIdentifierType, 
                                                                             event.getFile().getFileName(),
                                                                             event.getFile().getContentType(), 
                                                                             event.getFile().getInputstream());
            fileName = event.getFile().getFileName();

            if (deviceIdsValidated.getDevicesValidated() > 0) {
                String campaignsWithAudience = checkCampaignsUsingAudience();
                if (campaignsWithAudience != null) {
                    confirmationBody = FacesUtils.getBundleMessage("page.audience.source.labels.confirmuploaddialog.message",
                            Long.toString(deviceIdsValidated.getDevicesValidated()), Long.toString(deviceIdsValidated.getDevicesRead()));
                    confirmationMessage = campaignsWithAudience;
                    RequestContext.getCurrentInstance().execute("confirmationChanges.show()");
                    fromUpload = true;
                    return;
                }
                saveDeviceIds(null);
            } else {  // No valid devideIDs exist based on the given Device ID Type
                FacesMessage msg = new FacesMessage(FacesUtils.getBundleMessage(DEVICE_UPLOAD_WARNING_NOTFOUND_KEY, audienceDeviceIdentifierType.getName()));
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
                FacesContext.getCurrentInstance().addMessage(UPLOAD_DEVICE_IDS_FORM_KEY, msg);
            }
        }
    }

    public void cancelSave(/*ActionEvent event*/) {
        fromUpload = false;
    }

    public void saveDeviceIds(ActionEvent event) {
        Map<String, String> uploadResponse = audienceService.processDeviceIdsFileUpload(audienceDto.getName(),
                                                                                        firstPartyAudience,
                                                                                        deviceIdsValidated.getIdsValidated(), 
                                                                                        deviceIdsValidated.getDevicesRead(), 
                                                                                        deviceIdsValidated.getDevicesValidated(),
                                                                                        fileName, audienceDeviceIdentifierType);
        
        if (uploadResponse.containsValue(AudienceService.DEVICE_IDS_UPLOAD_STATUS_SUCCESS)) {
            FacesMessage msg = new FacesMessage(FacesUtils.getBundleMessage(DEVICE_UPLOAD_SUCCESS_KEY, fileName,
                                                Long.toString(deviceIdsValidated.getDevicesRead()),
                                                Long.toString(deviceIdsValidated.getDevicesValidated()),
                                                uploadResponse.get(AudienceService.DEVICE_IDS_UPLOAD_NUM_INSERTED_RECORDS)));
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            FacesContext.getCurrentInstance().addMessage(UPLOAD_DEVICE_IDS_FORM_KEY, msg);
        }

        // reload so we get the correct history etc.
        audienceDto = audienceService.getAudienceDtoById(audienceDto.getId());
        updateAudienceBeans(audienceDto);
        firstPartyAudience = audienceDto.getFirstPartyAudience();
        setChangesNotSaved(false);
        LOGGER.debug("uploadDeviceIds<--");
    }

    public Long getSegmentSize() {
        if (audienceDto != null && audienceDto.persisted() && audienceDto.getFirstPartyAudience() != null) {
            return audienceService.getMuidSegmentSize(audienceDto.getFirstPartyAudience().getMuidSegmentId());
        }
        return 0L;
    }

    public enum CampaignEventType {
        CLICKERS(CLICKERS_LABEL_KEY), INSTALLERS(INSTALLERS_LABEL_KEY), CONVERTERS(CONVERTERS_LABEL_KEY);

        private final String label;

        private CampaignEventType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public CampaignEventType[] getCampaignEventTypes() {
        return CampaignEventType.values();
    }

    public AudienceDto getAudienceDto() {
        return audienceDto;
    }

    public void setAudienceDto(AudienceDto audienceDto) {
        this.audienceDto = audienceDto;
    }

    public String getDmpType() {
        if (dmpType == null) {
            this.dmpType = DMP_DEFAULT_SEGMENTS_TYPE;
        }
        return dmpType;
    }

    public void setDmpType(String dmpType) {
        this.dmpType = dmpType;
    }
    
    public String getDmpFactualType() {
        if (dmpFactualType == null) {
            this.dmpFactualType = DMP_FACTUAL_PROXIMITY_TYPE;
        }
        return dmpFactualType;
    }

    public void setDmpFactualType(String dmpFactualType) {
        this.dmpFactualType = dmpFactualType;
    }

    public List<DMPSelectorDto> getCustomSegmentId() {
        List<DMPSelectorDto> list = new ArrayList<DMPSelectorDto>();
        if (customSelector != null) {
            list.add(customSelector);
        }
        return list;
    }

    public void setCustomSegmentId(List<DMPSelectorDto> customSegmentId) {
        // this.customSegmentId = customSegmentId;
    }

    public DMPVendorDto getDmpVendor() {
        return dmpVendor;
    }

    public void setDmpVendor(DMPVendorDto dmpVendor) {
        this.dmpVendor = dmpVendor;
    }

    public List<DMPAttributeDto> getDmpAttributes() {
        return dmpAttributes;
    }

    public Map<String, Boolean> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(Map<String, Boolean> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public DMPAudienceDto getDmpAudience() {
        return dmpAudience;
    }

    public void setDmpAudience(DMPAudienceDto dmpAudience) {
        this.dmpAudience = dmpAudience;
    }

    public CampaignEventType getCampaignEventType() {
        return campaignEventType;
    }

    public void setCampaignEventType(CampaignEventType campaignEventType) {
        this.campaignEventType = campaignEventType;
    }

    public List<CampaignTypeAheadDto> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<CampaignTypeAheadDto> campaigns) {
        this.campaigns = campaigns;
    }

    public LazyDataModel<FirstPartyAudienceDeviceIdsUploadHistoryDto> getHistoryLazyDataModel() {
        return historyLazyDataModel;
    }

    public void setHistoryLazyDataModel(LazyDataModel<FirstPartyAudienceDeviceIdsUploadHistoryDto> historyLazyDataModel) {
        this.historyLazyDataModel = historyLazyDataModel;
    }

    public DeviceIdentifierTypeDto getAudienceDeviceIdentifierType() {
        if (audienceDeviceIdentifierType == null) {
            this.audienceDeviceIdentifierType = getToolsApplicationBean().getAudienceDeviceIdentifierTypes().get(0);
        }
        return audienceDeviceIdentifierType;
    }

    public void setAudienceDeviceIdentifierType(DeviceIdentifierTypeDto audienceDeviceIdentifierType) {
        this.audienceDeviceIdentifierType = audienceDeviceIdentifierType;
    }

    public DMPSelectorDto getCustomSelector() {
        return customSelector;
    }

    public void setCustomSelector(DMPSelectorDto customSelector) {
        this.customSelector = customSelector;
    }

    public boolean isChangesNotSaved() {
        return changesNotSaved;
    }

    public void setChangesNotSaved(boolean changesNotSaved) {
        this.changesNotSaved = changesNotSaved;
    }

    public Integer getDeviceFileUploadMaxBytes() {
        return deviceFileUploadMaxBytes;
    }

    public void setDeviceFileUploadMaxBytes(Integer deviceFileUploadMaxBytes) {
        this.deviceFileUploadMaxBytes = deviceFileUploadMaxBytes;
    }

	public DMPSelectorDto getCustomFactualProximitySelector() {
		return customFactualProximitySelector;
	}

	public void setCustomFactualProximitySelector(DMPSelectorDto customFactualProximitySelector) {
		this.customFactualProximitySelector = customFactualProximitySelector;
	}

	public List<DMPSelectorDto> getCustomFactualAudienceSelectors() {
		return customFactualAudienceSelectors;
	}
	
	public void setCustomFactualAudienceSelectors(List<DMPSelectorDto> customFactualAudienceSelectors) {
		this.customFactualAudienceSelectors = customFactualAudienceSelectors;
	}

	public String getAudienceCollectionTag() {
        // if(audienceDto!=null && audienceCollectionTag == null &&
        // getAudienceSetupBean().getType().equals(AudienceType.SITE_APP)){
        // audienceCollectionTag =
        // Constants.AUDIENCE_COLLECTION_JAVASCRIPT_TAG.replaceAll(":ADVERTISER_EXTERNAL_ID",
        // getUser().getAdvertiserDto().getExternalID()).replaceAll(":AUDIENCE_EXTERNAL_ID",
        // audienceDto.getExternalId());
        // }
        return audienceCollectionTag;
    }

	public void setAudienceCollectionTag(String audienceCollectionTag) {
        this.audienceCollectionTag = audienceCollectionTag;
    }

    public String getSelectedUploadType() {
        if (this.selectedUploadType==null){
           if (AudienceType.DEVICE.equals(getAudienceMBean().getAudienceSetupMBean().getType())){
               if (this.audienceSourceS3MBean.getTotalFilesAssigned()>0){
                   selectedUploadType = Constants.S3_UPLOAD;
               }else{
                   selectedUploadType = Constants.FILE_UPLOAD;
               }
           }else{
               selectedUploadType = Constants.FILE_UPLOAD;
           }
        }
        return selectedUploadType;
    }

    public void setSelectedUploadType(String selectedUploadType) {
        this.selectedUploadType = selectedUploadType;
    }
    
    public String getFileMoverRootBucket() {
        return fileMoverRootBucket;
    }

    public AdvertiserCloudInformationDto getAdvertiserCloudInformation() {
        return advertiserCloudInformation;
    }

    private String checkCampaignsUsingAudience() {
        LOGGER.debug("checkCampaignsUsingAudience-->");
        List<CampaignUsingAudienceDto> audienceCampaigns = audienceService.getAllCampaignsUsingAudience(audienceDto);
        if (CollectionUtils.isNotEmpty(audienceCampaigns)) {
            StringBuilder msg = new StringBuilder();
            int lines = 0;
            for (CampaignUsingAudienceDto c : audienceCampaigns) {
                if (lines > 0) {
                    msg.append(ITEM_SEPARATOR).append(SPACE);
                }
                lines++;
                msg.append(c.getName());
            }
            return msg.toString();
        }
        LOGGER.debug("<--checkCampaignsUsingAudience");
        return null;
    }
    
    private FirstPartyAudience.Type getFirstPartyAudienceType(){
        FirstPartyAudience.Type type = FirstPartyAudience.Type.UPLOAD; 
        switch (campaignEventType) {
            case CLICKERS:
                type = FirstPartyAudience.Type.CLICK;
                break;
            case INSTALLERS:
                type = FirstPartyAudience.Type.INSTALL;
                break;
            case CONVERTERS:
                type = FirstPartyAudience.Type.CONVERSION;
            default:
                break;
        }
        return type;
    }
    
    private List<FirstPartyAudienceCampaignDto> getFirstPartyAudienceCampaigns(){
        List<FirstPartyAudienceCampaignDto> fpaCampaignsDto = new ArrayList<>(campaigns.size());
        for (CampaignTypeAheadDto c : campaigns) {
            FirstPartyAudienceCampaignDto f = new FirstPartyAudienceCampaignDto();
            f.setId(c.getId());
            f.setName(c.getName());
            fpaCampaignsDto.add(f);
        }
        return fpaCampaignsDto;
    }
    
    public Collection<String> getSelectedUploadTypes(){
        List<String> uploadTypes = new ArrayList<>();
        uploadTypes.add(Constants.FILE_UPLOAD);
        uploadTypes.add(Constants.S3_UPLOAD);
        return uploadTypes;
    }
    
    public String getSelectedUploadTypesLabel(String type){
        String label = null;
        if (type.equals(Constants.FILE_UPLOAD)){
            label = FacesUtils.getBundleMessage("page.audience.source.fileupload");
        }else{
            label = FacesUtils.getBundleMessage("page.audience.source.s3upload");
        }
        return label;
    }

    public AudienceSourceS3MBean getAudienceSourceS3MBean() {
        return audienceSourceS3MBean;
    }
    
    public Long getAudienceSize(){
        if (audienceSize==null){
            AudienceType audienceType = getAudienceMBean().getAudienceSetupMBean().getType();
            switch (audienceType) {
                case DEVICE:
                case LOCATION:
                    FileType fileType = (audienceType == AudienceType.DEVICE) ? FileType.DEVICES : FileType.GEOPOINTS;
                    audienceSize = audienceFileService.getAudienceSize(fileType, getAudienceMBean().getAudienceDto().getId());
                    break;
                default:
                    break;
            }
        }
        
        return audienceSize;
    }
	
	public boolean isFactualAudience() {
		return (dmpVendor != null) ? dmpVendor.getName().equals(FACTUAL) : false;
	}
	
}
