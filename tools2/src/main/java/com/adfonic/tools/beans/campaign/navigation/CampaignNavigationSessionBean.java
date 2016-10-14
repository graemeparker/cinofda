package com.adfonic.tools.beans.campaign.navigation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.tools.beans.campaign.CampaignMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("session")
public class CampaignNavigationSessionBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // default url to navigate
    private String navigate;

    private Map<String, String> menuStyleClass = new HashMap<String, String>(0);

    private boolean schedulingDisabled = true;
    private boolean targetingDisabled = true;
    private boolean inventoryTargetingDisabled = true;
    private boolean creativeDisabled = true;
    private boolean biddingDisabled = true;
    private boolean trackingDisabled = true;
    private boolean confirmationDisabled = true;

    private boolean campaignBlocked = false;

    private boolean fromSetup = false;
    private boolean fromCopy = false;

    private String encodedId;

    private Map<Long, Integer> campaignNavigationSaved = null;

    public CampaignNavigationSessionBean() {
        initNavigation();
    }

    public String getNavigate() {
        CampaignMBean bean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.CAMPAIGN_MBEAN);
        if (bean.getCampaignDto() != null && bean.getCampaignDto().getId() != null && bean.getCampaignDto().getId().intValue() > 0) {

        } else {
            if (StringUtils.isEmpty(navigate) || Constants.DEFAULT_NAVIGATION.equals(navigate)) {
                navigate = Constants.DEFAULT_NAVIGATION;
            }
        }
        return navigate;
    }

    public void restartMenu() {
        schedulingDisabled = true;
        targetingDisabled = true;
        inventoryTargetingDisabled = true;
        creativeDisabled = true;
        biddingDisabled = true;
        trackingDisabled = true;
        confirmationDisabled = true;
    }

    public void initNavigation() {
        menuStyleClass.clear();
        menuStyleClass.put("sectionClass", "current");
        menuStyleClass.put("section", "t1");

        restartMenu();
    }

    public void openAllMenu() {

        menuStyleClass.clear();
        menuStyleClass.put("sectionClass", "current");
        menuStyleClass.put("section", "t7");

        schedulingDisabled = false;
        targetingDisabled = false;
        inventoryTargetingDisabled = false;
        creativeDisabled = false;
        biddingDisabled = false;
        trackingDisabled = false;
        confirmationDisabled = false;
    }

    public void doNavigateTo(ActionEvent event) {
        String navigateTo = (String) event.getComponent().getAttributes().get(Constants.NAVIGATE_TO);
        if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SETUP.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_setup.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_SETUP);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SCHEDULING.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_scheduling.xhtml";

            updateMenuStyles(Constants.MENU_NAVIGATE_TO_SCHEDULING);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_TARGETING.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_targeting.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_TARGETING);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_INVENTORY_TARGETING.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_inventory.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_INVENTORY_TARGETING);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_CREATIVE.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_creative.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_CREATIVE);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_TRACKING.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_tracking.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_TRACKING);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_BIDDING.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_bidding.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_BIDDING);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_CONFIRMATION.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_confirmation.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_HISTORY.equals(navigateTo)) {
            navigate = "/WEB-INF/jsf/campaign/section_history.xhtml";
            updateMenuStyles(Constants.MENU_NAVIGATE_TO_HISTORY);
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

        if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SETUP.equals(navigateTo)) {
            menuStyleClass.clear();
            menuStyleClass.put("sectionClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_SCHEDULING.equals(navigateTo)) {
            menuStyleClass.clear();
            menuStyleClass.put("schedulingClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_TARGETING.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("targetingClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_INVENTORY_TARGETING.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("inventoryTargetingClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_CREATIVE.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("creativeClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_BIDDING.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("budgetClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_TRACKING.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("trackingClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_CONFIRMATION.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("confirmationClass", "current");
            menuStyleClass.put("section", getSection());

        } else if (!StringUtils.isEmpty(navigateTo) && Constants.MENU_NAVIGATE_TO_HISTORY.equals(navigateTo)) {

            menuStyleClass.clear();
            menuStyleClass.put("historyClass", "current");
            menuStyleClass.put("section", "t8");

        }
    }

    public void saveCampaignNavigation(long campaignId, int navigation) {
        if (campaignNavigationSaved == null) {
            loadCampaignsMapFromCookies();
        }
        campaignNavigationSaved.put(campaignId, navigation);
        addCampaignNavigationCookie();
    }

    public void removeCampaignNavigation(long campaignId) {
        if (campaignNavigationSaved == null) {
            loadCampaignsMapFromCookies();
        }
        campaignNavigationSaved.remove(campaignId);
        addCampaignNavigationCookie();
    }

    public void setNavigationForCampaign(long campaignId) {
        if (campaignNavigationSaved == null) {
            loadCampaignsMapFromCookies();
        }
        int index = Constants.MENU_SCHEDULING;
        // We have information from the campaign
        if (campaignNavigationSaved.containsKey(campaignId)) {
            index = campaignNavigationSaved.get(campaignId);
        }
        enabledSectionsFromIndex(index);
        updateMenuStyles(getNavigateTo(index));
    }

    private void loadCampaignsMapFromCookies() {
        campaignNavigationSaved = new HashMap<Long, Integer>();
        Map<String, Object> cookiesMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        if (!CollectionUtils.isEmpty(cookiesMap)) {
            if (cookiesMap.get(Constants.COOKIE_CAMP_NAVIGATION) != null) {
                Cookie cookie = (Cookie) cookiesMap.get(Constants.COOKIE_CAMP_NAVIGATION);
                String value = cookie.getValue();
                if (!StringUtils.isEmpty(cookie.getValue())) {
                    String[] buffer = value.split("#");
                    for (int i = 0; i < buffer.length; i++) {
                        try {
                            campaignNavigationSaved.put(Long.valueOf(buffer[i].split("-")[0]), Integer.valueOf(buffer[i].split("-")[1]));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private String getSection() {
        if (!biddingDisabled) {
            return "t7";
        } else if (!trackingDisabled) {
            return "t6";
        } else if (!creativeDisabled) {
            return "t5";
        } else if (!inventoryTargetingDisabled) {
            return "t4";
        } else if (!targetingDisabled) {
            return "t3";
        } else if (!schedulingDisabled) {
            return "t2";
        } else {
            return "t1";
        }
    }

    private void addCampaignNavigationCookie() {
        String cookieValue = Utils.getCampaignNavigationCookieProperties(campaignNavigationSaved);
        if (!StringUtils.isEmpty(cookieValue)) {
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(Constants.COOKIE_CAMP_NAVIGATION, cookieValue, props);
        }

    }

    private void enabledSectionsFromIndex(int index) {
        if (index > Constants.MENU_SETUP) {
            this.schedulingDisabled = false;
        }
        if (index > Constants.MENU_SCHEDULING) {
            this.targetingDisabled = false;
        }
        if (index > Constants.MENU_TARGETING) {
            this.inventoryTargetingDisabled = false;
        }
        if (index > Constants.MENU_INVENTORY_TARGETING) {
            this.creativeDisabled = false;
        }
        if (index > Constants.MENU_CREATIVE) {
            this.trackingDisabled = false;
        }
        if (index > Constants.MENU_TRACKING) {
            this.biddingDisabled = false;
        }
        if (index > Constants.MENU_BIDDING) {
            this.biddingDisabled = false;
        }
    }

    private String getNavigateTo(int index) {
        if (index == Constants.MENU_SETUP) {
            this.navigate = "/WEB-INF/jsf/campaign/section_setup.xhtml";
            return Constants.MENU_NAVIGATE_TO_SETUP;
        } else if (index == Constants.MENU_SCHEDULING) {
            this.navigate = "/WEB-INF/jsf/campaign/section_scheduling.xhtml";
            return Constants.MENU_NAVIGATE_TO_SCHEDULING;
        } else if (index == Constants.MENU_TARGETING) {
            this.navigate = "/WEB-INF/jsf/campaign/section_targeting.xhtml";
            return Constants.MENU_NAVIGATE_TO_TARGETING;
        } else if (index == Constants.MENU_CREATIVE) {
            this.navigate = "/WEB-INF/jsf/campaign/section_creative.xhtml";
            return Constants.MENU_NAVIGATE_TO_CREATIVE;
        } else if (index == Constants.MENU_TRACKING) {
            this.navigate = "/WEB-INF/jsf/campaign/section_tracking.xhtml";
            return Constants.MENU_NAVIGATE_TO_TRACKING;
        } else if (index == Constants.MENU_BIDDING) {
            this.navigate = "/WEB-INF/jsf/campaign/section_bidding.xhtml";
            return Constants.MENU_NAVIGATE_TO_BIDDING;
        } else {
            this.navigate = "/WEB-INF/jsf/campaign/section_confirmation.xhtml";
            return Constants.MENU_NAVIGATE_TO_CONFIRMATION;
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

    public boolean isSchedulingDisabled() {
        return schedulingDisabled;
    }

    public void setSchedulingDisabled(boolean schedulingDisabled) {
        this.schedulingDisabled = schedulingDisabled;
    }

    public boolean isTargetingDisabled() {
        return targetingDisabled;
    }

    public void setTargetingDisabled(boolean targetingDisabled) {
        this.targetingDisabled = targetingDisabled;
    }

    public boolean isInventoryTargetingDisabled() {
        return inventoryTargetingDisabled;
    }

    public void setInventoryTargetingDisabled(boolean inventoryTargetingDisabled) {
        this.inventoryTargetingDisabled = inventoryTargetingDisabled;
    }

    public boolean isCreativeDisabled() {
        return creativeDisabled;
    }

    public void setCreativeDisabled(boolean creativeDisabled) {
        this.creativeDisabled = creativeDisabled;
    }

    public boolean isBiddingDisabled() {
        return biddingDisabled;
    }

    public void setBiddingDisabled(boolean biddingDisabled) {
        this.biddingDisabled = biddingDisabled;
    }

    public boolean isTrackingDisabled() {
        return trackingDisabled;
    }

    public void setTrackingDisabled(boolean trackingDisabled) {
        this.trackingDisabled = trackingDisabled;
    }

    public boolean isConfirmationDisabled() {
        return confirmationDisabled;
    }

    public void setConfirmationDisabled(boolean confirmationDisabled) {
        this.confirmationDisabled = confirmationDisabled;
    }

    public String getEncodedId() {
        return encodedId;
    }

    public void setEncodedId(String encodedId) {
        this.encodedId = encodedId;
    }

    public boolean isFromSetup() {
        return fromSetup;
    }

    public void setFromSetup(boolean fromSetup) {
        this.fromSetup = fromSetup;
    }

    public boolean isFromCopy() {
        return fromCopy;
    }

    public void setFromCopy(boolean fromCopy) {
        this.fromCopy = fromCopy;
    }

    public boolean isCampaignBlocked() {
        return campaignBlocked;
    }

    public void setCampaignBlocked(boolean campaignBlocked) {
        this.campaignBlocked = campaignBlocked;
    }
}
