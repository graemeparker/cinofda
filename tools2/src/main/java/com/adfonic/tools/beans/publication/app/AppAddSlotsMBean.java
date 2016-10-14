package com.adfonic.tools.beans.publication.app;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.adspace.AdSpaceDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.format.FormatService;
import com.adfonic.presentation.publication.adspace.AdSpaceService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class AppAddSlotsMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppSettingsMBean.class);

    @Autowired
    private FormatService fService;

    @Autowired
    private AdSpaceService aService;

    @Autowired
    private PublicationService pService;

    private PublicationDto publicationDto;

    private AdSpaceDto newAdspace;

    private List<FormatDto> lFormats = null;

    private FormatDto format;

    private boolean showNewSlot = false;

    @Override
    public void init() {
        updatePublicationBeans(publicationDto);
    }

    public String launch() throws Exception {

        if (CollectionUtils.isEmpty(publicationDto.getAdSpaces())) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "submitButton", null, "page.publication.message.noadspaces");
            return null;
        }

        if (getPublicationMBean().isNewPublication()) {
            publicationDto = pService.submit(publicationDto, getUser());
        }

        getPublicationMBean().doInitPublication();

        return "pretty:dashboard-publisher";
    }

    public void doSave(ActionEvent event) throws Exception {
        if (!isDuplicatedName(newAdspace.getName(), null)) {
            // save and continue campaign;
            newAdspace.setColorScheme(ColorScheme.grey);
            newAdspace.getFormats().clear();
            newAdspace.getFormats().add(format);

            AdSpaceDto a = aService.save(newAdspace, publicationDto);
            publicationDto.getAdSpaces().add(a);
            publicationDto = pService.save(publicationDto);

            format = null;
            newAdspace = null;
            showNewSlot = false;
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "slot-name", null, "page.publication.error.validation.adspace.duplicatednamme");
        }
    }

    public void doUpdate(ActionEvent event) throws Exception {
        int adspaceId = (Integer) event.getComponent().getAttributes().get(Constants.ADSPACE_ID);
        // save and continue campaign;
        AdSpaceDto adSpaceDto = publicationDto.getAdSpaces().get(adspaceId);
        if (!isDuplicatedName(adSpaceDto.getName(), adSpaceDto.getId())) {
            adSpaceDto = aService.save(adSpaceDto, publicationDto);
            publicationDto = pService.save(publicationDto);
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, event.getComponent().getParent().getId() + ":" + adspaceId + ":slot-name", null,
                    "page.publication.error.validation.adspace.duplicatednamme");
        }

    }

    public String getButtonText() {
        if (getPublicationMBean().isNewPublication()) {
            return FacesUtils.getBundleMessage("page.campaign.confirmation.button.launch.label");
        }
        return FacesUtils.getBundleMessage("page.publication.settings.label.app.slot.done");
    }

    public void cancelAdSpace(ActionEvent event) {
        LOGGER.debug("cancelAdSpace-->");

        publicationDto = pService.getPublicationById(publicationDto.getId());

        LOGGER.debug("cancelAdSpace<--");
    }

    public void cancelNew(ActionEvent event) {
        this.showNewSlot = false;
        newAdspace = new AdSpaceDto();
    }

    public boolean isAdSpacesEmpty() {
        return CollectionUtils.isEmpty(publicationDto.getAdSpaces());
    }

    public void showNew(ActionEvent event) {
        showNewSlot = true;
    }

    public PublicationDto getPublicationDto() {
        return publicationDto;
    }

    public void setPublicationDto(PublicationDto publicationDto) {
        this.publicationDto = publicationDto;
    }

    public AdSpaceDto getNewAdspace() {
        if (newAdspace == null) {
            newAdspace = new AdSpaceDto();
        }
        return newAdspace;
    }

    public void setNewAdspace(AdSpaceDto newAdspace) {
        this.newAdspace = newAdspace;
    }

    public List<FormatDto> getlFormats() {
        lFormats = fService.getFormatDtos(publicationDto.getPublicationType());
        return lFormats;
    }

    public void setlFormats(List<FormatDto> lFormats) {
        this.lFormats = lFormats;
    }

    public FormatDto getFormat() {
        return format;
    }

    public void setFormat(FormatDto format) {
        this.format = format;
    }

    public boolean getSlotTableRendered() {
        return publicationDto.getAdSpaces().size() > 0;
    }

    public String getTestId() {
        return Constants.ADSPACE_TEST_ID;
    }

    public boolean isShowNewSlot() {
        return showNewSlot;
    }

    public void setShowNewSlot(boolean showNewSlot) {
        this.showNewSlot = showNewSlot;
    }

    public String getLinkText() {
        String medium = getPublicationNavigationBean().getMedium();
        if (medium.equals(Constants.MEDIUM_APPLICATION)) {
            return FacesUtils.getBundleMessage("page.publication.settings.label.app.slot.sdkdownload");
        } else {
            return FacesUtils.getBundleMessage("page.publication.settings.label.app.slot.webdetails");
        }
    }

    public String getLink() {
        String medium = getPublicationNavigationBean().getMedium();
        if (medium.equals(Constants.MEDIUM_APPLICATION)) {
            return getToolsApplicationBean().getExternalUrls().get("downloadSDKUrl");
        } else {
        	return getToolsApplicationBean().getExternalUrls().get("mobileSitesUrl");
        }
    }

    private AdSpaceDto getAdspace(long id) {
        for (AdSpaceDto a : publicationDto.getAdSpaces()) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    private boolean isDuplicatedName(String name, Long id) {
        if (name != null) {
            for (AdSpaceDto ad : publicationDto.getAdSpaces()) {
                if (ad.getName().equals(name)) {
                    if (id == null || ad.getId() != id) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
