package com.adfonic.presentation.reporting.campaign;

import java.util.Locale;

import com.adfonic.domain.Company;
import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.presentation.reporting.campaign.impl.AdvertiserCampaignReportServiceImpl.CampaignDetailedReportOption;
import com.adfonic.reporting.Report;

public interface AdvertiserCampaignReportService extends AdvertiserReportService {
    
    public void init(Locale userLocale, Company company);
    public Report getReport(CampaignDetailedReportOption option, AdvertiserReportFilter reportFilter, AdvertiserQueryParameters queryParams);
}
