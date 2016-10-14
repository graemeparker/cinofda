package com.adfonic.presentation.reporting.location.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.location.AdvertiserLocationSnapshotService;
import com.adfonic.reporting.sql.ToolsSQLQuery;
import com.adfonic.reporting.sql.dto.LocationDetailDto;

@Component("locationSnapshotService")
public class AdvertiserLocationSnapshotServiceImpl implements AdvertiserLocationSnapshotService {
    
    @Autowired
    private ToolsSQLQuery toolsSqlQuery;
    
    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        toolsSqlQuery.init(userLocale, companyTimeZone);
    }

    @Override
    public Map<String,Long> getImpressionsPerCountry(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParameters){
        List<LocationDetailDto> report = getUsageMapReport(filter, queryParameters);
        
        Collections.sort(report, new Comparator<LocationDetailDto>() {
            @Override
            public int compare(LocationDetailDto o1, LocationDetailDto o2) {
                return ((Long) o1.getImpressions()).compareTo(((Long) o2.getImpressions()));
            }
        });

        Map<String,Long> countryCount = new HashMap<String,Long>();
        String isocode = "??";
        for(LocationDetailDto dto : report){
            isocode = dto.getCountryIsocode();
            if (countryCount.containsKey(isocode)) {
                long impressionValue = countryCount.get(isocode);
                countryCount.put(isocode, impressionValue + dto.getImpressions());
            } else {
                countryCount.put(dto.getCountryIsocode(), dto.getImpressions());
            }
        }
        return countryCount;
    }
 
    @Override
    public List<LocationDetailDto> getUsageMapReport(AdvertiserReportFilter filter, AdvertiserQueryParameters queryParams) {
        return toolsSqlQuery.getLocationReportDetail(queryParams.getAdvertiserId(), 
                                                          queryParams.getCampaignIds(), 
                                                          queryParams.getDateRange().getStart(),
                                                          queryParams.getDateRange().getEnd());
    }
}
