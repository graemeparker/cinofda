package com.adfonic.tools.beans.reporting.navigation;

import static com.adfonic.tools.beans.util.Constants.NAVIGATE_TO;
import static com.adfonic.tools.beans.util.Constants.REPORTING_BUDGETS_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_BUDGETS_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CAMPAIGNS_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CAMPAIGNS_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CONNECTIONS_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CONNECTIONS_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CREATIVES_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_CREATIVES_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_DEVICES_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_DEVICES_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_LOCATIONS_CLASS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_LOCATIONS_VIEW;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_BUDGETS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_CAMPAIGNS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_CONNECTIONS;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_CREATIVES;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_DEVICES;
import static com.adfonic.tools.beans.util.Constants.REPORTING_MENU_NAVIGATE_TO_LOCATIONS;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class ReportingNavigationSessionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ReportingNavigationSessionBean.class);

    private Map<String, String> menuStyleClass = new HashMap<String, String>(0);
    private String navigate;

    public ReportingNavigationSessionBean() {
        LOGGER.debug("new report navigation session bean");
        initNavigation();
    }

    /**
     * Set default reporting navigation to Campaigns
     */
    public void initNavigation() {
        updateMenuStyles(REPORTING_MENU_NAVIGATE_TO_CAMPAIGNS);
    }

    public void doNavigateTo(ActionEvent event) {
        updateMenuStyles((String) event.getComponent().getAttributes().get(NAVIGATE_TO));
    }

    public void updateMenuStyles(String navigateTo) {
        if (StringUtils.isNotEmpty(navigateTo)) {
            switch (navigateTo) {
            case REPORTING_MENU_NAVIGATE_TO_CAMPAIGNS:
                storeMenuStyleAndView(REPORTING_CAMPAIGNS_CLASS, "t2", REPORTING_CAMPAIGNS_VIEW);
                break;
            case REPORTING_MENU_NAVIGATE_TO_DEVICES:
                storeMenuStyleAndView(REPORTING_DEVICES_CLASS, "t3", REPORTING_DEVICES_VIEW);
                break;
            case REPORTING_MENU_NAVIGATE_TO_BUDGETS:
                storeMenuStyleAndView(REPORTING_BUDGETS_CLASS, "t4", REPORTING_BUDGETS_VIEW);
                break;
            case REPORTING_MENU_NAVIGATE_TO_CREATIVES:
                storeMenuStyleAndView(REPORTING_CREATIVES_CLASS, "t5", REPORTING_CREATIVES_VIEW);
                break;
            case REPORTING_MENU_NAVIGATE_TO_LOCATIONS:
                storeMenuStyleAndView(REPORTING_LOCATIONS_CLASS, "t6", REPORTING_LOCATIONS_VIEW);
                break;
            case REPORTING_MENU_NAVIGATE_TO_CONNECTIONS:
                storeMenuStyleAndView(REPORTING_CONNECTIONS_CLASS, "t7", REPORTING_CONNECTIONS_VIEW);
                break;
            default:
                break;
            }
        }
    }

    private void storeMenuStyleAndView(String styleClass, String level, String view) {
        menuStyleClass.clear();
        menuStyleClass.put(styleClass, "current");
        menuStyleClass.put("section", level);
        navigate = view;
    }

    // Getters for the Reporting menu

    public String getNavigate() {
        return navigate;
    }

    public Map<String, String> getMenuStyleClass() {
        return menuStyleClass;
    }

}
