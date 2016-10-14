package com.adfonic.presentation.reporting.location;

import java.util.List;
import java.util.Map;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.AdvertiserReportService;
import com.adfonic.reporting.sql.dto.LocationDetailDto;

/**
 * Transforms impression data to populate the World chart (Geo chart) on the Reporting Location page.
 * Important: call init() to initialise the dao class in reporting and prevent the dao giving a NPE.
 *
 */
public interface AdvertiserLocationSnapshotService extends AdvertiserReportService {

    public abstract Map<String,Long> getImpressionsPerCountry(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams);

    /**
     * Drives the world map chart on the Location reporting page
     * @param filter
     * @param queryParameters
     * @return
     */
    public abstract List<LocationDetailDto> getUsageMapReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams);
}
