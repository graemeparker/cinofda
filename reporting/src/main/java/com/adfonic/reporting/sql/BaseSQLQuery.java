package com.adfonic.reporting.sql;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Parameter.AdvertiserTimeByDay;
import com.adfonic.reporting.Report;

public class BaseSQLQuery extends JdbcDaoSupport {
    
    private static final Logger logger = Logger.getLogger(BaseSQLQuery.class.getName());
    
    protected static final String CAMPAIGN_COLUMN         = "Campaign";
    protected static final String DATE_COLUMN             = "Date";
    protected static final String BUDGET_COLUMN           = "BUDGET";
    protected static final String BUDGET_DEPLETED_COLUMN  = "BUDGET_DEPLETED";
    protected static final String BUDGET_REMAINING_COLUMN = "BUDGET_REMAINING";
    protected static final String DATE_START_COLUMN       = "Date.start";
    protected static final String DATE_END_COLUMN         = "Date.end";
    protected static final String RESULT_OBJECT           = "result";

    protected Locale locale;
    protected TimeZone timezone;
    private DateFormat dateFormatter = null;
    protected List<Metric> metrics;
    protected List<Parameter> parameters;
    
    public void init(Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
        this.metrics = new ArrayList<Metric>();
        this.parameters = new ArrayList<Parameter>();
    }

    public void addParameters(Parameter... parameters) {
        
        for (Parameter p : parameters) {
            this.parameters.add(p);
        }
    }

    public void addMetrics(Metric... metrics) {
        for (Metric m : metrics) {
            this.metrics.add(m);
        }
    }

    protected Report generateColumnsForReport(Report report) {
        for (int i = 0; i < parameters.size(); i++)  {
            if (report != null) { 
                Parameter parameter = parameters.get(i);
                if (parameter instanceof AdvertiserTimeByDay){
                    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM,locale);
//                    df.setCalendar(((AdvertiserTimeByDay) parameter).getCalendar());
                    report.addColumn("Date", Date.class, df, false);
                }else{
                    parameter.addColumn(report, locale);
                }
            }
        }

        for (int i = 0; i < metrics.size(); i++) {
            if (report != null) { 
                addColumn(report, metrics.get(i)); 
            }
        }
        return report;
    }

    protected String getDateAsString(Date date) {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat("yyyyMMdd");
        }
        dateFormatter.setTimeZone(timezone);
        return dateFormatter.format(date);
    }

    @SuppressWarnings("rawtypes")
    protected void addColumn(Report report, Metric metric) {
        NumberFormat format = metric.getFormat(locale);
        Class dataType = null;
        boolean showTotal = true;
        boolean showAsAverage = false;

        Metric numerator = null;
        Metric divisor = null;
        double factor = 1.0;

        switch (metric) {
            case REQUESTS:
            case IMPRESSIONS:
            case BEACONS:
            case CLICKS:
            case CONVERSIONS:
            case TOTAL_VIEWS:
            case COMPLETED_VIEWS:
                dataType = Integer.class;
                break;
            case AVERAGE_DURATION:
                dataType = Integer.class;
                showAsAverage = true;
                break;
            case FILL_RATE:
                numerator = Metric.IMPRESSIONS;
                divisor = Metric.REQUESTS;
                break;
            case CTR:
                numerator = Metric.CLICKS;
                divisor = Metric.IMPRESSIONS;
                break;
            case CONVERSION_PERCENT:
                numerator = Metric.CONVERSIONS;
                divisor = Metric.CLICKS;
                break;
            case DEVICE_PERCENT_REQUESTS:
            case DEVICE_PERCENT_IMPRESSIONS:
            case LOCATION_PERCENT_REQUESTS:
            case LOCATION_PERCENT_IMPRESSIONS:
            case PLATFORM_PERCENT_IMPRESSIONS:
            case ENGAGEMENT_SCORE:
            case Q1_PERCENT:
            case Q2_PERCENT:
            case Q3_PERCENT:
            case Q4_PERCENT:
                showTotal = false;
                break;
            case COST_PER_CONVERSION:
                numerator = Metric.COST;
                divisor = Metric.CONVERSIONS;
                break;
            case COST_PER_VIEW:
                numerator = Metric.COST;
                divisor = Metric.TOTAL_VIEWS;
                break;
            case COST:
            case PAYOUT:
                dataType = Double.class;
                break;
            case ECPM_PUB:
                numerator = Metric.PAYOUT;
                divisor = Metric.IMPRESSIONS;
                factor = 1000.0;
                break;
            case ECPC_PUB:
                numerator = Metric.PAYOUT;
                divisor = Metric.IMPRESSIONS;
                break;
            case ECPM_AD:
                numerator = Metric.COST;
                divisor = Metric.IMPRESSIONS;
                factor = 1000.0;
                break;
            case ECPC_AD:
                numerator = Metric.COST;
                divisor = Metric.CLICKS;
                break;
            default:
                break;
        }
        
        if (showAsAverage) {
            report.addAverageColumn(metric.toString(), dataType, factor, format, showTotal);
        } else if (numerator != null) {
            report.addPercentColumn(metric.toString(), numerator.toString(), divisor.toString(), factor, format, showTotal);
        } else {
            report.addColumn(metric.toString(), dataType, format, showTotal);
        }
    }
    
//    public String stringFromTimestamp(String date){
//        Date theDate = xxxxxdateFromTimeStamp(date);
//        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM,locale);
//        return df.format(theDate);
//    }

    protected Date dateFromTimeStamp(String date) {
        Date dateObject = null;
        try {
            if (date != null) {
                long timestamp = (long) (Double.parseDouble(date)*1000L);
                dateObject = new Date(timestamp);
            } 
        } catch (NumberFormatException e){
            logger.warning(date + " was not a timestamp");
        } catch (NullPointerException e){
            logger.warning("date was null - This should no longer be happening");
        }
        return dateObject;
    }    
}
