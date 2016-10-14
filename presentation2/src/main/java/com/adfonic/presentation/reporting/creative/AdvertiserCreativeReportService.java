package com.adfonic.presentation.reporting.creative;

import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.reporting.Report;

public interface AdvertiserCreativeReportService extends AdvertiserReportService {
    
    public abstract Report getReport(AdvertiserReportFilter reportFilter, AdvertiserCreativeQueryParameters queryParams);

}
