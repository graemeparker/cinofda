package com.adfonic.tools.beans.publication.app;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.adspace.AdSpaceDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.search.PublicationSearchDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class AppSettingsMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettingsMBean.class);

    private PublicationDto publicationDto;

    @Autowired
    private PublicationService pService;

    private List<PublicationtypeDto> lTypes = null;

    private String name;

    private Integer appLive = 0;

    private String urlString;

    private String description;

    private PublicationtypeDto publicationType;

    private boolean backfillEnabled;

    private boolean autoApproval;

    private int genderMix = 50;

    private int minAge;

    private int maxAge;

    private boolean showAdvanced;

    @Override
    public void init() {
        getPublicationNavigationBean().setMedium(Constants.MEDIUM_APPLICATION);
    }

    public void doSave(ActionEvent event) throws Exception {
        if (!isDuplicatedName(name)) {
            publicationDto = pService.save(prepareDto(publicationDto));

            updatePublicationBeans(publicationDto);
            getPublicationNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_APP_ADSLOT);
            getPublicationNavigationBean().setNavigate("/WEB-INF/jsf/addpublication/section_addslot_addapp.xhtml");
            getPublicationNavigationBean().setPubCreated(true);
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "app-name", null, "page.publication.error.validation.duplicatednamme");
        }
    }

    public PublicationDto prepareDto(PublicationDto dto) {
        dto.setPublisher(getUser().getPublisherDto());
        dto.setName(name);
        if (appLive == 1) {
            dto.setUrlString(urlString);
            dto.setDescription("");
        } else {
            dto.setUrlString("");
            dto.setDescription(description);
        }
        dto.setPublicationType(publicationType);
        dto.setBackfillEnabled(backfillEnabled);
        dto.setAutoApproval(autoApproval);
        dto.setGenderMix(new BigDecimal(genderMix).divide(new BigDecimal(100)));
        dto.setMinAge(minAge);
        dto.setMaxAge(maxAge);
        if (publicationDto.getAdSpaces() == null) {
            publicationDto.setAdSpaces(new ArrayList<AdSpaceDto>());
        }
        return dto;
    }

    public void loadDto(PublicationDto dto) {
        this.name = dto.getName();
        if (!StringUtils.isEmpty(dto.getUrlString())) {
            appLive = 1;
            urlString = dto.getUrlString();
        } else if (!StringUtils.isEmpty(dto.getDescription())) {
            appLive = 2;
            description = dto.getDescription();
        }
        if (dto.getPublicationType() != null) {
            publicationType = dto.getPublicationType();
        }
        backfillEnabled = dto.isBackfillEnabled();
        autoApproval = dto.isAutoApproval();
        genderMix = (dto.getGenderMix().multiply(new BigDecimal(100))).intValue();
        minAge = dto.getMinAge();
        maxAge = dto.getMaxAge();
        publicationDto = dto;
    }

    public void cancel(ActionEvent event) {
        loadDto(publicationDto);
        getPublicationNavigationBean().navigateTo(Constants.MENU_NAVIGATE_TO_SETTINGS);
    }

    public String getButtonText() {
        if (getPublicationMBean().isNewPublication()) {
            return FacesUtils.getBundleMessage("page.publication.settings.label.app.continue");
        }
        return FacesUtils.getBundleMessage("page.campaign.button.save.confirm.label");
    }

    public void displayAdvanced(ActionEvent event) {
        showAdvanced = true;
    }

    public PublicationDto getPublicationDto() {
        return publicationDto;
    }

    public void setPublicationDto(PublicationDto publicationDto) {
        this.publicationDto = publicationDto;
    }

    public List<PublicationtypeDto> getlTypes() {
        if (lTypes == null) {
            lTypes = pService.getPublicationType(Constants.MEDIUM_APPLICATION);
        }
        return lTypes;
    }

    public void setlTypes(List<PublicationtypeDto> lTypes) {
        this.lTypes = lTypes;
    }

    public Integer getAppLive() {
        return appLive;
    }

    public void setAppLive(Integer appLive) {
        this.appLive = appLive;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PublicationtypeDto getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(PublicationtypeDto publicationType) {
        this.publicationType = publicationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBackfillEnabled() {
        return backfillEnabled;
    }

    public void setBackfillEnabled(boolean backfillEnabled) {
        this.backfillEnabled = backfillEnabled;
    }

    public boolean isAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public int getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(int genderMix) {
        this.genderMix = genderMix;
    }

    public int getGenderMixOpposite() {
        return 100 - genderMix;
    }

    public void setGenderMixOpposite(int genderMixOpposite) {
        if (100 - genderMix != genderMixOpposite) {
            LOGGER.warn("Gender mix in UI doesn't fit");
        }
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public boolean isShowAdvanced() {
        return showAdvanced;
    }

    public void setShowAdvanced(boolean showAdvanced) {
        this.showAdvanced = showAdvanced;
    }

    private boolean isDuplicatedName(String name) {
        if (name != null) {
            PublicationSearchDto dto = new PublicationSearchDto();
            dto.setName(this.name);
            UserSessionBean bean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.USER_SESSION_BEAN);
            UserDTO userDto = (UserDTO) bean.getMap().get(Constants.USERDTO);
            dto.setPublisher(userDto.getPublisherDto());
            PublicationTypeAheadDto obj = pService.getPublicationWithNameForPublisher(dto);
            if (obj == null || obj.getId() == null) {
                return false;
            } else if (publicationDto == null || publicationDto.getId() == null
                    || publicationDto.getId().longValue() != obj.getId().longValue()) {
                return true;
            }
        }
        return false;
    }
}
