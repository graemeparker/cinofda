package com.adfonic.presentation.reporting.device;

import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown;
import com.adfonic.reporting.Report;

public interface AdvertiserDeviceReportService extends AdvertiserReportService {

    public abstract Report getReport(LocationBreakdown option, AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams);
}
