package com.adfonic.tools.beans.reporting.creative;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.creative.CreativeFormatDto;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.presentation.format.FormatService;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeQueryParameters;
import com.adfonic.presentation.reporting.creative.AdvertiserCreativeReportService;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserCreativeReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = 1L;

    // ////////////////////
    // Option fields
    // ////////////////////

    // Creatives list
    private Collection<CreativeFormatDto> creatives;
    private Long selectedCreativeId;

    // Format list
    private Collection<FormatDto> formats;
    private Long selectedFormatId;

    // ////////////////////
    // Services
    // ////////////////////

    @Autowired
    private AdvertiserCreativeReportService creativeReportService;
    @Autowired
    private FormatService formatService;

    // ////////////////////
    // Overrides
    // ////////////////////

    @Override
    protected String getReportName() {
        return Constants.REPORT_CREATIVES;
    }

    @Override
    protected void initReport() {
        creatives = Collections.emptyList();
        formats = formatService.getAllFormats();
    }

    @Override
    protected void generateReport() {

        Set<Long> selectedCreativeIds = isAllSelected(selectedCreativeId) ? Collections.<Long> emptySet() : Collections
                .singleton(selectedCreativeId);
        Set<Long> selectedFormatIds = isAllSelected(selectedFormatId) ? Collections.<Long> emptySet() : Collections
                .singleton(selectedFormatId);

        AdvertiserCreativeQueryParameters queryParams = new AdvertiserCreativeQueryParameters.Builder()
        .advertiserId(getUser().getAdvertiserDto().getId().longValue()).campaignIds(getSelectedCampaignIdsForProc())
        .creativeIds(getSelectedCreativeIdsForProc(selectedCreativeIds)).formatIds(getSelectedFormatIdsForProc(selectedFormatIds))
        .dateRange(new Range<Date>(getStartDate(), getEndDate())).build();
        AdvertiserReportFilter reportFilter = new AdvertiserReportFilter.Builder().groupByCategory(isGroupByCategory())
        .groupByInventorySource(isGroupByInventory()).detailedByDay(isDetailedByDay())
        .useConversionTracking(isConversionTrackingUsed()).build();

        creativeReportService.init(getUserLocale(), getCompanyTimeZone());
        setReport(creativeReportService.getReport(reportFilter, queryParams));
    }

    // ////////////////////
    // Event Handlers
    // ////////////////////

    public void updateCreatives() {
        creatives = campaignService.getAllCreativesForCampaignIds(Arrays.asList(getSelectedCampaignIds()));
    }

    // ////////////////////
    // Private methods
    // ////////////////////

    /** Construct creative id list */
    private String getSelectedCreativeIdsForProc(Set<Long> selectedCreativeIds) {
        return (selectedCreativeIds.isEmpty()) ? null : getTildeSeparatedString(selectedCreativeIds);
    }

    /** Construct format id list */
    private String getSelectedFormatIdsForProc(Set<Long> selectedFormatIds) {
        return (selectedFormatIds.isEmpty()) ? null : getTildeSeparatedString(selectedFormatIds);
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public Long getSelectedCreativeId() {
        return selectedCreativeId;
    }

    public void setSelectedCreativeId(Long selectedCreativeId) {
        this.selectedCreativeId = selectedCreativeId;
    }

    public Long getSelectedFormatId() {
        return selectedFormatId;
    }

    public void setSelectedFormatId(Long selectedFormatId) {
        this.selectedFormatId = selectedFormatId;
    }

    public Collection<CreativeFormatDto> getCreatives() {
        return creatives;
    }

    public Collection<FormatDto> getFormats() {
        return formats;
    }

}
