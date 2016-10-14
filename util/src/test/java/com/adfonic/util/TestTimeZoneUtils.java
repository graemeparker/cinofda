package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.Test;

public class TestTimeZoneUtils {
    private static final transient Logger LOG = Logger.getLogger(TestTimeZoneUtils.class.getName());

    @Test
    public void test() throws Exception {
        for (int k = 0; k < 3; ++k) {
            for (String countryIsoCode : new String[] { "US", "GB", "MX" }) {
                // LOG.info("Testing country: " + countryIsoCode);
                String[] ids = TimeZoneUtils.getAvailableIDs(countryIsoCode);
                assertNotNull(countryIsoCode + " has no available IDs", ids);
                assertTrue(countryIsoCode + " has zero available IDs", ids.length > 0);
                // LOG.info(Arrays.asList(ids).toString());
            }
        }
    }

    @Test
    public void testGetDefaultTimeZone() {
        TimeZone tz = TimeZoneUtils.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals(TimeZoneUtils.DEFAULT_TIME_ZONE_ID, tz.getID());
    }

    @Test
    public void testFreeform() {
        assertNull(TimeZoneUtils.getTimeZoneByFreeformInput(null));
        assertNull(TimeZoneUtils.getTimeZoneByFreeformInput(""));
        assertNull(TimeZoneUtils.getTimeZoneByFreeformInput("blahblahblah"));
        assertNotNull(TimeZoneUtils.getTimeZoneByFreeformInput("EST5EDT"));
        assertNotNull(TimeZoneUtils.getTimeZoneByFreeformInput("+12.0"));
    }

    @Test
    public void testOffset() {
        for (double offset = -23.0; offset <= 23.0; offset += 1.0) {
            assertNotNull(TimeZoneUtils.getTimeZoneByOffset(offset));
        }

        for (double offset = -720.0; offset <= 720.0; offset += 60.0) {
            assertNotNull(TimeZoneUtils.getTimeZoneByOffset(offset));
        }

        for (double offset = -43200.0; offset <= 43200.0; offset += 3600.0) {
            assertNotNull(TimeZoneUtils.getTimeZoneByOffset(offset));
        }

        assertNotNull(TimeZoneUtils.getTimeZoneByOffset(3600000L)); // ms
        assertNotNull(TimeZoneUtils.getTimeZoneByOffset(-3600000L)); // ms
        assertNotNull(TimeZoneUtils.getTimeZoneByOffset(-7200000L)); // ms

        assertNull(TimeZoneUtils.getTimeZoneByOffset(123456));
    }

    @Test
    public void testGetTimeZoneNonBlocking() {
        assertNotNull(TimeZoneUtils.getTimeZoneNonBlocking("GMT"));
        assertNotNull(TimeZoneUtils.getTimeZoneNonBlocking("Europe/London"));
        assertNotNull(TimeZoneUtils.getTimeZoneNonBlocking("America/Los_Angeles"));
        assertNull(TimeZoneUtils.getTimeZoneNonBlocking("junketyjunk"));
        assertNull(TimeZoneUtils.getTimeZoneNonBlocking(null));
    }

    @Test
    public void testAllAvailableTimeZones() {
        for (String countryIsoCode : COUNTRY_ISO_CODES) {
            String[] tzIds = TimeZoneUtils.getAvailableIDs(countryIsoCode);
            if (tzIds == null) {
                LOG.warning("Got null IDs for " + countryIsoCode);
            } else if (tzIds.length == 0) {
                LOG.warning("Got 0 IDs for " + countryIsoCode);
            } else {
                for (String tzId : tzIds) {
                    TimeZone tz = TimeZoneUtils.getTimeZoneNonBlocking(tzId);
                    if (tz == null) {
                        // See if the conventional TimeZone.getTimeZone method
                        // finds it...
                        tz = TimeZone.getTimeZone(tzId);
                        if (tz != null) {
                            // This is bad
                            LOG.severe("getTimeZoneNonBlocking failed for available ID \"" + tzId + "\" in " + countryIsoCode + ", TimeZone.getTimeZone returned non-null: " + tz);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testGetPreferredTimeZoneIDByHoursOffset() {
        // For code coverage...
        assertNull(TimeZoneUtils.getPreferredTimeZoneIDByHoursOffset(1.23)); // non-integer
                                                                             // value
    }

    /*
     * @Test public void testRelativeSpeedOfTimeZoneMethods() throws Exception {
     * final String id = "Europe/London";
     * 
     * // Call once up front to initialize the static caches
     * TimeZoneUtils.getTimeZoneNonBlocking(id);
     * 
     * final int numThreads = 3; final int iterations = 10000000;
     * 
     * final CyclicBarrier barrier = new CyclicBarrier(numThreads + 1); final
     * CountDownLatch latch = new CountDownLatch(numThreads);
     * 
     * for (int k = 0; k < numThreads; ++k) { new Thread() { public void run() {
     * TimeZone timeZone = null; try { barrier.await(); } catch (Exception e) {
     * throw new IllegalStateException(e); } for (int k = 0; k < iterations;
     * ++k) { //timeZone = TimeZone.getTimeZone(id); timeZone =
     * TimeZoneUtils.getTimeZoneNonBlocking(id); } latch.countDown();
     * LOG.info("finished: " + timeZone); } }.start(); }
     * 
     * StopWatch stopWatch = new StopWatch(); barrier.await();
     * stopWatch.start(); latch.await(); stopWatch.stop(); LOG.info("elapsed: "
     * + stopWatch); }
     */

    private static final String[] COUNTRY_ISO_CODES = { "00", "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ", "BA", "BB",
            "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL",
            "CM", "CN", "CO", "CR", "CU", "CV", "CW", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO",
            "FR", "GA", "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE",
            "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK",
            "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY",
            "MZ", "NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY",
            "QA", "RE", "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "ST", "SV", "SX", "SY", "SZ", "TC", "TD",
            "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ", "UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF",
            "WS", "YE", "YT", "ZA", "ZM", "ZW", };
}
