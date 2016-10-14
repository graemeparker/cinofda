package com.adfonic.tools.beans.campaign.inventory;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UISelectBoolean;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.publicationlist.PublicationForListDto;
import com.adfonic.dto.campaign.publicationlist.PublicationInfoSearchForListDto;
import com.adfonic.dto.campaign.publicationlist.PublicationListDto;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.campaign.publicationlist.PublicationSearchForListDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.publication.PublicationInfoDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.optimisation.service.AdvertiserOptimisationService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.presentation.publicationlist.service.PublicationListService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.export.SelectedPublicationReportDefinitionBuilder;

@Component
@Scope("view")
public class CampaignPublicationListMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignPublicationListMBean.class);

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private PublicationListService publicationListService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private CompanyService companyService;
    
    @Autowired
    @Qualifier("advertiserOptimisationService")
    private AdvertiserOptimisationService advertiserOptimisationService;
    
    // Black White list view fields
    private boolean isWhiteList;
    private List<PublicationListInfoDto> publicationsList;
    private PublicationListInfoDto selectedList = null;
    private LazyDataModel<PublicationInfoDto> selectedListLazyModel;

    // Edit List Dialog fields
    private String listName;
    private String searchName;
    private String searchType;
    private LazyDataModel<PublicationForListDto> searchedPublicationsLazyModel;
    private PublicationListInfoDto loadedList = new PublicationListDto();
    private Map<Long, PublicationInfoDto> loadedListPublications = new HashMap<Long, PublicationInfoDto>();
    private List<PublicationInfoDto> sortedLoadedListPublications;
    private List<PublicationForListDto> selectedSearchedPublications;
    private List<PublicationInfoDto> selectedPublications;
    private String idsList = "";
    
    private TimeZone userTimezone;
    private OptimisationReportCompanyPreferences companyPrefs;
    
    @Override
    @PostConstruct
    protected void init() {
    	this.userTimezone = companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
    	companyPrefs = advertiserOptimisationService.getOptimisationReportCompanyPreferences(getUser().getId());
    }
    
    public StreamedContent exportSelectedPublicationsToExcel() throws IOException {
        // Running report
    	SelectedPublicationReportDefinitionBuilder<PublicationInfoDto> builder = new SelectedPublicationReportDefinitionBuilder<PublicationInfoDto>(
                userTimezone, companyPrefs.getReportFields(), isAdminUserLoggedIn());
        ReportDefinition<PublicationInfoDto> reportDefinition = builder.build(getLoadedListPublications());
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), com.adfonic.presentation.util.Constants.CONTENT_TYPE_EXCEL_XLSX,
                "Selected publications.xlsx");
    }
    
    public LazyDataModel<PublicationInfoDto> getSelectedListLazyModel() {
        if (selectedListLazyModel == null) {
            selectedListLazyModel = new SelectedListPublicationsLazyModel();
        }
        return selectedListLazyModel;
    }

    public void setSelectedListLazyModel(LazyDataModel<PublicationInfoDto> selectedListLazyModel) {
        this.selectedListLazyModel = selectedListLazyModel;
    }

    public LazyDataModel<PublicationForListDto> getSearchedPublicationsLazyModel() {
        if (searchedPublicationsLazyModel == null) {
            searchedPublicationsLazyModel = new LazyDataModel<PublicationForListDto>() {
                private static final long serialVersionUID = 1L;

                @Override
                public List<PublicationForListDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                        Map<String, String> filters) {
                    if (searchName != null && searchName.length() > 2) {
                        try {
                            PublicationSearchForListDto publicationSearch = new PublicationSearchForListDto();
                            publicationSearch.setSearchName(searchName);
                            publicationSearch.setSearchType(searchType);
                            publicationSearch.setStart(Long.valueOf((first / pageSize) + 1));
                            publicationSearch.setNumberOfRecords(Long.valueOf(pageSize));
                            publicationSearch.setExcludedPublications(getPubIds());

                            PublicationSearchForListDto searchDto = publicationListService.search(publicationSearch);
                            List<PublicationForListDto> result = searchDto.getPublications();
                            this.setRowCount(searchDto.getNumTotalRecords().intValue());

                            return result;
                        } catch (Exception e) {
                            LOGGER.error("Error retrieving search result", e);
                            return new ArrayList<PublicationForListDto>();
                        }

                    } else {
                        return new ArrayList<PublicationForListDto>();
                    }
                }

                @Override
                public void setRowIndex(int rowIndex) {
                    /*
                     * The following is in ancestor (LazyDataModel):
                     * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex %
                     * pageSize);
                     */
                    if (rowIndex == -1 || getPageSize() == 0) {
                        super.setRowIndex(-1);
                    } else {
                        super.setRowIndex(rowIndex % getPageSize());
                    }
                }
            };
        }

        return searchedPublicationsLazyModel;
    }

    public void newList(ActionEvent event) {
        this.loadedList = new PublicationListDto();
        this.listName = "";
    }

    public void selectedAllSelected(AjaxBehaviorEvent event) {
        boolean selected = (boolean) ((UISelectBoolean) event.getSource()).getValue();
        for (PublicationInfoDto p : this.loadedListPublications.values()) {
            p.setSelected(selected);
        }
    }

    public void addPublications(ActionEvent event) {
        if (CollectionUtils.isNotEmpty(this.selectedSearchedPublications)) {
            for (PublicationForListDto pfl : this.selectedSearchedPublications) {
                PublicationInfoDto publicationInfoDto = new PublicationInfoDto();
                publicationInfoDto.setId(pfl.getPublicationId());
                publicationInfoDto.setDisplayName(pfl.getDisplayName());
                if (!containsPublication(publicationInfoDto)) {
                    this.loadedListPublications.put(publicationInfoDto.getId(), publicationInfoDto);
                }
            }
            // Sorting loaded publications list
            sortLoadedListPublications();
        }

        this.selectedSearchedPublications = null;
        this.selectedPublications = null;
    }

    public void removePublications(ActionEvent event) {
        if (CollectionUtils.isNotEmpty(this.selectedPublications)) {
            for (PublicationInfoDto publicationInfoDto : this.selectedPublications) {
                if (containsPublication(publicationInfoDto)) {
                    this.loadedListPublications.remove(publicationInfoDto.getId());
                    this.sortedLoadedListPublications.remove(publicationInfoDto);
                }
            }
        }

        this.selectedSearchedPublications = null;
        this.selectedPublications = null;
    }

    public PublicationListInfoDto prepareDto(CampaignDto campaignDto) {
        LOGGER.debug("prepareDto-->");
        LOGGER.debug("prepareDto<--");
        return this.selectedList;
    }

    public PublicationListInfoDto loadSelectedPublicationList(CampaignDto campaignDto) {

        LOGGER.debug("loadCampaign-->");
        try {
            this.selectedList = campaignService.loadPublicationList(campaignDto.getId());
            this.loadedList = this.selectedList;
            this.listName = ((this.selectedList != null) ? this.selectedList.getName() : "");
        } catch (Exception e) {
            LOGGER.error("Error retrieving the publication list linked to the campaign " + campaignDto.getExternalID(), e);
            this.selectedList = null;
            this.loadedList = null;
            this.listName = "";
        }
        LOGGER.debug("loadCampaign<--");
        return this.selectedList;
    }

    public void checkUpdate(ActionEvent event) {
        // If the name changed a new list will be created
        if (loadedList != null && !listName.equals(loadedList.getName())) {
            loadedList.setId(null);
        }
        if (loadedList != null && loadedList.getId() != null) {
            List<CampaignTypeAheadDto> campaigns = publicationListService.getCampaigsUsingPublicationList(loadedList);
            if (!CollectionUtils.isEmpty(publicationListService.getCampaigsUsingPublicationList(loadedList))) {
                if (campaigns.size() == 1 && campaigns.get(0).getId().equals(getCampaignInventoryTargetingBean().getCampaignDto().getId())) {
                    saveList(event);
                    return;
                } else {
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.execute("confirmationUpdateDialog.show();");
                    return;
                }
            }
        }
        saveList(event);
    }

    public void saveList(ActionEvent event) {
        if (this.loadedListPublications == null || this.loadedListPublications.size() == 0) {
            addMessage("page.campaign.inventory.emptylist.errormessage", "listName");
            return;
        } else if (nameDuplicated(listName, loadedList.getId())) {
            addMessage("page.campaign.inventory.duplicatename.errormessage", "listName");
            return;
        } else if (StringUtils.isEmpty(listName)) {
            addMessage("page.campaign.inventory.list.save.error.emptyname", "listName");
            return;
        }

        if (selectedList == null) {
            selectedList = new PublicationListDto();
        }
        loadedList.setName(listName);
        loadedList.setWhiteList(this.isWhiteList);
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        selectedList = publicationListService.save(loadedList, new ArrayList<PublicationInfoDto>(this.loadedListPublications.values()),
                userDto.getAdvertiserDto().getId(), userDto.getCompany().getId());
        if (!publicationsList.contains(selectedList)) {
            publicationsList.add(selectedList);
        }
        cleanSearchDialog(event);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("editList.hide();");
    }

    public void cleanSearchDialog(ActionEvent event) {
        this.searchName = "";
        this.loadedList = null;
        this.loadedListPublications = new HashMap<Long, PublicationInfoDto>();
        this.sortedLoadedListPublications = null;
        this.selectedSearchedPublications = null;
        this.selectedPublications = null;
        this.listName = null;
    }

    public void editList(ActionEvent event) {
        this.searchName = "";
        this.loadedList = new PublicationListDto();
        this.loadedList.setName(selectedList.getName());
        this.loadedListPublications.clear();
        for (PublicationInfoDto publicationInfo : getAllPublicationsFromSelectedList()) {
            this.loadedListPublications.put(publicationInfo.getId(), publicationInfo);
        }
        // Sorting loaded publications list
        sortLoadedListPublications();
        this.loadedList.setId(selectedList.getId());
        this.listName = selectedList.getName();
    }

    public void checkDelete(ActionEvent event) {
        if (selectedList != null && selectedList.getId() != null) {
            List<CampaignTypeAheadDto> campaigns = publicationListService.getCampaigsUsingPublicationList(selectedList);
            if (!CollectionUtils.isEmpty(campaigns)) {
                // If the deleted list is only used by this campaign the
                // inventory targeting is set to default on white list
                if (campaigns.size() == 1 && campaigns.get(0).getId().equals(getCampaignInventoryTargetingBean().getCampaignDto().getId())) {
                    deleteList(event);
                    if (this.isWhiteList) {
                        getCampaignInventoryTargetingBean().setToDefault();
                    }
                    return;
                }
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("confirmationDeleteDialog.show();");
                return;
            }
        }
        deleteList(event);
    }

    public void deleteList(ActionEvent event) {
        if (validList()) {
            boolean deleteLoaded = false;
            if (loadedList != null && selectedList != null && selectedList.getId() != null
                    && selectedList.getId().equals(loadedList.getId())) {
                deleteLoaded = true;
            }
            if (deleteLoaded) {
                loadedList = new PublicationListDto();
            }
            publicationListService.deletePublicationList(selectedList);
            selectedList = null;
        }
    }

    public void addIds(ActionEvent event) {
        LOGGER.debug("addIds-->");
        String[] ret = idsList.split("\r\n");
        Map<Long, PublicationInfoDto> publicationsInfo = publicationService.getActivePublicationInfoByExternalIds(Arrays.asList(ret));
        if (MapUtils.isNotEmpty(publicationsInfo)) {
            this.loadedListPublications.putAll(publicationsInfo);
            // Sorting loaded publications list
            sortLoadedListPublications();
        }
        idsList = "";
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("addListPublications.hide();");
        LOGGER.debug("addIds<--");
    }

    public String getConfirmationDeleteMessage() {
        String campaigns = getPublicationListCampaigns();
        if (!StringUtils.isEmpty(campaigns)) {
            if (isWhiteList) {
                return FacesUtils.getBundleMessage("page.campaign.inventory.delete.whitelist.confirmation.message", campaigns);
            } else {
                return FacesUtils.getBundleMessage("page.campaign.inventory.delete.blacklist.confirmation.message", campaigns);
            }
        }

        return "";
    }

    public String getConfirmationUpdateMessage() {
        String campaigns = getPublicationListCampaigns();
        if (!StringUtils.isEmpty(campaigns)) {
            return FacesUtils.getBundleMessage("page.campaign.inventory.update.confirmation.message", campaigns);
        }

        return "";
    }

    public void cancelAdd(ActionEvent event) {
        idsList = "";
    }

    public boolean validList() {
        if (selectedList != null && selectedList.getId() != null) {
            return true;
        } else {
            LOGGER.debug("Not publications list selected");
            addMessage("page.campaign.inventory.list.save.error.emptylist", "select-list");
            return false;
        }
    }

    private void addMessage(String message, String componentId) {
        String component = "";
        if (isWhiteList) {
            component = "whiteList:" + componentId;
        } else {
            component = "networkBlacklist:" + componentId;
        }
        addFacesMessage(FacesMessage.SEVERITY_ERROR, component, null, message);
    }

    public void cleanBean() {
        this.selectedList = null;
        this.searchName = "";
        this.loadedList = new PublicationListDto();
        this.listName = "";
    }

    public String getPublicationsSummary(boolean spaces) {
        String summary = null;
        if (this.selectedList != null) {
            summary = selectedList.getName();
        } else {
            summary = notSet();
        }
        return summary;
    }

    public boolean isListEditable() {
        return selectedList != null && selectedList.getId() != null;
    }

    public boolean isListSelected() {
        return selectedList != null && selectedList.getId() != null;
    }

    public List<PublicationListInfoDto> getPublicationsList() {
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        publicationsList = publicationListService.getSavedListsInfo(userDto.getCompany().getId(), userDto.getAdvertiserDto().getId(),
                isWhiteList);
        return publicationsList;
    }

    public PublicationListInfoDto getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(PublicationListInfoDto selectedList) {
        this.selectedList = selectedList;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public PublicationListInfoDto getLoadedList() {
        return loadedList;
    }

    public void setLoadedList(PublicationListDto loadedList) {
        this.loadedList = loadedList;
    }

    public boolean isWhiteList() {
        return isWhiteList;
    }

    public void setWhiteList(boolean isWhiteList) {
        this.isWhiteList = isWhiteList;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getIdsList() {
        return idsList;
    }

    public void setIdsList(String idsList) {
        this.idsList = idsList;
    }

    public List<PublicationForListDto> getSelectedSearchedPublications() {
        return selectedSearchedPublications;
    }

    public void setSelectedSearchedPublications(List<PublicationForListDto> selectedSearchedPublications) {
        this.selectedSearchedPublications = selectedSearchedPublications;
    }

    public List<PublicationInfoDto> getSelectedPublications() {
        return selectedPublications;
    }

    public void setSelectedPublications(List<PublicationInfoDto> selectedPublications) {
        this.selectedPublications = selectedPublications;
    }

    public List<PublicationInfoDto> getLoadedListPublications() {
        return sortedLoadedListPublications;
    }

    /** PRIVATE METHODS **/
    private List<PublicationInfoDto> getAllPublicationsFromSelectedList() {
        LazyDataModel<PublicationInfoDto> selectedListLazyModel = new SelectedListPublicationsLazyModel();
        List<PublicationInfoDto> publicationsInfo = selectedListLazyModel.load(0, 0, "name", SortOrder.ASCENDING, null);
        return publicationsInfo;
    }

    private List<Long> getPubIds() {
        List<Long> pubs = new ArrayList<Long>();
        if (loadedList != null && !MapUtils.isEmpty(this.loadedListPublications)) {
            pubs = new ArrayList<Long>(this.loadedListPublications.keySet());
        }
        return pubs;
    }

    private String getCampaignNames(List<CampaignTypeAheadDto> campaigns) {
        if (CollectionUtils.isEmpty(campaigns)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (CampaignTypeAheadDto c : campaigns) {
            sb.append(c.getName());
            sb.append(", ");
        }
        String cs = sb.toString();
        return cs.substring(0, cs.length() - 2);
    }

    private boolean nameDuplicated(String name, Long id) {
        boolean newList = false;
        if (id == null) {
            newList = true;
        }
        for (PublicationListInfoDto p : publicationsList) {
            if (p.getName().equals(name)) {
                if (newList || !id.equals(p.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsPublication(PublicationInfoDto pub) {
        return this.loadedListPublications.containsKey(pub.getId());
    }

    private void sortLoadedListPublications() {
        this.sortedLoadedListPublications = new ArrayList<PublicationInfoDto>(this.loadedListPublications.values());
        Collections.sort(this.sortedLoadedListPublications, new Comparator<PublicationInfoDto>() {
            @Override
            public int compare(PublicationInfoDto o1, PublicationInfoDto o2) {
                if (o2 == null || o2.getFriendlyName() == null) {
                    return 1;
                } else if (o1.getFriendlyName() == null) {
                    return -1;
                } else {
                    return o1.getFriendlyName().toLowerCase().compareTo(o2.getFriendlyName().toLowerCase());
                }
            }
        });
    }

    private String getPublicationListCampaigns() {
        String campaigns = "";
        if (selectedList != null && selectedList.getId() != null) {
            campaigns = getCampaignNames(publicationListService.getCampaigsUsingPublicationList(selectedList));
        }
        return campaigns;
    }

    public class SelectedListPublicationsLazyModel extends LazyDataModel<PublicationInfoDto> {
        private static final long serialVersionUID = 1L;

        @Override
        public List<PublicationInfoDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
            List<PublicationInfoDto> publications = null;
            if (selectedList != null) {
                try {
                    PublicationInfoSearchForListDto searchDto = new PublicationInfoSearchForListDto(selectedList, first, pageSize,
                            sortField, isAscending(sortOrder));
                    searchDto.setPublicationListInfoDto(selectedList);
                    searchDto.setFirst(first);
                    searchDto.setPageSize(pageSize);
                    searchDto.setSortField(sortField);

                    publications = publicationListService.getPublicationsInfo(searchDto);
                    setRowCount(publicationListService.countAllPublicationsInfo(searchDto));
                } catch (Exception e) {
                    LOGGER.error("Having problems to retrieve the publication list for the publication list " + selectedList.getName(), e);
                    publications = new ArrayList<PublicationInfoDto>();
                }
            } else {
                publications = new ArrayList<PublicationInfoDto>();
            }
            return publications;
        }

        @Override
        public void setRowIndex(int rowIndex) {
            /*
             * The following is in ancestor (LazyDataModel): this.rowIndex =
             * rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
             */
            if (rowIndex == -1 || getPageSize() == 0) {
                super.setRowIndex(-1);
            } else {
                super.setRowIndex(rowIndex % getPageSize());
            }
        }

        private Boolean isAscending(SortOrder sortOrder) {
            boolean ascending = true;
            if (sortOrder != null) {
                ascending = (sortOrder.equals(SortOrder.ASCENDING) ? true : false);
            }
            return ascending;
        }
    }
}
