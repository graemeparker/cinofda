package com.adfonic.tools.beans.audience.navigation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.audience.enums.AudienceType;
import com.adfonic.tools.beans.audience.AudienceMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("session")
public class AudienceNavigationSessionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceNavigationSessionBean.class);

    // default url to navigate
    private String navigate;
    private boolean sourceDisabled = true;
    private boolean confirmationDisabled = true;
    private boolean fromSetup = false;
    private AudienceType type = null;

    private Map<String, String> menuStyleClass = new HashMap<String, String>(0);

    private String encodedId;

    public AudienceNavigationSessionBean() {
        LOGGER.debug("new session bean");
        initNavigation();
    }

    public void restartMenu() {
        setSourceDisabled(true);
        confirmationDisabled = true;
    }

    public void initNavigation() {
        menuStyleClass.clear();
        menuStyleClass.put("setupClass", "current");
        menuStyleClass.put("setup", "t1");
        restartMenu();
    }

    public String getNavigate() {
        AudienceMBean bean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.AUDIENCE_MBEAN);
        if (bean.getAudienceDto() != null && bean.getAudienceDto().getId() != null && bean.getAudienceDto().getId().intValue() > 0) {

        } else {
            if (StringUtils.isEmpty(navigate) || Constants.AUDIENCE_DEFAULT_NAVIGATION.equals(navigate)) {
                navigate = Constants.AUDIENCE_DEFAULT_NAVIGATION;
            }
        }
        return navigate;
    }

    public void doNavigateTo(ActionEvent event) {
        String navigateTo = (String) event.getComponent().getAttributes().get(Constants.NAVIGATE_TO);

        if (StringUtils.isNotEmpty(navigateTo)) {
            switch (navigateTo) {
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_SOURCE:
                navigate = Constants.AUDIENCE_SOURCE_VIEW;
                updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SOURCE);
                break;
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION:
                navigate = Constants.AUDIENCE_CONFIRMATION_VIEW;
                updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
                break;
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_SETUP:
            default:
                navigate = Constants.AUDIENCE_SETUP_VIEW;
                updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SETUP);
            }
        }
    }

    public void updateMenuStyles(String navigateTo) {
        if (StringUtils.isNotEmpty(navigateTo)) {
            switch (navigateTo) {
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_SETUP:
                menuStyleClass.clear();
                menuStyleClass.put("setupClass", "current");
                menuStyleClass.put("section", getSection());
                break;
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_SOURCE:
                menuStyleClass.clear();
                menuStyleClass.put("sourceClass", "current");
                menuStyleClass.put("section", getSection());
                break;
            case Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION:
                menuStyleClass.clear();
                menuStyleClass.put("confirmationClass", "current");
                menuStyleClass.put("section", getSection());
                break;
            default:
                // moop
            }
        }
    }

    public void openAllMenu() {
        menuStyleClass.clear();
        menuStyleClass.put("section", "current");
        menuStyleClass.put("section", "t3");
        sourceDisabled = false;
        confirmationDisabled = false;
    }

    private String getSection() {
        if (!confirmationDisabled) {
            return "t3";
        } else if (!sourceDisabled) {
            return "t2";
        } else {
            return "t1";
        }
    }

    public void setNavigate(String navigate) {
        this.navigate = navigate;
    }

    public Map<String, String> getMenuStyleClass() {
        return menuStyleClass;
    }

    public void setMenuStyleClass(Map<String, String> menuStyleClass) {
        this.menuStyleClass = menuStyleClass;
    }

    public String getEncodedId() {
        return encodedId;
    }

    public void setEncodedId(String encodedId) {
        this.encodedId = encodedId;
    }

    public boolean isSourceDisabled() {
        return sourceDisabled;
    }

    public void setSourceDisabled(boolean sourceDisabled) {
        this.sourceDisabled = sourceDisabled;
    }

    public boolean isConfirmationDisabled() {
        return confirmationDisabled;
    }

    public void setConfirmationDisabled(boolean confirmationDisabled) {
        this.confirmationDisabled = confirmationDisabled;
    }

    public boolean isFromSetup() {
        return fromSetup;
    }

    public void setFromSetup(boolean fromSetup) {
        this.fromSetup = fromSetup;
    }

    public AudienceType getType() {
        return type;
    }

    public void setType(AudienceType type) {
        this.type = type;
    }

}
