package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

public class TestCampaignTimePeriod {
    @Test
    public void testIsCurrent() {
        CampaignTimePeriod ctp = new CampaignTimePeriod();

        ctp.setStartDate(null);
        ctp.setEndDate(null);
        assertTrue(ctp.isCurrent());

        Date dateInThePast = DateUtils.addDays(new Date(), -1);
        Date dateInTheFuture = DateUtils.addDays(new Date(), 1);

        ctp.setStartDate(dateInThePast);
        ctp.setEndDate(null);
        assertTrue(ctp.isCurrent());

        ctp.setStartDate(dateInThePast);
        ctp.setEndDate(dateInTheFuture);
        assertTrue(ctp.isCurrent());

        ctp.setStartDate(null);
        ctp.setEndDate(dateInTheFuture);
        assertTrue(ctp.isCurrent());

        ctp.setStartDate(dateInTheFuture);
        ctp.setEndDate(null);
        assertFalse(ctp.isCurrent());

        ctp.setStartDate(null);
        ctp.setEndDate(dateInThePast);
        assertFalse(ctp.isCurrent());

        ctp.setStartDate(dateInTheFuture);
        ctp.setEndDate(dateInThePast);
        assertFalse(ctp.isCurrent());
    }

    @Test
    public void testIsFuture() {
        CampaignTimePeriod ctp = new CampaignTimePeriod();

        ctp.setStartDate(null);
        assertFalse(ctp.isFuture());

        Date dateInTheFuture = DateUtils.addDays(new Date(), 1);
        ctp.setStartDate(dateInTheFuture);
        assertTrue(ctp.isFuture());

        Date dateInThePast = DateUtils.addDays(new Date(), -1);
        ctp.setStartDate(dateInThePast);
        assertFalse(ctp.isFuture());
    }

    @Test
    public void testOverlaps() throws Exception {
        CampaignTimePeriod c1 = new CampaignTimePeriod();
        CampaignTimePeriod c2 = new CampaignTimePeriod();

        // All null by default
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // One has one discrete endpoint, the other has both null
        c1.setStartDate(new Date());
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // One has two discrete endpoints, the other has both null
        c1.setEndDate(DateUtils.addDays(c1.getStartDate(), 1));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // One has two discrete endpoints, the other has one discrete endpoint,
        // and it starts when the first one ends (should not overlap)
        c2.setStartDate(c1.getEndDate());
        if (c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // One has two discrete endpoints, the other has one discrete endpoint,
        // and it starts prior to when the first one ends (should overlap)
        c2.setStartDate(DateUtils.addHours(c1.getStartDate(), 1));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // Both have two discrete endpoints and one's start is the other's end.
        // Should not overlap
        c2.setStartDate(c1.getEndDate());
        c2.setEndDate(DateUtils.addDays(c2.getStartDate(), 1));
        if (c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // Both have two discrete endpoints, and one completely contains the
        // other (should overlap)
        c2.setStartDate(DateUtils.addDays(c1.getStartDate(), -1));
        c2.setEndDate(DateUtils.addDays(c1.getEndDate(), 1));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // Both have two discrete endpoints, and one completely contains the
        // other, one endpoint shared (should overlap)
        c2.setStartDate(c1.getStartDate());
        c2.setEndDate(DateUtils.addDays(c1.getEndDate(), 1));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // Both have two discrete endpoints, and one completely contains the
        // other, one endpoint shared (should overlap)
        c2.setStartDate(DateUtils.addDays(c1.getStartDate(), -1));
        c2.setEndDate(c1.getEndDate());
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        // Both have two discrete endpoints, and they partially overlap
        c1.setEndDate(DateUtils.addDays(c1.getStartDate(), 2));
        c2.setStartDate(DateUtils.addDays(c1.getStartDate(), 1));
        c2.setEndDate(DateUtils.addDays(c1.getEndDate(), 1));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("overlaps failure");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("overlaps failure");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        c1.setStartDate(sdf.parse("2010-07-30 00:00:00"));
        c1.setEndDate(sdf.parse("2010-07-31 00:00:00"));
        c2.setStartDate(sdf.parse("2010-07-30 00:00:00"));
        c2.setEndDate(sdf.parse("2010-07-30 23:59:59"));
        if (!c1.overlaps(c2)) {
            throw new RuntimeException("Holy schnikes");
        }
        else if (!c2.overlaps(c1)) {
            throw new RuntimeException("Holy schnikes");
        }
    }

    @Test
    public void testEquals() {
        // TODO: fill this in once Pierre updates the equals() method
        CampaignTimePeriod c1 = new CampaignTimePeriod();
        CampaignTimePeriod c2 = new CampaignTimePeriod();

        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        c1.setStartDate(date);
        c2.setStartDate(timestamp);

        assertEquals(c1, c2);
        assertEquals(c2, c1);
    }

    @Test
    public void testEqualsDeux() {
        try {
            CampaignTimePeriod c1 = new CampaignTimePeriod();
            CampaignTimePeriod c2 = new CampaignTimePeriod();

            Date startDate = new Date();

            c1.setStartDate(startDate);
            c2.setStartDate(startDate);

            c1.setEndDate(DateUtils.addDays(startDate, 1));

            assertFalse(c1.equals(c2));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }

    @Test
    public void testCompareTo() {
        CampaignTimePeriod c1 = new CampaignTimePeriod();
        assertEquals(0, c1.compareTo(c1));

        CampaignTimePeriod c2 = new CampaignTimePeriod();
        assertEquals(0, c1.compareTo(c2));

        Date dateInThePast = DateUtils.addDays(new Date(), -1);
        Date dateInTheFuture = DateUtils.addDays(new Date(), 1);

        c2.setStartDate(dateInThePast);
        assertEquals(-1, c1.compareTo(c2));

        c1.setStartDate(dateInThePast);
        c2.setStartDate(null);
        assertEquals(1, c1.compareTo(c2));

        c2.setStartDate(dateInThePast);
        assertEquals(0, c1.compareTo(c2));

        c1.setStartDate(dateInThePast);
        c2.setStartDate(dateInTheFuture);
        assertEquals(-1, c1.compareTo(c2));

        c1.setStartDate(dateInTheFuture);
        c2.setStartDate(dateInThePast);
        assertEquals(1, c1.compareTo(c2));

        c1.setStartDate(null);
        c2.setStartDate(null);

        c1.setEndDate(null);
        c2.setEndDate(null);
        assertEquals(0, c1.compareTo(c2));

        c1.setEndDate(null);
        c2.setEndDate(dateInTheFuture);
        assertEquals(1, c1.compareTo(c2));

        c1.setEndDate(dateInTheFuture);
        c2.setEndDate(null);
        assertEquals(-1, c1.compareTo(c2));

        c1.setEndDate(dateInTheFuture);
        c2.setEndDate(dateInTheFuture);
        assertEquals(0, c1.compareTo(c2));

        c1.setEndDate(dateInThePast);
        c2.setEndDate(dateInTheFuture);
        assertEquals(-1, c1.compareTo(c2));

        c1.setEndDate(dateInTheFuture);
        c2.setEndDate(dateInThePast);
        assertEquals(1, c1.compareTo(c2));
    }
}
