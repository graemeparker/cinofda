package com.adfonic.tools.beans.optimisation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.optimisation.OptimisationUserInterfaceLivePublicationDto;
import com.adfonic.dto.optimisation.OptimisationUserInterfaceRemovedPublicationDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.optimisation.service.AdvertiserOptimisationService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.tools.beans.data.NoMatchDataBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.export.LivePublicationReportDefinitionBuilder;
import com.adfonic.tools.export.RemovedPublicationReportDefinitionBuilder;
import com.adfonic.tools.util.AbstractLazyDataModelWrapper;
import com.ibm.icu.util.GregorianCalendar;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

@Component
@Scope("view")
@URLMapping(id = "optimisation-advertiser", pattern = "/optimisation/advertiser", viewId = "/WEB-INF/jsf/optimisation/optimisation.jsf")
public class OptimisationUserInterfaceMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OptimisationUserInterfaceMBean.class);
    private static final String PID_LINE_SEPARATOR = "\r\n";
    private static final String PUBLICATION_TYPE_MEDIUM_PRE = "page.optimisation.table.type.";
    private static final String CONTENT_TYPE_EXCEL_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private Date from;
    private Date to;
    private String previousSelection = null;

    @Autowired
    private CompanyService companyService;

    @Autowired
    @Qualifier("campaignService")
    private CampaignService cService;

    @Autowired
    @Qualifier("advertiserOptimisationService")
    private AdvertiserOptimisationService advertiserOptimisationService;

    @Autowired
    @Qualifier("publicationService")
    private PublicationService publicationService;

    /** Current client to produce report for. */
    private UserDTO user;

    /** Current advertiser */
    private AdvertiserDto advertiser;

    private CampaignTypeAheadDto campaign;

    private OptimisationReportCompanyPreferences companyPrefs;

    protected LazyDataModel<OptimisationUserInterfaceLivePublicationDto> livePublicationsLazyDataModel;
    protected LazyDataModel<OptimisationUserInterfaceRemovedPublicationDto> removedPublicationsLazyDataModel;

    protected OptimisationUserInterfaceLivePublicationDto[] livePublicationsSelectedRows;
    protected OptimisationUserInterfaceRemovedPublicationDto[] removedPublicationsSelectedRows;

    protected List<OptimisationUserInterfaceLivePublicationDto> filteredLivePublications;
    protected List<OptimisationUserInterfaceRemovedPublicationDto> filteredRemovedPublications;

    /** Break the report down by creative */
    private boolean breakdownByCreative = false;

    /** list of publications to remove **/
    private List<PublicationDto> pidsList = new ArrayList<PublicationDto>(0);
    private List<PublicationDto> pidsToRemoveList = new ArrayList<PublicationDto>(0);
    private String pidsAddList;
    private List<NoMatchDataBean> unrecognizedPublications = new ArrayList<NoMatchDataBean>(0);

    private TimeZone userTimezone;

    // -----------------------------------------------------------------------------------------------------------------
    // Initalization code
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void init() throws Exception {
        userTimezone = companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
    }

    @URLAction(mappingId = "optimisation-advertiser", onPostback = false)
    public void load() throws Exception {
        LOGGER.debug("load-->");

        // required for tab to be properly selected
        getNavigationSessionBean().navigate(Constants.OPTIMISATION);

        // check feature enabled and dsp lic role
        if (!getToolsApplicationBean().isAdvertiserOptimisationEnabled()) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/dashboard");
            return;
        }

        // AT-1020 breakdownByCreative=true by default
        breakdownByCreative = true;
        user = getUser();
        advertiser = user.getAdvertiserDto();
        setFrom(getToday());
        setTo(getToday());
        setPreviousSelection("1");
        companyPrefs = advertiserOptimisationService.getOptimisationReportCompanyPreferences(user.getId());
        LOGGER.debug("<--load");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Campaign type-ahead dropdown
    // -----------------------------------------------------------------------------------------------------------------

    public Collection<CampaignTypeAheadDto> completeCampaigns(String query) {
        CampaignSearchDto dto = new CampaignSearchDto();
        dto.setName(query);
        dto.setAdvertiser(advertiser);
        return cService.getOptimisableCampaigns(dto).getCampaigns();
    }

    public CampaignTypeAheadDto getCampaign() {
        return campaign;
    }

    public void setCampaign(CampaignTypeAheadDto campaign) {
        this.campaign = campaign;
    }

    public void campaignSelectedEvent(org.primefaces.event.SelectEvent event) {
        CampaignTypeAheadDto campaign = (CampaignTypeAheadDto) event.getObject();
        if (campaign != null) {
            this.campaign = campaign;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Breakdown by Creative checkbox
    // -----------------------------------------------------------------------------------------------------------------

    public boolean isBreakdownByCreative() {
        return breakdownByCreative;
    }

    public void setBreakdownByCreative(boolean breakdownByCreative) {
        this.breakdownByCreative = breakdownByCreative;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Date Picker
    // -----------------------------------------------------------------------------------------------------------------

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getPreviousSelection() {
        return previousSelection;
    }

    public void setPreviousSelection(String previousSelection) {
        this.previousSelection = previousSelection;
    }

    public void processDatePickerValueChange(AjaxBehaviorEvent event) {
        String newValue = previousSelection;
        Date[] dateRange = Utils.getDateRange(newValue);
        if (dateRange != null && dateRange.length == 2) {
            setFrom(dateRange[0]);
            setTo(dateRange[1]);
        }
    }

    public boolean isRenderLastMonth() {
        GregorianCalendar gc = new GregorianCalendar();
        return gc.get(Calendar.DAY_OF_MONTH) <= 7;
    }

    public Date getToday() {
        return returnNow();
    }

    public String getDateSelection() {
        if (previousSelection != null) {
            Map<String, String> map = getToolsApplicationBean().getOptimisationDatePickerPresetsMap();
            Iterator<String> bundleKeysIt = getToolsApplicationBean().getOptimisationDatePickerPresetsMap().keySet().iterator();
            while (bundleKeysIt.hasNext()) {
                String key = bundleKeysIt.next();
                if (map.get(key).equals(previousSelection)) {
                    return key;
                }
            }
        }
        return "";
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Tables building
    // -----------------------------------------------------------------------------------------------------------------

    public void doRun() {
        LOGGER.debug("doRun-->");
        LOGGER.debug("Date range: " + getPreviousSelection());
        LOGGER.debug("isBreakdownByCreative: " + isBreakdownByCreative());
        LOGGER.debug("Campaign: " + getCampaign().getId() + ":" + getCampaign().getName());

        this.livePublicationsLazyDataModel = this.createLivePublicationLazyDataModel();
        this.removedPublicationsLazyDataModel = this.createRemovedPublicationLazyDataModel();

        LOGGER.debug("<--doRun");
    }

    protected LazyDataModel<OptimisationUserInterfaceLivePublicationDto> createLivePublicationLazyDataModel() {
        return new AbstractLazyDataModelWrapper<OptimisationUserInterfaceLivePublicationDto>(
                this.advertiserOptimisationService.createLivePublicationLazyDataModel(this.getCampaign(), // NameIdBusinessDto
                                                                                                          // campaign,
                        this.advertiser, // AdvertiserDto advertiser,
                        getPreviousSelection(), // String dateRange,
                        isBreakdownByCreative()) // boolean
                                                 // breakdownByCreative);
        );

    }

    protected LazyDataModel<OptimisationUserInterfaceRemovedPublicationDto> createRemovedPublicationLazyDataModel() {
        return new AbstractLazyDataModelWrapper<OptimisationUserInterfaceRemovedPublicationDto>(
                this.advertiserOptimisationService.createRemovedPublicationLazyDataModel(this.getCampaign(), // NameIdBusinessDto
                                                                                                             // campaign,
                        this.advertiser, // AdvertiserDto advertiser,
                        getPreviousSelection(), // String dateRange,
                        isBreakdownByCreative()) // boolean
                                                 // breakdownByCreative);
        );

    }

    public boolean isFieldVisible(String fieldName) {
        boolean isVisible = false;
        if (companyPrefs!=null){
            isVisible = OptimisationFields.isFieldVisible(fieldName, companyPrefs.getReportFields());
        }
        return isVisible;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Publication removal/reenabling
    // -----------------------------------------------------------------------------------------------------------------

    protected Long getUserId() {
        UserDTO userDto = this.getUser();
        if (userDto == null) {
            return null;
        } else {
            return userDto.getId();
        }
    }

    protected Long getAdfonicUserId() {
        AdfonicUser adfonicUser = this.getAdfonicUser();
        if (adfonicUser == null) {
            return null;
        } else {
            return adfonicUser.getId();
        }
    }

    public void doNewRemoveChecked() {
        LOGGER.debug("doRemoveChecked() starts");
        if (livePublicationsSelectedRows == null || livePublicationsSelectedRows.length == 0) {
            return;
        }
        for (OptimisationUserInterfaceLivePublicationDto dto : livePublicationsSelectedRows) {
            RemovalInfo.RemovalType removalType = RemovalInfo.RemovalType.USER;
            Long creativeId = dto.getCreativeId();
            Long publicationId = dto.getPublicationId();
            try {
                advertiserOptimisationService.removePublicationFromCreative(this.campaign.getId(), creativeId, publicationId, removalType,
                        getUserId(), getAdfonicUserId());
                LOGGER.debug("Removed publication id: " + publicationId + " from creative id: " + creativeId + " with removalType "
                        + removalType.toString());
            } catch (Exception e) {
                LOGGER.debug("Failed to remove publication id: " + publicationId + " from creative id: " + creativeId, e);
            }
        }
        // Regenerate both datamodels, so the row counts are recalculated
        doRun();
        LOGGER.debug("doRemoveChecked() returns");
    }

    public StreamedContent exportLivePublicationsToExcel() throws IOException {
        // Getting all live publications
        AbstractLazyDataModelWrapper<OptimisationUserInterfaceLivePublicationDto> model = (AbstractLazyDataModelWrapper<OptimisationUserInterfaceLivePublicationDto>) this
                .createLivePublicationLazyDataModel();

        List<OptimisationUserInterfaceLivePublicationDto> allRows = null;
        if (model.getRowCount() > 0) {
            allRows = model.load(0, model.getRowCount(), null, null, null);
        }

        // Running report
        LivePublicationReportDefinitionBuilder<OptimisationUserInterfaceLivePublicationDto> builder = new LivePublicationReportDefinitionBuilder<OptimisationUserInterfaceLivePublicationDto>(
                userTimezone, companyPrefs.getReportFields());
        ReportDefinition<OptimisationUserInterfaceLivePublicationDto> reportDefinition = builder.build(allRows);
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), CONTENT_TYPE_EXCEL_XLSX, "Live publications "
                + this.campaign.getName() + ".xlsx");
    }

    public StreamedContent exportRemovedPublicationsToExcel() throws IOException {
        // Getting all live publications
        AbstractLazyDataModelWrapper<OptimisationUserInterfaceRemovedPublicationDto> model = (AbstractLazyDataModelWrapper<OptimisationUserInterfaceRemovedPublicationDto>) this
                .createRemovedPublicationLazyDataModel();
        List<OptimisationUserInterfaceRemovedPublicationDto> allRows = null;
        if (model.getRowCount() > 0) {
            allRows = model.load(0, model.getRowCount(), null, null, null);
        }

        // Running report
        RemovedPublicationReportDefinitionBuilder<OptimisationUserInterfaceRemovedPublicationDto> builder = new RemovedPublicationReportDefinitionBuilder<OptimisationUserInterfaceRemovedPublicationDto>(
                userTimezone, companyPrefs.getReportFields());
        ReportDefinition<OptimisationUserInterfaceRemovedPublicationDto> reportDefinition = builder.build(allRows);
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), CONTENT_TYPE_EXCEL_XLSX,
                "Removed publications " + this.campaign.getName() + ".xlsx");
    }

    public void doNewReEnableChecked() {
        LOGGER.debug("doReEnableChecked() starts");
        if (removedPublicationsSelectedRows == null || removedPublicationsSelectedRows.length == 0) {
            return;
        }
        for (OptimisationUserInterfaceRemovedPublicationDto dto : removedPublicationsSelectedRows) {
            Long creativeId = dto.getCreativeId();
            Long publicationId = dto.getPublicationId();
            try {
                advertiserOptimisationService.unremovePublicationFromCreative(this.campaign.getId(), creativeId, publicationId,
                        getUserId(), getAdfonicUserId());
                LOGGER.debug("Re-enabled publication id: " + publicationId + " for creative id: " + creativeId);
            } catch (Exception e) {
                LOGGER.debug("Failed to re-enable publication id: " + publicationId + " for creative id: " + creativeId, e);
            }
        }
        // Regenerate both datamodels, so the row counts are recalculated
        doRun();
        LOGGER.debug("doReEnableChecked() returns");
    }

    public OptimisationUserInterfaceLivePublicationDto[] getLivePublicationsSelectedRows() {
        return livePublicationsSelectedRows;
    }

    public void setLivePublicationsSelectedRows(OptimisationUserInterfaceLivePublicationDto[] livePublicationsSelectedRows) {
        this.livePublicationsSelectedRows = livePublicationsSelectedRows;
    }

    public OptimisationUserInterfaceRemovedPublicationDto[] getRemovedPublicationsSelectedRows() {
        return removedPublicationsSelectedRows;
    }

    public void setRemovedPublicationsSelectedRows(OptimisationUserInterfaceRemovedPublicationDto[] removedPublicationsSelectedRows) {
        this.removedPublicationsSelectedRows = removedPublicationsSelectedRows;
    }

    public List<OptimisationUserInterfaceLivePublicationDto> getFilteredLivePublications() {
        return filteredLivePublications;
    }

    public void setFilteredLivePublications(List<OptimisationUserInterfaceLivePublicationDto> filteredLivePublications) {
        this.filteredLivePublications = filteredLivePublications;
    }

    public List<OptimisationUserInterfaceRemovedPublicationDto> getFilteredRemovedPublications() {
        return filteredRemovedPublications;
    }

    public void setFilteredRemovedPublications(List<OptimisationUserInterfaceRemovedPublicationDto> filteredRemovedPublications) {
        this.filteredRemovedPublications = filteredRemovedPublications;
    }

    public LazyDataModel<OptimisationUserInterfaceLivePublicationDto> getLivePublicationsLazyDataModel() {
        return livePublicationsLazyDataModel;
    }

    public LazyDataModel<OptimisationUserInterfaceRemovedPublicationDto> getRemovedPublicationsLazyDataModel() {
        return removedPublicationsLazyDataModel;
    }

    /*
     * "publication type" in the tables is MEDIUM
     */
    public static String getPublicationTypeLabel(String medium) {
        if (!StringUtils.isBlank(medium) || !StringUtils.isBlank(medium)) {
            try {
                return FacesUtils.getBundleMessage(PUBLICATION_TYPE_MEDIUM_PRE + medium);
            } catch (java.util.MissingResourceException missing) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("No properties resource for Medium: " + PUBLICATION_TYPE_MEDIUM_PRE + medium);
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Add pid list
    // -----------------------------------------------------------------------------------------------------------------

    public String getPidsAddList() {
        StringBuilder pidsAddList = new StringBuilder();
        for (PublicationDto publication : pidsList) {
            pidsAddList.append(publication.getName()).append(PID_LINE_SEPARATOR);
        }
        return pidsAddList.toString();
    }

    public void setPidsAddList(String pidsAddList) {
        this.pidsAddList = pidsAddList;
    }

    public List<NoMatchDataBean> getUnrecognizedPublications() {
        return unrecognizedPublications;
    }

    public void setUnrecognizedPublications(List<NoMatchDataBean> unrecognizedPublications) {
        this.unrecognizedPublications = unrecognizedPublications;
    }

    public void addPids(ActionEvent event) {
        LOGGER.debug("addPids-->");
        String[] ret = pidsAddList.split("\r\n");
        for (int j = 0; j < ret.length; j++) {
            if (!StringUtils.isEmpty(ret[j])) {
                PublicationDto publication = publicationService.getPublicationByExternalId(ret[j]);
                if (publication != null && publication.getId() != null) {
                    if (!pidsToRemoveList.contains(publication)) {
                        LOGGER.debug(publication.getExternalID() + " found for removal");
                        pidsToRemoveList.add(publication);
                    }
                } else if (!StringUtils.isEmpty(ret[j])) {
                    LOGGER.debug(ret[j] + " not recognized");
                    NoMatchDataBean data = new NoMatchDataBean();
                    data.setName(ret[j]);
                    unrecognizedPublications.add(data);
                }
            }
        }
        LOGGER.debug("addPids<--");
    }

    public void cancelList(ActionEvent event) {
        pidsToRemoveList.clear();
        unrecognizedPublications.clear();
    }

    public void removePublicationsList() {
        LOGGER.debug("-->removePublicationsList");
        unrecognizedPublications.clear();
        advertiserOptimisationService.removePublicationsFromCreatives(this.campaign.getId(), pidsToRemoveList,
                RemovalInfo.RemovalType.USER, getUserId(), getAdfonicUserId());
        pidsToRemoveList.clear();
        // Refresh tables
        doRun();
        LOGGER.debug("removePublicationsList<--");
    }

    public String getMatchingMessage() {
        return FacesUtils.getBundleMessage("page.optimisation.addlist.itemsfound",
                Integer.toString(pidsToRemoveList.size() + unrecognizedPublications.size()), Integer.toString(pidsToRemoveList.size()));
    }

    public String getNotMatchingMessage() {
        return FacesUtils.getBundleMessage("page.optimisation.addlist.itemsnotfound", Integer.toString(unrecognizedPublications.size()));
    }

    public Boolean isCampaignCompleted() {
        Boolean isCompleted = false;
        if (getCampaign().getStatus() == CampaignStatus.COMPLETED.getStatus()) {
            isCompleted = true;
        }
        return isCompleted;
    }
}
