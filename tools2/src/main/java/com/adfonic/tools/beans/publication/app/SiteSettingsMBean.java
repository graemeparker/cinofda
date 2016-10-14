package com.adfonic.tools.beans.publication.app;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

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
public class SiteSettingsMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteSettingsMBean.class);

    private PublicationDto publicationDto;

    @Autowired
    private PublicationService pService;

    private List<PublicationtypeDto> lTypes = null;

    private String name;

    private String urlString;

    private boolean backfillEnabled;

    private boolean autoApproval;

    private int genderMix = 50;

    private int minAge;

    private int maxAge;

    private boolean showAdvanced = false;

    @Override
    public void init() {
        getPublicationNavigationBean().setMedium(Constants.MEDIUM_SITE);
    }

    public void doSave(ActionEvent event) throws Exception {
        if (!isDuplicatedName(name)) {
            // save and continue campaign;
            publicationDto = pService.save(prepareDto(publicationDto));

            updatePublicationBeans(publicationDto);
            getPublicationNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_APP_ADSLOT);
            getPublicationNavigationBean().setNavigate("/WEB-INF/jsf/addpublication/section_addslot_addapp.xhtml");
            getPublicationNavigationBean().setPubCreated(true);
            getPublicationNavigationBean().setMedium(Constants.MEDIUM_SITE);
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "app-name", null, "page.publication.error.validation.duplicatednamme");
        }
    }

    public PublicationDto prepareDto(PublicationDto dto) {
        if (dto.getAdSpaces() == null) {
            dto.setAdSpaces(new ArrayList<AdSpaceDto>());
        }
        dto.setPublisher(getUser().getPublisherDto());
        dto.setName(name);
        dto.setUrlString(urlString);
        dto.setBackfillEnabled(backfillEnabled);
        dto.setAutoApproval(autoApproval);
        dto.setGenderMix(new BigDecimal(genderMix).divide(new BigDecimal(100)));
        dto.setMinAge(minAge);
        dto.setMaxAge(maxAge);
        publicationDto.setPublicationType(pService.getPublicationTypeBySystemName(Constants.MOBILE_SITE));
        return dto;
    }

    public void cancel(ActionEvent event) {
        loadDto(publicationDto);
        getPublicationNavigationBean().navigateTo(Constants.MENU_NAVIGATE_TO_SETTINGS);
    }

    public void displayAdvanced(ActionEvent event) {
        showAdvanced = true;
    }

    public void loadDto(PublicationDto dto) {
        this.name = dto.getName();
        urlString = dto.getUrlString();
        backfillEnabled = dto.isBackfillEnabled();
        autoApproval = dto.isAutoApproval();
        genderMix = (dto.getGenderMix().multiply(new BigDecimal(100))).intValue();
        minAge = dto.getMinAge();
        maxAge = dto.getMaxAge();
        publicationDto = dto;
    }

    public String getButtonText() {
        if (getPublicationMBean().isNewPublication()) {
            return FacesUtils.getBundleMessage("page.publication.settings.label.app.continue");
        }
        return FacesUtils.getBundleMessage("page.campaign.button.save.confirm.label");
    }

    public PublicationDto getPublicationDto() {
        return publicationDto;
    }

    public void setPublicationDto(PublicationDto publicationDto) {
        this.publicationDto = publicationDto;
    }

    public List<PublicationtypeDto> getlTypes() {
        if (lTypes == null) {
            lTypes = pService.getPublicationType(Constants.MEDIUM_SITE);
        }
        return lTypes;
    }

    public void setlTypes(List<PublicationtypeDto> lTypes) {
        this.lTypes = lTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
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
