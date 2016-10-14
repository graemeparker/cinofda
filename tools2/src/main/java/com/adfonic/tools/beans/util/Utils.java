package com.adfonic.tools.beans.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.presentation.util.DateUtils;
import com.ibm.icu.util.GregorianCalendar;

public class Utils {

    /***
     * Static method that given a FacesContext and Bean name, returns the jsf
     * instance of that bean.
     *
     * */
    @SuppressWarnings("unchecked")
    public static <T> T findBean(FacesContext context, String beanName) {
        return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
    }

    public static String getCampaignCookieProperties(List<NameIdBusinessDto> campaigns) {
        String result = "";
        for (int k = 0; k < campaigns.size(); k++) {
            NameIdBusinessDto c = campaigns.get(k);
            result = result + Long.toString(c.getId()) + "#";
        }
        return result;
    }

    public static String getCampaignNavigationCookieProperties(Map<Long, Integer> campaigns) {
        String result = "";
        for (Map.Entry<Long, Integer> entry : campaigns.entrySet()) {
            if (entry.getValue() != null && !StringUtils.isEmpty(Integer.toString(entry.getValue()))) {
                result = result + Long.toString(entry.getKey()) + "-" + Integer.toString(entry.getValue()) + "#";
            }
        }
        return result;
    }

    public static String getPublicationCookieProperties(List<PublicationTypeAheadDto> publications) {
        String result = "";
        for (int k = 0; k < publications.size(); k++) {
            PublicationTypeAheadDto c = publications.get(k);
            result = result + Long.toString(c.getId()) + "#";
        }
        return result;
    }

    public static String getAdvertiserCookieProperties(List<AdvertiserDto> advertisers) {
        String result = "";
        for (int k = 0; k < advertisers.size(); k++) {
            AdvertiserDto c = advertisers.get(k);
            result = result + Long.toString(c.getId()) + "#";
        }
        return result;
    }

    public static Date[] getDateRange(String value) {
        Date[] result = new Date[2];
        if (Constants.TODAY.equals(value)) {
            result[0] = new DateTime().toCalendar(FacesContext.getCurrentInstance().getViewRoot().getLocale()).getTime();
            result[1] = result[0];
        } else if (Constants.YESTERDAY.equals(value)) {
            DateTime dt = new DateTime();
            dt = dt.minusDays(1);
            result[0] = dt.toCalendar(FacesContext.getCurrentInstance().getViewRoot().getLocale()).getTime();
            result[1] = dt.toCalendar(FacesContext.getCurrentInstance().getViewRoot().getLocale()).getTime();
        } else if (Constants.LAST_7_DAYS.equals(value)) {
            DateTime dt = new DateTime();
            dt = dt.minusDays(1);
            DateTime dt2 = dt.minusDays(6);
            result[0] = dt2.toCalendar(FacesContext.getCurrentInstance().getViewRoot().getLocale()).getTime();
            result[1] = dt.toCalendar(FacesContext.getCurrentInstance().getViewRoot().getLocale()).getTime();
        } else if (Constants.THIS_MONTH.equals(value)) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR_OF_DAY, 0);
            gc.set(Calendar.MINUTE, 0);
            result[0] = gc.getTime();
            result[1] = new Date();
        } else if (Constants.LAST_MONTH.equals(value)) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR_OF_DAY, 23);
            gc.set(Calendar.MINUTE, 0);
            gc.add(Calendar.DATE, -1);
            result[1] = gc.getTime();
            gc.set(Calendar.DAY_OF_MONTH, 1);
            gc.set(Calendar.HOUR_OF_DAY, 23);
            result[0] = gc.getTime();
        }
        return result;
    }

    public static Map<String, Boolean> getChartPeriodAvailable(Date[] dateRange) {
        Date from = dateRange[0];
        Date to = dateRange[1];
        DateTime dtFrom = new DateTime(from);
        DateTime dtTo = new DateTime(to);
        int days = Days.daysBetween(dtFrom.toDateMidnight(), dtTo.plusDays(1).toDateMidnight()).getDays();
        Map<String, Boolean> result = new HashMap<String, Boolean>(0);
        if (days <= 2) {
            // enable hourly,day
            result.put(Constants.HOUR, true);
            result.put(Constants.DAY, true);
        } else if (2 < days && days <= 7) {
            // enable daily
            result.put(Constants.DAY, true);
        } else if (days > 7) {
            // enable daily , weekly
            result.put(Constants.DAY, true);

            // WEEKLY disabled by default
            // result.put(Constants.WEEK,true);
        }
        return result;
    }

    /**
     * Given a Date[], sets the date[0] to 00:000 and the date[1] to 23:59
     * **/
    public static Date[] getNormalizedDate(Date[] dates) {
        DateMidnight dtFrom = new DateTime(dates[0]).toDateMidnight();
        DateMidnight dtTo = new DateTime(dates[1]).plusDays(1).toDateMidnight();
        if (Days.daysBetween(dtFrom.toDateTime().toDateMidnight(), dtTo.toDateTime().toDateMidnight()).getDays() == 1) {
            // same day, we do the conversion.
            DateTime dTo = dtTo.toDateTime().minusMinutes(1);

            return new Date[] { dtFrom.toDate(), dTo.toDate() };
        } else {
            return dates;
        }

    }

    public static CampaignTimePeriodDto setDateAndHourDetails(CampaignTimePeriodDto period, Date date, TimeZone timezone,
            boolean isStartDate) {
        if (isStartDate) {
            // get the start date withouth the timezone offset.
            Date startDate = DateUtils.getTimezoneDate(date, timezone);

            // still we need to substract the hours/minutes if user has selected
            // any
            // No timezone here cause we alreday have substracted the timezone
            // in previous
            int minutes = DateUtils.getMinuteOffset(startDate);

            period.setStartDate(startDate);
            period.setStartTimeOffset(minutes);

        } else {
            // get the start date withouth the timezone offset.
            Date endDate = DateUtils.getTimezoneDate(date, timezone);

            // still we need to substract the hours/minutes if user has selected
            // any
            // No timezone here cause we alreday have substracted the timezone
            // in previous
            int minutes = DateUtils.getMinuteOffset(endDate);

            period.setEndDate(endDate);
            period.setEndTimeOffset(minutes);

        }
        return period;
    }

    public static List<Long> getCampaignListId(List<StatisticsDto> list) {
        List<Long> ids = new ArrayList<Long>(0);
        for (StatisticsDto dto : list) {
            ids.add(dto.getCampaignId());
        }
        return ids;
    }

    public static List<Long> getAdvertisersListId(List<AgencyConsoleStatisticsDto> list) {
        List<Long> ids = new ArrayList<Long>(0);
        for (AgencyConsoleStatisticsDto dto : list) {
            ids.add(dto.getAdvertiserId());
        }
        return ids;
    }

    public static double getPercenteatgeAmount(long value, int percen) {
        if (value <= 0.001) {
            return 0.001;
        }
        if (value <= 0.01) {
            return 0.01;
        }
        if (value <= 0.1) {
            return 0.1;
        }
        double val = (value * percen) / 100;
        if (val == 0) {
            return 0.5;
        }
        return val;
    }

    public static Interval getChartsInterval(Date from, Date to, String chartId) {
        // HOURS("1"), THREE_HOURS("3"), SIX_HOURS("6"), TWELVE_HOURS("12"),
        // DAY("24");
        DateTime dtFrom = new DateTime(from);
        DateTime dtTo = new DateTime(to);
        int days = Days.daysBetween(dtFrom.toDateMidnight(), dtTo.toDateMidnight().plusDays(1)).getDays();
        if (days <= 2) {
            return Interval.HOURS;
        } else {
            return Interval.DAY;
        }
    }

    public static String getXAxisInterval(Interval i) {

        if (i.equals(Interval.HOURS)) {
            return Constants.ONE_HOUR;
        } else if (i.equals(Interval.THREE_HOURS)) {
            return Constants.THREE_HOUR;
        } else {
            return Constants.ONE_DAY;
        }
    }

    public static String getXDateFormat(String dateSelection) {
        int sel = Integer.valueOf(dateSelection);

        if (sel == 1 || sel == 2) {
            return "'%H:%M'";
        } else if (sel == 3) {
            return "'%a'";
        } else {
            return "'%d'";
        }
    }

    public static String getXAxisInterval(String dateSelection) {
        int sel = Integer.valueOf(dateSelection);

        if (sel == 1 || sel == 2) {
            return Constants.ONE_HOUR;
        } else {
            return Constants.ONE_DAY;
        }
    }

    public static List<Long> getPublicationListId(List<PublisherStatisticsDto> list) {
        List<Long> ids = new ArrayList<Long>(0);
        for (PublisherStatisticsDto dto : list) {
            ids.add(dto.getPublicationId());
        }
        return ids;
    }

    public static String getIntervalAxis(final double maxValue, final double minValue) {
        double newMaxValue;
        if (minValue != 0) {
            newMaxValue = maxValue - minValue;
        } else {
            newMaxValue = maxValue;
        }

        if (newMaxValue == 0 && minValue == 0) {
            return "1";
        }

        int pot = ((int) Math.log10(newMaxValue) - 1);
        double value = maxValue / Math.pow(10, pot);
        double interval;

        if (value <= 10) {
            interval = Constants.LESS_THAN_TEN * Math.pow(10, pot);
        } else if (value <= 50) {
            interval = Constants.LESS_THAN_FIFTY * Math.pow(10, pot);
        } else {
            interval = Constants.LESS_THAN_HUNDRED * Math.pow(10, pot);
        }

        if (interval < 1) {
            return Double.toString(interval);
        } else {
            return Integer.toString((int) interval);
        }
    }

    public static <T> List<T> removeDuplicated(List<T> list) {
        Set<T> set = new HashSet<T>(list);
        return new ArrayList<T>(set);
    }

    public static String shortMessage(String message, int maxChars) {
        if (message != null && message.length() > maxChars) {
            return message.substring(0, maxChars - 2) + "...";
        }
        return message;
    }

    // A quick utility method for breaking up the long campaign name strings
    // on various datatables around the application.
    public static String wrapText(String message, int breakOn) {
        if (message != null && message.length() > breakOn) {
            String newMessage = "";
            int cursor = 0;
            int lines = message.length() / breakOn;

            for (int i = 0; i < lines; i++) {
                newMessage += message.substring(cursor, breakOn * (i + 1)) + "-\n";
                cursor = breakOn * (i + 1);
            }

            newMessage += message.substring(cursor, message.length());

            return newMessage;
        } else {
            return message;
        }
    }

    public static <T> void fillSetWithList(List<T> list, Set<T> set) {
        set.clear();
        if (!CollectionUtils.isEmpty(list)) {
            List<T> realList = list;
            Iterator<T> it = realList.iterator();
            while (it.hasNext()) {
                T c = it.next();
                if (!set.contains(c)) {
                    set.add(c);
                }
            }
        }
    }

    public static boolean validateTimePeriod(Date startDate, Integer startOffset, Date endDate, Integer endOffset) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        if (startOffset != null) {
            startCal.add(Calendar.MINUTE, startOffset);
        }
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        if (endOffset != null) {
            endCal.add(Calendar.MINUTE, endOffset);
        }
        return startCal.getTime().after(endCal.getTime()) || startCal.getTime().equals(endCal.getTime());
    }

    public static boolean isValidEmailAddress(String value) {

        if (value == null || value.equals("")) {
            return false;
        }
        if (value.indexOf("..") != -1) {
            return false;
        }
        int at = value.indexOf('@');
        int dot = value.lastIndexOf('.');
        if (at <= 0) {
            return false;
        } else if (dot <= 0 || dot < at) {
            return false;
        } else if (dot == (value.length() - 1)) {
            return false;
        }

        try {
            new javax.mail.internet.InternetAddress(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
