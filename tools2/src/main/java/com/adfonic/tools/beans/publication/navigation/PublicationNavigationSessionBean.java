package com.adfonic.tools.beans.publication.navigation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.publication.PublicationMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("session")
public class PublicationNavigationSessionBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // default url to navigate
    private String navigate;

    private boolean newPub;

    private boolean pubCreated = false;

    private Map<String, String> menuStyleClass = new HashMap<String, String>(0);

    private String medium;

    private String encodedId;

    public PublicationNavigationSessionBean() {
        init();
    }

    public void init() {
        menuStyleClass.clear();
        menuStyleClass.put("settingsClass", "");
        menuStyleClass.put("addSlotClass", "");
        menuStyleClass.put("section", "t1");
        newPub = true;
        pubCreated = false;
        navigate = "/WEB-INF/jsf/addpublication/section_new.xhtml";
        medium = "";
        updateMenuStyles(Constants.MENU_NAVIGATE_TO_NEW);
    }

    public String getNavigate() {
        PublicationMBean bean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.PUBLICATION_MBEAN);
        if (bean.getPublicationDto() != null && bean.getPublicationDto().getId() != null && bean.getPublicationDto().getId().intValue() > 0) {

        } else {
            if (StringUtils.isEmpty(navigate) || Constants.PUBLICATION_DEFAULT_NAVIGATION.equals(navigate)) {
                navigate = Constants.PUBLICATION_DEFAULT_NAVIGATION;
            }
        }
        return navigate;
    }

    public void doNavigateTo(ActionEvent event) {
        String navigateTo = (String) event.getComponent().getAttributes().get(Constants.NAVIGATE_TO);

        if (Constants.MENU_NAVIGATE_TO_APP_SETTINGS.equals(navigateTo)) {
            medium = Constants.MEDIUM_APPLICATION;
            navigateTo = Constants.MENU_NAVIGATE_TO_SETTINGS;
        }
        if (Constants.MENU_NAVIGATE_TO_SITE_SETTINGS.equals(navigateTo)) {
            medium = Constants.MEDIUM_SITE;
            navigateTo = Constants.MENU_NAVIGATE_TO_SETTINGS;
        }
        if (Constants.MENU_NAVIGATE_TO_NEW.equals(navigateTo)) {
            medium = "";
        }
        navigateTo(navigateTo);
    }

    public void navigateTo(String navigateTo) {
        if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_NEW.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/addpublication/section_new.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_NEW);
            newPub = true;
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SETTINGS.equals(navigateTo)
                && !medium.equals(Constants.MEDIUM_SITE)) {
            navigate = "/WEB-INF/jsf/addpublication/section_settings_addapp.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_APP_SETTINGS);
            newPub = false;
        } else if ((!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SETTINGS.equals(navigateTo))
                || (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_APP_SETTINGS.equals(navigateTo) && medium
                        .equals(Constants.MEDIUM_SITE))) {
            navigate = "/WEB-INF/jsf/addpublication/section_settings_addsite.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_APP_SETTINGS);
            newPub = false;
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_APP_ADSLOT.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/addpublication/section_addslot_addapp.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_APP_ADSLOT);
        }
    }

    /**
     * private void prepareStyleClass() { if (getCampaignDto().isSetupDone()) {
     * if (getCampaignDto().isScheduleDone()) { // update section class
     * menuStyleClass.remove("section"); menuStyleClass.put("section", "t3");
     *
     * // update current menu style class menuStyleClass.remove("sectionClass");
     * menuStyleClass.remove("schedulingClass");
     *
     * menuStyleClass.put("targetingClass", "current"); } else { // update
     * section class menuStyleClass.put("section", "t2"); // update current menu
     * style class menuStyleClass.remove("sectionClass");
     * menuStyleClass.put("schedulingClass", "current"); }
     *
     * } else { menuStyleClass.put("sectionClass", "current");
     * menuStyleClass.put("section", "t1"); } }
     */
    public void updateMenuStyles(String navigateTo) {
        if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_NEW.equals(navigateTo)) {
            menuStyleClass.clear();
            menuStyleClass.put("section", "t1");

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_APP_SETTINGS.equals(navigateTo)) {
            menuStyleClass.clear();
            menuStyleClass.put("settingsClass", "current");
            menuStyleClass.put("section", "t2");

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_APP_ADSLOT.equals(navigateTo)) {
            menuStyleClass.clear();
            menuStyleClass.put("addSlotClass", "current");
            menuStyleClass.put("section", "t3");

        }
    }

    // private PublicationDto getPublicationDto() {
    // PublicationMBean bean = Utils.findBean(FacesContext.getCurrentInstance(),
    // Constants.PUBLICATION_MBEAN);
    // return bean.getPublicationDto();
    // }

    public void setNavigate(String navigate) {
        this.navigate = navigate;
    }

    public Map<String, String> getMenuStyleClass() {
        return menuStyleClass;
    }

    public void setMenuStyleClass(Map<String, String> menuStyleClass) {
        this.menuStyleClass = menuStyleClass;
    }

    public boolean isNewPub() {
        return newPub;
    }

    public void setNewPub(boolean newPub) {
        this.newPub = newPub;
    }

    public boolean isPubCreated() {
        return pubCreated;
    }

    public void setPubCreated(boolean pubCreated) {
        this.pubCreated = pubCreated;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getEncodedId() {
        return encodedId;
    }

    public void setEncodedId(String encodedId) {
        this.encodedId = encodedId;
    }
}
