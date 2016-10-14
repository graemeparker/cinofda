package com.adfonic.presentation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.adfonic.presentation.FacesUtils;

public class DateUtils {

    private static final int ONE_MIN_IN_MILLIS = 1000 * 60;

    private DateUtils() {
    }
    
    // Given a date, we substract the timezone
    public static Date getTimezoneDate(Date date, TimeZone timezone) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, timezone.getOffset(new Date().getTime()));
        return cal.getTime();
    }
    
    public static Date getDateFormatFromString(String stringDate, String dateFormat) {
        try {
            return new SimpleDateFormat(dateFormat).parse(stringDate);
        } catch (ParseException e) {
            return new Date();
        }
    }
    
    // number of minutes from start of day
    public static int getMinuteOffset(Date date) {
        int minutes = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // cal.add(Calendar.MILLISECOND, -1 * timeZone.getOffset(new
        // Date().getTime()));
        long diff = cal.getTimeInMillis() - getStartOfDay(date).getTime();

        if (diff > 0) {
            minutes = (int) diff / ONE_MIN_IN_MILLIS;
        }

        return minutes;
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    public static String getLongDateFormat() {
        Locale locale = FacesUtils.getLocale();
        if (locale.equals(Locale.US)) {
            return Constants.US_LONG_DATE_FORMAT;
        } else if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE) || locale.equals(Locale.CHINA)
                || locale.equals(Locale.CHINESE)) {
            return Constants.JAP_LONG_DATE_FORMAT;
        } else {
            return Constants.DEFAULT_LONG_DATE_FORMAT;
        }
    }
    
    public static String getDateFormat() {
        Locale locale = FacesUtils.getLocale();
        if (locale.equals(Locale.US)) {
            return Constants.US_DATE_FORMAT;
        } else if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE) || locale.equals(Locale.CHINA)
                || locale.equals(Locale.CHINESE)) {
            return Constants.JAP_DATE_FORMAT;
        } else {
            return Constants.DEFAULT_DATE_FORMAT;
        }
    }
    
    public static String getDateFormatTooltips() {
        Locale locale = FacesUtils.getLocale();
        if (locale.equals(Locale.US)) {
            return Constants.US_DATE_FORMAT_TOOLTIPS;
        } else if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE) || locale.equals(Locale.CHINA)
                || locale.equals(Locale.CHINESE)) {
            return Constants.JAP_DATE_FORMAT_TOOLTIPS;
        } else {
            return Constants.DEFAULT_DATE_FORMAT_TOOLTIPS;
        }
    }
    
    public static String getTimeStampFormat() {
        return getDateFormat() + Constants.DEFAULT_TIMESTAMP_FORMAT;
    }
    
}
