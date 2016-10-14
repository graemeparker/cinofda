package com.adfonic.tools.beans.reporting;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.ArrayUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Row;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.export.ReportingReportDefinitionBuilder;
import com.adfonic.util.AdfonicTimeZone;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "reporting-advertiser", pattern = "/reporting/advertiser", viewId = "/WEB-INF/jsf/reporting/advertiser/reporting.jsf") })
public abstract class AdvertiserReportingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvertiserReportingMBean.class);

    protected static final String TILDE = "~";
    protected static final String EXPORT_REPORT_FILENAME_PREFIX = "Advertiser-";
    private static final String SDF_REPORT_NAME_FORMAT = "yyyyMMddhhmm";

    // ////////////////////
    // Option fields
    // ////////////////////

    // Campaign list
    private Collection<CampaignTypeAheadDto> campaigns;
    private Long[] selectedCampaignIds;

    // Group by options
    private boolean groupByCategory;
    private boolean groupByInventory;
    private boolean detailedByDay;
    private boolean dailyStatistics;

    // Date range options
    private Date startDate;
    private Date endDate;
    protected boolean disableEndDate;

    // The final report
    private Report report;

    // Flag for report is running
    private boolean reportRunning;

    private Long selectedCampaignId;

    // ////////////////////
    // Services
    // ////////////////////

    @Autowired
    protected CampaignService campaignService;

    @Override
    @PostConstruct
    protected void init() throws Exception {
        startDate = endDate = returnNow();
        disableEndDate = false;
        campaigns = getAdvertiserCampaigns(campaignService);
        setGroupByInventory(Boolean.FALSE);
        setGroupByCategory(Boolean.FALSE);
        setDailyStatistics(Boolean.FALSE);
        setDetailedByDay(Boolean.FALSE);
        initReport();
    }

    // ////////////////////
    // Abstract methods
    // ////////////////////

    protected abstract void initReport();

    protected abstract void generateReport();

    protected abstract String getReportName();

    // ////////////////////
    // Report generation
    // ////////////////////

    /** Construct campaign id list or null if all campaigns were selected */
    protected String getSelectedCampaignIdsForProc() {
        return isAllCampaignsSelected() ? null : getTildeSeparatedString(Arrays.asList(selectedCampaignIds));
    }

    /** Construct the tilde (~) separated entity id list */
    protected String getTildeSeparatedString(Collection<Long> entityIds) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Long entityId : entityIds) {
            sb.append(sep).append(entityId);
            sep = TILDE;
        }
        return sb.toString();
    }

    /** Generate report button event handler */
    @SuppressWarnings("squid:S1172")
    public void generateReport(ActionEvent actionEvent) {
        if (validation()) {
            reportRunning = true;
            generateReport();
            reportRunning = false;
        }
    }

    @SuppressWarnings("squid:S1172")
    public void createCSV(ActionEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        StringWriter writer = new StringWriter();
        report.writeCSV(writer);
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseContentType("text/csv");
        externalContext.setResponseHeader("Content-Disposition",
                "attachment; filename=" + getExportReportName() + "-" + getReportNameSDF() + ".csv");
        try {
            OutputStream outputStream = externalContext.getResponseOutputStream();
            outputStream.write(writer.getBuffer().toString().getBytes("UTF-8"));
            externalContext.responseFlushBuffer();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            LOGGER.error("Error during generating CSV report", e);
        }
        facesContext.responseComplete();
    }

    public String timeZoneDescription() {
        return AdfonicTimeZone.getAdfonicTimeZoneDescription(getCompanyTimeZone());
    }

    protected boolean isConversionTrackingUsed() {
        return campaignService.isConversionTrackingUsed(isAllCampaignsSelected() ? null : Arrays.asList(selectedCampaignIds), getUser()
                .getAdvertiserDto().getId());
    }

    protected boolean isAllCampaignsSelected() {
        return selectedCampaignIds.length == 0 || selectedCampaignIds.length == campaigns.size();
    }

    // ////////////////////
    // Private methods
    // ////////////////////
    
    private boolean validation() {
        if (endDate.before(startDate)) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "endDate", null, "page.reporting.commons.date.end.sooner");
            return false;
        }
        return true;
    }
    
    private String getReportNameSDF() {
        return new SimpleDateFormat(SDF_REPORT_NAME_FORMAT).format(returnNow());
    }

    // ////////////////////
    // Getters
    // ////////////////////

    public Date getToday() {
        return returnNow();
    }

    public String getAllCampaignLabel() {
        return FacesUtils.getBundleMessage("page.reporting.commons.campaigns.label.all", String.valueOf(getCampaigns().size()));
    }

    public boolean is(String reportName) {
        return (reportName.equals(getReportName())) ? true : false;
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public boolean isGroupByCategory() {
        return groupByCategory;
    }

    public void setGroupByCategory(boolean groupByCategory) {
        this.groupByCategory = groupByCategory;
    }

    public boolean isGroupByInventory() {
        return groupByInventory;
    }

    public void setGroupByInventory(boolean groupByInventory) {
        this.groupByInventory = groupByInventory;
    }

    public boolean isDetailedByDay() {
        return detailedByDay;
    }

    public void setDetailedByDay(boolean detailedByDay) {
        this.detailedByDay = detailedByDay;
    }

    public boolean isDailyStatistics() {
        return dailyStatistics;
    }

    public void setDailyStatistics(boolean dailyStatistics) {
        this.dailyStatistics = dailyStatistics;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = (startDate == null ? null : new Date(startDate.getTime()));
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = (endDate == null ? null : new Date(endDate.getTime()));
    }

    public boolean isDisableEndDate() {
        return disableEndDate;
    }

    public Long[] getSelectedCampaignIds() {
        return selectedCampaignIds;
    }

    public void setSelectedCampaignIds(Long[] selectedCampaignIds) {
        this.selectedCampaignIds = (selectedCampaignIds == null ? null : selectedCampaignIds.clone());
    }

    public Collection<CampaignTypeAheadDto> getCampaigns() {
        return campaigns;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public boolean isReportRunning() {
        return reportRunning;
    }

    // ////////////////////
    // This methods just used by budget and creative reports
    // ////////////////////

    public Long getSelectedCampaignId() {
        return selectedCampaignId;
    }

    public void setSelectedCampaignId(Long selectedCampaignId) {
        this.selectedCampaignId = selectedCampaignId;
        // Empty or all campaigns
        if (isAllSelected(selectedCampaignId)) {
            setSelectedCampaignIds(ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
        } else {
            setSelectedCampaignIds(new Long[] { selectedCampaignId });
        }
    }

    protected boolean isAllSelected(Long selectedId) {
        return selectedId == null || selectedId.equals(0L);
    }

    // ////////////////////
    // Listeners
    // ////////////////////

    public void reportViewChange(ValueChangeEvent valueChangeEvent) {
        if (valueChangeEvent != null) {
            this.disableEndDate = Constants.REPORTING_VIEW_OPTION_HOURLY.equals(valueChangeEvent.getNewValue());
        }
    }

    public StreamedContent exportToExcel() throws IOException {

        // Running report
        ReportingReportDefinitionBuilder<Row> builder = new ReportingReportDefinitionBuilder<Row>(getExportReportName(), null, report);
        ReportDefinition<Row> reportDefinition = builder.build(report.getRows());
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), com.adfonic.presentation.util.Constants.CONTENT_TYPE_EXCEL_XLSX, getExportReportName()
                + "-" + getReportNameSDF() + ".xlsx");
    }

    private String getExportReportName() {
        return EXPORT_REPORT_FILENAME_PREFIX + getReportName();
    }
}
