package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class TestDateUtils {

    /*
     * @Test public void testGetTimeID() { Date date = new Date(); int timeID =
     * DateUtils.getTimeID(date);
     * assertEquals(Integer.parseInt(FastDateFormat.getInstance
     * ("yyyyMMddHH").format(date)), timeID); }
     */

    @Test
    public void testGetTimeIDWithTimeZone() {
        Date date = new Date();
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        int timeID = DateUtils.getTimeID(date, timeZone);
        assertEquals(Integer.parseInt(FastDateFormat.getInstance("yyyyMMddHH", timeZone).format(date)), timeID);
    }

    /*
     * @Test public void testGetDateByTimeID() { int timeID = 2010051517;
     * Calendar cal = Calendar.getInstance(); cal.set(Calendar.YEAR, 2010);
     * cal.set(Calendar.MONTH, Calendar.MAY); cal.set(Calendar.DAY_OF_MONTH,
     * 15); cal.set(Calendar.HOUR_OF_DAY, 17); cal =
     * org.apache.commons.lang.time.DateUtils.truncate(cal,
     * Calendar.HOUR_OF_DAY); assertEquals(cal.getTime(),
     * DateUtils.getDateByTimeID(timeID)); }
     * 
     * @Test public void testGetDateByTimeIDWithTimeZone() { int timeID =
     * 2010051517; TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
     * Calendar cal = Calendar.getInstance(timeZone); cal.set(Calendar.YEAR,
     * 2010); cal.set(Calendar.MONTH, Calendar.MAY);
     * cal.set(Calendar.DAY_OF_MONTH, 15); cal.set(Calendar.HOUR_OF_DAY, 17);
     * cal = org.apache.commons.lang.time.DateUtils.truncate(cal,
     * Calendar.HOUR_OF_DAY); assertEquals(cal.getTime(),
     * DateUtils.getDateByTimeID(timeID, timeZone)); }
     */

    /*
     * @Test public void testGetYear() { int year = 1974; Calendar cal =
     * Calendar.getInstance(); cal.set(Calendar.YEAR, year); Date date =
     * cal.getTime(); assertEquals(year, DateUtils.getYear(date)); }
     * 
     * @Test public void testGetYearWithTimeZone() { int year = 1974; TimeZone
     * timeZone = TimeZone.getTimeZone("Pacific/Truk"); Calendar cal =
     * Calendar.getInstance(timeZone); cal.set(Calendar.YEAR, year); Date date =
     * cal.getTime(); assertEquals(year, DateUtils.getYear(date, timeZone)); }
     * 
     * @Test public void testGetMonth() { int month = Calendar.MAY; Calendar cal
     * = Calendar.getInstance(); cal.set(Calendar.MONTH, month); Date date =
     * cal.getTime(); assertEquals(month, DateUtils.getMonth(date)); }
     * 
     * @Test public void testGetMonthWithTimeZone() { int month = Calendar.MAY;
     * TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk"); Calendar cal =
     * Calendar.getInstance(timeZone); cal.set(Calendar.MONTH, month); Date date
     * = cal.getTime(); assertEquals(month, DateUtils.getMonth(date, timeZone));
     * }
     * 
     * @Test public void testGetDay() { int dayOfMonth = 15; Calendar cal =
     * Calendar.getInstance(); cal.set(Calendar.DAY_OF_MONTH, dayOfMonth); Date
     * date = cal.getTime(); assertEquals(dayOfMonth, DateUtils.getDay(date)); }
     * 
     * @Test public void testGetDayWithTimeZone() { int dayOfMonth = 15;
     * TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk"); Calendar cal =
     * Calendar.getInstance(timeZone); cal.set(Calendar.DAY_OF_MONTH,
     * dayOfMonth); Date date = cal.getTime(); assertEquals(dayOfMonth,
     * DateUtils.getDay(date, timeZone)); }
     * 
     * @Test public void testGetHour() { int hourOfDay = 17; Calendar cal =
     * Calendar.getInstance(); cal.set(Calendar.HOUR_OF_DAY, hourOfDay); Date
     * date = cal.getTime(); assertEquals(hourOfDay, DateUtils.getHour(date)); }
     * 
     * @Test public void testGetHourWithTimeZone() { int hourOfDay = 17;
     * TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk"); Calendar cal =
     * Calendar.getInstance(timeZone); cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
     * Date date = cal.getTime(); assertEquals(hourOfDay,
     * DateUtils.getHour(date, timeZone)); }
     */

    @Test
    public void testGetStartofNextHour() {
        Calendar cal = Calendar.getInstance(TimeZoneUtils.getDefaultTimeZone());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date next = org.apache.commons.lang.time.DateUtils.addHours(cal.getTime(), 1);
        assertEquals(next, DateUtils.getStartOfNextHour(cal.getTime(), TimeZoneUtils.getDefaultTimeZone()));
    }

    @Test
    public void testGetStartOfDayWithTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        Calendar cal = Calendar.getInstance(timeZone);
        assertEquals(org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.DATE).getTime(), DateUtils.getStartOfDay(cal.getTime(), timeZone));
    }

    @Test
    public void testGetStartOfDayTomorrowWithTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        Calendar cal = Calendar.getInstance(timeZone);
        Date testValue = DateUtils.getStartOfDayTomorrow(cal.getTime(), timeZone);
        cal.add(Calendar.DATE, 1);
        assertEquals(org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.DATE).getTime(), testValue);
    }

    @Test
    public void testGetEndOfDay() {
        Calendar cal = org.apache.commons.lang.time.DateUtils.truncate(Calendar.getInstance(TimeZoneUtils.getDefaultTimeZone()), Calendar.DATE);
        Date now = cal.getTime();
        cal.add(Calendar.DATE, 1);
        cal.add(Calendar.MILLISECOND, -1);
        assertEquals(cal.getTime(), DateUtils.getEndOfDay(now, TimeZoneUtils.getDefaultTimeZone()));
    }

    @Test
    public void testGetEndOfDayWithTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        Date now = new Date();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(now);
        cal.add(Calendar.DATE, 1);
        Calendar midnightTomorrow = org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.DATE);
        assertEquals(org.apache.commons.lang.time.DateUtils.addMilliseconds(midnightTomorrow.getTime(), -1), DateUtils.getEndOfDay(now, timeZone));
    }

    @Test
    public void testGetStartOfMonth() {
        Calendar cal = Calendar.getInstance(TimeZoneUtils.getDefaultTimeZone());
        Date now = cal.getTime();
        cal = org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.MONTH);
        assertEquals(cal.getTime(), DateUtils.getStartOfMonth(now, TimeZoneUtils.getDefaultTimeZone()));
    }

    @Test
    public void testGetStartOfMonthWithTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        Date now = new Date();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(now);
        assertEquals(org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.MONTH).getTime(), DateUtils.getStartOfMonth(now, timeZone));
    }

    @Test
    public void testGetEndOfMonth() {
        Calendar cal = Calendar.getInstance(TimeZoneUtils.getDefaultTimeZone());
        Date now = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal = org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.MONTH);
        cal.add(Calendar.MILLISECOND, -1);
        assertEquals(cal.getTime(), DateUtils.getEndOfMonth(now, TimeZoneUtils.getDefaultTimeZone()));
    }

    @Test
    public void testGetEndOfMonthWithTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
        Date now = new Date();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(now);
        cal.add(Calendar.MONTH, 1);
        cal = org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.MONTH);
        assertEquals(org.apache.commons.lang.time.DateUtils.addMilliseconds(cal.getTime(), -1), DateUtils.getEndOfMonth(now, timeZone));
    }

    /*
     * @Test public void testDiffInSeconds() { Date now = new Date(); int
     * seconds = 12345; assertEquals(seconds,
     * DateUtils.diffInSeconds(org.apache.
     * commons.lang.time.DateUtils.addSeconds(now, seconds), now));
     * assertEquals(seconds, DateUtils.diffInSeconds(now,
     * org.apache.commons.lang.time.DateUtils.addSeconds(now, seconds))); }
     */

    @Test
    public void testCurrentYear() {
        assertEquals(Calendar.getInstance().get(Calendar.YEAR), DateUtils.currentYear());
    }

    @Test
    public void testGetCalendarIcon() {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Pacific/Truk");
            String[] formats = { "yyyy-MM-dd" };
            Date date = org.apache.commons.lang.time.DateUtils.parseDate("2021-12-21", formats);

            String expected = "21";
            String result = DateUtils.getCalendarIcon(date, timeZone, "day");
            assertEquals(expected, result);

            expected = "dec";
            result = DateUtils.getCalendarIcon(date, timeZone, "month");
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testLas7DaysDashboard() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Tongatapu");
        DateUtils.Period period7 = DateUtils.Period.LAST_7_DAYS_DASHBOARD;
        Range<Date> dateRange7 = period7.getRange(timeZone);

        DateTime now = new DateTime(DateTimeZone.forTimeZone(TimeZoneUtils.getTimeZoneNonBlocking("Pacific/Tongatapu")));

        Date start = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusDays(7))).toDate();
        Date end = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusDays(1))).toDate();

        assertEquals(start, dateRange7.getStart());
        assertEquals(end, dateRange7.getEnd());
    }

    @Test
    public void testLas7DaysDashboard_DefaultTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        DateUtils.Period period7 = DateUtils.Period.LAST_7_DAYS_DASHBOARD;
        Range<Date> dateRange7 = period7.getRange(timeZone);

        DateTime now = new DateTime(DateTimeZone.getDefault());

        Date start = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusDays(7))).toDate();
        Date end = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusDays(1))).toDate();

        assertEquals(start, dateRange7.getStart());
        assertEquals(end, dateRange7.getEnd());
    }

    @Test
    public void testLasMonthDashboard() {
        TimeZone timeZone = TimeZone.getTimeZone("Pacific/Tongatapu");
        DateUtils.Period period7 = DateUtils.Period.LAST_MONTH_DASHBOARD;
        Range<Date> dateRange7 = period7.getRange(timeZone);

        DateTime now = new DateTime(DateTimeZone.forTimeZone(TimeZoneUtils.getTimeZoneNonBlocking("Pacific/Tongatapu")));

        Date start = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusMonths(1).withDayOfMonth(1))).toDate();
        Date end = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.withDayOfMonth(1).minusDays(1))).toDate();

        assertEquals(start, dateRange7.getStart());
        assertEquals(end, dateRange7.getEnd());
    }

    @Test
    public void testLasMonthDashboard_DefaultTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        DateUtils.Period period7 = DateUtils.Period.LAST_MONTH_DASHBOARD;
        Range<Date> dateRange7 = period7.getRange(timeZone);

        DateTime now = new DateTime(DateTimeZone.getDefault());

        Date start = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.minusMonths(1).withDayOfMonth(1))).toDate();
        Date end = DateUtils.STORED_PROC_FORMAT.parseDateTime(DateUtils.STORED_PROC_FORMAT.print(now.withDayOfMonth(1).minusDays(1))).toDate();

        assertEquals(start, dateRange7.getStart());
        assertEquals(end, dateRange7.getEnd());
    }
}
