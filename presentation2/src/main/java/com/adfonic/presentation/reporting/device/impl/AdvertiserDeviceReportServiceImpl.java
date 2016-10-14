package com.adfonic.presentation.reporting.device.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.device.AdvertiserDeviceQueryParameters;
import com.adfonic.presentation.reporting.device.AdvertiserDeviceReportService;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("deviceReportService")
public class AdvertiserDeviceReportServiceImpl implements AdvertiserDeviceReportService {

    protected static ToolsSQLQuery toolsSqlQuery;
    private TimeZone companyTimeZone;

    @Autowired
    public void setToolsSqlQuery(ToolsSQLQuery toolsSqlQuery) {
        AdvertiserDeviceReportServiceImpl.toolsSqlQuery = toolsSqlQuery;
    }

    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        toolsSqlQuery.init(userLocale, companyTimeZone);
        this.companyTimeZone = companyTimeZone;
    }
    
    @Override
    public Report getReport(LocationBreakdown option, AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
        ReportUtil.addReportMetrics(toolsSqlQuery, false, filter.isUseConversionTracking());
        if (filter.isDetailedByDay()) {
            return option.getDeviceReportByDay(filter, queryParams, companyTimeZone);
        }
        return option.getDeviceReport(filter, queryParams);
    }
    
    public enum LocationBreakdown {
        REGION_BY_ALL(DeviceGrouping.ALL) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.Device());
                return toolsSqlQuery.getDeviceReportByRegion(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.Device());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByRegionDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        },
        REGION_BY_PLATFORM(DeviceGrouping.PLATFORM) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.GroupByPlatform());
                return toolsSqlQuery.getDeviceReportByRegionByPlatform(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.GroupByPlatform());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByRegionByPlatformDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        },
        REGION_BY_VENDOR(DeviceGrouping.VENDOR) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                // TODO get rid of Parameter.Devices in favour of an sql only 'parameter' for Vendors - refactoring in reporting project required
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.Devices(true));
                return toolsSqlQuery.getDeviceReportByRegionByBrand(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByRegions(), new Parameter.Devices(true));
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByRegionByBrandDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        },
        COUNTRY_BY_ALL(DeviceGrouping.ALL) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.Device());
                return toolsSqlQuery.getDeviceReportByCountry(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.Device());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByCountryDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        }, 
        COUNTRY_BY_PLATFORM(DeviceGrouping.PLATFORM) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.GroupByPlatform());
                return toolsSqlQuery.getDeviceReportByCountryByPlatformDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.GroupByPlatform());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByCountryByPlatform(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        }, 
        COUNTRY_BY_VENDOR(DeviceGrouping.VENDOR) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.Devices(true));
                return toolsSqlQuery.getDeviceReportByCountryByBrand(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.LocationByCountries(), new Parameter.Devices(true));
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByCountryByBrandDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), 
                        queryParams.getModelIds(), queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        }, 
        SUMMARY_BY_ALL(DeviceGrouping.ALL) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.Device());
                return toolsSqlQuery.getDeviceReport(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.Device());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        }, 
        SUMMARY_BY_PLATFORM(DeviceGrouping.PLATFORM) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.GroupByPlatform());
                return toolsSqlQuery.getDeviceReportByPlatform(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.GroupByPlatform());
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByPlatformDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        }, 
        SUMMARY_BY_VENDOR(DeviceGrouping.VENDOR) {
            @Override
            protected Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams) {
                toolsSqlQuery.addParameters(new Parameter.Devices(true));
                return toolsSqlQuery.getDeviceReportByBrand(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }

            @Override
            protected Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone) {
                toolsSqlQuery.addParameters(new Parameter.Devices(true));
                toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()));
                return toolsSqlQuery.getDeviceReportByBrandDay(queryParams.getAdvertiserId(), queryParams.getCampaignIds(), queryParams.getVendorIds(), queryParams.getModelIds(), 
                        queryParams.getDateRange().getStart(), queryParams.getDateRange().getEnd(), filter.isUseConversionTracking(), false);
            }
        };
        
        DeviceGrouping deviceGrouping;
        
        LocationBreakdown(DeviceGrouping grouping) {
            this.deviceGrouping = grouping;
        }
        
        protected abstract Report getDeviceReport(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams);
        
        protected abstract Report getDeviceReportByDay(AdvertiserReportFilter filter, AdvertiserDeviceQueryParameters queryParams, TimeZone companyTimeZone);

        public enum DeviceGrouping {
            PLATFORM,
            VENDOR,
            ALL;
        }
    }
}
