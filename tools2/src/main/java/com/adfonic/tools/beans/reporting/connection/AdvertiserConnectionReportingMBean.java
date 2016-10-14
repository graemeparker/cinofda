package com.adfonic.tools.beans.reporting.connection;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.presentation.reporting.AdvertiserReportFilter;
import com.adfonic.presentation.reporting.operator.AdvertiserOperatorReportService;
import com.adfonic.tools.beans.reporting.AdvertiserReportingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.util.Range;

@Component
@Scope("view")
public class AdvertiserConnectionReportingMBean extends AdvertiserReportingMBean {

    private static final long serialVersionUID = 6323294325154320670L;

    @Autowired
    AdvertiserOperatorReportService service;

    @Override
    protected void initReport() {
        // no specific initialisation for this report
    }

    @Override
    protected String getReportName() {
        return Constants.REPORT_CONNECTIONS;
    }

    @Override
    protected void generateReport() {
        service.init(getUserLocale(), getCompanyTimeZone());
        setReport(service.getReport(
                new AdvertiserReportFilter.Builder().detailedByDay(isDailyStatistics()).useConversionTracking(isConversionTrackingUsed())
                        .build(),
                new AdvertiserQueryParameters.Builder().advertiserId(getUser().getAdvertiserDto().getId())
                        .campaignIds(getSelectedCampaignIdsForProc()).dateRange(new Range<Date>(getStartDate(), getEndDate())).build()));
    }

}
