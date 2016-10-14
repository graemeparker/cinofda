package com.adfonic.tools.beans.reporting.device;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.model.ModelDto;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.device.AdvertiserDeviceQueryParameters;
import com.adfonic.presentation.reporting.device.AdvertiserDeviceReportService;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown.DeviceGrouping;
import com.adfonic.tools.beans.commons.DeviceModelsMBean;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserDeviceReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = 1L;

    @Autowired
    private DeviceModelsMBean deviceModelsMBean;
    @Autowired
    private AdvertiserDeviceReportService deviceReportService;

    // ////////////////////
    // Option fields
    // ////////////////////

    private DeviceGrouping reportView;
    private LocationBreakdown locationBreakdown;

    // ////////////////////
    // Overrides
    // ////////////////////

    @Override
    protected String getReportName() {
        return Constants.REPORT_DEVICES;
    }

    @Override
    protected void initReport() {
        setReportView(DeviceGrouping.VENDOR.name());
        setLocationBreakdown(LocationBreakdown.SUMMARY_BY_ALL.name());
    }

    @Override
    protected void generateReport() {

        // Collect the unique model and vendor ids
        Set<Long> selectedModelIds = new HashSet<>();
        Set<Long> selectedVendorIds = new HashSet<>();
        for (ModelDto model : getDeviceModelsMBean().getSelectedDeviceModels()) {
            selectedModelIds.add(model.getId());
            selectedVendorIds.add(model.getVendor().getId());
        }

        AdvertiserDeviceQueryParameters queryParams = new AdvertiserDeviceQueryParameters.Builder()
        .advertiserId(getUser().getAdvertiserDto().getId().longValue()).campaignIds(getSelectedCampaignIdsForProc())
        .dateRange(new Range<Date>(getStartDate(), getEndDate())).modelIds(getSelectedModelIdsForProc(selectedModelIds))
        .vendorIds(getSelectedVendorIdsForProc(selectedVendorIds)).build();
        AdvertiserReportFilter reportFilter = new AdvertiserReportFilter.Builder().detailedByDay(isDetailedByDay())
        .useConversionTracking(isConversionTrackingUsed()).build();

        deviceReportService.init(getUserLocale(), getCompanyTimeZone());
        setReport(deviceReportService.getReport(
                LocationBreakdown.valueOf(locationBreakdown.name().split("_")[0] + "_BY_" + reportView.name()), reportFilter, queryParams));
    }

    // ////////////////////
    // Private methods
    // ////////////////////

    /** Construct vendor id list or null if none were selected */
    private String getSelectedVendorIdsForProc(Set<Long> selectedVendorIds) {
        return (selectedVendorIds.isEmpty()) ? null : getTildeSeparatedString(selectedVendorIds);
    }

    /** Construct model id list or null if none were selected */
    private String getSelectedModelIdsForProc(Set<Long> selectedModelIds) {
        return (selectedModelIds.isEmpty()) ? null : getTildeSeparatedString(selectedModelIds);
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public String getReportView() {
        return reportView.name();
    }

    public void setReportView(String reportView) {
        this.reportView = DeviceGrouping.valueOf(reportView);
    }

    public String getLocationBreakdown() {
        return locationBreakdown.name();
    }

    public void setLocationBreakdown(String locationBreakdown) {
        this.locationBreakdown = LocationBreakdown.valueOf(locationBreakdown);
    }

    public DeviceModelsMBean getDeviceModelsMBean() {
        return deviceModelsMBean;
    }
}
