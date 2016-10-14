package com.adfonic.beans.approval.publication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.beans.AdminGeneralException;
import com.adfonic.beans.approval.AbstractApprovalDashboardMBean;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.export.PublicationApprovalReportDefinitionBuilder;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.model.PublicationApprovalModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchResultModel;
import com.adfonic.presentation.publication.model.PublicationAssignedToUserModel;
import com.adfonic.presentation.publication.service.PublicationApprovalService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.presentation.util.Constants;
import com.adfonic.util.LogUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
    @URLMapping(id = "approval-publications",
                pattern = "/admin/approval/publications",
                viewId = "/admin/approval/publications.xhtml") })
public class PublicationApprovalDashboardMBean extends AbstractApprovalDashboardMBean<PublicationApprovalModel> {

    private static final transient Logger LOG = Logger.getLogger(PublicationApprovalDashboardMBean.class.getName());
    private static List<NameIdModel> searchFieldNameIndexList = populateSortFieldNameIndexList(PublicationApprovalModel.class);

    // Search constants
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final String DB_INPUT_PARAM_SEP = "~";
    private static final Integer DEFAULT_SORT_FIELD_INDEX = 1;

    private PublicationApprovalSearchResultModel searchResult;

    private PublicationApprovalSearchModel search;

    // Lists for holding elements of filter drop downs
    private List<NameIdModel> publicationTypes;
    private List<NameIdModel> publicationAssignedToUsers;
    private List<NameIdModel> publicationAccountTypes;
    private List<NameIdModel> publicationAlgorithmStatuses;
    private List<NameIdModel> publicationDeadZoneStatuses;

    // Store the selected multiple filter values
    private String[] selectedTypeIds;
    private String[] selectedStatusNames;
    private String[] selectedAssignedToUserIds;
    private String[] selectedAccountTypeIds;
    private String[] selectedAlgorithmStatusIds;
    private String[] selectedDeadZoneStatusIds;

    /** Bulk Status Change */
    private String bulkStatusTo;

    @Value("${approval.publication.dashboard.exportRecordSize:1000}")
    private int exportRecordSize;
    
    // For continuous export
    private int exportPageNumber;
    private int exportRemaining;
    private int exportRecordFrom;
    private int exportRecordTo;
    
    @Value("${approval.publication.dashboard.pollInterval:5}")
    private int pollInterval;
    
    /** Store the running query to display disabled button state */
    private boolean isQueryRunning;
    
    /** Store the running export to display disabled button state */
    private boolean isExportRunning;
    
    // Service dependencies

    @Autowired
    private PublicationApprovalService publicationApprovalService;
    @Autowired
    private PublicationService publicationService;

    @PostConstruct
    public void init() {
        if (isRestrictedUser()) {
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex) {
                throw new AdminGeneralException("Internal error");
            }
        }

        // Define search filters
        initFilters();

        // Populate filter option drop downs
        publicationTypes = publicationApprovalService.searchForPublicationTypes();
        publicationAssignedToUsers = narrowDownAssignedToUsers(getAssignedToUsers());
        publicationAccountTypes = publicationApprovalService.searchForPublicationAccountTypes();
        publicationAlgorithmStatuses = publicationApprovalService.searchForPublicationAlgorithmStatuses();
        publicationDeadZoneStatuses = publicationApprovalService.searchForPublicationDeadzoneStatuses();
    }

    // Methods for the view

    /** Search for publications */
    public void runQuery() {
        isQueryRunning = true;
        
        setupMultiFilters(search);
        setupTextFilters(search);

        LogUtils.logWithTitle(LOG, Level.FINE, "Publication Approval Search", search);
        searchResult = publicationApprovalService.searchForPublicationApprovals(search);
        
        // Initialize export variables 
        exportPageNumber = 1;
        exportRemaining = getResultCount().intValue();
        exportRecordFrom = 1;
        exportRecordTo = getNextExportedRecordCount(); 
        
        isQueryRunning = false;
    }
    
    /** Retrieve next planned exported record count */
    private int getNextExportedRecordCount() {
        return (exportRemaining >= exportRecordSize) ? exportRecordSize : exportRemaining;
    }

    @Override
    protected void assignToUser(PublicationApprovalModel dto, Long assignToUserId) {
        publicationService.assignUserToPublication(dto, adfonicUser(), getBulkAssignedToUserId());
    }
    
    public StreamedContent exportPublications() throws IOException {
        isExportRunning = true;
        
        // Prepare the same search with the export record size
        PublicationApprovalSearchModel exportedResult = SerializationUtils.clone(search);
        exportedResult.setPageSize(exportRecordSize);
        exportedResult.setFirst(exportPageNumber);
        exportedResult.setPageSize(exportRecordSize);
        setupTextFilters(exportedResult);

        // Prepare current export filename
        String exportFileName = FacesUtils.getBundleMessage("page.approval.publication.export.excel.name",
                String.valueOf(exportRecordFrom), String.valueOf(exportRecordTo));
        
        // Prepare next export page
        exportRemaining -= exportRecordSize;
        if (isExportRemaining()) {
            exportRecordFrom = exportRecordSize * exportPageNumber + 1;
            exportRecordTo = exportRecordSize * exportPageNumber + getNextExportedRecordCount();
            exportPageNumber++;
        }
        
        // Getting publication approvals
        PublicationApprovalSearchResultModel results = publicationApprovalService.searchForPublicationApprovals(exportedResult);
        List<PublicationApprovalModel> allRows = results.getResultList();

        // Running report
        PublicationApprovalReportDefinitionBuilder<PublicationApprovalModel> builder
            = new PublicationApprovalReportDefinitionBuilder<PublicationApprovalModel>(getDefaultTimeZone());
        ReportDefinition<PublicationApprovalModel> reportDefinition = builder.build(allRows);
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        isExportRunning = false;
        return new DefaultStreamedContent(
                new ByteArrayInputStream(osReport.toByteArray()), Constants.CONTENT_TYPE_EXCEL_XLSX, exportFileName);
    }

    /** Bulk status change */
    public void bulkStatusTo() {
        String logTitle = "Publication Bulk Status Assign";

        if (CollectionUtils.isEmpty(getFilteredRows())) {
            LogUtils.logWithTitle(LOG, Level.FINE, logTitle, "No rows selected, not changing any bulk assignments");
            return;
        }

        LogUtils.logWithTitle(LOG, Level.INFO, logTitle, "Bulk status change to: " + bulkStatusTo);

        for (PublicationApprovalModel dto : getFilteredRows()) {
            statusTo(Long.valueOf(dto.getInternalId()), bulkStatusTo);
        }
        
        refreshView();
    }
    
    /** Refresh some view part after bulk updates on several publications */
    @Override
    protected void refresh() {
        
        // Refresh with same pagination, limit and ordering, but only based on internal id's
        PublicationApprovalSearchModel refreshSearchWithIds = new PublicationApprovalSearchModel();
        
        // Use the same pagination / ordering
        refreshSearchWithIds.setFirst(search.getFirst());
        refreshSearchWithIds.setPageSize(search.getPageSize());
        refreshSearchWithIds.setSortFieldIndex(search.getSortFieldIndex());
        refreshSearchWithIds.setAscending(search.getAscending());
        
        // Produce tilde separated internal id's
        StringBuilder allInternalIds = new StringBuilder();
        String sep = StringUtils.EMPTY;
        for (PublicationApprovalModel publication : searchResult.getResultList()) {
            allInternalIds.append(sep).append(publication.getInternalId());
            sep = TILDE;
        }
        refreshSearchWithIds.setInternalId(allInternalIds.toString());

        LogUtils.logWithTitle(LOG, Level.INFO, "Publication Approval Refresh Search", refreshSearchWithIds);
        searchResult = publicationApprovalService.searchForPublicationApprovals(refreshSearchWithIds);
    }

    /** Change publication status */
    private void statusTo(Long publicationId, String bulkStatusTo) {
        publicationService.changePublicationStatusWithHistory(publicationId, PublicationStatus.valueOf(bulkStatusTo), adfonicUser());
    }

    /** Prepare search parameters for proc with multiple values */
    private void setupMultiFilters(PublicationApprovalSearchModel search) {
        search.setType(prepareSelectedValuesForDb(selectedTypeIds, publicationTypes.size()));
        search.setStatus(prepareSelectedValuesForDb(selectedStatusNames, PublicationStatus.values().length - 1));
        search.setAssignedTo(prepareSelectedValuesForDb(selectedAssignedToUserIds, publicationAssignedToUsers.size()));
        search.setAccountType(prepareSelectedValuesForDb(selectedAccountTypeIds, publicationAccountTypes.size()));
        search.setAlgorithmStatus(prepareSelectedValuesForDb(selectedAlgorithmStatusIds, publicationAlgorithmStatuses.size()));
        search.setDeadZoneStatus(prepareSelectedValuesForDb(selectedDeadZoneStatusIds, publicationDeadZoneStatuses.size()));
    }

    /** Prepare free text search parameters for proc */
    private void setupTextFilters(PublicationApprovalSearchModel search) {
        search.setInternalId(normalizeField(search.getInternalId()));
        search.setName(normalizeField(search.getName()));
        search.setFriendlyName(normalizeField(search.getFriendlyName()));
        search.setSupplierName(normalizeField(search.getSupplierName()));
        search.setSupplierUserName(normalizeField(search.getSupplierUserName()));
        search.setExternalId(normalizeField(search.getExternalId()));
        search.setBundle(normalizeField(search.getBundle()));
        search.setRtbId(normalizeField(search.getRtbId()));
        search.setSellerNetworkId(normalizeField(search.getSellerNetworkId()));
    }
    
    /** Further cleanup before push down input parameter to proc */
    private String normalizeField(String input) {
        
        // Convert empty fields to null parameters
        String normalizedInput = emptyToNull(input);
        if (normalizedInput != null) {
            
            // Trim apostrophe character
            normalizedInput = normalizedInput.replaceAll("^'+", "").replaceAll("'+$", "");
            
            // Escape the rest apostrophe chars if not escaped
            if(!normalizedInput.contains("\\")) {
                normalizedInput = normalizedInput.replaceAll("'", "\\\\\\'");
            }
        }
        return normalizedInput;
    }
        
    /** Set default filters */
    public void initFilters() {
        search = new PublicationApprovalSearchModel();
        searchResult = null;
        
        // Filters
        search.setInternalId(null);
        search.setName(null);
        search.setFriendlyName(null);
        search.setSupplierName(null);
        search.setSupplierUserName(null);
        search.setExternalId(null);
        search.setBundle(null);
        search.setRtbId(null);
        search.setSellerNetworkId(null);
        
        // Pagination / Ordering
        search.setFirst(1);
        search.setPageSize(DEFAULT_PAGE_SIZE);
        search.setSortFieldIndex(DEFAULT_SORT_FIELD_INDEX);
        search.setAscending(false);
        
        // Multi filters
        selectedTypeIds = new String[] {};
        selectedStatusNames = new String[] {};
        selectedAssignedToUserIds = new String[] {};
        selectedAccountTypeIds = new String[] {};
        selectedAlgorithmStatusIds = new String[] {};
        selectedDeadZoneStatusIds = new String[] {};
    }

    // Private methods

    /** Populate drop down for column sorting */
    private static List<NameIdModel> populateSortFieldNameIndexList(Class<PublicationApprovalModel> clazz) {
        List<NameIdModel> propertyIndexMap = new ArrayList<NameIdModel>();

        Field field;
        NameIdModel nameIdModel;
        int orderIndex = 0;
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            nameIdModel = new NameIdModel();
            field = clazz.getDeclaredFields()[i];
            
            // Skip static fields like serialVersionUid
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            } else {
                nameIdModel.setName(WordUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), ' ')));
                nameIdModel.setId(Long.valueOf(++orderIndex));
                propertyIndexMap.add(nameIdModel);
            }
        }
        return propertyIndexMap;
    }

    /** Select the already assigned user to any publication from the all users */
    private List<NameIdModel> narrowDownAssignedToUsers(List<NameIdModel> assignedToUsers) {
        if (CollectionUtils.isNotEmpty(assignedToUsers)) {
            List<NameIdModel> subSet = new ArrayList<NameIdModel>();
            for (NameIdModel nameIdModel : assignedToUsers) {
                PublicationAssignedToUserModel user = (PublicationAssignedToUserModel) nameIdModel;

                // The result set is ordered by assigned_to_any desc
                if (user.getAssignedToAny()) {
                    subSet.add(user);
                } else {
                    break;
                }
            }
            return subSet;
        }
        return Collections.emptyList();
    }

    private String prepareSelectedValuesForDb(String[] selectedValues, int allSelectedValues) {
        // None or all were selected
        if (ArrayUtils.isEmpty(selectedValues) || selectedValues.length == allSelectedValues) {
            return null;
            
        // One was selected
        } else if (selectedValues.length == 1) {
            return selectedValues[0];
            
        // More were selected
        } else {
            StringBuilder sb = new StringBuilder();
            String sep = StringUtils.EMPTY;
            for (String idStr : selectedValues) {
                sb.append(sep).append(idStr);
                sep = DB_INPUT_PARAM_SEP;
            }
            return sb.toString();
        }
    }

    // Getters for the view

    public Integer[] getPageNumbers() {
        int totalPages = (getResultCount().intValue() + getPageSize() - 1) / getPageSize();
        Integer[] options = new Integer[totalPages];
        for (int i = 0; i < totalPages; i++) {
            options[i] = Integer.valueOf(i + 1);
        }
        return options;
    }

    public List<PublicationApprovalModel> getPublications() {
        return (searchResult == null) ? Collections.<PublicationApprovalModel> emptyList() : searchResult.getResultList();
    }

    public BigDecimal getResultCount() {
        return (searchResult == null) ? BigDecimal.TEN.negate() : searchResult.getResultCount();
    }

    public Integer getPageSize() {
        return search.getPageSize();
    }

    public void setPageSize(int pageSize) {
        search.setPageSize(pageSize);
    }

    public PublicationApprovalSearchModel getSearch() {
        return search;
    }

    public List<NameIdModel> getPublicationTypes() {
        return publicationTypes;
    }

    public List<NameIdModel> getPublicationAccountTypes() {
        return publicationAccountTypes;
    }

    public List<NameIdModel> getPublicationAssignedToUsers() {
        return publicationAssignedToUsers;
    }

    public List<NameIdModel> getPublicationAlgorithmStatuses() {
        return publicationAlgorithmStatuses;
    }

    public List<NameIdModel> getPublicationDeadZoneStatuses() {
        return publicationDeadZoneStatuses;
    }

    public List<NameIdModel> getSearchFieldNameIndexList() {
        return searchFieldNameIndexList;
    }
    
    public String getPublicationMultiSelectTypeLabel() {
        return resolveMultiSelectLabel("page.approval.publication.type.label", selectedTypeIds.length, publicationTypes.size());
    }

    public String getPublicationMultiSelectStatusLabel() {
        return resolveMultiSelectLabel("page.approval.publication.status.label", selectedStatusNames.length, PublicationStatus.values().length - 1);
    }
    
    public String getPublicationMultiSelectAssignedToLabel() {
        return resolveMultiSelectLabel("page.approval.publication.assignedto.label", selectedAssignedToUserIds.length, publicationAssignedToUsers.size());
    }
    
    public String getPublicationMultiSelectAccountTypeLabel() {
        return resolveMultiSelectLabel("page.approval.publication.accounttype.label", selectedAccountTypeIds.length, publicationAccountTypes.size());
    }
    
    public String getPublicationMultiSelectAlgorithmStatusLabel() {
        return resolveMultiSelectLabel("page.approval.publication.algorithm.status.label", selectedAlgorithmStatusIds.length, publicationAlgorithmStatuses.size());
    }

    public String getPublicationMultiSelectDeadZoneStatusLabel() {
        return resolveMultiSelectLabel("page.approval.publication.deadzone.status.label", selectedDeadZoneStatusIds.length, publicationDeadZoneStatuses.size());
    }
    
    public boolean isQueryRunning() {
        return isQueryRunning;
    }
    
    public boolean isExportRunning() {
        return isExportRunning;
    }
    
    public int getExportRecordFrom() {
        return exportRecordFrom;
    }
    
    public int getExportRecordTo() {
        return exportRecordTo;
    }
    
    public boolean isExportRemaining() {
        return (exportRemaining > 0) ? true : false;
    }

    // Getters / Setters

    public String[] getSelectedTypeIds() {
        return selectedTypeIds;
    }

    public void setSelectedTypeIds(String[] selectedTypeIds) {
        this.selectedTypeIds = (String[])selectedTypeIds.clone();
    }

    public String[] getSelectedStatusNames() {
        return selectedStatusNames;
    }

    public void setSelectedStatusNames(String[] selectedStatusNames) {
        this.selectedStatusNames = (String[])selectedStatusNames.clone();
    }

    public String[] getSelectedAssignedToUserIds() {
        return selectedAssignedToUserIds;
    }

    public void setSelectedAssignedToUserIds(String[] selectedAssignedToUserIds) {
        this.selectedAssignedToUserIds = (String[])selectedAssignedToUserIds.clone();
    }

    public String[] getSelectedAccountTypeIds() {
        return selectedAccountTypeIds;
    }

    public void setSelectedAccountTypeIds(String[] selectedAccountTypeIds) {
        this.selectedAccountTypeIds = (String[])selectedAccountTypeIds.clone();
    }

    public String[] getSelectedAlgorithmStatusIds() {
        return selectedAlgorithmStatusIds;
    }

    public void setSelectedAlgorithmStatusIds(String[] selectedAlgorithmStatusIds) {
        this.selectedAlgorithmStatusIds = (String[])selectedAlgorithmStatusIds.clone();
    }

    public String[] getSelectedDeadZoneStatusIds() {
        return selectedDeadZoneStatusIds;
    }

    public void setSelectedDeadZoneStatusIds(String[] selectedDeadZoneStatusIds) {
        this.selectedDeadZoneStatusIds = (String[])selectedDeadZoneStatusIds.clone();
    }

    public String getBulkStatusTo() {
        return bulkStatusTo;
    }

    public void setBulkStatusTo(String bulkStatusTo) {
        this.bulkStatusTo = bulkStatusTo;
    }

    public int getExportRecordSize() {
        return exportRecordSize;
    }

    public void setExportRecordSize(int exportRecordSize) {
        this.exportRecordSize = exportRecordSize;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }
    
}
