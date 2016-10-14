package com.adfonic.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {

    public static final DateTimeFormatter STORED_PROC_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");

    public enum Period {
        TODAY {
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                Date now = new Date();
                Date startOfDay = DateUtils.getStartOfDay(now, timeZone);
                return new Range<Date>(startOfDay, now);
            }
        },
        YESTERDAY {
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                Calendar calendar = NonBlockingCalendarPool.acquireCalendar();
                try {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    Date startOfYesterday = DateUtils.getStartOfDay(calendar.getTime(), timeZone);
                    Date endOfYesterday = DateUtils.getEndOfDay(calendar.getTime(), timeZone);
                    return new Range<Date>(startOfYesterday, endOfYesterday);
                } finally {
                    NonBlockingCalendarPool.releaseCalendar(calendar);
                }
            }
        },
        LAST_7_DAYS {
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                Calendar calendar = NonBlockingCalendarPool.acquireCalendar(timeZone);
                try {
                    calendar.add(Calendar.DAY_OF_MONTH, -7);
                    Date startOf7DaysAgo = DateUtils.getStartOfDay(calendar.getTime(), timeZone);
                    calendar.add(Calendar.DAY_OF_MONTH, 5);
                    Date endOfPreviousDay = DateUtils.getEndOfDay(calendar.getTime(), timeZone);
                    return new Range<Date>(startOf7DaysAgo, endOfPreviousDay);
                } finally {
                    NonBlockingCalendarPool.releaseCalendar(calendar);
                }
            }
        },
        LAST_7_DAYS_DASHBOARD {
            // AD-246 implemented using joda time
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                DateTime now = new DateTime(DateTimeZone.forTimeZone(timeZone));
                DateTime last7Start = now.minusDays(7);
                DateTime last7End = now.minusDays(1);
                return new Range<Date>(STORED_PROC_FORMAT.parseDateTime(STORED_PROC_FORMAT.print(last7Start)).toDate(), STORED_PROC_FORMAT
                        .parseDateTime(STORED_PROC_FORMAT.print(last7End)).toDate());
            }
        },
        LAST_MONTH_DASHBOARD {
            // AD-246 implemented using joda time
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                DateTime now = new DateTime(DateTimeZone.forTimeZone(timeZone));
                DateTime lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
                DateTime lastMonthEnd = now.withDayOfMonth(1).minusDays(1);
                return new Range<Date>(STORED_PROC_FORMAT.parseDateTime(STORED_PROC_FORMAT.print(lastMonthStart)).toDate(), STORED_PROC_FORMAT.parseDateTime(
                        STORED_PROC_FORMAT.print(lastMonthEnd)).toDate());
            }
        },
        THIS_MONTH {
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                Date now = new Date();
                Date startOfMonth = DateUtils.getStartOfMonth(now, timeZone);
                return new Range<Date>(startOfMonth, now);
            }
        },
        LAST_MONTH {
            @Override
            public Range<Date> getRange(TimeZone timeZone) {
                Calendar calendar = NonBlockingCalendarPool.acquireCalendar(timeZone);
                try {
                    calendar.add(Calendar.MONTH, -1);
                    Date startOfLastMonth = DateUtils.getStartOfMonth(calendar.getTime(), timeZone);
                    Date endOfLastMonth = DateUtils.getEndOfMonth(calendar.getTime(), timeZone);
                    return new Range<Date>(startOfLastMonth, endOfLastMonth);
                } finally {
                    NonBlockingCalendarPool.releaseCalendar(calendar);
                }
            }
        };
        
        public abstract Range<Date> getRange(TimeZone timeZone);
    }

    /** Returns an integer in the format YYYYMMDDHH */
    public static int getTimeID(Date date, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            return // YYYYMMDDHH
            calendar.get(Calendar.YEAR) * 1000000 + (calendar.get(Calendar.MONTH) + 1) * 10000 + calendar.get(Calendar.DAY_OF_MONTH) * 100 + calendar.get(Calendar.HOUR_OF_DAY);
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /** Returns the first millisecond of the given date (midnight). */
    public static Date getStartOfDay(Date date, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /**
     * Returns the first millisecond of "tomorrow" (midnight) in the given time
     * zone.
     */
    public static Date getStartOfDayTomorrow(Date today, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(today, timeZone);
        try {
            calendar.add(Calendar.DATE, 1); // tomorrow
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /**
     * Returns the first millisecond of 'next hour' in the given time zone.
     * 
     * @param today
     * @param timeZone
     * @return
     */
    public static Date getStartOfNextHour(Date today, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(today, timeZone);
        try {
            calendar.add(Calendar.HOUR_OF_DAY, 1); // next hour
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /**
     * Returns the last millisecond of the given date (23:59:59.999).
     * 
     * @deprecated use getEndOfDay(Date, TimeZone) instead
     */
    @Deprecated
    public static Date getEndOfDay(Date date) {
        return getEndOfDay(date, TimeZoneUtils.getDefaultTimeZone());
    }

    /** Returns the last millisecond of the given date (23:59:59.999). */
    public static Date getEndOfDay(Date date, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /**
     * Returns the first millisecond of the given date (midnight).
     * 
     * @deprecated use getStartOfMonth(Date, TimeZone) instead
     */
    @Deprecated
    public static Date getStartOfMonth(Date date) {
        return getStartOfMonth(date, TimeZoneUtils.getDefaultTimeZone());
    }

    /** Returns the first millisecond of the given date (midnight). */
    public static Date getStartOfMonth(Date date, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /**
     * Returns the last millisecond of the given date (23:59:59.999).
     * 
     * @deprecated use getEndOfMonth(Date, TimeZone) instead
     */
    @Deprecated
    public static Date getEndOfMonth(Date date) {
        return getEndOfMonth(date, TimeZoneUtils.getDefaultTimeZone());
    }

    /** Returns the last millisecond of the given date (23:59:59.999). */
    public static Date getEndOfMonth(Date date, TimeZone timeZone) {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return calendar.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    /*
     * public static int diffInSeconds(Date date1, Date date2) { long diffMilli
     * = Math.abs(date1.getTime() - date2.getTime()); return (int)(diffMilli /
     * 1000); }
     */

    public static int currentYear() {
        Calendar calendar = NonBlockingCalendarPool.acquireCalendar(TimeZoneUtils.getDefaultTimeZone());
        try {
            return calendar.get(Calendar.YEAR);
        } finally {
            NonBlockingCalendarPool.releaseCalendar(calendar);
        }
    }

    // helper for getting date formatted and lowercased for calendar icons
    public static String getCalendarIcon(Date date, TimeZone timeZone, String datePart) {
        String pattern = "MMM"; // default to month
        if ("day".equals(datePart)) {
            pattern = "dd";
        }
        FastDateFormat dateFormat = FastDateFormat.getInstance(pattern, timeZone == null ? TimeZoneUtils.getDefaultTimeZone() : timeZone);
        return dateFormat.format(date).toLowerCase();
    }

    public static Date now() {
        return new Date();
    }

    /**
     * The name of this method should probably be changed to make it clear what
     * it's doing. Since MySQL doesn't store milliseconds on timestamp columns,
     * any date you get back from the database is going to be truncated to the
     * second. This method does just that on any arbitrary date, it chops off
     * any milliseconds.
     */
    public static Date sanitizeDate(Date date) {
        if (date == null) {
            return null;
        }
        return org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.SECOND);
    }

}
