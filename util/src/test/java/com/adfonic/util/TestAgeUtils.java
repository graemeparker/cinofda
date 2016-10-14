package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class TestAgeUtils {
    @Test
    public void testGetAgeInYears() {
        assertEquals(-1, AgeUtils.getAgeInYears(null));

        assertEquals(0, AgeUtils.getAgeInYears(org.apache.commons.lang.time.DateUtils.addDays(new Date(), 7)));

        // March 11, 2012 at 7:00pm...it's now EDT.
        // The UK hasn't yet kicked into BST.
        // In GMT it's March 11, 2012 at 11:00pm.
        //
        // But last year...
        // March 11, 2011 at 7:00pm...it was still EST then!
        // In GMT that's March 12, 2012 at 00:00am.
        //
        // Notice the difference in "day of the month"?!?!
        //
        // This caused problems this year when this test ran between the hours
        // of 7pm and 8pm EDT.
        //
        // Suffice it to say we don't have any of these issues running on UK
        // servers.
        // But to work around this, just use 9am local as the test time, which
        // will
        // never "be tomorrow in GMT."
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZoneUtils.getDefaultTimeZone());
        cal = org.apache.commons.lang.time.DateUtils.truncate(cal, Calendar.DATE);
        cal.set(Calendar.HOUR_OF_DAY, 9);

        Date now = cal.getTime();
        assertEquals(0, AgeUtils.getAgeInYears(now));

        for (int k = 1; k < 365; ++k) {
            Date dob = org.apache.commons.lang.time.DateUtils.addDays(now, -k);
            int age = AgeUtils.getAgeInYears(dob);
            assertEquals(0, age);
        }

        for (int k = 1; k <= 100; ++k) {
            Date dob = org.apache.commons.lang.time.DateUtils.addYears(now, -k);
            int age = AgeUtils.getAgeInYears(dob);
            assertEquals(k, age);
        }
    }

    @Test
    public void testGetLatestDateOfBirthForMinAge() {
        assertNull(AgeUtils.getLatestDateOfBirthForMinAge(-1));

        for (int k = 0; k < 100; ++k) {
            AgeUtils.getLatestDateOfBirthForMinAge(k);
        }
    }

    @Test
    public void testGetEarliestDateOfBirthForMaxAge() {
        assertNull(AgeUtils.getEarliestDateOfBirthForMaxAge(-1));

        for (int k = 0; k < 100; ++k) {
             AgeUtils.getEarliestDateOfBirthForMaxAge(k);
        }
    }
}
