package com.adfonic.presentation.reporting.operator;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.reporting.Report;

public interface AdvertiserOperatorReportService extends AdvertiserReportService {

    public abstract Report getReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams);
}
