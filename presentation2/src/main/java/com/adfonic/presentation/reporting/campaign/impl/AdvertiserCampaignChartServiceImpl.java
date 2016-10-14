package com.adfonic.presentation.reporting.campaign.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Company;
import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.campaign.AdvertiserCampaignChartService;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Row;
import com.adfonic.reporting.sql.ReportUtil;
import com.adfonic.reporting.sql.ToolsSQLQuery;

@Service("campaignChartService")
public class AdvertiserCampaignChartServiceImpl implements AdvertiserCampaignChartService {
    
    @Autowired
    private ToolsSQLQuery toolsSqlQuery;
    private TimeZone companyTimeZone;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d MMMM yyyy");
    private Object[] xAxisTicks;

    @Override
    public void init(Locale userLocale, Company company) {
        TimeZone defaultTimeZone = company.getDefaultTimeZone();
        this.companyTimeZone = defaultTimeZone;
        toolsSqlQuery.init(userLocale, defaultTimeZone);
        formatter.setTimeZone(companyTimeZone);
    }   
    
    @Override
    public String getCampaignHourlyStatistics(AdvertiserQueryParameters queryParams) {
        toolsSqlQuery.addParameters(new Parameter.AdvertiserTimeByDay(companyTimeZone, queryParams.getDateRange()), 
                                    new Parameter.AdvertiserTimeByHour(companyTimeZone, queryParams.getDateRange()));
        ReportUtil.addReportMetrics(toolsSqlQuery, false, true);
        Report report = toolsSqlQuery.getCampaignReportTotalByHour(queryParams.getAdvertiserId(), 
                                                            queryParams.getCampaignIds(), 
                                                            queryParams.getDateRange().getStart(), true, false);
        int reportSize = report.getRows().size();
        if (reportSize > 0) {
            xAxisTicks = new Object[reportSize];
            int i = 0;
            Object[] hourZeroRowCells = null;
            String hourZeroXAxisTick = "";
            StringBuffer chartRows = new StringBuffer();
            chartRows.append("[");
            for (Iterator<Row> iter = report.getRows().iterator(); iter.hasNext(); i++) {
                Row row = iter.next();
                
                String hour = row.getRawCells()[1];
                if (hour.equals("00")) {
                    hourZeroRowCells = extractMetrics(row.getRawCells(), "{v:24, f:'00:00'}"); // forcing this to come after 23!
                    hourZeroXAxisTick = "{v:" + 24 + ", f:'" + hour + "'}"; 
                    continue;
                }                     
                
                xAxisTicks[i-1] = "{v:" + hour + ", f:'" + hour + "'}"; // skipped first hour, adding it at the end.

                Object[] rawCellsMinusDateAndEcpm = extractMetrics(row.getRawCells(), "{v:" + hour + ", f:'" + hour.concat(":00") + "'}");
                chartRows.append(Arrays.toString(rawCellsMinusDateAndEcpm));
                chartRows.append(",");
            }
            
            if (reportSize == 24) {
                xAxisTicks[xAxisTicks.length - 1] = hourZeroXAxisTick;
                chartRows.append(Arrays.toString(hourZeroRowCells));
            } else {
                chartRows.deleteCharAt(chartRows.lastIndexOf(","));
            }
            chartRows.append("]");
            return chartRows.toString();
        }
        return null; 
    }
    
    @Override
    public String getxAxisTicks() {
        return Arrays.toString(xAxisTicks);
    }
    
    private Object[] extractMetrics(String[] row, String formattedHour) {
        int columnIndex = 1;
        return new Object[]{formattedHour, // Hour
                          row[columnIndex+1], // Impressions
                          row[columnIndex+2], // Clicks
                          row[columnIndex+3], // CTR
                          row[columnIndex+6], // Spend
                          row[columnIndex+7], // Conversions
                          row[columnIndex+9]}; // Cost per Conversion
    }
    
    @Override
    public void init(Locale userLocale, TimeZone companyTimeZone) {
        // TODO remove in parent
    }
}
