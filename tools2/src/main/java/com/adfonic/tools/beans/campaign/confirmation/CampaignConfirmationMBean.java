package com.adfonic.tools.beans.campaign.confirmation;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.Category;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.category.CategoryDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.tools.beans.campaign.category.CategorySearchMBean;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignConfirmationMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private CampaignService service;
    
    @Autowired
    private CompanyService companyService;

    private CampaignDto campaignDto;

    private CampaignCreativeDto campaignCreativeDto;

    @Autowired
    private CategorySearchMBean categorySearchMBean;

    private String advertiserDomain;

    private List<CategoryHierarchyDto> excludedCategories;
    private Map<CategoryHierarchyDto, String> excludedCategoryLabelMap;

    private CategoryHierarchyDto campaignIabCategory;

    public boolean approveAllNewCreatives = true;

    private String selectedStatus = "ACTIVE";
    private List<SelectItem> status = null;

    private SegmentSafetyLevel safetyLevel;
    
    // Account fixed margin [MAD-3348]
    private AccountFixedMarginDto accountFixedMarginDto;

    @Override
    protected void init() {
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
        this.loadExcludedCategories();
        this.loadCampaignIabCategory();
        if (campaignDto != null) {
            this.advertiserDomain = campaignDto.getAdvertiserDomain();
        } else {
            this.advertiserDomain = null;
        }
        this.safetyLevel = service.getSafetyLevelForCampaign(campaignDto);
        this.accountFixedMarginDto = companyService.getAccountFixedMargin(getUser().getCompany().getId());
    }

    public CampaignCreativeDto getCampaignCreativeDto() {
        return campaignCreativeDto;
    }

    public void setCampaignCreativeDto(CampaignCreativeDto campaignCreativeDto) {
        this.campaignCreativeDto = campaignCreativeDto;
    }

    public String showCampaignConfirmation() throws Exception {
        if (isNewCampaign()) {
            RequestContext.getCurrentInstance().execute("confirmCampaignChanges.show()");
            return null;
        } else {
            return launch(null);
        }
    }

    public String pendingToPaused() throws Exception {
        return launch(CampaignStatus.PENDING_PAUSED);
    }

    public String pendingToActive() throws Exception {
        return launch(CampaignStatus.PENDING);
    }

    private String launch(CampaignStatus campaignSubmissionStatus) throws Exception {
        if (StringUtils.isEmpty(campaignDto.getName())) {
            updateCampaignBeans(null);
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/error");
        }

        if (isNewCampaign()) {
            campaignDto = service.submit(getCampaignCreativeBean().getCampaignDto(), getUser(), campaignSubmissionStatus);
            addFacesMessage(FacesMessage.SEVERITY_INFO, "navigationDiv", null, "page.campaign.message.campaigncreated",
                    campaignDto.getName());
        }

        getCampaignMBean().initCampaignWorkflow();
        updateCampaignBeans(null);

        return "pretty:dashboard-advertiser";
    }

    public Date getStartDate() {
        Date date = null;
        if (campaignDto != null && campaignDto.getTimePeriods() != null) {
            for (CampaignTimePeriodDto period : campaignDto.getTimePeriods()) {
                if (date == null) {
                    date = period.getStartDate();
                } else if (period.getStartDate().before(date)) {
                    date = period.getStartDate();
                }
            }
        }
        return date;
    }

    public Date getEndDate() {
        Date date = null;
        for (CampaignTimePeriodDto period : campaignDto.getTimePeriods()) {
            if (date == null || period.getEndDate() == null || period.getEndDate().after(date)) {
                date = period.getEndDate();
            }
        }
        return date;
    }

    public boolean getDisplayIspOperators() {
        return campaignDto != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getIspOperators());
    }
    
    public boolean getDisplayMobileOperators() {
        return campaignDto != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getMobileOperators());
    }

    public String getExcludeOpera() {
        if (getCampaignTargetingBean().getCampaignTargetingConnectionMBean().isExcludedOpera()) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String getConnectionsSummary() {
        if (campaignDto != null) {
            String message = "";
            String connectionTypes = campaignDto.getSegments().get(0).getConnectionType();
            if (!StringUtils.isEmpty(connectionTypes) && connectionTypes.equals("WIFI")) {
                message += FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.wifi.label");
            }
            if (!StringUtils.isEmpty(connectionTypes) && connectionTypes.equals("OPERATOR")) {
                message += FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.operator.label");
            }
            if (!StringUtils.isEmpty(connectionTypes) && connectionTypes.equals("BOTH")) {
                message += FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.wifi.label") + " and "
                        + FacesUtils.getBundleMessage("page.campaign.targeting.connection.connectionoptions.operator.label");
            }
            return message;
        }
        return notSet();
    }

    public String displayTimePeriods() {
        if (campaignDto != null && campaignDto.getTimePeriods() != null && campaignDto.getTimePeriods().size() > 1) {
            return "";
        } else {
            return "display:none";
        }
    }

    public String getContinueButtonMessage() {
        if (isNewCampaign()) {
            return FacesUtils.getBundleMessage("page.campaign.confirmation.button.launch.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.creative.done.label");
        }
    }

    public String getApprovalButtonMessage() {
        if (isNewCampaign()) {
            return FacesUtils.getBundleMessage("page.campaign.confirmation.button.adopsapproval.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.confirmation.button.adopsedit.label");
        }
    }

    public String getFixedMargin() {
        String fixedMargin = null;
        if (campaignDto == null){ 
            fixedMargin = null;
        }else{
            if ((accountFixedMarginDto!=null) && 
                ( (campaignDto.getCurrentTradingDeskMargin()==null) || ((campaignDto.getCurrentTradingDeskMargin()!=null)&&(campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().compareTo(accountFixedMarginDto.getMargin())==0)))){
                fixedMargin = FacesUtils.getBundleMessage("page.campaign.bidbudget.fixedmargin.placeholder");
            }else if (campaignDto.getCurrentTradingDeskMargin()!=null){
                fixedMargin = campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().multiply(new BigDecimal(100)).setScale(2) + "%";
            }
        }
        return fixedMargin;      
    }

    public BigDecimal getAgencyDiscount() {
        if (campaignDto == null || campaignDto.getCurrentAgencyDiscount() == null
                || campaignDto.getCurrentAgencyDiscount().getDiscount().doubleValue() == 0.0) {
            return null;
        }
        return campaignDto.getCurrentAgencyDiscount().getDiscount().multiply(new BigDecimal(100));
    }

    public boolean isRenderBlackList() {
        if ((isExchangeInventory(getCampaignInventoryTargetingBean().getSelectedInventory()) && getCampaignInventoryTargetingBean()
                .getExchangeInventoryBlackListMBean().isHasSelectedList())
                || (isIABCategory(getCampaignInventoryTargetingBean().getSelectedInventory()) && getCampaignInventoryTargetingBean()
                        .getCategoriesBlackListMBean().isHasSelectedList())) {
            return true;
        }
        // Add categories case when created
        return false;
    }

    public boolean isRenderWhiteList() {
        return isAppSiteList(getCampaignInventoryTargetingBean().getSelectedInventory());
    }

    public boolean isRenderPrivateMarketplace() {
        return isPrivateMarketPlace(getCampaignInventoryTargetingBean().getSelectedInventory());
    }

    private boolean isNewCampaign() {
        return campaignDto.getStatus().equals(CampaignStatus.NEW_REVIEW.getStatus())
                || campaignDto.getStatus().equals(CampaignStatus.NEW.getStatus());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Category handling
    // ---------------------------------------------------------------------------------------------------------------

    public String getCategoryHierarchyName(CategoryHierarchyDto category) {
        return categorySearchMBean.getHierarchicalName(category);
    }

    // ---------------------------------------------------------------------------------------------------------------
    public void loadExcludedCategories() {
        excludedCategories = categorySearchMBean.getExcludedCategoriesForCampaign(campaignDto);
        Map<CategoryHierarchyDto, String> labelMap = new HashMap<CategoryHierarchyDto, String>();
        if (excludedCategories == null) {
            excludedCategories = new ArrayList<>();
        }
        for (CategoryHierarchyDto c : excludedCategories) {
            labelMap.put(c, categorySearchMBean.getHierarchicalName(c));
        }
        this.excludedCategoryLabelMap = labelMap;
    }

    public List<CategoryHierarchyDto> getExcludedCategories() {
        return this.excludedCategories;
    }

    public void setExcludedCategories(List<CategoryHierarchyDto> excludedCategories) {
        // this.excludedCategories = excludedCategories;
    }

    public List<CategoryHierarchyDto> getExcludedCategoriesAsList() {
        return new ArrayList<CategoryHierarchyDto>(this.excludedCategories);
    }

    public Map<CategoryHierarchyDto, String> getExcludedCategoryLabelMap() {
        return this.excludedCategoryLabelMap;
    }

    public void doRemoveExcludedCategory(CategoryDto category) {
        this.excludedCategories.remove(category);
    }

    public void handleSelectedExcludedCategory(SelectEvent event) {
        CategoryHierarchyDto c = (CategoryHierarchyDto) event.getObject();
        if (!this.excludedCategories.contains(c)) {
            this.excludedCategories.add(c);
        }
    }

    public void handleSelectedExcludedCategory(UnselectEvent event) {
        CategoryHierarchyDto c = (CategoryHierarchyDto) event.getObject();
        this.excludedCategories.remove(c);
    }

    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Returns a list even tho this is a single item, so that the PF control
     * renders it with the [x] box
     *
     * @return
     */
    public List<CategoryHierarchyDto> getCampaignIabCategory() {
        List<CategoryHierarchyDto> list = new ArrayList<>();
        if (campaignIabCategory != null) {
            list.add(campaignIabCategory);
        }
        return list;
    }

    public void setCampaignIabCategory(List<CategoryHierarchyDto> campaignIabCategory) {
        // this.campaignIabCategory = campaignIabCategory;
    }

    /**
     * Category.NOT_CATEGORIZED_NAME cannot be translated to a hierarchical
     * name, so if that is what is selected, leave the control empty
     */
    public void loadCampaignIabCategory() {
        CategoryHierarchyDto c = categorySearchMBean.getCampaignIabCategory(campaignDto);
        if (c != null && !Category.NOT_CATEGORIZED_NAME.equals(c.getName())) {
            campaignIabCategory = c;
        }
    }

    public void handleSelectedCampaignIabCategory(SelectEvent event) {
        CategoryHierarchyDto c = (CategoryHierarchyDto) event.getObject();
        this.campaignIabCategory = c;
    }

    public void handleSelectedCampaignIabCategory(UnselectEvent event) {
        this.campaignIabCategory = null;
    }

    // ---------------------------------------------------------------------------------------------------------------

    public CategorySearchMBean getCategorySearchMBean() {
        return categorySearchMBean;
    }

    public void setCategorySearchMBean(CategorySearchMBean categorySearchMBean) {
        this.categorySearchMBean = categorySearchMBean;
    }

    // ---------------------------------------------------------------------------------------------------------------

    public String getAdvertiserDomain() {
        return advertiserDomain;
    }

    public void setAdvertiserDomain(String advertiserDomain) {
        this.advertiserDomain = advertiserDomain;
    }

    public String getAdOpsApprovalCampaignDomainLabel() {
        return FacesUtils.getBundleMessage("page.campaign.confirmation.adopsapproval.campaigndomain.label");
    }

    public String getAdOpsApprovalSafetyLevelLabel() {
        return FacesUtils.getBundleMessage("page.campaign.confirmation.adopsapproval.safetylevel.label");
    }

    public String getAdOpsApprovalCampaignIabCategoryLabel() {
        return FacesUtils.getBundleMessage("page.campaign.confirmation.adopsapproval.campaigniabcategory.label");
    }

    public String getAdOpsApprovalBlacklistedPublicationsCategoriesLabel() {
        return FacesUtils.getBundleMessage("page.campaign.confirmation.adopsapproval.blacklistedpublicationscategories.label");
    }

    public String adOpsApproval() throws Exception {
        if (campaignIabCategory == null) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "mainFormAdops:campaignIabCategoryAutoComplete", null,
                    "page.campaign.validation.campaigniabcategory.required");
            return null;
        }

        AdfonicUser adfonicUser = getAdfonicUser();

        if (isNewCampaign()) {
            service.adOpsActivateNewCampaign(campaignDto, advertiserDomain, safetyLevel, campaignIabCategory, excludedCategories,
                    adfonicUser);
        } else {
            service.adOpsUpdateExistingCampaign(campaignDto, advertiserDomain, safetyLevel, campaignIabCategory, excludedCategories,
                    approveAllNewCreatives, adfonicUser);
        }

        updateStatus();

        getCampaignMBean().initCampaignWorkflow();
        updateCampaignBeans(null);

        return "pretty:dashboard-advertiser";
    }

    public boolean isApproveAllNewCreatives() {
        return approveAllNewCreatives;
    }

    public void setApproveAllNewCreatives(boolean approveAllNewCreatives) {
        this.approveAllNewCreatives = approveAllNewCreatives;
    }

    public String getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(String selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public List<SelectItem> getStatus() {
        if (status == null) {
            status = new ArrayList<SelectItem>();
            status.add(new SelectItem("ACTIVE", "ACTIVE"));
            status.add(new SelectItem("PAUSED", "PAUSED"));
        }
        return status;
    }

    public void setStatus(List<SelectItem> status) {
        this.status = status;
    }

    private void updateStatus() {
        if (selectedStatus.equals("ACTIVE") && !campaignDto.getStatus().equals(Status.ACTIVE)) {
            service.changeCampaignStatus(Collections.singletonList(campaignDto.getId()), CampaignStatus.ACTIVE);
        } else if (selectedStatus.equals("PAUSED") && !campaignDto.getStatus().equals(Status.PAUSED)) {
            service.changeCampaignStatus(Collections.singletonList(campaignDto.getId()), CampaignStatus.PAUSED);
        }
    }

    public SegmentSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SegmentSafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel;
    }
}
