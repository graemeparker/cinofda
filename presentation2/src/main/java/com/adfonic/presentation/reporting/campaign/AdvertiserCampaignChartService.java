package com.adfonic.presentation.reporting.campaign;

import java.util.Locale;

import com.adfonic.domain.Company;
import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportService;

/**
 * Responsible for the chart on the Campaign report page 
 * 'Show detailed statistics by hour for start date' option.
 *
 */
public interface AdvertiserCampaignChartService extends AdvertiserReportService {

    /**
     * Call this method to get a list of arrays that are returned from the Report object
     * Including metrics e.g. Impressions, Clicks, CTR, Cost (Spend), ECPM, Conversions, ECPC
     * @param queryParams
     * @return
     */
    public abstract String getCampaignHourlyStatistics(AdvertiserQueryParameters queryParams);
    
    public String getxAxisTicks();
    
    public void init(Locale userLocale, Company company);
}
