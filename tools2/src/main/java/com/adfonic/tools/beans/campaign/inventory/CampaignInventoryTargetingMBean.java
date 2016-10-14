package com.adfonic.tools.beans.campaign.inventory;

import static com.adfonic.tools.beans.util.Constants.EXCHANGE_INVENTORY;
import static com.adfonic.tools.beans.util.Constants.MENU_CREATIVE;
import static com.adfonic.tools.beans.util.Constants.MENU_NAVIGATE_TO_CONFIRMATION;
import static com.adfonic.tools.beans.util.Constants.MENU_NAVIGATE_TO_CREATIVE;

import java.io.Serializable;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.enums.InventoryTargetingType;
import com.adfonic.dto.campaign.enums.SegmentSafetyLevel;
import com.adfonic.dto.campaign.privatemarketplace.PrivateMarketplaceDto;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.targetpublisher.TargetPublisherService;
import com.adfonic.tools.beans.campaign.targeting.CampaignTargetingInventoryMBean;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignInventoryTargetingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignInventoryTargetingMBean.class);

    private CampaignDto campaignDto;

    private TargetPublisherDto exchange;

    private String dealId;

    private SegmentSafetyLevel safetyLevel;

    private PublicationListInfoDto publicationListInfoDto;

    @Autowired
    private CampaignService service;
    @Autowired
    private TargetPublisherService tpService;

    // CATEGORIES
    @Autowired
    private CampaignInventoryCategoriesMBean campaignInventoryCategoriesMBean;
    @Autowired
    private CategoriesBlackListMBean categoriesBlackListMBean;

    // EXCHANGE_INVENTORY // before known as RUN_OF_NETWORK (MAD-1516)
    @Autowired
    private CampaignTargetingInventoryMBean campaignTargetingInventoryMBean;
    @Autowired
    private ExchangeInventoryBlackListMBean exchangeInventoryBlackListMBean;

    // WHITELIST
    @Autowired
    private WhiteListMBean whiteListMBean;

    private String selectedInventory = EXCHANGE_INVENTORY;

    public void doSave(ActionEvent event) throws Exception {
        LOGGER.debug("doSave-->");

        campaignDto = service.saveInventoryTargeting(prepareDto(campaignDto), publicationListInfoDto,
                campaignInventoryCategoriesMBean.includeUncategorized());
        updateCampaignBeans(campaignDto);

        if (getCNavigationBean().isCreativeDisabled()) {
            getCNavigationBean().setCreativeDisabled(false);
            getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), MENU_CREATIVE);
        }

        if (getCampaignMBean().isNewCampaign()) {
            getCNavigationBean().updateMenuStyles(MENU_NAVIGATE_TO_CREATIVE);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_creative.xhtml");
        } else {
            getCNavigationBean().updateMenuStyles(MENU_NAVIGATE_TO_CONFIRMATION);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        }
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        dto.setInventoryType(InventoryTargetingType.valueOf(this.selectedInventory));

        // Exchange Inventory
        if (isExchangeInventory(selectedInventory)) {
            dto = campaignTargetingInventoryMBean.prepareDto(dto);
            if (exchangeInventoryBlackListMBean.getSelectedList() != null
                    && exchangeInventoryBlackListMBean.getSelectedList().getId() != null) {
                if (exchangeInventoryBlackListMBean.validList()) {
                    this.publicationListInfoDto = exchangeInventoryBlackListMBean.prepareDto(dto);
                }
            } else {
                this.publicationListInfoDto = new PublicationListInfoDto();
                exchangeInventoryBlackListMBean.setHasSelectedList(false);
                exchangeInventoryBlackListMBean.cleanBean();
            }
            whiteListMBean.cleanBean();
            exchangeInventoryBlackListMBean.setHasSelectedList(false);
            exchangeInventoryBlackListMBean.cleanBean();
            campaignInventoryCategoriesMBean.cleanBean();
            categoriesBlackListMBean.setHasSelectedList(false);
            categoriesBlackListMBean.cleanBean();
            this.dealId = null;

            // App Site List
        } else if (isAppSiteList(selectedInventory)) {
            if (whiteListMBean.validList()) {
                this.publicationListInfoDto = whiteListMBean.prepareDto(dto);
                campaignTargetingInventoryMBean.cleanBean();
                exchangeInventoryBlackListMBean.setHasSelectedList(false);
                exchangeInventoryBlackListMBean.cleanBean();
                campaignInventoryCategoriesMBean.cleanBean();
                categoriesBlackListMBean.setHasSelectedList(false);
                categoriesBlackListMBean.cleanBean();
                this.dealId = null;
            }

            // IAB Category
        } else if (isIABCategory(selectedInventory)) {
            dto = campaignInventoryCategoriesMBean.prepareDto(dto);
            if (categoriesBlackListMBean.getSelectedList() != null && categoriesBlackListMBean.getSelectedList().getId() != null) {
                if (categoriesBlackListMBean.validList()) {
                    this.publicationListInfoDto = categoriesBlackListMBean.prepareDto(dto);
                }
            } else {
                this.publicationListInfoDto = new PublicationListInfoDto();
                categoriesBlackListMBean.setHasSelectedList(false);
                categoriesBlackListMBean.cleanBean();
            }
            whiteListMBean.cleanBean();
            campaignTargetingInventoryMBean.cleanBean();
            exchangeInventoryBlackListMBean.setHasSelectedList(false);
            exchangeInventoryBlackListMBean.cleanBean();
            this.dealId = null;

            // Private Marketplace
        } else if (isPrivateMarketPlace(selectedInventory)) {
            if (this.exchange != null) {
                PrivateMarketplaceDto pm = new PrivateMarketplaceDto();
                pm.setDealId(this.dealId);
                pm.setPublisher(this.exchange.getPublisher());
                this.campaignDto.setPrivateMarketPlaceDeal(pm);
            }
            campaignTargetingInventoryMBean.cleanBean();
            exchangeInventoryBlackListMBean.setHasSelectedList(false);
            exchangeInventoryBlackListMBean.cleanBean();
            whiteListMBean.cleanBean();
            campaignInventoryCategoriesMBean.cleanBean();
            categoriesBlackListMBean.setHasSelectedList(false);
            categoriesBlackListMBean.cleanBean();
        }

        if (!isAppSiteList(selectedInventory) && !isPrivateMarketPlace(selectedInventory)) {
            dto.getCurrentSegment().setSafetyLevel(this.safetyLevel.getSafetyLevel());
        } else {
            dto.getCurrentSegment().setSafetyLevel(SegmentSafetyLevel.OFF.getSafetyLevel());
        }
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void loadCampaign(CampaignDto dto) {
        LOGGER.debug("loadCampaign-->");
        if (dto != null) {
            this.whiteListMBean.setWhiteList(true);
            this.exchangeInventoryBlackListMBean.setWhiteList(false);
            this.categoriesBlackListMBean.setWhiteList(false);

            this.campaignDto = dto;
            InventoryTargetingType inventoryTargetingType = dto.getInventoryType();
            if (inventoryTargetingType == null) {
                this.selectedInventory = EXCHANGE_INVENTORY;
            } else {
                this.selectedInventory = inventoryTargetingType.name();
            }

            // Exchange Inventory
            if (isExchangeInventory(selectedInventory)) {
                campaignTargetingInventoryMBean.loadCampaignDto(dto);
                this.publicationListInfoDto = exchangeInventoryBlackListMBean.loadSelectedPublicationList(dto);

                // App Site List
            } else if (isAppSiteList(selectedInventory)) {
                this.publicationListInfoDto = whiteListMBean.loadSelectedPublicationList(dto);

                // IAB Category
            } else if (isIABCategory(selectedInventory)) {
                this.publicationListInfoDto = categoriesBlackListMBean.loadSelectedPublicationList(dto);

                // Private Marketplace
            } else if (isPrivateMarketPlace(selectedInventory)) {
                if (campaignDto.getPrivateMarketPlaceDeal() != null && campaignDto.getPrivateMarketPlaceDeal().getPublisher() != null) {
                    this.exchange = tpService.getTargetPublisherByPublisherId(campaignDto.getPrivateMarketPlaceDeal().getPublisher()
                            .getId());
                    this.dealId = campaignDto.getPrivateMarketPlaceDeal().getDealId();
                }
            }
            campaignInventoryCategoriesMBean.loadCampaignDto(dto);

            // Selecting safety Level
            if (this.campaignDto.getCurrentSegment().getSafetyLevel() != null) {
                this.safetyLevel = SegmentSafetyLevel.valueOf(this.campaignDto.getCurrentSegment().getSafetyLevel().name());
            } else if (isAppSiteList(selectedInventory)) {
                this.safetyLevel = SegmentSafetyLevel.OFF;
            } else {
                this.safetyLevel = SegmentSafetyLevel.SILVER;
            }
        }

        LOGGER.debug("loadCampaign<--");
    }

    // If safety level is off we show a dialog to confirm
    public void checkContinue(ActionEvent event) throws Exception {
        if (isValidInventory()) {
            if ((isExchangeInventory(selectedInventory) || isIABCategory(selectedInventory))
                    && this.safetyLevel.equals(SegmentSafetyLevel.OFF)) {
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("confirmationSave.show()");
            } else {
                doSave(event);
            }
        }
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        this.loadCampaign(campaignDto);
        LOGGER.debug("cancel<--");
    }

    public void changeSelection(ValueChangeEvent event) {
        // Changing from 'App Site List' or 'Private Marketplace' to the others
        // resets safety level selection
        // In other words unselect brand safety level in case of 'Exchange
        // Inventory' or 'IAB Category' to force the user to select a specific
        // safety level
        if ((isAppSiteList(event.getOldValue().toString()) || isPrivateMarketPlace(event.getOldValue().toString())
                && (isExchangeInventory(event.getNewValue().toString()) || isIABCategory(event.getNewValue().toString())))) {
            this.safetyLevel = null;
        }
    }

    public String getInventorySelectionSummary() {
        String inventorySummary = null;

        if (selectedInventory != null) {
            if (isExchangeInventory(selectedInventory) && CollectionUtils.isEmpty(campaignTargetingInventoryMBean.getRtb())
                    && CollectionUtils.isEmpty(campaignTargetingInventoryMBean.getNonRtb())
                    && !campaignTargetingInventoryMBean.isIncludeAdfonicNetwork()) {
                inventorySummary = notSet();
            }
            if (isExchangeInventory(selectedInventory)) {
                inventorySummary = FacesUtils.getBundleMessage("page.campaign.inventory.option.network");
            } else if (isAppSiteList(selectedInventory)) {
                inventorySummary = FacesUtils.getBundleMessage("page.campaign.inventory.option.list");
            } else if (isIABCategory(selectedInventory)) {
                inventorySummary = FacesUtils.getBundleMessage("page.campaign.inventory.option.category");
            } else if (isPrivateMarketPlace(selectedInventory)) {
                inventorySummary = FacesUtils.getBundleMessage("page.campaign.inventory.option.marketplace");
            }
        }
        return getDisplayValueNotSetOrUndefined(inventorySummary);
    }

    public String getExchangeSummary(boolean spaces) {
        String exchangeSummary = null;

        if (selectedInventory != null) {
            if (isExchangeInventory(selectedInventory)) {
                exchangeSummary = campaignTargetingInventoryMBean.getInventorySummary(spaces);
            } else if (isAppSiteList(selectedInventory)) {
                exchangeSummary = "N/A";
            } else if (isIABCategory(selectedInventory)) {
                exchangeSummary = campaignInventoryCategoriesMBean.getCategoriesSummary(spaces);
            } else if (isPrivateMarketPlace(selectedInventory) && this.exchange != null) {
                exchangeSummary = this.exchange.getName();
            }
        }
        return getDisplayValueNotSetOrUndefined(exchangeSummary);
    }

    public String getListSummary(boolean spaces) {
        String listSummary = null;

        if (selectedInventory != null) {
            if (isExchangeInventory(selectedInventory)) {
                listSummary = exchangeInventoryBlackListMBean.getPublicationsSummary(spaces);
            } else if (isAppSiteList(selectedInventory)) {
                listSummary = whiteListMBean.getPublicationsSummary(spaces);
            } else if (isIABCategory(selectedInventory)) {
                listSummary = categoriesBlackListMBean.getPublicationsSummary(spaces);
            } else if (isPrivateMarketPlace(selectedInventory)) {
                listSummary = this.dealId;
            }
        }
        return getDisplayValueNotSetOrUndefined(listSummary);
    }

    public String getSafetySummary() {
        String safetySummary = null;

        if (selectedInventory != null && this.safetyLevel != null) {
            safetySummary = FacesUtils.getBundleMessage(this.safetyLevel.getSafetyLevelShortStr());
        }

        return getDisplayValueNotSetOrUndefined(safetySummary);
    }

    public String getInventoryLabel() {
        String inventoryLabel = null;

        if (selectedInventory != null) {
            if (isAppSiteList(selectedInventory)) {
                inventoryLabel = FacesUtils.getBundleMessage("page.campaign.inventory.menu.whitelist.label");
            } else if (isPrivateMarketPlace(selectedInventory)) {
                inventoryLabel = FacesUtils.getBundleMessage("page.campaign.inventory.marketplace.dealid.label");
            } else {
                inventoryLabel = FacesUtils.getBundleMessage("page.campaign.inventory.menu.blacklist.label");
            }
        }

        return getDisplayValueNotSetOrUndefined(inventoryLabel);
    }

    public String getWhiteListBlackListOrDealIdName() {
        String listName = null;

        if (isAppSiteList(selectedInventory)) {
            listName = whiteListMBean.getListName();
        } else if (isExchangeInventory(selectedInventory)) {
            listName = exchangeInventoryBlackListMBean.getListName();
        } else if (isIABCategory(selectedInventory)) {
            listName = categoriesBlackListMBean.getListName();
        } else if (isPrivateMarketPlace(selectedInventory)) {
            listName = dealId;
        }

        return getDisplayValueNotSetOrUndefined(listName);
    }

    public void addNetworkBlackList(ActionEvent event) {
        exchangeInventoryBlackListMBean.setHasSelectedList(true);
    }

    public void addCategoriesBlackList(ActionEvent event) {
        categoriesBlackListMBean.setHasSelectedList(true);
    }

    public void setToDefault() {
        this.selectedInventory = EXCHANGE_INVENTORY;
        if (this.campaignTargetingInventoryMBean.getRtb() != null) {
            this.campaignTargetingInventoryMBean.getRtb().clear();
        }
        if (this.campaignTargetingInventoryMBean.getNonRtb() != null) {
            this.campaignTargetingInventoryMBean.getNonRtb().clear();
        }

        this.campaignTargetingInventoryMBean.setIncludeAdfonicNetwork(false);
        try {
            campaignDto = service.saveInventoryTargeting(prepareDto(campaignDto), publicationListInfoDto, false);
        } catch (Exception e) {
        }
        campaignTargetingInventoryMBean.loadCampaignDto(campaignDto);
    }

    @Override
    protected void init() {
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public CampaignTargetingInventoryMBean getCampaignTargetingInventoryMBean() {
        return campaignTargetingInventoryMBean;
    }

    public void setCampaignTargetingInventoryMBean(CampaignTargetingInventoryMBean campaignTargetingInventoryMBean) {
        this.campaignTargetingInventoryMBean = campaignTargetingInventoryMBean;
    }

    public String getSelectedInventory() {
        return selectedInventory;
    }

    public void setSelectedInventory(String selectedInventory) {
        this.selectedInventory = selectedInventory;
    }

    public WhiteListMBean getWhiteListMBean() {
        return whiteListMBean;
    }

    public void setWhiteListMBean(WhiteListMBean whiteListMBean) {
        this.whiteListMBean = whiteListMBean;
    }

    public ExchangeInventoryBlackListMBean getExchangeInventoryBlackListMBean() {
        return exchangeInventoryBlackListMBean;
    }

    public void setExchangeInventoryBlackListMBean(ExchangeInventoryBlackListMBean exchangeInventoryBlackListMBean) {
        this.exchangeInventoryBlackListMBean = exchangeInventoryBlackListMBean;
    }

    public CampaignInventoryCategoriesMBean getCampaignInventoryCategoriesMBean() {
        return campaignInventoryCategoriesMBean;
    }

    public void setCampaignInventoryCategoriesMBean(CampaignInventoryCategoriesMBean campaignInventoryCategoriesMBean) {
        this.campaignInventoryCategoriesMBean = campaignInventoryCategoriesMBean;
    }

    public CategoriesBlackListMBean getCategoriesBlackListMBean() {
        return categoriesBlackListMBean;
    }

    public void setCategoriesBlackListMBean(CategoriesBlackListMBean categoriesBlackListMBean) {
        this.categoriesBlackListMBean = categoriesBlackListMBean;
    }

    public TargetPublisherDto getExchange() {
        return exchange;
    }

    public void setExchange(TargetPublisherDto exchange) {
        this.exchange = exchange;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public SegmentSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SegmentSafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    // PRIVATE METHODS

    private boolean isValidInventory() {
        boolean bResult = true;
        if (isAppSiteList(selectedInventory)) {
            bResult = whiteListMBean.validList();
        } else if (isExchangeInventory(selectedInventory)) {
            bResult = this.campaignTargetingInventoryMBean.isValid();
        } else if (isIABCategory(selectedInventory)) {
            bResult = this.campaignInventoryCategoriesMBean.isValid();
        }
        return bResult;
    }
}
