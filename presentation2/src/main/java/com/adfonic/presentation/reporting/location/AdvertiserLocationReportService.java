package com.adfonic.presentation.reporting.location;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.reporting.Report;

public interface AdvertiserLocationReportService extends AdvertiserReportService {

    public abstract Report getReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams);
}
