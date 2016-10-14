package com.adfonic.util;

import java.util.Calendar;
import java.util.Date;

public class AgeUtils {
    
    private AgeUtils(){
        
    }
    
    public static int getAgeInYears(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return -1;
        }
        Date now = new Date();
        if (!dateOfBirth.before(now)) {
            return 0;
        }
        Calendar dobC = null;
        Calendar nowC = null;
        try {
            dobC = NonBlockingCalendarPool.acquireCalendar(dateOfBirth);
            nowC = NonBlockingCalendarPool.acquireCalendar(now);

            int age = nowC.get(Calendar.YEAR) - dobC.get(Calendar.YEAR);
            int nowMonth = nowC.get(Calendar.MONTH);
            int dobMonth = dobC.get(Calendar.MONTH);
            if (nowMonth < dobMonth) {
                --age; // Their birthday hasn't passed yet this year.
            } else if ((nowMonth == dobMonth) && (nowC.get(Calendar.DAY_OF_MONTH) < dobC.get(Calendar.DAY_OF_MONTH))) {
                // It's the same month as their birth month, but the day of
                // the month hasn't passed yet.
                --age;
            }
            return age;
        } finally {
            NonBlockingCalendarPool.releaseCalendar(dobC);
            NonBlockingCalendarPool.releaseCalendar(nowC);
        }
    }

    /**
     * Get the latest date on which a person could have been born in order for
     * their age to be at least minAge
     */
    public static Date getLatestDateOfBirthForMinAge(int minAge) {
        if (minAge == -1) {
            return null;
        }
        Calendar cal = NonBlockingCalendarPool.acquireCalendar();
        try {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.YEAR, 0 - minAge);
            cal.add(Calendar.DATE, 1);
            cal.add(Calendar.MILLISECOND, -1);
            return cal.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(cal);
        }
    }

    /**
     * Get the earliest date on which a person could have been born in order for
     * their age to be at most maxAge
     */
    public static Date getEarliestDateOfBirthForMaxAge(int maxAge) {
        if (maxAge == -1) {
            return null;
        }
        Calendar cal = NonBlockingCalendarPool.acquireCalendar();
        try {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.YEAR, 0 - (maxAge + 1));
            cal.add(Calendar.DATE, 1);
            return cal.getTime();
        } finally {
            NonBlockingCalendarPool.releaseCalendar(cal);
        }
    }
}
